package com.devsu.clientes.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cliente hereda de Persona
 */
@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "persona_id")
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Persona {

    @NotBlank(message = "El clienteId es obligatorio")
    @Column(name = "cliente_id", nullable = false, unique = true)
    private String clienteId;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String contrasena;

    @NotNull
    @Column(nullable = false)
    private Boolean estado = true;
}
