package org.dibpt.patepontointeligenteweb.DTO;

import java.time.LocalDate;

public record AtualizarPessoaDTO(
        String nome,
        String cpf,
        String rg,
        String dataNascimento,
        String funcao,
        String cardUID
) {
}