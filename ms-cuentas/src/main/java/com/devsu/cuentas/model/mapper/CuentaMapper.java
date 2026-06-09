package com.devsu.cuentas.model.mapper;

import com.devsu.cuentas.model.dto.*;
import com.devsu.cuentas.model.entity.Cuenta;
import com.devsu.cuentas.model.entity.Movimiento;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaRequestDTO dto) {
        return Cuenta.builder()
                .numeroCuenta(dto.getNumeroCuenta())
                .tipoCuenta(dto.getTipoCuenta())
                .saldoInicial(dto.getSaldoInicial())
                .saldoDisponible(dto.getSaldoInicial())
                .estado(dto.getEstado() != null ? dto.getEstado() : true)
                .clienteId(dto.getClienteId())
                .build();
    }

    public CuentaResponseDTO toDTO(Cuenta cuenta) {
        return CuentaResponseDTO.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .saldoDisponible(cuenta.getSaldoDisponible())
                .estado(cuenta.getEstado())
                .clienteId(cuenta.getClienteId())
                .build();
    }

    public MovimientoResponseDTO toMovimientoDTO(Movimiento m) {
        return MovimientoResponseDTO.builder()
                .id(m.getId())
                .fecha(m.getFecha())
                .tipoMovimiento(m.getTipoMovimiento())
                .valor(m.getValor())
                .saldo(m.getSaldo())
                .numeroCuenta(m.getCuenta().getNumeroCuenta())
                .build();
    }
}
