package br.com.fiap.redeprato.controller;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health check used by Render ({@code /api/health}). */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final Environment environment;
    private final Clock clock;

    public HealthController(Environment environment, Clock clock) {
        this.environment = environment;
        this.clock = clock;
    }

    @GetMapping
    public Map<String, String> health() {
        String[] profiles = environment.getActiveProfiles();
        Map<String, String> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("app", "Rede de Prato");
        body.put("profile", profiles.length == 0 ? "default" : String.join(",", profiles));
        body.put("time", OffsetDateTime.now(clock).withNano(0).toString());
        return body;
    }
}
