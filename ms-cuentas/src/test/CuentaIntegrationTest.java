package com.devsu.cuentas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devsu.cuentas.model.entity.ClienteRef;
import com.devsu.cuentas.model.entity.Cuenta;
import com.devsu.cuentas.repository.ClienteRefRepository;
import com.devsu.cuentas.repository.CuentaRepository;
import com.devsu.cuentas.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("F6 - Pruebas de integración — flujo completo Cuentas y Movimientos")
class CuentasIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CuentaRepository cuentaRepository;
    @Autowired private MovimientoRepository movimientoRepository;
    @Autowired private ClienteRefRepository clienteRefRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Limpiar en orden correcto por las FK
        movimientoRepository.deleteAll();
        cuentaRepository.deleteAll();
        clienteRefRepository.deleteAll();

        // Crear referencia local del cliente (normalmente llega via RabbitMQ)
        clienteRefRepository.save(ClienteRef.builder()
                .clienteId("jose123")
                .nombre("Jose Lema")
                .build());

        // Crear una cuenta con saldo inicial
        cuentaRepository.save(Cuenta.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000.00"))
                .saldoDisponible(new BigDecimal("2000.00"))
                .estado(true)
                .clienteId("jose123")
                .build());
    }

    // TEST 1 — F2: Depósito actualiza saldo en BD
    @Test
    @DisplayName("POST /movimientos deposito actualiza saldo disponible en BD")
    void deposito_DebeActualizarSaldoEnBD() throws Exception {
        String body = """
            { "numeroCuenta": "478758", "valor": 600 }
            """;

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("CREDITO"))
                .andExpect(jsonPath("$.saldo").value(2600.0));

        // Verificar directamente en BD que el saldo fue actualizado
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta("478758").orElseThrow();
        assertThat(cuenta.getSaldoDisponible())
                .isEqualByComparingTo(new BigDecimal("2600.00"));

        // Verificar que el movimiento fue persistido
        assertThat(movimientoRepository.findByCuenta_NumeroCuenta("478758")).hasSize(1);
    }

    // TEST 2 — F2: Retiro actualiza saldo en BD
    @Test
    @DisplayName("POST /movimientos retiro decrementa saldo disponible en BD")
    void retiro_DebeDecrementarSaldoEnBD() throws Exception {
        String body = """
            { "numeroCuenta": "478758", "valor": -575 }
            """;

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("DEBITO"))
                .andExpect(jsonPath("$.saldo").value(1425.0));

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta("478758").orElseThrow();
        assertThat(cuenta.getSaldoDisponible())
                .isEqualByComparingTo(new BigDecimal("1425.00"));
    }

    // TEST 3 — F3: Saldo insuficiente → 422 y BD sin cambios
    @Test
    @DisplayName("POST /movimientos sin saldo retorna 422 y NO modifica la BD")
    void retiro_SinSaldo_DebeRetornar422YNoCambiarBD() throws Exception {
        String body = """
            { "numeroCuenta": "478758", "valor": -9999 }
            """;

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));

        // Verificar que el saldo NO cambió en BD — la transacción hizo rollback
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta("478758").orElseThrow();
        assertThat(cuenta.getSaldoDisponible())
                .isEqualByComparingTo(new BigDecimal("2000.00"));

        // Verificar que NO se creó ningún movimiento
        assertThat(movimientoRepository.findByCuenta_NumeroCuenta("478758")).isEmpty();
    }

    // TEST 4 — F4: Reporte retorna JSON con estructura correcta
    @Test
    @DisplayName("GET /reportes retorna movimientos del cliente en rango de fechas")
    void reporte_DebeRetornarMovimientosDelCliente() throws Exception {
        // Crear un movimiento previo
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "numeroCuenta": "478758", "valor": 500 }
                            """))
                .andExpect(status().isCreated());

        // Consultar el reporte
        mockMvc.perform(get("/reportes")
                        .param("fecha", "2020-01-01,2030-12-31")
                        .param("clienteId", "jose123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].Cliente").value("Jose Lema"))
                .andExpect(jsonPath("$[0]['Numero Cuenta']").value("478758"))
                .andExpect(jsonPath("$[0].Movimiento").value(500.0));
    }

    // TEST 5 — Crear cuenta para cliente que no existe → 404
    @Test
    @DisplayName("POST /cuentas con clienteId inexistente retorna 404")
    void crearCuenta_ClienteInexistente_DebeRetornar404() throws Exception {
        String body = """
            {
              "numeroCuenta": "999999",
              "tipoCuenta": "Ahorro",
              "saldoInicial": 1000,
              "estado": true,
              "clienteId": "noExiste"
            }
            """;

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(
                        org.hamcrest.Matchers.containsString("noExiste")));
    }
}