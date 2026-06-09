package com.devsu.cuentas.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Cuenta.
 */
@Entity
@Table(name = "cuenta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El número de cuenta es obligatorio")
    @Column(name = "numero_cuenta", nullable = false, unique = true)
    private String numeroCuenta;

    @NotBlank(message = "El tipo de cuenta es obligatorio")
    @Column(name = "tipo_cuenta", nullable = false)
    private String tipoCuenta;

    @NotNull(message = "El saldo inicial es obligatorio")
    @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_disponible", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(nullable = false)
    private Boolean estado = true;

    @NotBlank(message = "El clienteId es obligatorio")
    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @OneToMany(mappedBy = "cuenta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Movimiento> movimientos = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.saldoDisponible == null) {
            this.saldoDisponible = this.saldoInicial;
        }
    }
}
