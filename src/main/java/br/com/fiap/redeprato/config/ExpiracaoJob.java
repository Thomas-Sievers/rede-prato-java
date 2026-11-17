package br.com.fiap.redeprato.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.fiap.redeprato.service.LoteService;

/** UC22 / HU21: expires overdue lots every 5 minutes. */
@Component
@ConditionalOnProperty(name = "app.expiracao.enabled", havingValue = "true", matchIfMissing = true)
public class ExpiracaoJob {

    private static final Logger log = LoggerFactory.getLogger(ExpiracaoJob.class);

    private final LoteService loteService;

    public ExpiracaoJob(LoteService loteService) {
        this.loteService = loteService;
    }

    @Scheduled(fixedDelayString = "${app.expiracao.intervalo-ms:300000}", initialDelay = 60000)
    public void expirar() {
        int n = loteService.expirarVencidos();
        if (n > 0) {
            log.info("Expiry job: {} lote(s) expired", n);
        }
    }
}
