package com.devsu.clientes.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Clase base Persona.
 */
@Entity
@Table(name = "persona")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El género es obligatorio")
    @Column(nullable = false)
    private String genero;

    @NotNull(message = "La edad es obligatoria")
    @Positive(message = "La edad debe ser positiva")
    @Column(nullable = false)
    private Integer edad;

    @NotBlank(message = "La identificación es obligatoria")
    @Column(nullable = false, unique = true)
    private String identificacion;

    @NotBlank(message = "La dirección es obligatoria")
    @Column(nullable = false)
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false)
    private String telefono;
}
