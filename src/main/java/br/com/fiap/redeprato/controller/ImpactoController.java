package br.com.fiap.redeprato.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.redeprato.service.ImpactoService;
import br.com.fiap.redeprato.dto.ImpactoDTO;

/** Painel de impacto (HU15): read-only aggregates. */
@RestController
@RequestMapping("/api/impacto")
public class ImpactoController {

    private final ImpactoService service;

    public ImpactoController(ImpactoService service) {
        this.service = service;
    }

    @GetMapping("/ofertantes/{id}")
    public ImpactoDTO.Ofertante porOfertante(@PathVariable Long id, @RequestParam(required = false) String mes) {
        return service.porOfertante(id, mes);
    }

    @GetMapping("/rede")
    public ImpactoDTO.Rede rede(@RequestParam(required = false) String mes) {
        return service.rede(mes);
    }
}
