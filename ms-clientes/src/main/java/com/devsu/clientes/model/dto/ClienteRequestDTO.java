package com.devsu.clientes.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO de request para crear/actualizar Cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El género es obligatorio")
    private String genero;

    @NotNull(message = "La edad es obligatoria")
    @Positive(message = "La edad debe ser un número positivo")
    private Integer edad;

    @NotBlank(message = "La identificación es obligatoria")
    private String identificacion;

    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotBlank(message = "El clienteId es obligatorio")
    private String clienteId;

    @NotBlank(message = "La contraseña es obligatoria")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    private Boolean estado = true;
}
