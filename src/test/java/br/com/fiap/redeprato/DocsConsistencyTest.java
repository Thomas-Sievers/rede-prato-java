package br.com.fiap.redeprato;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Keeps docs/endpoints.md, docs/der.md and docs/class-diagram.md in sync with the code. */
class DocsConsistencyTest {

    private static List<Class<?>> scan(Class<? extends java.lang.annotation.Annotation> ann) throws Exception {
        ClassPathScanningCandidateComponentProvider p = new ClassPathScanningCandidateComponentProvider(false);
        p.addIncludeFilter(new AnnotationTypeFilter(ann));
        List<Class<?>> out = new ArrayList<>();
        for (var bd : p.findCandidateComponents("br.com.fiap.redeprato")) {
            out.add(Class.forName(bd.getBeanClassName()));
        }
        return out;
    }

    private static String first(String[] a) {
        return a.length == 0 ? "" : a[0];
    }

    static List<String> endpoints() throws Exception {
        List<String> out = new ArrayList<>();
        for (Class<?> c : scan(RestController.class)) {
            if (!c.getPackageName().equals("br.com.fiap.redeprato.controller")) {
                continue; // test-only fake controllers
            }
            RequestMapping rm = c.getAnnotation(RequestMapping.class);
            String base = rm == null ? "" : first(rm.value());
            for (Method m : c.getDeclaredMethods()) {
                String verb = null;
                String path = null;
                if (m.isAnnotationPresent(GetMapping.class)) {
                    verb = "GET"; path = first(m.getAnnotation(GetMapping.class).value());
                } else if (m.isAnnotationPresent(PostMapping.class)) {
                    verb = "POST"; path = first(m.getAnnotation(PostMapping.class).value());
                } else if (m.isAnnotationPresent(PutMapping.class)) {
                    verb = "PUT"; path = first(m.getAnnotation(PutMapping.class).value());
                } else if (m.isAnnotationPresent(DeleteMapping.class)) {
                    verb = "DELETE"; path = first(m.getAnnotation(DeleteMapping.class).value());
                }
                if (verb != null) {
                    out.add("`" + verb + "` | `" + base + path + "`");
                }
            }
        }
        return out;
    }

    private static String read(String file) throws Exception {
        return Files.readString(Path.of(file), StandardCharsets.UTF_8);
    }

    @Test
    void todoEndpointEstaDocumentado() throws Exception {
        String doc = read("docs/endpoints.md");
        List<String> eps = endpoints();
        assertThat(eps).hasSizeGreaterThanOrEqualTo(57);
        assertThat(eps).allSatisfy(e -> assertThat(doc).as("docs/endpoints.md").contains(e));
    }

    @Test
    void toda_tabela_e_entidade_estao_nos_diagramas() throws Exception {
        String der = read("docs/der.md");
        String classes = read("docs/class-diagram.md");
        List<Class<?>> entities = scan(Entity.class);
        assertThat(entities).hasSize(9);
        for (Class<?> e : entities) {
            assertThat(der).contains(e.getAnnotation(Table.class).name());
            assertThat(classes).contains("class " + e.getSimpleName());
        }
    }
}
