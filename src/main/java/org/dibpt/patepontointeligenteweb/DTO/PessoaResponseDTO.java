package org.dibpt.patepontointeligenteweb.DTO;

import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PessoaResponseDTO(
        Long id,
        String nome,
        String cpf,
        String rg,
        LocalDate dataNascimento,
        String funcao,
        String cardUID,
        LocalDateTime dataCadastro
) {

    public PessoaResponseDTO(Pessoa pessoa) {
        this(
                pessoa.getId(),
                pessoa.getNome(),
                pessoa.getCpf(),
                pessoa.getRg(),
                pessoa.getDataNascimento(),
                pessoa.getFuncao().name().toLowerCase(),
                pessoa.getCardUID(),
                pessoa.getDataCadastro()
        );
    }
}