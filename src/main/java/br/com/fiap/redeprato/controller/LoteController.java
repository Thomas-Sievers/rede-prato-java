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

import br.com.fiap.redeprato.service.DestinacaoService;
import br.com.fiap.redeprato.service.LoteService;
import br.com.fiap.redeprato.dto.DestinacaoDTO;
import br.com.fiap.redeprato.dto.LoteDTO;
import br.com.fiap.redeprato.entity.enums.ModalidadeLote;
import br.com.fiap.redeprato.entity.enums.StatusLote;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteService service;
    private final DestinacaoService destinacaoService;

    public LoteController(LoteService service, DestinacaoService destinacaoService) {
        this.service = service;
        this.destinacaoService = destinacaoService;
    }

    /** HU14: registers consumo and/or compostagem of one lote in one call. */
    @PostMapping("/{id}/destinacoes")
    public ResponseEntity<List<DestinacaoDTO.Response>> registrarDestinos(@PathVariable Long id,
                                                                          @RequestBody DestinacaoDTO.LoteRequest req) {
        return ResponseEntity.status(201).body(destinacaoService.registrarLote(id, req));
    }

    @GetMapping
    public List<LoteDTO.Response> listar(@RequestParam(required = false) StatusLote status,
                                         @RequestParam(required = false) Long idOfertante,
                                         @RequestParam(required = false) Long idCategoria,
                                         @RequestParam(required = false) ModalidadeLote modalidade,
                                         @RequestParam(required = false) Boolean precisaTransporte) {
        return service.listar(status, idOfertante, idCategoria, modalidade, precisaTransporte);
    }

    @GetMapping("/disponiveis")
    public List<LoteDTO.Response> disponiveis(@RequestParam(required = false) Long idCategoria,
                                              @RequestParam(required = false) ModalidadeLote modalidade,
                                              @RequestParam(required = false) String bairro) {
        return service.listarDisponiveis(idCategoria, modalidade, bairro);
    }

    @GetMapping("/{id}")
    public LoteDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<LoteDTO.Response> criar(@RequestBody LoteDTO.Request req) {
        LoteDTO.Response r = service.criar(req);
        return ResponseEntity.created(URI.create("/api/lotes/" + r.idLote())).body(r);
    }

    @PutMapping("/{id}")
    public LoteDTO.Response atualizar(@PathVariable Long id, @RequestBody LoteDTO.Request req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Runs the expiry routine now (admin/demo). */
    @PostMapping("/expirar")
    public LoteDTO.ExpiracaoResponse expirar() {
        return new LoteDTO.ExpiracaoResponse(service.expirarVencidos());
    }
}
