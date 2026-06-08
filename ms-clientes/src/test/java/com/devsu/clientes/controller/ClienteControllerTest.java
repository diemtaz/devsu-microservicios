package com.devsu.clientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devsu.clientes.exception.GlobalExceptionHandler;
import com.devsu.clientes.exception.RecursoDuplicadoException;
import com.devsu.clientes.exception.RecursoNoEncontradoException;
import com.devsu.clientes.model.dto.ClientePatchDTO;
import com.devsu.clientes.model.dto.ClienteResponseDTO;
import com.devsu.clientes.service.IClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("Pruebas de endpoints — ClienteController (todos los verbos)")
class ClienteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  IClienteService clienteService;

    private ClienteResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        responseDTO = ClienteResponseDTO.builder()
                .id(1L).clienteId("jose123").nombre("Jose Lema")
                .genero("Masculino").edad(30).identificacion("0101010101")
                .direccion("Otavalo sn y principal").telefono("098254785")
                .estado(true).build();
    }

    private String clienteJson() {
        return """
        {
        "nombre": "Jose Lema",
        "genero": "Masculino",
        "edad": 30,
        "identificacion": "0101010101",
        "direccion": "Otavalo sn y principal",
        "telefono": "098254785",
        "clienteId": "jose123",
        "contrasena": "1234",
        "estado": true
        }
        """;
    }

    private String clienteJsonSinNombre() {
        return """
        {
        "nombre": "",
        "genero": "Masculino",
        "edad": 30,
        "identificacion": "0101010101",
        "direccion": "Otavalo sn y principal",
        "telefono": "098254785",
        "clienteId": "jose123",
        "contrasena": "1234",
        "estado": true
        }
        """;
    }

    // ──────────────────────────────────────────────────────────────────
    //  GET
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /clientes → 200 con lista de clientes")
    void listarTodos_DebeRetornar200ConLista() throws Exception {

         Page<ClienteResponseDTO> page = new PageImpl<>(
                List.of(responseDTO), 
                PageRequest.of(0, 10),
                1
        );
        when(clienteService.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].clienteId").value("jose123"))
                .andExpect(jsonPath("$.content[0].nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.content[0].estado").value(true));
    }

    @Test
    @DisplayName("GET /clientes/{clienteId} existente → 200")
    void buscarPorId_Existente_DebeRetornar200() throws Exception {
        when(clienteService.buscarPorClienteId("jose123")).thenReturn(responseDTO);

        mockMvc.perform(get("/clientes/jose123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("jose123"))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"));
    }

    @Test
    @DisplayName("GET /clientes/{clienteId} inexistente → 404 con mensaje")
    void buscarPorId_Inexistente_DebeRetornar404() throws Exception {
        when(clienteService.buscarPorClienteId("noExiste"))
                .thenThrow(new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: noExiste"));

        mockMvc.perform(get("/clientes/noExiste"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cliente no encontrado con clienteId: noExiste"));
    }

    // ──────────────────────────────────────────────────────────────────
    //  POST
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /clientes con datos válidos → 201 Created")
    void crear_DatosValidos_DebeRetornar201() throws Exception {
        when(clienteService.crear(any())).thenReturn(responseDTO);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value("jose123"))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"));
    }

    @Test
    @DisplayName("POST /clientes sin nombre → 400 con detalle de validación")
    void crear_SinNombre_DebeRetornar400() throws Exception {

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJsonSinNombre()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.nombre").exists());
    }

    @Test
    @DisplayName("POST /clientes con clienteId duplicado → 409 Conflict")
    void crear_Duplicado_DebeRetornar409() throws Exception {
        when(clienteService.crear(any()))
                .thenThrow(new RecursoDuplicadoException(
                        "Ya existe un cliente con clienteId: jose123"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // ──────────────────────────────────────────────────────────────────
    //  PUT
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /clientes/{clienteId} → 200 con cliente actualizado")
    void actualizar_DebeRetornar200() throws Exception {
        when(clienteService.actualizar(eq("jose123"), any())).thenReturn(responseDTO);

        mockMvc.perform(put("/clientes/jose123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("jose123"));
    }

    @Test
    @DisplayName("PUT /clientes/{clienteId} inexistente → 404")
    void actualizar_Inexistente_DebeRetornar404() throws Exception {
        when(clienteService.actualizar(eq("noExiste"), any()))
                .thenThrow(new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: noExiste"));

        mockMvc.perform(put("/clientes/noExiste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson()))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────────────────
    //  PATCH
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /clientes/{clienteId} → solo cambia estado a false, resto intacto")
    void actualizarParcial_SoloEstado_DebeRetornar200() throws Exception {
        // Solo enviamos estado. Los demás campos son null → el servicio los ignora.
        ClientePatchDTO patch = ClientePatchDTO.builder().estado(false).build();

        ClienteResponseDTO desactivado = ClienteResponseDTO.builder()
                .id(1L).clienteId("jose123").nombre("Jose Lema")
                .genero("Masculino").edad(30).identificacion("0101010101")
                .direccion("Otavalo sn y principal").telefono("098254785")
                .estado(false).build();

        when(clienteService.actualizarParcial(eq("jose123"), any())).thenReturn(desactivado);

        mockMvc.perform(patch("/clientes/jose123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(false))
                // Nombre NO cambia — sigue siendo "Jose Lema"
                .andExpect(jsonPath("$.nombre").value("Jose Lema"));
    }

    @Test
    @DisplayName("PATCH /clientes/{clienteId} → cambia nombre y dirección únicamente")
    void actualizarParcial_NombreYDireccion_DebeRetornar200() throws Exception {
        ClientePatchDTO patch = ClientePatchDTO.builder()
                .nombre("Jose Lema Modificado")
                .direccion("Nueva calle 456")
                .build();

        ClienteResponseDTO modificado = ClienteResponseDTO.builder()
                .id(1L).clienteId("jose123").nombre("Jose Lema Modificado")
                .genero("Masculino").edad(30).identificacion("0101010101")
                .direccion("Nueva calle 456").telefono("098254785")
                .estado(true).build();

        when(clienteService.actualizarParcial(eq("jose123"), any())).thenReturn(modificado);

        mockMvc.perform(patch("/clientes/jose123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jose Lema Modificado"))
                .andExpect(jsonPath("$.direccion").value("Nueva calle 456"))
                // Teléfono NO se modifica — sigue igual
                .andExpect(jsonPath("$.telefono").value("098254785"));
    }

    @Test
    @DisplayName("PATCH /clientes/{clienteId} inexistente → 404")
    void actualizarParcial_Inexistente_DebeRetornar404() throws Exception {
        ClientePatchDTO patch = ClientePatchDTO.builder().estado(false).build();

        when(clienteService.actualizarParcial(eq("noExiste"), any()))
                .thenThrow(new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: noExiste"));

        mockMvc.perform(patch("/clientes/noExiste")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    // ──────────────────────────────────────────────────────────────────
    //  DELETE
    // ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /clientes/{clienteId} → 204 No Content")
    void eliminar_DebeRetornar204() throws Exception {
        mockMvc.perform(delete("/clientes/jose123"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /clientes/{clienteId} inexistente → 404")
    void eliminar_Inexistente_DebeRetornar404() throws Exception {
        when(clienteService.buscarPorClienteId("noExiste"))
                .thenThrow(new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: noExiste"));

        mockMvc.perform(get("/clientes/noExiste"))
                .andExpect(status().isNotFound());
    }
}
