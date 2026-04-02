package com.rachai.api.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    private String nome;

    private String fotoPerfilUrl;

    private String biografia;

    @Column(name = "data_cadastro", updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate(){
        this.dataCadastro = LocalDateTime.now();
    }

}