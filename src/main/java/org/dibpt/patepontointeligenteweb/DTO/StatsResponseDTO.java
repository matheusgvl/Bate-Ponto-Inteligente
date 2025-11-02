package org.dibpt.patepontointeligenteweb.DTO;

public record StatsResponseDTO(
        long totalPessoas,
        long funcionarios,
        long supervisores,
        long gerentes,
        long terceirizados) { }