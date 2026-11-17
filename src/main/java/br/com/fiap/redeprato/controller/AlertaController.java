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

import br.com.fiap.redeprato.service.AlertaService;
import br.com.fiap.redeprato.dto.AlertaDTO;
import br.com.fiap.redeprato.entity.enums.TipoAlerta;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService service;

    public AlertaController(AlertaService service) {
        this.service = service;
    }

    /** UC04: the feirante announces the xepa of the banca's lots. */
    @PostMapping("/xepa")
    public ResponseEntity<AlertaDTO.XepaResponse> xepa(@RequestBody AlertaDTO.XepaRequest req) {
        return ResponseEntity.status(201).body(service.avisarXepa(req));
    }

    @GetMapping("/recentes")
    public List<AlertaDTO.Response> recentes(@RequestParam(required = false) Integer horas) {
        return service.recentes(horas);
    }

    @GetMapping
    public List<AlertaDTO.Response> listar(@RequestParam(required = false) TipoAlerta tipo,
                                           @RequestParam(required = false) Long idLote) {
        return service.listar(tipo, idLote);
    }

    @GetMapping("/{id}")
    public AlertaDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AlertaDTO.Response> criar(@RequestBody AlertaDTO.Request req) {
        AlertaDTO.Response r = service.criar(req);
        return ResponseEntity.created(URI.create("/api/alertas/" + r.idAlerta())).body(r);
    }

    @PutMapping("/{id}")
    public AlertaDTO.Response atualizar(@PathVariable Long id, @RequestBody AlertaDTO.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
