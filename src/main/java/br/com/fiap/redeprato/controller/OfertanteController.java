package br.com.fiap.redeprato.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.redeprato.bo.OfertanteBO;
import br.com.fiap.redeprato.dto.OfertanteDTO;
import br.com.fiap.redeprato.entity.enums.TipoOfertante;

@RestController
@RequestMapping("/api/ofertantes")
public class OfertanteController {

    private final OfertanteBO bo;

    public OfertanteController(OfertanteBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<OfertanteDTO.Response> listar(@RequestParam(required = false) TipoOfertante tipo,
                                              @RequestParam(required = false) Boolean ativo,
                                              @RequestParam(required = false) String bairro) {
        return bo.listar(tipo, ativo, bairro);
    }

    @GetMapping("/{id}")
    public OfertanteDTO.Response buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<OfertanteDTO.Response> criar(@RequestBody OfertanteDTO.Request req) {
        OfertanteDTO.Response r = bo.criar(req);
        return ResponseEntity.created(URI.create("/api/ofertantes/" + r.idOfertante())).body(r);
    }

    @PutMapping("/{id}")
    public OfertanteDTO.Response atualizar(@PathVariable Long id, @RequestBody OfertanteDTO.Request req) {
        return bo.atualizar(id, req);
    }

    /** Logical delete (ativo = 'N'). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/selo")
    public OfertanteDTO.Response concederSelo(@PathVariable Long id) {
        return bo.concederSelo(id);
    }

    @DeleteMapping("/{id}/selo")
    public ResponseEntity<Void> revogarSelo(@PathVariable Long id) {
        bo.revogarSelo(id);
        return ResponseEntity.noContent().build();
    }
}
