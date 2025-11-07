package org.dibpt.patepontointeligenteweb.DTO;

import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import java.time.LocalDateTime;

public record AcessoLogResponseDTO(
        Long id,
        LocalDateTime dataHora,
        String tipoAcesso,
        String nomePessoa,
        String nomeLocal
) {
    public AcessoLogResponseDTO(Acesso acesso) {
        this(
                acesso.getId(),
                acesso.getDataHora(),
                acesso.getTipoAcesso().name(),
                acesso.getPessoa() != null ? acesso.getPessoa().getNome() : "Desconhecido",
                acesso.getLocal().getNome()
        );
    }
}