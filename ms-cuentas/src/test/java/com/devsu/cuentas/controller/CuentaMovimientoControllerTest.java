package com.devsu.cuentas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.devsu.cuentas.exception.GlobalExceptionHandler;
import com.devsu.cuentas.exception.RecursoNoEncontradoException;
import com.devsu.cuentas.exception.SaldoInsuficienteException;
import com.devsu.cuentas.model.dto.*;
import com.devsu.cuentas.service.ICuentaService;
import com.devsu.cuentas.service.IMovimientoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

/**
 * Pruebas unitarias de endpoints para CuentaController y MovimientoController.
 */
@WebMvcTest(controllers = {CuentaController.class, MovimientoController.class})
@Import(GlobalExceptionHandler.class)
@DisplayName("Pruebas de endpoints — Cuenta y Movimiento (todos los verbos)")
class CuentaMovimientoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ICuentaService cuentaService;
    @MockBean IMovimientoService movimientoService;

    private CuentaResponseDTO cuentaResponse;
    private MovimientoResponseDTO movimientoResponse;

    @BeforeEach
    void setUp() {
        cuentaResponse = CuentaResponseDTO.builder()
                .id(1L)
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000.00"))
                .saldoDisponible(new BigDecimal("2000.00"))
                .clienteId("jose123")
                .estado(true)
                .build();

        movimientoResponse = MovimientoResponseDTO.builder()
                .id(1L)
                .tipoMovimiento("DEBITO")
                .valor(new BigDecimal("-575.00"))
                .saldo(new BigDecimal("1425.00"))
                .fecha(LocalDateTime.now())
                .numeroCuenta("478758")
                .build();
    }

    // ══════════════════════════════════════════════════════════════════
    //  CUENTAS — GET, POST, PUT, PATCH, DELETE
    // ══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /cuentas → 200 con lista paginada")
    void getCuentas_DebeRetornar200() throws Exception {

        Page<CuentaResponseDTO> page = new PageImpl<>(
                List.of(cuentaResponse), 
                PageRequest.of(0, 10),
                1
        );

        when(cuentaService.listarTodas(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].numeroCuenta").value("478758"))
                .andExpect(jsonPath("$.content[0].tipoCuenta").value("Ahorro"))
                .andExpect(jsonPath("$.content[0].saldoDisponible").value(2000.00));
    }     

    @Test
    @DisplayName("GET /cuentas/{numeroCuenta} existente → 200")
    void getCuentaPorNumero_Existente_DebeRetornar200() throws Exception {
        when(cuentaService.buscarPorNumeroCuenta("478758")).thenReturn(cuentaResponse);

        mockMvc.perform(get("/cuentas/478758"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value("478758"))
                .andExpect(jsonPath("$.clienteId").value("jose123"));
    }

    @Test
    @DisplayName("GET /cuentas/{numeroCuenta} inexistente → 404")
    void getCuentaPorNumero_Inexistente_DebeRetornar404() throws Exception {
        when(cuentaService.buscarPorNumeroCuenta("000000"))
                .thenThrow(new RecursoNoEncontradoException("Cuenta no encontrada: 000000"));

        mockMvc.perform(get("/cuentas/000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("Cuenta no encontrada: 000000"));
    }

    @Test
    @DisplayName("POST /cuentas → 201 Created")
    void postCuenta_DebeRetornar201() throws Exception {
        CuentaRequestDTO req = CuentaRequestDTO.builder()
                .numeroCuenta("478758").tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000")).estado(true)
                .clienteId("jose123").build();

        when(cuentaService.crear(any())).thenReturn(cuentaResponse);

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("478758"));
    }

    @Test
    @DisplayName("PUT /cuentas/{numeroCuenta} → 200 actualizado")
    void putCuenta_DebeRetornar200() throws Exception {
        CuentaRequestDTO req = CuentaRequestDTO.builder()
                .numeroCuenta("478758").tipoCuenta("Corriente")
                .saldoInicial(new BigDecimal("2000")).estado(true)
                .clienteId("jose123").build();

        CuentaResponseDTO actualizado = CuentaResponseDTO.builder()
                .numeroCuenta("478758").tipoCuenta("Corriente")
                .saldoInicial(new BigDecimal("2000")).saldoDisponible(new BigDecimal("2000"))
                .estado(true).clienteId("jose123").build();

        when(cuentaService.actualizar(eq("478758"), any())).thenReturn(actualizado);

        mockMvc.perform(put("/cuentas/478758")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoCuenta").value("Corriente"));
    }

    @Test
    @DisplayName("PATCH /cuentas/{numeroCuenta} → solo cambia estado a false")
    void patchCuenta_SoloEstado_DebeRetornar200() throws Exception {
        // Solo enviamos el campo estado — los demás se ignoran (permanecen igual)
        CuentaPatchDTO patch = new CuentaPatchDTO(null, false, null);

        CuentaResponseDTO desactivada = CuentaResponseDTO.builder()
                .numeroCuenta("478758").tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000")).saldoDisponible(new BigDecimal("2000"))
                .estado(false).clienteId("jose123").build();

        when(cuentaService.actualizarParcial(eq("478758"), any())).thenReturn(desactivada);

        mockMvc.perform(patch("/cuentas/478758")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value(false))
                // tipoCuenta NO cambia — sigue siendo "Ahorro"
                .andExpect(jsonPath("$.tipoCuenta").value("Ahorro"));
    }

    @Test
    @DisplayName("PATCH /cuentas/{numeroCuenta} inexistente → 404")
    void patchCuenta_Inexistente_DebeRetornar404() throws Exception {
        CuentaPatchDTO patch = new CuentaPatchDTO(null, false, null);

        when(cuentaService.actualizarParcial(eq("000000"), any()))
                .thenThrow(new RecursoNoEncontradoException("Cuenta no encontrada: 000000"));

        mockMvc.perform(patch("/cuentas/000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /cuentas/{numeroCuenta} → 204 No Content")
    void deleteCuenta_DebeRetornar204() throws Exception {
        mockMvc.perform(delete("/cuentas/478758"))
                .andExpect(status().isNoContent());
    }

    // ══════════════════════════════════════════════════════════════════
    //  MOVIMIENTOS — GET, POST, PUT, PATCH, DELETE
    // ══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /movimientos → 200 con lista")
    void getMovimientos_DebeRetornar200() throws Exception {
        Page<MovimientoResponseDTO> page = new PageImpl<>(
                List.of(movimientoResponse), 
                PageRequest.of(0, 10),
                1
        );
 
        when(movimientoService.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/movimientos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipoMovimiento").value("DEBITO"))
                .andExpect(jsonPath("$.content[0].numeroCuenta").value("478758"));
    }

    @Test
    @DisplayName("POST /movimientos depósito → 201 Created")
    void postMovimiento_Deposito_DebeRetornar201() throws Exception {
        MovimientoRequestDTO req = new MovimientoRequestDTO("478758", new BigDecimal("600"));
        MovimientoResponseDTO depositoResp = MovimientoResponseDTO.builder()
                .id(2L).tipoMovimiento("CREDITO")
                .valor(new BigDecimal("600")).saldo(new BigDecimal("700"))
                .numeroCuenta("225487").build();

        when(movimientoService.registrar(any())).thenReturn(depositoResp);

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("CREDITO"))
                .andExpect(jsonPath("$.saldo").value(700.0));
    }

    @Test
    @DisplayName("POST /movimientos sin saldo → 422 con 'Saldo no disponible'")
    void postMovimiento_SinSaldo_DebeRetornar422() throws Exception {
        MovimientoRequestDTO req = new MovimientoRequestDTO("495878", new BigDecimal("-999"));

        when(movimientoService.registrar(any()))
                .thenThrow(new SaldoInsuficienteException());

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));
    }

    @Test
    @DisplayName("PUT /movimientos/{id} → 200 con movimiento actualizado completo")
    void putMovimiento_DebeRetornar200() throws Exception {
        MovimientoRequestDTO req = new MovimientoRequestDTO("478758", new BigDecimal("-300"));
        MovimientoResponseDTO actualizado = MovimientoResponseDTO.builder()
                .id(1L).tipoMovimiento("DEBITO")
                .valor(new BigDecimal("-300")).saldo(new BigDecimal("1700"))
                .numeroCuenta("478758").build();

        when(movimientoService.actualizar(eq(1L), any())).thenReturn(actualizado);

        mockMvc.perform(put("/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(-300.0))
                .andExpect(jsonPath("$.saldo").value(1700.0));
    }

    @Test
    @DisplayName("PATCH /movimientos/{id} → solo cambia el valor, cuenta permanece igual")
    void patchMovimiento_SoloValor_DebeRetornar200() throws Exception {
        // PATCH: solo enviamos el nuevo valor — numeroCuenta no se toca
        MovimientoPatchDTO patch = new MovimientoPatchDTO(new BigDecimal("-200"));

        MovimientoResponseDTO parcialResp = MovimientoResponseDTO.builder()
                .id(1L).tipoMovimiento("DEBITO")
                .valor(new BigDecimal("-200")).saldo(new BigDecimal("1800"))
                .numeroCuenta("478758").build();

        when(movimientoService.actualizarParcial(eq(1L), any())).thenReturn(parcialResp);

        mockMvc.perform(patch("/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(-200.0))
                .andExpect(jsonPath("$.saldo").value(1800.0))
                .andExpect(jsonPath("$.numeroCuenta").value("478758"));
    }

    @Test
    @DisplayName("PATCH /movimientos/{id} con valor nulo → 200 sin cambios")
    void patchMovimiento_ValorNulo_DebeRetornar200SinCambios() throws Exception {
        // PATCH con valor null: el servicio devuelve el movimiento sin modificar
        MovimientoPatchDTO patch = new MovimientoPatchDTO(null);

        when(movimientoService.actualizarParcial(eq(1L), any())).thenReturn(movimientoResponse);

        mockMvc.perform(patch("/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PATCH /movimientos/{id} sin saldo → 422 Saldo no disponible")
    void patchMovimiento_SinSaldo_DebeRetornar422() throws Exception {
        MovimientoPatchDTO patch = new MovimientoPatchDTO(new BigDecimal("-99999"));

        when(movimientoService.actualizarParcial(eq(1L), any()))
                .thenThrow(new SaldoInsuficienteException());

        mockMvc.perform(patch("/movimientos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));
    }

    @Test
    @DisplayName("DELETE /movimientos/{id} → 204 No Content")
    void deleteMovimiento_DebeRetornar204() throws Exception {
        mockMvc.perform(delete("/movimientos/1"))
                .andExpect(status().isNoContent());
    }

    // ══════════════════════════════════════════════════════════════════
    //  REPORTE F4
    // ══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("GET /reportes?fecha=...&clienteId=... → 200 con lista JSON")
    void getReporte_DebeRetornar200() throws Exception {
        ReporteMovimientoDTO reporte = ReporteMovimientoDTO.builder()
                .cliente("Marianela Montalvo").numeroCuenta("225487")
                .tipo("Corriente").saldoInicial(new BigDecimal("100"))
                .movimiento(new BigDecimal("600")).saldoDisponible(new BigDecimal("700"))
                .estado(true).build();
        Page<ReporteMovimientoDTO> page = new PageImpl<>(
                List.of(reporte),
                PageRequest.of(0, 10),
                1
        );

        when(movimientoService.generarReporte(eq("marianela456"), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/reportes")
                        .param("fecha", "2022-01-01,2022-12-31")
                        .param("clienteId", "marianela456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].Cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.content[0]['Saldo Disponible']").value(700.0));
    }
}
