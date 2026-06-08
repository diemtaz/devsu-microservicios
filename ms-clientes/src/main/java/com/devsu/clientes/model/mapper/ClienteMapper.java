package com.devsu.clientes.model.mapper;

import com.devsu.clientes.model.dto.ClienteRequestDTO;
import com.devsu.clientes.model.dto.ClienteResponseDTO;
import com.devsu.clientes.model.entity.Cliente;
import org.springframework.stereotype.Component;

/**
 * Mapper manual. En proyectos más grandes se usaría MapStruct.
 * Centralizar la conversión DTO <-> Entidad en una clase evita lógica
 * duplicada en servicios y controladores.
 */
@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequestDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setClienteId(dto.getClienteId());
        cliente.setContrasena(dto.getContrasena());
        cliente.setEstado(dto.getEstado() != null ? dto.getEstado() : true);
        return cliente;
    }

    public ClienteResponseDTO toDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .clienteId(cliente.getClienteId())
                .nombre(cliente.getNombre())
                .genero(cliente.getGenero())
                .edad(cliente.getEdad())
                .identificacion(cliente.getIdentificacion())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .estado(cliente.getEstado())
                .build();
    }

    public void updateEntityFromDTO(ClienteRequestDTO dto, Cliente cliente) {
        cliente.setNombre(dto.getNombre());
        cliente.setGenero(dto.getGenero());
        cliente.setEdad(dto.getEdad());
        cliente.setIdentificacion(dto.getIdentificacion());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        if (dto.getContrasena() != null && !dto.getContrasena().isBlank()) {
            cliente.setContrasena(dto.getContrasena());
        }
        if (dto.getEstado() != null) {
            cliente.setEstado(dto.getEstado());
        }
    }
}
