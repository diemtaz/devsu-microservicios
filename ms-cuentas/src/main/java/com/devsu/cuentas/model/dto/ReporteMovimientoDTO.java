package com.devsu.cuentas.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO del reporte de estado de cuenta (F4).
 * Coincide exactamente con el JSON de ejemplo del enunciado:
 * { "Fecha", "Cliente", "Numero Cuenta", "Tipo", "Saldo Inicial",
 *   "Estado", "Movimiento", "Saldo Disponible" }
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReporteMovimientoDTO {

    @JsonFormat(pattern = "dd/MM/yyyy")
    @JsonProperty("Fecha")
    private LocalDateTime fecha;

    @JsonProperty("Cliente")
    private String cliente;

    @JsonProperty("Numero Cuenta")
    private String numeroCuenta;

    @JsonProperty("Tipo")
    private String tipo;

    @JsonProperty("Saldo Inicial")
    private BigDecimal saldoInicial;

    @JsonProperty("Estado")
    private Boolean estado;

    @JsonProperty("Movimiento")
    private BigDecimal movimiento;

    @JsonProperty("Saldo Disponible")
    private BigDecimal saldoDisponible;
}
