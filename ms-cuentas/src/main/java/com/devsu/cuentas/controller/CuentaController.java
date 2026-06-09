package com.devsu.cuentas.controller;

import com.devsu.cuentas.model.dto.CuentaPatchDTO;
import com.devsu.cuentas.model.dto.CuentaRequestDTO;
import com.devsu.cuentas.model.dto.CuentaResponseDTO;
import com.devsu.cuentas.service.ICuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final ICuentaService cuentaService;

    @GetMapping
    public ResponseEntity<Page<CuentaResponseDTO>> listarTodas(Pageable pageable) {
        return ResponseEntity.ok(cuentaService.listarTodas(pageable));
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> buscarPorNumero(@PathVariable String numeroCuenta) {
        return ResponseEntity.ok(cuentaService.buscarPorNumeroCuenta(numeroCuenta));
    }

    @PostMapping
    public ResponseEntity<CuentaResponseDTO> crear(@Valid @RequestBody CuentaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.crear(dto));
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> actualizar(
            @PathVariable String numeroCuenta,
            @Valid @RequestBody CuentaRequestDTO dto) {
        return ResponseEntity.ok(cuentaService.actualizar(numeroCuenta, dto));
    }

    @PatchMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponseDTO> actualizarParcial(
            @PathVariable String numeroCuenta,
            @RequestBody CuentaPatchDTO dto) {
        return ResponseEntity.ok(cuentaService.actualizarParcial(numeroCuenta, dto));
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminar(@PathVariable String numeroCuenta) {
        cuentaService.eliminar(numeroCuenta);
        return ResponseEntity.noContent().build();
    }
}
