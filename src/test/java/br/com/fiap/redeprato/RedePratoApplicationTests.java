package br.com.fiap.redeprato;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RedePratoApplicationTests {

    @Autowired
    private Clock clock;

    @Test
    void contextLoadsOnH2() {
        assertThat(clock.getZone()).isEqualTo(ZoneId.of("America/Sao_Paulo"));
    }
}
