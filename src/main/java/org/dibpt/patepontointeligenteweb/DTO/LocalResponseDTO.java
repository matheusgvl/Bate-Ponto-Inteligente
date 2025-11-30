package org.dibpt.patepontointeligenteweb.DTO;

import org.dibpt.patepontointeligenteweb.Entity.Local;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record LocalResponseDTO(
        Long id,
        String nome,
        String descricao,
        String nivelRestricao,
        Integer capacidadeMaxima,
        Boolean ativo,
        LocalDateTime dataCadastro,
        List<String> funcoesAutorizadas
) {
    public LocalResponseDTO(Local local) {
        this(
                local.getId(),
                local.getNome(),
                local.getDescricao(),
                local.getNivelRestricao().name(),
                local.getCapacidadeMaxima(),
                local.getAtivo(),
                local.getDataCadastro(),
                local.getFuncoesAutorizadas().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList())
        );
    }
}