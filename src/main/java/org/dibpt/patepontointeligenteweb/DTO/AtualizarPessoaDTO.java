package org.dibpt.patepontointeligenteweb.DTO;

public record AtualizarPessoaDTO(
        String nome,
        String cpf,
        String rg,
        String dataNascimento,
        String funcao,
        String cardUID
) {
}