package org.dibpt.patepontointeligenteweb.DTO;

import org.dibpt.patepontointeligenteweb.Entity.Local;
import java.time.LocalDateTime;

public record LocalResponseDTO(
        Long id,
        String nome,
        String descricao,
        String nivelRestricao,
        Integer capacidadeMaxima,
        Boolean ativo,
        LocalDateTime dataCadastro
) {
    public LocalResponseDTO(Local local) {
        this(
                local.getId(),
                local.getNome(),
                local.getDescricao(),
                local.getNivelRestricao(),
                local.getCapacidadeMaxima(),
                local.getAtivo(),
                local.getDataCadastro()
        );
    }
}