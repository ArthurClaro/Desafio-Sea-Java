package br.com.sea.desafio.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "telefones")
@Getter
@Setter
@NoArgsConstructor
public class Telefone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTelefone tipo;

    /** Telefone persistido sem máscara (apenas dígitos, com DDD). */
    @Column(nullable = false, length = 11)
    private String numero;

    public Telefone(TipoTelefone tipo, String numero) {
        this.tipo = tipo;
        this.numero = numero;
    }
}
