package br.com.fiap.redeprato;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RedePratoApplication {

    public static final String ZONE = "America/Sao_Paulo";

    public static void main(String[] args) {
        // Render runs in UTC: force Brazilian time for "now" and for DATE columns.
        TimeZone.setDefault(TimeZone.getTimeZone(ZONE));
        SpringApplication.run(RedePratoApplication.class, args);
    }
}
