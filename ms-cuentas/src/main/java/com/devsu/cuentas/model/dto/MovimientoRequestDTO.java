package com.devsu.cuentas.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

/**
 * DTO para registrar un movimiento.
 * El valor puede ser positivo (depósito) o negativo (retiro).
 * No se envía el tipo: el servicio lo infiere del signo del valor.
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoRequestDTO {

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String numeroCuenta;

    @NotNull(message = "El valor es obligatorio")
    private BigDecimal valor;
}
