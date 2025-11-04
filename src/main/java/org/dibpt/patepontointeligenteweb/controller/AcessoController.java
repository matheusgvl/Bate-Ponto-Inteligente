package org.dibpt.patepontointeligenteweb.controller;

import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO; // Importe o novo DTO
import org.dibpt.patepontointeligenteweb.service.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/acesso")
@CrossOrigin(origins = "*")
public class AcessoController {

    @Autowired
    private AcessoService acessoService; //

    @PostMapping("/registrar/cartao")
    public ResponseEntity<AcessoResponseDTO> registrarAcesso(@RequestBody Map<String, String> requestBody) {
        String uid = requestBody.get("uid");
        return acessoService.registrarAcessoPorCartao(uid);
    }
}