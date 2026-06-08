package com.devsu.clientes.service;

import com.devsu.clientes.exception.RecursoDuplicadoException;
import com.devsu.clientes.exception.RecursoNoEncontradoException;
import com.devsu.clientes.messaging.ClienteEventProducer;
import com.devsu.clientes.model.dto.ClienteEventoDTO;
import com.devsu.clientes.model.dto.ClientePatchDTO;
import com.devsu.clientes.model.dto.ClienteRequestDTO;
import com.devsu.clientes.model.dto.ClienteResponseDTO;
import com.devsu.clientes.model.entity.Cliente;
import com.devsu.clientes.model.mapper.ClienteMapper;
import com.devsu.clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Implementación de la lógica de negocio para Clientes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ClienteServiceImpl implements IClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final ClienteEventProducer eventProducer;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDTO> listarTodos(Pageable pageable) {
        log.debug("Listando todos los clientes");
        return clienteRepository.findAll(pageable)
                .map(clienteMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDTO buscarPorClienteId(String clienteId) {
        log.debug("Buscando cliente con clienteId: {}", clienteId);
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: " + clienteId));
        return clienteMapper.toDTO(cliente);
    }

    @Override
    public ClienteResponseDTO crear(ClienteRequestDTO dto) {
        log.info("Creando nuevo cliente con clienteId: {}", dto.getClienteId());

        if (clienteRepository.existsByClienteId(dto.getClienteId())) {
            throw new RecursoDuplicadoException(
                    "Ya existe un cliente con clienteId: " + dto.getClienteId());
        }
        if (clienteRepository.existsByIdentificacion(dto.getIdentificacion())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una persona con identificación: " + dto.getIdentificacion());
        }

        Cliente cliente = clienteMapper.toEntity(dto);
        Cliente guardado = clienteRepository.save(cliente);

        // Publicar evento asíncrono al broker para que MS-Cuentas se entere
        eventProducer.publicarClienteCreado(ClienteEventoDTO.builder()
                .clienteId(guardado.getClienteId())
                .nombre(guardado.getNombre())
                .tipoEvento("CREADO")
                .build());

        log.info("Cliente creado exitosamente con id: {}", guardado.getId());
        return clienteMapper.toDTO(guardado);
    }

    @Override
    public ClienteResponseDTO actualizar(String clienteId, ClienteRequestDTO dto) {
        log.info("Actualizando cliente con clienteId: {}", clienteId);

        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: " + clienteId));

        clienteMapper.updateEntityFromDTO(dto, cliente);
        Cliente actualizado = clienteRepository.save(cliente);

        eventProducer.publicarClienteActualizado(ClienteEventoDTO.builder()
            .clienteId(actualizado.getClienteId())
            .nombre(actualizado.getNombre())
            .tipoEvento("ACTUALIZADO")
            .build());

        log.info("Cliente actualizado exitosamente: {}", clienteId);
        return clienteMapper.toDTO(actualizado);
    }

    @Override
    public ClienteResponseDTO actualizarParcial(String clienteId, ClientePatchDTO dto) {
        log.info("Actualizacion parcial de cliente: {}", clienteId);
        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: " + clienteId));

        if (dto.getNombre()     != null) cliente.setNombre(dto.getNombre());
        if (dto.getGenero()     != null) cliente.setGenero(dto.getGenero());
        if (dto.getEdad()       != null) cliente.setEdad(dto.getEdad());
        if (dto.getDireccion()  != null) cliente.setDireccion(dto.getDireccion());
        if (dto.getTelefono()   != null) cliente.setTelefono(dto.getTelefono());
        if (dto.getContrasena() != null) cliente.setContrasena(dto.getContrasena());
        if (dto.getEstado()     != null) cliente.setEstado(dto.getEstado());

        Cliente actualizado = clienteRepository.save(cliente);

        // Solo publicar evento si el nombre fue modificado,
        if (dto.getNombre() != null) {
            eventProducer.publicarClienteActualizado(ClienteEventoDTO.builder()
                    .clienteId(actualizado.getClienteId())
                    .nombre(actualizado.getNombre())
                    .tipoEvento("ACTUALIZADO")
                    .build());
        }

        log.info("Cliente actualizado parcialmente: {}", clienteId);
        return clienteMapper.toDTO(actualizado);
    }
    @Override
    public void eliminar(String clienteId) {
        log.info("Eliminando cliente con clienteId: {}", clienteId);

        Cliente cliente = clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: " + clienteId));

        clienteRepository.delete(cliente);

        // Notificar a MS-Cuentas para que maneje la eliminación del cliente
        eventProducer.publicarClienteEliminado(ClienteEventoDTO.builder()
                .clienteId(clienteId)
                .nombre(cliente.getNombre())
                .tipoEvento("ELIMINADO")
                .build());

        log.info("Cliente eliminado exitosamente: {}", clienteId);
    }
}
