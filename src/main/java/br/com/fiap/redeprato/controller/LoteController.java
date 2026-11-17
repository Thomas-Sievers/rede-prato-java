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

import br.com.fiap.redeprato.bo.LoteBO;
import br.com.fiap.redeprato.dto.LoteDTO;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteBO bo;

    public LoteController(LoteBO bo) {
        this.bo = bo;
    }

    @GetMapping
    public List<LoteDTO.Response> listar(@RequestParam(required = false) StatusLote status,
                                         @RequestParam(required = false) Long idOfertante,
                                         @RequestParam(required = false) Long idCategoria,
                                         @RequestParam(required = false) ModalidadeLote modalidade) {
        return bo.listar(status, idOfertante, idCategoria, modalidade);
    }

    @GetMapping("/disponiveis")
    public List<LoteDTO.Response> disponiveis(@RequestParam(required = false) Long idCategoria,
                                              @RequestParam(required = false) ModalidadeLote modalidade,
                                              @RequestParam(required = false) String bairro) {
        return bo.listarDisponiveis(idCategoria, modalidade, bairro);
    }

    @GetMapping("/{id}")
    public LoteDTO.Response buscar(@PathVariable Long id) {
        return bo.buscar(id);
    }

    @PostMapping
    public ResponseEntity<LoteDTO.Response> criar(@RequestBody LoteDTO.Request req) {
        LoteDTO.Response r = bo.criar(req);
        return ResponseEntity.created(URI.create("/api/lotes/" + r.idLote())).body(r);
    }

    @PutMapping("/{id}")
    public LoteDTO.Response atualizar(@PathVariable Long id, @RequestBody LoteDTO.Request req) {
        return bo.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        bo.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Runs the expiry routine now (admin/demo). */
    @PostMapping("/expirar")
    public LoteDTO.ExpiracaoResponse expirar() {
        return new LoteDTO.ExpiracaoResponse(bo.expirarVencidos());
    }
}
