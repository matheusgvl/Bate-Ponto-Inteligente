package org.dibpt.patepontointeligenteweb.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AcessoResponseDTO(
        String status,
        String motivo,
        String nomePessoa
) {
}