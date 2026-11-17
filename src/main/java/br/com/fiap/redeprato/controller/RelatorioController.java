package br.com.fiap.redeprato.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.redeprato.service.RelatorioService;
import br.com.fiap.redeprato.dto.RelatorioDTO;

/** HU22: monthly impact report. */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/ofertantes/{id}/mensal")
    public RelatorioDTO.Mensal mensal(@PathVariable Long id, @RequestParam(required = false) String mes) {
        return service.mensal(id, mes);
    }
}
