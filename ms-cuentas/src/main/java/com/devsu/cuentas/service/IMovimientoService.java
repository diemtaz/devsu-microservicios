package com.devsu.cuentas.service;

import com.devsu.cuentas.model.dto.MovimientoPatchDTO;
import com.devsu.cuentas.model.dto.MovimientoRequestDTO;
import com.devsu.cuentas.model.dto.MovimientoResponseDTO;
import com.devsu.cuentas.model.dto.ReporteMovimientoDTO;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMovimientoService {
    Page<MovimientoResponseDTO> listarTodos(Pageable pageable);
    MovimientoResponseDTO buscarPorId(Long id);
    MovimientoResponseDTO registrar(MovimientoRequestDTO dto);
    MovimientoResponseDTO actualizar(Long id, MovimientoRequestDTO dto);
    MovimientoResponseDTO actualizarParcial(Long id, MovimientoPatchDTO dto);
    void eliminar(Long id);

    // F4 - Reporte
    Page<ReporteMovimientoDTO> generarReporte(
            String clienteId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable);
}
