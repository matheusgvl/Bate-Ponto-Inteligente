package org.dibpt.patepontointeligenteweb.service; // (Confirme se o pacote está correto)

import org.dibpt.patepontointeligenteweb.Entity.Acesso;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso;
import org.dibpt.patepontointeligenteweb.Entity.Pessoa;
import org.dibpt.patepontointeligenteweb.repository.AcessoRepository;
import org.dibpt.patepontointeligenteweb.repository.PessoaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.dibpt.patepontointeligenteweb.DTO.AcessoResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AcessoService {

    @Autowired
    private AcessoRepository acessoRepository;

    @Autowired
    private PessoaRepository pessoaRepository; //


    public ResponseEntity<AcessoResponseDTO> registrarAcessoPorCartao(String cardUID) {
        if (cardUID == null || cardUID.trim().isEmpty()) {
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "UID do cartão não fornecido.", null);
            return ResponseEntity.badRequest().body(response);
        }

        Optional<Pessoa> pessoaOpt = pessoaRepository.findByCardUID(cardUID.trim()); //

        if (pessoaOpt.isEmpty()) {
            Acesso falha = new Acesso(TipoAcesso.NEGADO_CARTAO_NAO_ENCONTRADO, null);
            acessoRepository.save(falha);
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Cartão não cadastrado.", null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Pessoa pessoa = pessoaOpt.get();

        if (!pessoa.getAtivo()) { //
            Acesso falha = new Acesso(TipoAcesso.NEGADO_PESSOA_INATIVA, pessoa);
            acessoRepository.save(falha);
            AcessoResponseDTO response = new AcessoResponseDTO("NEGADO", "Usuário inativo.", pessoa.getNome());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        Acesso sucesso = new Acesso(TipoAcesso.PERMITIDO, pessoa);
        acessoRepository.save(sucesso);
        AcessoResponseDTO response = new AcessoResponseDTO("PERMITIDO", null, pessoa.getNome());
        return ResponseEntity.ok(response);
    }


    public long contarAcessosHoje() {
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDoDia = LocalDate.now().atTime(23, 59, 59);

        return acessoRepository.countByDataHoraBetween(inicioDoDia, fimDoDia); //
    }

    public long contarAcessosNegadosHoje() {
        LocalDateTime inicioDoDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDoDia = LocalDate.now().atTime(23, 59, 59);

        List<TipoAcesso> tiposNegados = List.of(
                TipoAcesso.NEGADO_CARTAO_NAO_ENCONTRADO,
                TipoAcesso.NEGADO_ROSTO_NAO_RECONHECIDO,
                TipoAcesso.NEGADO_PESSOA_INATIVA,
                TipoAcesso.NEGADO_FALHA_LEITURA_CARTAO,
                TipoAcesso.NEGADO_FALHA_DETECCAO_ROSTO
        );

        return acessoRepository.countByTipoAcessoInAndDataHoraBetween(tiposNegados, inicioDoDia, fimDoDia); //
    }
}