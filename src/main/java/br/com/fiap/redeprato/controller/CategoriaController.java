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
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.redeprato.bo.CategoriaAlimentoBO;
import br.com.fiap.redeprato.dto.CategoriaDTO;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaAlimentoBO bo;

    public CategoriaController(CategoriaAlimentoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<CategoriaDTO.Response> listar() {
        return bo.listar();
    }

    @GetMapping("/{id}")
    public CategoriaDTO.Response buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO.Response> criar(@RequestBody CategoriaDTO.Request req) {
        CategoriaDTO.Response r = bo.criar(req);
        return ResponseEntity.created(URI.create("/api/categorias/" + r.idCategoria())).body(r);
    }

    @PutMapping("/{id}")
    public CategoriaDTO.Response atualizar(@PathVariable Long id, @RequestBody CategoriaDTO.Request req) {
        return bo.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
