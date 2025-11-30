package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.DTO.LocalRequestDTO;
import org.dibpt.patepontointeligenteweb.DTO.LocalResponseDTO;
import org.dibpt.patepontointeligenteweb.service.LocalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/test")
    public ResponseEntity<String> testeEndpoint() {
        return ResponseEntity.ok("Endpoint funcionando!");
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocalResponseDTO> buscarLocalPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(localService.buscarLocalPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocalResponseDTO> atualizarLocal(@PathVariable Long id, @RequestBody LocalRequestDTO dto) {
        try {
            return ResponseEntity.ok(localService.atualizarLocal(id, dto));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativarLocal(@PathVariable Long id) {
        try {
            localService.desativarLocal(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{id}/acessos")
    public ResponseEntity<List<org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO>> listarPessoasAutorizadas(@PathVariable Long id) {
        try {
            List<org.dibpt.patepontointeligenteweb.DTO.PessoaResponseDTO> pessoas = localService.listarPessoasAutorizadas(id);
            return ResponseEntity.ok(pessoas);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{localId}/permissoes/{pessoaId}")
    public ResponseEntity<Void> adicionarPermissao(@PathVariable Long localId, @PathVariable Long pessoaId) {
        try {
            localService.adicionarPermissao(localId, pessoaId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{localId}/permissoes/{pessoaId}")
    public ResponseEntity<Void> removerPermissao(@PathVariable Long localId, @PathVariable Long pessoaId) {
        try {
            localService.removerPermissao(localId, pessoaId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}