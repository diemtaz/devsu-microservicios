package com.devsu.cuentas.controller;

import com.devsu.cuentas.model.dto.MovimientoPatchDTO;
import com.devsu.cuentas.model.dto.MovimientoRequestDTO;
import com.devsu.cuentas.model.dto.MovimientoResponseDTO;
import com.devsu.cuentas.model.dto.ReporteMovimientoDTO;
import com.devsu.cuentas.service.IMovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Controlador de Movimientos y Reportes.
 *
 * RUTAS (con context-path /api ya aplicado en application.yml):
 *   GET    http://localhost:8082/api/movimientos
 *   GET    http://localhost:8082/api/movimientos/{id}
 *   POST   http://localhost:8082/api/movimientos
 *   PUT    http://localhost:8082/api/movimientos/{id}
 *   PATCH  http://localhost:8082/api/movimientos/{id}
 *   DELETE http://localhost:8082/api/movimientos/{id}
 *   GET    http://localhost:8082/api/reportes?fecha=2022-01-01,2022-12-31&clienteId=jose123
 *
 */
@RestController
@RequiredArgsConstructor
public class MovimientoController {

    private final IMovimientoService movimientoService;

    // ─── CRUD /movimientos ───────────────────────────────────────────

    @GetMapping("/movimientos")
    public ResponseEntity<Page<MovimientoResponseDTO>> listarTodos(Pageable pageable) {
        return ResponseEntity.ok(movimientoService.listarTodos(pageable));
    }

    @GetMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.buscarPorId(id));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<MovimientoResponseDTO> registrar(
            @Valid @RequestBody MovimientoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoService.registrar(dto));
    }

    @PutMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MovimientoRequestDTO dto) {
        return ResponseEntity.ok(movimientoService.actualizar(id, dto));
    }

    @PatchMapping("/movimientos/{id}")
    public ResponseEntity<MovimientoResponseDTO> actualizarParcial(
            @PathVariable Long id,
            @RequestBody MovimientoPatchDTO dto) {
        return ResponseEntity.ok(movimientoService.actualizarParcial(id, dto));
    }

    @DeleteMapping("/movimientos/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ─── F4: Reporte /reportes ───────────────────────────────────────
    // URL completa: GET http://localhost:8082/api/reportes?fecha=2022-01-01,2022-12-31&clienteId=jose123

    @GetMapping("/reportes")
    public ResponseEntity<Page<ReporteMovimientoDTO>> generarReporte(
            @RequestParam String fecha,
            @RequestParam String clienteId,
            Pageable pageable) {

        String[] partes = fecha.split(",");
        LocalDateTime fechaInicio = LocalDate.parse(partes[0].trim()).atStartOfDay();
        LocalDateTime fechaFin = partes.length > 1
                ? LocalDate.parse(partes[1].trim()).atTime(LocalTime.MAX)
                : LocalDateTime.now();

        return ResponseEntity.ok(
                movimientoService.generarReporte(clienteId, fechaInicio, fechaFin, pageable));
    }
}
