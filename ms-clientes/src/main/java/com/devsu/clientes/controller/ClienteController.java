package com.devsu.clientes.controller;

import com.devsu.clientes.model.dto.ClientePatchDTO;
import com.devsu.clientes.model.dto.ClienteRequestDTO;
import com.devsu.clientes.model.dto.ClienteResponseDTO;
import com.devsu.clientes.service.IClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Controlador REST para el recurso /clientes.
 * - @Valid activa las validaciones Bean Validation del DTO (NotBlank, NotNull, etc.)
 * - ResponseEntity permite controlar el código HTTP de respuesta explícitamente
 * - 201 Created para POST (creación exitosa), 200 OK para GET/PUT, 204 No Content para DELETE
 */
@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@Slf4j
public class ClienteController {

    private final IClienteService clienteService;

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDTO>> listarTodos(Pageable pageable) {
        return ResponseEntity.ok(clienteService.listarTodos(pageable));
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> buscarPorClienteId(@PathVariable String clienteId) {
        return ResponseEntity.ok(clienteService.buscarPorClienteId(clienteId));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO creado = clienteService.crear(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> actualizar(
            @PathVariable String clienteId,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(clienteId, dto));
    }

    @PatchMapping("/{clienteId}")
    public ResponseEntity<ClienteResponseDTO> actualizarParcial(
            @PathVariable String clienteId,
            @RequestBody ClientePatchDTO dto) {
        return ResponseEntity.ok(clienteService.actualizarParcial(clienteId, dto));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> eliminar(@PathVariable String clienteId) {
        clienteService.eliminar(clienteId);
        return ResponseEntity.noContent().build();
    }
}
