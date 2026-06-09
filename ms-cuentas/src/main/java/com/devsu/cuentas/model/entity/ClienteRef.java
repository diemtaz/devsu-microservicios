package com.devsu.cuentas.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Referencia local del Cliente dentro de MS-Cuentas.
 */
@Entity
@Table(name = "cliente_ref")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, unique = true)
    private String clienteId;

    @Column(nullable = false)
    private String nombre;
}
