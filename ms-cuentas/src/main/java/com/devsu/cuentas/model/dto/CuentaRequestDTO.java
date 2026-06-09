package com.devsu.cuentas.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CuentaRequestDTO {

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
    private BigDecimal saldoInicial;

    @Builder.Default
    private Boolean estado = true;

    @NotBlank(message = "El clienteId es obligatorio")
    private String clienteId;
}
