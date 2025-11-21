package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.DTO.LocalRequestDTO;
import org.dibpt.patepontointeligenteweb.DTO.LocalResponseDTO;
import org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO;
import org.dibpt.patepontointeligenteweb.service.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/local")
@CrossOrigin(origins = "*")
public class LocalController {

    private final LocalService localService;

    @Autowired
    public LocalController(LocalService localService) {
        this.localService = localService;
    }

    @PostMapping
    public ResponseEntity<LocalResponseDTO> salvarLocal(@RequestBody LocalRequestDTO dto) {
        LocalResponseDTO response = localService.salvarLocal(dto);
        URI location = URI.create("/api/local/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/list")
    public ResponseEntity<List<LocalResponseDTO>> listarLocais() {
        return ResponseEntity.ok(localService.listarLocaisAtivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalResponseDTO> buscarLocalPorId(@PathVariable Long id) {
        return ResponseEntity.ok(localService.buscarLocalPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalResponseDTO> atualizarLocal(@PathVariable Long id, @RequestBody LocalRequestDTO dto) {
        return ResponseEntity.ok(localService.atualizarLocal(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarLocal(@PathVariable Long id) {
        localService.desativarLocal(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/acessos")
    public ResponseEntity<List<PessoaResponseDTO>> listarPessoasAutorizadas(@PathVariable Long id) {
        return ResponseEntity.ok(localService.listarPessoasAutorizadas(id));
    }

    @PostMapping("/{localId}/permissoes/{pessoaId}")
    public ResponseEntity<Void> adicionarPermissao(@PathVariable Long localId, @PathVariable Long pessoaId) {
        localService.adicionarPermissao(localId, pessoaId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{localId}/permissoes/{pessoaId}")
    public ResponseEntity<Void> removerPermissao(@PathVariable Long localId, @PathVariable Long pessoaId) {
        localService.removerPermissao(localId, pessoaId);
        return ResponseEntity.noContent().build();
    }
}