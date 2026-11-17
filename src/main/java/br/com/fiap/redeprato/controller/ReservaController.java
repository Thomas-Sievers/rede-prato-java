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

import br.com.fiap.redeprato.service.ReservaService;
import br.com.fiap.redeprato.dto.ReservaDTO;
import br.com.fiap.redeprato.entity.enums.StatusReserva;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReservaDTO.Response> listar(@RequestParam(required = false) Long idBeneficiario,
                                            @RequestParam(required = false) Long idOfertante,
                                            @RequestParam(required = false) StatusReserva status,
                                            @RequestParam(required = false) Long idLote) {
        return service.listar(idBeneficiario, idOfertante, status, idLote);
    }

    @GetMapping("/{id}")
    public ReservaDTO.Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ReservaDTO.Response> criar(@RequestBody ReservaDTO.Request req) {
        ReservaDTO.Response r = service.criar(req);
        return ResponseEntity.created(URI.create("/api/reservas/" + r.idReserva())).body(r);
    }

    @PutMapping("/{id}")
    public ReservaDTO.Response atualizar(@PathVariable Long id, @RequestBody ReservaDTO.UpdateRequest req) {
        return service.atualizar(id, req);
    }

    @PutMapping("/{id}/cancelar")
    public ReservaDTO.Response cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    /** UC09: confirms the pickup (reserva RETIRADA, lote RETIRADO). */
    @PutMapping("/{id}/retirada")
    public ReservaDTO.Response confirmarRetirada(@PathVariable Long id) {
        return service.confirmarRetirada(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
