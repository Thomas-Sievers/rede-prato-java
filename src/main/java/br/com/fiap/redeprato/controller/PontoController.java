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

import br.com.fiap.redeprato.service.PontoDistribuicaoService;
import br.com.fiap.redeprato.dto.PontoDTO;

@RestController
@RequestMapping("/api/pontos")
public class PontoController {

    private final PontoDistribuicaoService service;

    public PontoController(PontoDistribuicaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PontoDTO.Response> listar(@RequestParam(required = false) Boolean ativo,
                                          @RequestParam(required = false) String bairro) {
        return service.listar(ativo, bairro);
    }

    @GetMapping("/{id}")
    public PontoDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<PontoDTO.Response> criar(@RequestBody PontoDTO.Request req) {
        PontoDTO.Response r = service.criar(req);
        return ResponseEntity.created(URI.create("/api/pontos/" + r.idPonto())).body(r);
    }

    @PutMapping("/{id}")
    public PontoDTO.Response atualizar(@PathVariable Long id, @RequestBody PontoDTO.Request req) {
        return service.atualizar(id, req);
    }

    /** Logical delete (ativo = 'N'). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
