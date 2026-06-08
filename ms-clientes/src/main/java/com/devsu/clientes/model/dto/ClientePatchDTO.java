package com.devsu.clientes.model.dto;

import lombok.*;

/**
 * DTO para actualización parcial (PATCH) de cliente.
 * Solo los campos no-null se aplican al recurso existente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientePatchDTO {
    private String nombre;
    private String genero;
    private Integer edad;
    private String direccion;
    private String telefono;
    private String contrasena;
    private Boolean estado;
}
