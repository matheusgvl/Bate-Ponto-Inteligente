package org.dibpt.patepontointeligenteweb.DTO;

public record SalvarPessoaDTO(
        String nome,
        String cpf,
        String rg,
        String dataNascimento,
        String funcao,
        String cardUID
) {
}