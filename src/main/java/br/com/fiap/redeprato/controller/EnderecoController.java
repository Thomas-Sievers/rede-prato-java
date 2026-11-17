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

import br.com.fiap.redeprato.bo.EnderecoBO;
import br.com.fiap.redeprato.dto.EnderecoDTO;

@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {

    private final EnderecoBO bo;

    public EnderecoController(EnderecoBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<EnderecoDTO.Response> listar(@RequestParam(required = false) String bairro) {
        return bo.listar(bairro);
    }

    @GetMapping("/{id}")
    public EnderecoDTO.Response buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<EnderecoDTO.Response> criar(@RequestBody EnderecoDTO.Request req) {
        EnderecoDTO.Response r = bo.criar(req);
        return ResponseEntity.created(URI.create("/api/enderecos/" + r.idEndereco())).body(r);
    }

    @PutMapping("/{id}")
    public EnderecoDTO.Response atualizar(@PathVariable Long id, @RequestBody EnderecoDTO.Request req) {
        return bo.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
