package org.dibpt.patepontointeligenteweb.DTO;

import java.time.LocalDate;

public record SalvarPessoaDTO(
        String nome,
        String cpf,
        String rg,
        String dataNascimento,
        String funcao,
        String cardUID
) {
}