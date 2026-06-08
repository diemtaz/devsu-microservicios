package com.devsu.clientes.service;

import com.devsu.clientes.model.dto.ClientePatchDTO;
import com.devsu.clientes.model.dto.ClienteRequestDTO;
import com.devsu.clientes.model.dto.ClienteResponseDTO;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz del servicio. Definir contrato via interfaz es buena práctica:
 * - Facilita el mockeo en pruebas unitarias (Mockito.mock(IClienteService.class))
 * - Permite cambiar la implementación sin modificar el controlador
 * - Sigue el principio de Inversión de Dependencias (SOLID - D)
 */
public interface IClienteService {
    Page<ClienteResponseDTO> listarTodos(Pageable pageable);
    ClienteResponseDTO buscarPorClienteId(String clienteId);
    ClienteResponseDTO crear(ClienteRequestDTO dto);
    ClienteResponseDTO actualizar(String clienteId, ClienteRequestDTO dto);
    ClienteResponseDTO actualizarParcial(String clienteId, ClientePatchDTO dto);
    void eliminar(String clienteId);
}
