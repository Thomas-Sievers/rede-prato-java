package br.com.fiap.redeprato.bo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.DestinacaoDAO;
import br.com.fiap.redeprato.dto.RelatorioDTO;
import br.com.fiap.redeprato.entity.Destinacao;
import br.com.fiap.redeprato.entity.Lote;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;

/** HU22 / UC23: monthly report per ofertante (lotes with destination, kg and estimated value). */
@Service
public class RelatorioBO {

    private final DestinacaoDAO destinacaoDAO;
    private final OfertanteBO ofertanteBO;
    private final ImpactoBO impactoBO;

    public RelatorioBO(DestinacaoDAO destinacaoDAO, OfertanteBO ofertanteBO, ImpactoBO impactoBO) {
        this.destinacaoDAO = destinacaoDAO;
        this.ofertanteBO = ofertanteBO;
        this.impactoBO = impactoBO;
    }

    @Transactional(readOnly = true)
    public RelatorioDTO.Mensal mensal(Long idOfertante, String mes) {
        Ofertante o = ofertanteBO.find(idOfertante);
        YearMonth ym = impactoBO.parseMes(mes);
        LocalDateTime ini = ym.atDay(1).atStartOfDay();
        LocalDateTime fim = ym.plusMonths(1).atDay(1).atStartOfDay();

        Map<Long, List<Destinacao>> porLote = new LinkedHashMap<>();
        for (Destinacao d : destinacaoDAO.doOfertanteNoPeriodo(o.getIdOfertante(), ini, fim)) { // BR-18.1
            porLote.computeIfAbsent(d.getLote().getIdLote(), k -> new ArrayList<>()).add(d);
        }
        List<RelatorioDTO.Item> itens = new ArrayList<>();
        BigDecimal consumoTotal = BigDecimal.ZERO;
        BigDecimal compostagemTotal = BigDecimal.ZERO;
        BigDecimal valorTotal = BigDecimal.ZERO;
        for (List<Destinacao> ds : porLote.values()) {
            Lote l = ds.get(0).getLote();
            BigDecimal consumo = soma(ds, TipoDestinacao.CONSUMO);
            BigDecimal compostagem = soma(ds, TipoDestinacao.COMPOSTAGEM);
            LocalDateTime data = ds.get(ds.size() - 1).getDataDestinacao();
            itens.add(new RelatorioDTO.Item(l.getIdLote(), l.getDescricao(),
                    l.getCategoria() == null ? null : l.getCategoria().getNome(), data, l.getPesoKg(),
                    l.getValorEstimado(), consumo, compostagem));
            consumoTotal = consumoTotal.add(consumo);
            compostagemTotal = compostagemTotal.add(compostagem);
            if (l.getValorEstimado() != null) { // BR-18.2
                valorTotal = valorTotal.add(l.getValorEstimado());
            }
        }
        return new RelatorioDTO.Mensal(o.getIdOfertante(), o.getNome(), ym.toString(), itens,
                consumoTotal.add(compostagemTotal), consumoTotal, compostagemTotal, valorTotal);
    }

    private static BigDecimal soma(List<Destinacao> ds, TipoDestinacao tipo) {
        return ds.stream().filter(d -> d.getTipo() == tipo).map(Destinacao::getPesoKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
