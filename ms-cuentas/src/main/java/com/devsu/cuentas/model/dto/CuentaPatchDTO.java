package com.devsu.cuentas.model.dto;

import lombok.*;
import java.math.BigDecimal;

/**
 * DTO para actualización parcial (PATCH) de cuenta.
 * Solo los campos no-null se aplican al recurso existente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaPatchDTO {
    private String tipoCuenta;   // null = no modificar
    private Boolean estado;       // null = no modificar
    private BigDecimal saldoInicial; // null = no modificar
}
