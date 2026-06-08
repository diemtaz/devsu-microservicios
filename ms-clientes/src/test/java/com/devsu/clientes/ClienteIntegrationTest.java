package com.devsu.clientes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devsu.clientes.model.entity.Cliente;
import com.devsu.clientes.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("F6 - Pruebas de integración — flujo completo Cliente")
class ClienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private String clienteJson;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();

        clienteJson = """
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

    // TEST 1
    @Test
    @DisplayName("POST /clientes crea cliente y lo persiste en BD")
    void crear_DebeCrearClienteEnBD() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value("jose123"))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"));

        assertThat(clienteRepository.existsByClienteId("jose123")).isTrue();

        Cliente guardado = clienteRepository.findByClienteId("jose123").orElseThrow();
        assertThat(guardado.getNombre()).isEqualTo("Jose Lema");
        assertThat(guardado.getEstado()).isTrue();
    }

    // TEST 2
    @Test
    @DisplayName("GET /clientes/{clienteId} retorna cliente previamente creado")
    void crear_LuegoGetPorId_DebeRetornarMismoDato() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/clientes/jose123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("jose123"))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.identificacion").value("0101010101"));
    }

    // TEST 3
    @Test
    @DisplayName("POST /clientes con clienteId duplicado debe retornar 409")
    void crear_Duplicado_DebeRetornar409() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    // TEST 4
    @Test
    @DisplayName("PUT /clientes/{clienteId} actualiza correctamente en BD")
    void actualizar_DebeModificarEnBD() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isCreated());

        String jsonActualizado = """
        {
          "nombre": "Jose Lema Actualizado",
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

        mockMvc.perform(put("/clientes/jose123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonActualizado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Jose Lema Actualizado"));

        Cliente actualizado = clienteRepository.findByClienteId("jose123").orElseThrow();
        assertThat(actualizado.getNombre()).isEqualTo("Jose Lema Actualizado");
    }

    // TEST 5
    @Test
    @DisplayName("DELETE /clientes/{clienteId} elimina el registro de BD")
    void eliminar_DebeEliminarDeDB() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clienteJson))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/clientes/jose123"))
                .andExpect(status().isNoContent());

        assertThat(clienteRepository.existsByClienteId("jose123")).isFalse();

        mockMvc.perform(get("/clientes/jose123"))
                .andExpect(status().isNotFound());
    }
}