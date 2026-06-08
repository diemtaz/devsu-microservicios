package com.devsu.clientes.model.dto;

import lombok.*;

/**
 * DTO de respuesta. No incluye contraseña (seguridad).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponseDTO {
    private Long id;
    private String clienteId;
    private String nombre;
    private String genero;
    private Integer edad;
    private String identificacion;
    private String direccion;
    private String telefono;
    private Boolean estado;
}
