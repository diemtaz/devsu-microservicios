package com.devsu.cuentas.model.dto;

import lombok.*;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoPatchDTO {
    private BigDecimal valor; // null = no modificar
}
