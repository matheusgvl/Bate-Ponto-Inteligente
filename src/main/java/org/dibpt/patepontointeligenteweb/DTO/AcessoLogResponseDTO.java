package org.dibpt.patepontointeligenteweb.DTO;

import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import java.time.LocalDateTime;

public record AcessoLogResponseDTO(
        Long id,
        LocalDateTime dataHora,
        String tipoAcesso,
        String nomePessoa, // Pode ser null se for um acesso negado sem pessoa
        String nomeLocal
) {
    public AcessoLogResponseDTO(Acesso acesso) {
        this(
                acesso.getId(),
                acesso.getDataHora(),
                acesso.getTipoAcesso().name(), // Pega o nome do ENUM (ex: "NEGADO_SEM_PERMISSAO_LOCAL")
                acesso.getPessoa() != null ? acesso.getPessoa().getNome() : "Desconhecido", // Checa se a pessoa não é nula
                acesso.getLocal().getNome() // O Local nunca deve ser nulo
        );
    }
}