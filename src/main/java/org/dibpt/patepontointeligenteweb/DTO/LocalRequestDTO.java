package org.dibpt.patepontointeligenteweb.DTO;

public record LocalRequestDTO(
        String nome,
        String descricao,
        String nivelRestricao,
        Integer capacidadeMaxima
) {}