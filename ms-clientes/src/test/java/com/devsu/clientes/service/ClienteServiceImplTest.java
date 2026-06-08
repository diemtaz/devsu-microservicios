package com.devsu.clientes.service;

import com.devsu.clientes.exception.RecursoDuplicadoException;
import com.devsu.clientes.exception.RecursoNoEncontradoException;
import com.devsu.clientes.messaging.ClienteEventProducer;
import com.devsu.clientes.model.dto.ClienteRequestDTO;
import com.devsu.clientes.model.dto.ClienteResponseDTO;
import com.devsu.clientes.model.entity.Cliente;
import com.devsu.clientes.model.mapper.ClienteMapper;
import com.devsu.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import static org.mockito.ArgumentMatchers.any;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;


@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - ClienteService")
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private ClienteEventProducer eventProducer;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteRequestDTO requestDTO;
    private Cliente clienteEntity;
    private ClienteResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        requestDTO = ClienteRequestDTO.builder()
                .nombre("Jose Lema")
                .genero("Masculino")
                .edad(30)
                .identificacion("1234567890")
                .direccion("Otavalo sn y principal")
                .telefono("098254785")
                .clienteId("jose123")
                .contrasena("1234")
                .estado(true)
                .build();

        clienteEntity = new Cliente();
        clienteEntity.setId(1L);
        clienteEntity.setNombre("Jose Lema");
        clienteEntity.setClienteId("jose123");
        clienteEntity.setEstado(true);

        responseDTO = ClienteResponseDTO.builder()
                .id(1L)
                .clienteId("jose123")
                .nombre("Jose Lema")
                .estado(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 1: Crear cliente exitosamente y verificar evento publicado
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Debe crear un cliente y publicar evento a RabbitMQ")
    void crear_DebeCrearClienteYPublicarEvento() {
        // ARRANGE (Given)
        when(clienteRepository.existsByClienteId("jose123")).thenReturn(false);
        when(clienteRepository.existsByIdentificacion("1234567890")).thenReturn(false);
        when(clienteMapper.toEntity(requestDTO)).thenReturn(clienteEntity);
        when(clienteRepository.save(clienteEntity)).thenReturn(clienteEntity);
        when(clienteMapper.toDTO(clienteEntity)).thenReturn(responseDTO);

        // ACT (When)
        ClienteResponseDTO resultado = clienteService.crear(requestDTO);

        // ASSERT (Then)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getClienteId()).isEqualTo("jose123");
        assertThat(resultado.getNombre()).isEqualTo("Jose Lema");

        // Verificar que el evento fue publicado (comportamiento secundario)
        verify(eventProducer, times(1)).publicarClienteCreado(any());
        verify(clienteRepository, times(1)).save(clienteEntity);
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 2: Crear cliente con clienteId duplicado lanza excepción
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Debe lanzar RecursoDuplicadoException si clienteId ya existe")
    void crear_DeberiaLanzarExcepcionSiClienteIdDuplicado() {
        // ARRANGE
        when(clienteRepository.existsByClienteId("jose123")).thenReturn(true);

        // ACT + ASSERT
        assertThatThrownBy(() -> clienteService.crear(requestDTO))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessageContaining("jose123");

        // Verificar que NO se guardó ni se publicó evento
        verify(clienteRepository, never()).save(any());
        verify(eventProducer, never()).publicarClienteCreado(any());
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 3: Buscar cliente por ID inexistente lanza excepción
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Debe lanzar RecursoNoEncontradoException si clienteId no existe")
    void buscarPorClienteId_DeberiaLanzarExcepcionSiNoExiste() {
        // ARRANGE
        when(clienteRepository.findByClienteId("noExiste")).thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() -> clienteService.buscarPorClienteId("noExiste"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("noExiste");
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 4: Listar clientes retorna lista mapeada
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Debe retornar lista de clientes mapeados a DTO")
    void listarTodos_DebeRetornarListaDeClientes() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> page = new PageImpl<>(List.of(clienteEntity));
        
        when(clienteRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(clienteMapper.toDTO(clienteEntity)).thenReturn(responseDTO);

        Page<ClienteResponseDTO> resultado = clienteService.listarTodos(pageable);

        assertThat(resultado).isNotEmpty();
        assertThat(resultado.getContent().get(0).getClienteId()).isEqualTo("jose123");
        verify(clienteRepository).findAll(pageable);
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 5: Eliminar cliente publica evento ELIMINADO
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Debe eliminar cliente y publicar evento ELIMINADO")
    void eliminar_DebeEliminarYPublicarEventoEliminado() {
        // ARRANGE
        when(clienteRepository.findByClienteId("jose123")).thenReturn(Optional.of(clienteEntity));

        // ACT
        clienteService.eliminar("jose123");

        // ASSERT
        verify(clienteRepository, times(1)).delete(clienteEntity);
        verify(eventProducer, times(1)).publicarClienteEliminado(
                argThat(e -> "ELIMINADO".equals(e.getTipoEvento())
                          && "jose123".equals(e.getClienteId())));
    }
}
