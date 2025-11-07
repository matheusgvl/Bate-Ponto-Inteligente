package org.dibpt.patepontointeligenteweb.DTO;

import org.dibpt.patepontointeligenteweb.Entity.Local;
import java.time.LocalDateTime;

public record LocalResponseDTO(
        Long id,
        String nome,
        String descricao,
        Boolean ativo,
        LocalDateTime dataCadastro
) {
    public LocalResponseDTO(Local local) {
        this(
                local.getId(),
                local.getNome(),
                local.getDescricao(),
                local.getAtivo(),
                local.getDataCadastro()
        );
    }
}