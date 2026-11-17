package br.com.fiap.redeprato.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.fiap.redeprato.entity.CategoriaAlimento;
import br.com.fiap.redeprato.entity.Endereco;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.CondicaoLote;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;

/** Request/response records for {@link Lote}. */
public final class LoteDTO {

    private LoteDTO() {
    }

    public record Request(Long idOfertante, Long idCategoria, String descricao, BigDecimal pesoKg,
                          CondicaoLote condicao, ModalidadeLote modalidade, BigDecimal preco,
                          BigDecimal valorEstimado, LocalDateTime dataLimiteRetirada, String urlFoto,
                          Boolean precisaTransporte) {
    }

    /** Flat view of a lote with its ofertante, address and category. */
    public record Response(Long idLote, String descricao, BigDecimal pesoKg, CondicaoLote condicao,
                           ModalidadeLote modalidade, BigDecimal preco, BigDecimal valorEstimado,
                           LocalDateTime dataCadastro, LocalDateTime dataLimiteRetirada, String urlFoto,
                           boolean precisaTransporte, StatusLote status,
                           Long idOfertante, String nomeOfertante, TipoOfertante tipoOfertante,
                           boolean ofertantePossuiSelo, String telefoneOfertante,
                           String logradouro, String numero, String complemento, String bairro,
                           Long idCategoria, String nomeCategoria) {
        public static Response from(Lote l) {
            Ofertante o = l.getOfertante();
            Endereco e = o == null ? null : o.getEndereco();
            CategoriaAlimento c = l.getCategoria();
            return new Response(l.getIdLote(), l.getDescricao(), l.getPesoKg(), l.getCondicao(),
                    l.getModalidade(), l.getPreco(), l.getValorEstimado(), l.getDataCadastro(),
                    l.getDataLimiteRetirada(), l.getUrlFoto(), l.isPrecisaTransporte(), l.getStatus(),
                    o == null ? null : o.getIdOfertante(),
                    o == null ? null : o.getNome(),
                    o == null ? null : o.getTipo(),
                    o != null && o.isPossuiSelo(),
                    o == null ? null : o.getTelefone(),
                    e == null ? null : e.getLogradouro(),
                    e == null ? null : e.getNumero(),
                    e == null ? null : e.getComplemento(),
                    e == null ? null : e.getBairro(),
                    c == null ? null : c.getIdCategoria(),
                    c == null ? null : c.getNome());
        }
    }

    public record ExpiracaoResponse(int expirados) {
    }
}
