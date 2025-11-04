package org.dibpt.patepontointeligenteweb.Entity;

import jakarta.persistence.*;
import org.dibpt.patepontointeligenteweb.Entity.ENUM.TipoAcesso;
import java.time.LocalDateTime;

@Entity
@Table(name = "acessos")
public class Acesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public TipoAcesso getTipoAcesso() {
        return tipoAcesso;
    }

    public void setTipoAcesso(TipoAcesso tipoAcesso) {
        this.tipoAcesso = tipoAcesso;
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAcesso tipoAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = true)
    private Pessoa pessoa;

    public Acesso() {
        this.dataHora = LocalDateTime.now();
    }

    public Acesso(TipoAcesso tipoAcesso, Pessoa pessoa) {
        this.dataHora = LocalDateTime.now();
        this.tipoAcesso = tipoAcesso;
        this.pessoa = pessoa;
    }

}