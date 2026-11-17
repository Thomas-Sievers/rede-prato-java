package br.com.fiap.redeprato.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

/** D-16: Oracle is the default runtime database; H2 only on the dev profile, which every test uses. */
@SpringBootTest
class ProfilesConfigTest {

    @Autowired
    private Environment env;

    private static Properties load(String name) throws Exception {
        Properties p = new Properties();
        try (InputStream in = ProfilesConfigTest.class.getClassLoader().getResourceAsStream(name)) {
            assertThat(in).as(name).isNotNull();
            p.load(in);
        }
        return p;
    }

    @Test
    void appRodaNoOraclePorPadrao() throws Exception {
        assertThat(load("application.properties").getProperty("spring.profiles.default")).isEqualTo("oracle");
        Properties oracle = load("application-oracle.properties");
        assertThat(oracle.getProperty("spring.datasource.url")).isEqualTo("${ORACLE_URL}");
        assertThat(oracle.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
        assertThat(oracle.getProperty("app.seed.enabled")).isEqualTo("false");
    }

    @Test
    void h2SoNoPerfilDev() throws Exception {
        assertThat(load("application.properties").getProperty("spring.datasource.url")).isNull();
        assertThat(load("application-dev.properties").getProperty("spring.datasource.url")).startsWith("jdbc:h2:mem:");
    }

    @Test
    void testesRodamNoH2() {
        assertThat(env.getActiveProfiles()).containsExactly("dev");
        assertThat(env.getProperty("spring.datasource.url")).startsWith("jdbc:h2:mem:");
    }
}
