package com.devsu.cuentas.model.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CuentaResponseDTO {
    private Long id;
    private String numeroCuenta;
    private String tipoCuenta;
    private BigDecimal saldoInicial;
    private BigDecimal saldoDisponible;
    private Boolean estado;
    private String clienteId;
}
