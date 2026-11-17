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

import br.com.fiap.redeprato.service.BeneficiarioService;
import br.com.fiap.redeprato.dto.BeneficiarioDTO;
import br.com.fiap.redeprato.entity.enums.TipoBeneficiario;

@RestController
@RequestMapping("/api/beneficiarios")
public class BeneficiarioController {

    private final BeneficiarioService service;

    public BeneficiarioController(BeneficiarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<BeneficiarioDTO.Response> listar(@RequestParam(required = false) TipoBeneficiario tipo,
                                                 @RequestParam(required = false) Boolean ativo) {
        return service.listar(tipo, ativo);
    }

    @GetMapping("/{id}")
    public BeneficiarioDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<BeneficiarioDTO.Response> criar(@RequestBody BeneficiarioDTO.Request req) {
        BeneficiarioDTO.Response r = service.criar(req);
        return ResponseEntity.created(URI.create("/api/beneficiarios/" + r.idBeneficiario())).body(r);
    }

    @PutMapping("/{id}")
    public BeneficiarioDTO.Response atualizar(@PathVariable Long id, @RequestBody BeneficiarioDTO.Request req) {
        return service.atualizar(id, req);
    }

    /** Logical delete (ativo = 'N'). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
