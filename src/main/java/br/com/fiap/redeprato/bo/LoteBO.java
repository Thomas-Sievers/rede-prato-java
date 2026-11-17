package br.com.fiap.redeprato.bo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dao.ReservaDAO;
import br.com.fiap.redeprato.dto.LoteDTO;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.Reserva;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** Business rules for lotes (HU02, HU03, HU09, HU21). */
@Service
public class LoteBO {

    static final BigDecimal PESO_MAX = new BigDecimal("999999.99");

    private final LoteDAO dao;
    private final ReservaDAO reservaDAO;
    private final OfertanteBO ofertanteBO;
    private final CategoriaAlimentoBO categoriaBO;
    private final Clock clock;

    public LoteBO(LoteDAO dao, ReservaDAO reservaDAO, OfertanteBO ofertanteBO,
                  CategoriaAlimentoBO categoriaBO, Clock clock) {
        this.dao = dao;
        this.reservaDAO = reservaDAO;
        this.ofertanteBO = ofertanteBO;
        this.categoriaBO = categoriaBO;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<LoteDTO.Response> listar(StatusLote status, Long idOfertante, Long idCategoria,
                                         ModalidadeLote modalidade) {
        return dao.filtrar(status, idOfertante, idCategoria, modalidade).stream()
                .map(LoteDTO.Response::from).toList();
    }

    /** UC08: only DISPONIVEL lots within the deadline from active ofertantes. Runs expiry first. */
    @Transactional
    public List<LoteDTO.Response> listarDisponiveis(Long idCategoria, ModalidadeLote modalidade, String bairro) {
        expirarVencidos();
        return dao.disponiveis(now(), idCategoria, modalidade, Checks.clean(bairro)).stream()
                .map(LoteDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public LoteDTO.Response buscar(Long id) {
        return LoteDTO.Response.from(find(id));
    }

    @Transactional
    public LoteDTO.Response criar(LoteDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        Ofertante ofertante = ofertanteBO.find(req.idOfertante());
        checkOfertanteAtivo(ofertante);
        Lote l = new Lote();
        l.setOfertante(ofertante);
        apply(l, req);
        l.setStatus(StatusLote.DISPONIVEL); // BR-04.9
        l.setDataCadastro(now());
        validate(l);
        Lote salvo = dao.save(l);
        return LoteDTO.Response.from(salvo);
    }

    @Transactional
    public LoteDTO.Response atualizar(Long id, LoteDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        Lote l = find(id);
        checkDisponivelParaAlterar(l);
        apply(l, req); // idOfertante is ignored on update (BR-04.10)
        validate(l);
        return LoteDTO.Response.from(dao.save(l));
    }

    /** HU03 / decisão 6: deletes the lote together with its CANCELADA reservas (and alertas, feature 09). */
    @Transactional
    public void excluir(Long id) {
        Lote l = find(id);
        checkPodeExcluir(l);
        List<Reserva> canceladas = reservaDAO.findByLote_IdLoteAndStatus(l.getIdLote(), StatusReserva.CANCELADA);
        if (!canceladas.isEmpty()) {
            reservaDAO.deleteAll(canceladas);
        }
        dao.delete(l);
    }

    /**
     * BR-04.13: DISPONIVEL or RESERVADO lots past the deadline become EXPIRADO.
     *
     * @return how many lots expired
     */
    @Transactional
    public int expirarVencidos() {
        List<Lote> vencidos = dao.findByStatusInAndDataLimiteRetiradaBefore(
                EnumSet.of(StatusLote.DISPONIVEL, StatusLote.RESERVADO), now());
        for (Lote l : vencidos) {
            if (l.getStatus() == StatusLote.RESERVADO) {
                // D-7: the lote can no longer be picked up, so its ATIVA reserva is cancelled
                reservaDAO.findFirstByLote_IdLoteAndStatus(l.getIdLote(), StatusReserva.ATIVA).ifPresent(r -> {
                    r.setStatus(StatusReserva.CANCELADA);
                    reservaDAO.save(r);
                });
            }
            l.setStatus(StatusLote.EXPIRADO);
        }
        if (!vencidos.isEmpty()) {
            dao.saveAll(vencidos);
        }
        return vencidos.size();
    }

    /** Loads the entity or throws 404. Used by other BOs too. */
    public Lote find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Lote é obrigatório.");
        }
        return dao.findById(id).orElseThrow(() -> NotFoundException.of("Lote", id));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private void apply(Lote l, LoteDTO.Request req) {
        l.setCategoria(categoriaBO.find(req.idCategoria())); // BR-04.8
        l.setDescricao(Checks.clean(req.descricao()));
        l.setPesoKg(req.pesoKg());
        l.setCondicao(req.condicao());
        l.setModalidade(req.modalidade());
        l.setPreco(req.preco());
        l.setValorEstimado(req.valorEstimado());
        l.setDataLimiteRetirada(req.dataLimiteRetirada() == null ? null : req.dataLimiteRetirada().withNano(0));
        l.setUrlFoto(Checks.clean(req.urlFoto()));
        l.setPrecisaTransporte(Boolean.TRUE.equals(req.precisaTransporte()));
    }

    void validate(Lote l) {
        checkDescricao(l);
        checkPeso(l);
        checkCondicaoEModalidade(l);
        checkPreco(l);
        checkValorEstimado(l);
        checkPrazo(l);
        checkUrlFoto(l);
    }

    // BR-04.1
    private void checkDescricao(Lote l) {
        Checks.requiredF(l.getDescricao(), "Descrição", 200);
    }

    // BR-04.2
    private void checkPeso(Lote l) {
        BigDecimal p = l.getPesoKg();
        if (p == null) {
            throw new BusinessRuleException("Peso é obrigatório.");
        }
        if (p.signum() <= 0) {
            throw new BusinessRuleException("Peso deve ser maior que zero.");
        }
        if (Checks.decimals(p) > 2) {
            throw new BusinessRuleException("Peso deve ter no máximo 2 casas decimais.");
        }
        if (p.compareTo(PESO_MAX) > 0) {
            throw new BusinessRuleException("Peso deve ser no máximo 999999,99 kg.");
        }
    }

    // BR-04.14
    private void checkCondicaoEModalidade(Lote l) {
        Checks.notNull(l.getCondicao(), "Condição é obrigatória.");
        Checks.notNull(l.getModalidade(), "Modalidade é obrigatória.");
    }

    // BR-04.3 and BR-04.4
    private void checkPreco(Lote l) {
        if (l.getModalidade() == ModalidadeLote.DOACAO) {
            if (l.getPreco() != null) {
                throw new BusinessRuleException("Doação não tem preço.");
            }
            return;
        }
        if (l.getPreco() == null || l.getPreco().signum() <= 0) {
            throw new BusinessRuleException("Lote de venda precisa de preço maior que zero.");
        }
        Checks.money(l.getPreco(), "Preço");
    }

    // BR-04.5
    private void checkValorEstimado(Lote l) {
        if (l.getValorEstimado() != null && l.getValorEstimado().signum() < 0) {
            throw new BusinessRuleException("Valor estimado não pode ser negativo.");
        }
        Checks.money(l.getValorEstimado(), "Valor estimado");
    }

    // BR-04.6
    private void checkPrazo(Lote l) {
        LocalDateTime prazo = l.getDataLimiteRetirada();
        if (prazo == null) {
            throw new BusinessRuleException("Prazo de retirada é obrigatório.");
        }
        if (!prazo.isAfter(now())) {
            throw new BusinessRuleException("O prazo de retirada deve ser no futuro.");
        }
        if (l.getDataCadastro() != null && prazo.isBefore(l.getDataCadastro())) {
            throw new BusinessRuleException("O prazo de retirada não pode ser antes do cadastro do lote.");
        }
    }

    // BR-04.12
    private void checkUrlFoto(Lote l) {
        String url = l.getUrlFoto();
        if (url == null) {
            return;
        }
        Checks.maxLength(url, "URL da foto", 255);
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            throw new BusinessRuleException("URL da foto deve começar com http:// ou https://.");
        }
    }

    // BR-04.7
    private void checkOfertanteAtivo(Ofertante o) {
        if (!o.isAtivo()) {
            throw new BusinessRuleException("Ofertante inativo não pode registrar lotes.");
        }
    }

    // BR-04.10
    private void checkDisponivelParaAlterar(Lote l) {
        if (l.getStatus() != StatusLote.DISPONIVEL) {
            throw new ConflictException("Só é possível alterar um lote disponível.");
        }
    }

    // BR-04.11 (destinação check wired in feature 08)
    private void checkPodeExcluir(Lote l) {
        if (reservaDAO.existsByLote_IdLoteAndStatusIn(l.getIdLote(),
                EnumSet.of(StatusReserva.ATIVA, StatusReserva.RETIRADA))) {
            throw new ConflictException(MSG_NAO_EXCLUI);
        }
    }

    static final String MSG_NAO_EXCLUI =
            "Não é possível excluir: o lote tem reserva ativa ou retirada, ou destino registrado.";
}
