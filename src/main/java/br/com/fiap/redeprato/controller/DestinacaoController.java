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

import br.com.fiap.redeprato.bo.DestinacaoBO;
import br.com.fiap.redeprato.dto.DestinacaoDTO;
import br.com.fiap.redeprato.entity.enums.TipoDestinacao;

@RestController
@RequestMapping("/api/destinacoes")
public class DestinacaoController {

    private final DestinacaoBO bo;

    public DestinacaoController(DestinacaoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<DestinacaoDTO.Response> listar(@RequestParam(required = false) Long idLote,
                                               @RequestParam(required = false) TipoDestinacao tipo) {
        return bo.listar(idLote, tipo);
    }

    @GetMapping("/{id}")
    public DestinacaoDTO.Response buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<DestinacaoDTO.Response> criar(@RequestBody DestinacaoDTO.Request req) {
        DestinacaoDTO.Response r = bo.criar(req);
        return ResponseEntity.created(URI.create("/api/destinacoes/" + r.idDestinacao())).body(r);
    }

    @PutMapping("/{id}")
    public DestinacaoDTO.Response atualizar(@PathVariable Long id, @RequestBody DestinacaoDTO.Request req) {
        return bo.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
