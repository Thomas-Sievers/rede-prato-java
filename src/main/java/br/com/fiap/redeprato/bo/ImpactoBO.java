package br.com.fiap.redeprato.bo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.redeprato.dao.DestinacaoDAO;
import br.com.fiap.redeprato.dao.ReservaDAO;
import br.com.fiap.redeprato.dto.ImpactoDTO;
import br.com.fiap.redeprato.entity.Beneficiario;
import br.com.fiap.redeprato.entity.Ofertante;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;
import br.com.fiap.redeprato.exception.BusinessRuleException;

/** Impact panel (HU15): kg diverted from waste, always computed from T_RP_DESTINACAO. */
@Service
public class ImpactoBO {

    static final int SEMANAS = 4;

    private final DestinacaoDAO destinacaoDAO;
    private final ReservaDAO reservaDAO;
    private final OfertanteBO ofertanteBO;
    private final Clock clock;

    public ImpactoBO(DestinacaoDAO destinacaoDAO, ReservaDAO reservaDAO, OfertanteBO ofertanteBO, Clock clock) {
        this.destinacaoDAO = destinacaoDAO;
        this.reservaDAO = reservaDAO;
        this.ofertanteBO = ofertanteBO;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ImpactoDTO.Ofertante porOfertante(Long idOfertante, String mes) {
        Ofertante o = ofertanteBO.find(idOfertante); // BR-10.6
        YearMonth ym = parseMes(mes);
        Totais t = totais(o.getIdOfertante(), ym);
        return new ImpactoDTO.Ofertante(o.getIdOfertante(), o.getNome(), ym.toString(),
                t.total, t.consumo, t.compostagem, t.percentual, t.lotes, t.beneficiarios, t.pessoas,
                semanas(o.getIdOfertante()), o.isPossuiSelo(), o.getDataSelo());
    }

    @Transactional(readOnly = true)
    public ImpactoDTO.Rede rede(String mes) {
        YearMonth ym = parseMes(mes);
        Totais t = totais(null, ym);
        List<ImpactoDTO.KgPorOfertante> ranking = destinacaoDAO.ranking(inicio(ym), fim(ym)).stream()
                .map(k -> new ImpactoDTO.KgPorOfertante(k.idOfertante(), k.nome(), kg(k.totalKg())))
                .toList();
        return new ImpactoDTO.Rede(ym.toString(), t.total, t.consumo, t.compostagem, t.percentual,
                t.lotes, t.beneficiarios, t.pessoas, semanas(null), ranking);
    }

    private record Totais(BigDecimal total, BigDecimal consumo, BigDecimal compostagem, BigDecimal percentual,
                          long lotes, long beneficiarios, long pessoas) {
    }

    private Totais totais(Long idOfertante, YearMonth ym) {
        LocalDateTime ini = inicio(ym);
        LocalDateTime fim = fim(ym);
        BigDecimal consumo = BigDecimal.ZERO;
        BigDecimal compostagem = BigDecimal.ZERO;
        for (ImpactoDTO.KgPorTipo k : destinacaoDAO.somaPorTipo(idOfertante, ini, fim)) { // BR-10.1
            if (k.tipo() == TipoDestinacao.CONSUMO) {
                consumo = consumo.add(nz(k.kg()));
            } else if (k.tipo() == TipoDestinacao.COMPOSTAGEM) {
                compostagem = compostagem.add(nz(k.kg()));
            }
        }
        BigDecimal total = consumo.add(compostagem);
        long lotes = reservaDAO.contarLotesRetirados(idOfertante, ini, fim); // BR-10.2
        List<Beneficiario> atendidos = reservaDAO.beneficiariosAtendidos(idOfertante, ini, fim); // BR-10.3
        long pessoas = atendidos.stream().mapToLong(b -> b.getQtdPessoas() == null ? 1 : b.getQtdPessoas()).sum();
        return new Totais(kg(total), kg(consumo), kg(compostagem), percentual(consumo, total),
                lotes, atendidos.size(), pessoas);
    }

    /** BR-10.4: consumo / total × 100, one decimal, 0 when total is 0. */
    static BigDecimal percentual(BigDecimal consumo, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return consumo.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP);
    }

    /** BR-10.5: the last 4 Monday–Sunday weeks, the last one being the current week. */
    List<ImpactoDTO.Semana> semanas(Long idOfertante) {
        LocalDate segundaAtual = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<ImpactoDTO.Semana> out = new ArrayList<>();
        for (int i = SEMANAS - 1; i >= 0; i--) {
            LocalDate ini = segundaAtual.minusWeeks(i);
            LocalDate fimInclusivo = ini.plusDays(6);
            BigDecimal soma = destinacaoDAO.somaPeriodo(idOfertante, ini.atStartOfDay(), ini.plusDays(7).atStartOfDay());
            out.add(new ImpactoDTO.Semana(ini, fimInclusivo, kg(nz(soma))));
        }
        return out;
    }

    /** BR-10.7: "yyyy-MM", default = current month. */
    YearMonth parseMes(String mes) {
        String m = Checks.clean(mes);
        if (m == null) {
            return YearMonth.now(clock);
        }
        if (!m.matches("\\d{4}-\\d{2}")) {
            throw new BusinessRuleException("Mês deve estar no formato AAAA-MM.");
        }
        try {
            return YearMonth.parse(m);
        } catch (DateTimeParseException e) {
            throw new BusinessRuleException("Mês deve estar no formato AAAA-MM.");
        }
    }

    private static LocalDateTime inicio(YearMonth ym) {
        return ym.atDay(1).atStartOfDay();
    }

    private static LocalDateTime fim(YearMonth ym) {
        return ym.plusMonths(1).atDay(1).atStartOfDay();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal kg(BigDecimal v) {
        return nz(v).setScale(2, RoundingMode.HALF_UP);
    }
}
