package com.devsu.cuentas.service;

import com.devsu.cuentas.exception.RecursoNoEncontradoException;
import com.devsu.cuentas.exception.SaldoInsuficienteException;
import com.devsu.cuentas.model.dto.MovimientoRequestDTO;
import com.devsu.cuentas.model.dto.MovimientoResponseDTO;
import com.devsu.cuentas.model.entity.Cuenta;
import com.devsu.cuentas.model.entity.Movimiento;
import com.devsu.cuentas.model.mapper.CuentaMapper;
import com.devsu.cuentas.repository.ClienteRefRepository;
import com.devsu.cuentas.repository.CuentaRepository;
import com.devsu.cuentas.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MovimientoService (F2 y F3).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - MovimientoService")
class MovimientoServiceImplTest {

    @Mock private MovimientoRepository movimientoRepository;
    @Mock private CuentaRepository cuentaRepository;
    @Mock private ClienteRefRepository clienteRefRepository;
    @Mock private CuentaMapper cuentaMapper;

    @InjectMocks
    private MovimientoServiceImpl movimientoService;

    private Cuenta cuentaConSaldo;

    @BeforeEach
    void setUp() {
        cuentaConSaldo = Cuenta.builder()
                .id(1L)
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000.00"))
                .saldoDisponible(new BigDecimal("2000.00"))
                .clienteId("jose123")
                .estado(true)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 1: F2 - Depósito actualiza saldo correctamente
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("F2 - Depósito debe incrementar el saldo disponible")
    void registrar_DepositoDebeIncrementarSaldo() {
        // ARRANGE
        MovimientoRequestDTO dto = new MovimientoRequestDTO("478758", new BigDecimal("500.00"));

        Movimiento movimientoGuardado = Movimiento.builder()
                .id(1L)
                .valor(new BigDecimal("500.00"))
                .saldo(new BigDecimal("2500.00"))
                .tipoMovimiento("CREDITO")
                .cuenta(cuentaConSaldo)
                .build();

        MovimientoResponseDTO responseDTO = MovimientoResponseDTO.builder()
                .id(1L)
                .tipoMovimiento("CREDITO")
                .valor(new BigDecimal("500.00"))
                .saldo(new BigDecimal("2500.00"))
                .build();

        when(cuentaRepository.findByNumeroCuenta("478758"))
                .thenReturn(Optional.of(cuentaConSaldo));
        when(movimientoRepository.save(any())).thenReturn(movimientoGuardado);
        when(cuentaMapper.toMovimientoDTO(any())).thenReturn(responseDTO);

        // ACT
        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        // ASSERT
        assertThat(resultado.getTipoMovimiento()).isEqualTo("CREDITO");
        assertThat(resultado.getSaldo()).isEqualByComparingTo(new BigDecimal("2500.00"));

        // El saldo en la entidad Cuenta fue actualizado
        assertThat(cuentaConSaldo.getSaldoDisponible())
                .isEqualByComparingTo(new BigDecimal("2500.00"));

        verify(cuentaRepository, times(1)).save(cuentaConSaldo);
        verify(movimientoRepository, times(1)).save(any(Movimiento.class));
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 2: F2 - Retiro actualiza saldo con valor negativo
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("F2 - Retiro debe decrementar el saldo disponible")
    void registrar_RetiroDebeDecrementarSaldo() {
        // ARRANGE
        MovimientoRequestDTO dto = new MovimientoRequestDTO("478758", new BigDecimal("-575.00"));

        Movimiento movimientoGuardado = Movimiento.builder()
                .id(2L)
                .valor(new BigDecimal("-575.00"))
                .saldo(new BigDecimal("1425.00"))
                .tipoMovimiento("DEBITO")
                .cuenta(cuentaConSaldo)
                .build();

        MovimientoResponseDTO responseDTO = MovimientoResponseDTO.builder()
                .id(2L)
                .tipoMovimiento("DEBITO")
                .valor(new BigDecimal("-575.00"))
                .saldo(new BigDecimal("1425.00"))
                .build();

        when(cuentaRepository.findByNumeroCuenta("478758"))
                .thenReturn(Optional.of(cuentaConSaldo));
        when(movimientoRepository.save(any())).thenReturn(movimientoGuardado);
        when(cuentaMapper.toMovimientoDTO(any())).thenReturn(responseDTO);

        // ACT
        MovimientoResponseDTO resultado = movimientoService.registrar(dto);

        // ASSERT
        assertThat(resultado.getTipoMovimiento()).isEqualTo("DEBITO");
        assertThat(cuentaConSaldo.getSaldoDisponible())
                .isEqualByComparingTo(new BigDecimal("1425.00"));
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 3: F3 - Retiro sin saldo lanza SaldoInsuficienteException
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("F3 - Retiro mayor al saldo debe lanzar SaldoInsuficienteException")
    void registrar_SinSaldoDebeLanzarSaldoInsuficienteException() {
        // ARRANGE: saldo 0 en la cuenta
        cuentaConSaldo.setSaldoDisponible(BigDecimal.ZERO);
        MovimientoRequestDTO dto = new MovimientoRequestDTO("478758", new BigDecimal("-100.00"));

        when(cuentaRepository.findByNumeroCuenta("478758"))
                .thenReturn(Optional.of(cuentaConSaldo));

        // ACT + ASSERT
        assertThatThrownBy(() -> movimientoService.registrar(dto))
                .isInstanceOf(SaldoInsuficienteException.class)
                .hasMessage("Saldo no disponible");  // Mensaje exacto del enunciado

        // No se guardó ningún movimiento
        verify(movimientoRepository, never()).save(any());
        // El saldo de la cuenta NO fue modificado
        assertThat(cuentaConSaldo.getSaldoDisponible())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 4: Cuenta inexistente lanza RecursoNoEncontradoException
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Debe lanzar excepción si la cuenta no existe")
    void registrar_CuentaInexistenteLanzaExcepcion() {
        // ARRANGE
        when(cuentaRepository.findByNumeroCuenta("000000"))
                .thenReturn(Optional.empty());

        // ACT + ASSERT
        assertThatThrownBy(() ->
                movimientoService.registrar(new MovimientoRequestDTO("000000", BigDecimal.TEN)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("000000");
    }
}
