package org.dibpt.patepontointeligenteweb.DTO;
import java.util.List;

public record LocalRequestDTO(
        String nome,
        String descricao,
        String nivelRestricao,
        Integer capacidadeMaxima,
        List<String> funcoesAutorizadas
) {}