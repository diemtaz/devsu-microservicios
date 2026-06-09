package com.devsu.cuentas.service;

import com.devsu.cuentas.model.dto.CuentaPatchDTO;
import com.devsu.cuentas.model.dto.CuentaRequestDTO;
import com.devsu.cuentas.model.dto.CuentaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICuentaService {
    Page<CuentaResponseDTO> listarTodas(Pageable pageable);
    CuentaResponseDTO buscarPorNumeroCuenta(String numeroCuenta);
    CuentaResponseDTO crear(CuentaRequestDTO dto);
    CuentaResponseDTO actualizar(String numeroCuenta, CuentaRequestDTO dto);
    CuentaResponseDTO actualizarParcial(String numeroCuenta, CuentaPatchDTO dto);
    void eliminar(String numeroCuenta);
}
