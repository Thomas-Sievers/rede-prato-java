package br.com.fiap.redeprato.bo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.LoteDAO;
import br.com.fiap.redeprato.dao.ReservaDAO;
import br.com.fiap.redeprato.dto.ReservaDTO;
import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.PontoDistribuicao;
import br.com.fiap.redeprato.entity.Reserva;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.StatusReserva;
import br.com.fiap.redeprato.exception.BusinessRuleException;
import br.com.fiap.redeprato.exception.ConflictException;
import br.com.fiap.redeprato.exception.NotFoundException;

/** Business rules for reservas: UC12 (reservar), UC14 (cancelar), UC13 (venda: pagamento na retirada). */
@Service
public class ReservaBO {

    static final String MSG_PRAZO = "O prazo de retirada deste lote já passou.";
    static final String MSG_JA_RESERVADO = "Este lote já foi reservado por outra pessoa.";
    static final String MSG_RESERVA_ATIVA = "Este lote já tem uma reserva ativa.";

    private final ReservaDAO dao;
    private final LoteDAO loteDAO;
    private final LoteBO loteBO;
    private final BeneficiarioBO beneficiarioBO;
    private final PontoDistribuicaoBO pontoBO;
    private final Clock clock;

    public ReservaBO(ReservaDAO dao, LoteDAO loteDAO, LoteBO loteBO, BeneficiarioBO beneficiarioBO,
                     PontoDistribuicaoBO pontoBO, Clock clock) {
        this.dao = dao;
        this.loteDAO = loteDAO;
        this.loteBO = loteBO;
        this.beneficiarioBO = beneficiarioBO;
        this.pontoBO = pontoBO;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ReservaDTO.Response> listar(Long idBeneficiario, Long idOfertante, StatusReserva status, Long idLote) {
        return dao.filtrar(idBeneficiario, idOfertante, status, idLote).stream()
                .map(ReservaDTO.Response::from).toList();
    }

    @Transactional(readOnly = true)
    public ReservaDTO.Response buscar(Long id) {
        return ReservaDTO.Response.from(find(id));
    }

    /** UC12: reserves the whole lote and marks it RESERVADO in the same transaction (BR-07.8). */
    @Transactional
    public ReservaDTO.Response criar(ReservaDTO.Request req) {
        if (req == null) {
            throw new BusinessRuleException("Corpo da requisição é obrigatório.");
        }
        Beneficiario beneficiario = beneficiarioBO.find(req.idBeneficiario());
        checkBeneficiarioAtivo(beneficiario);
        loteBO.expirarVencidos(); // BR-07.3
        Lote lote = loteBO.find(req.idLote()); // BR-07.2
        checkPrazo(lote);
        checkLoteDisponivel(lote);
        checkSemReservaAtiva(lote);
        PontoDistribuicao ponto = pontoOpcional(req.idPonto());

        Reserva r = new Reserva();
        r.setLote(lote);
        r.setBeneficiario(beneficiario);
        r.setPonto(ponto);
        r.setStatus(StatusReserva.ATIVA); // BR-07.10
        r.setDataReserva(now());
        Reserva salva = dao.save(r);

        lote.setStatus(StatusLote.RESERVADO);
        loteDAO.save(lote);
        return ReservaDTO.Response.from(salva);
    }

    /** BR-07.12: only the pickup place of an ATIVA reserva can change. */
    @Transactional
    public ReservaDTO.Response atualizar(Long id, ReservaDTO.UpdateRequest req) {
        Reserva r = find(id);
        if (r.getStatus() != StatusReserva.ATIVA) {
            throw new ConflictException("Só é possível alterar uma reserva ativa.");
        }
        r.setPonto(pontoOpcional(req == null ? null : req.idPonto()));
        return ReservaDTO.Response.from(dao.save(r));
    }

    /** UC14 / BR-07.9: ATIVA → CANCELADA; the lote goes back to DISPONIVEL (or EXPIRADO after the deadline). */
    @Transactional
    public ReservaDTO.Response cancelar(Long id) {
        Reserva r = find(id);
        if (r.getStatus() != StatusReserva.ATIVA) {
            throw new ConflictException("Só é possível cancelar uma reserva ativa.");
        }
        r.setStatus(StatusReserva.CANCELADA);
        Lote lote = r.getLote();
        if (lote.getStatus() == StatusLote.RESERVADO) {
            lote.setStatus(lote.getDataLimiteRetirada().isAfter(now()) ? StatusLote.DISPONIVEL : StatusLote.EXPIRADO);
            loteDAO.save(lote);
        }
        return ReservaDTO.Response.from(dao.save(r));
    }

    /** BR-07.13: only CANCELADA reservas can be deleted. */
    @Transactional
    public void excluir(Long id) {
        Reserva r = find(id);
        if (r.getStatus() != StatusReserva.CANCELADA) {
            throw new ConflictException("Só é possível excluir reservas canceladas. Cancele a reserva primeiro.");
        }
        dao.delete(r);
    }

    /** Loads the entity or throws 404. Used by other BOs too. */
    public Reserva find(Long id) {
        if (id == null) {
            throw new BusinessRuleException("Reserva é obrigatória.");
        }
        return dao.findById(id).orElseThrow(() -> NotFoundException.ofFem("Reserva", id));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    // BR-07.6
    private PontoDistribuicao pontoOpcional(Long idPonto) {
        if (idPonto == null) {
            return null;
        }
        PontoDistribuicao p = pontoBO.find(idPonto);
        if (!p.isAtivo()) {
            throw new BusinessRuleException("Este ponto de distribuição não está ativo.");
        }
        return p;
    }

    // BR-07.1
    private void checkBeneficiarioAtivo(Beneficiario b) {
        if (!b.isAtivo()) {
            throw new BusinessRuleException("Beneficiário inativo não pode reservar lotes.");
        }
    }

    // BR-07.3
    private void checkPrazo(Lote l) {
        if (l.getStatus() == StatusLote.EXPIRADO || !l.getDataLimiteRetirada().isAfter(now())) {
            throw new BusinessRuleException(MSG_PRAZO);
        }
    }

    // BR-07.4
    private void checkLoteDisponivel(Lote l) {
        if (l.getStatus() != StatusLote.DISPONIVEL) {
            throw new ConflictException(MSG_JA_RESERVADO);
        }
    }

    // BR-07.5
    private void checkSemReservaAtiva(Lote l) {
        if (dao.existsByLote_IdLoteAndStatus(l.getIdLote(), StatusReserva.ATIVA)) {
            throw new ConflictException(MSG_RESERVA_ATIVA);
        }
    }
}
