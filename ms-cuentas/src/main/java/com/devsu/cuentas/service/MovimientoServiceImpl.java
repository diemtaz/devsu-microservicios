package com.devsu.cuentas.service;

import com.devsu.cuentas.exception.RecursoNoEncontradoException;
import com.devsu.cuentas.exception.SaldoInsuficienteException;
import com.devsu.cuentas.model.dto.MovimientoPatchDTO;
import com.devsu.cuentas.model.dto.MovimientoRequestDTO;
import com.devsu.cuentas.model.dto.MovimientoResponseDTO;
import com.devsu.cuentas.model.dto.ReporteMovimientoDTO;
import com.devsu.cuentas.model.entity.Cuenta;
import com.devsu.cuentas.model.entity.Movimiento;
import com.devsu.cuentas.model.mapper.CuentaMapper;
import com.devsu.cuentas.repository.ClienteRefRepository;
import com.devsu.cuentas.repository.CuentaRepository;
import com.devsu.cuentas.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Servicio de Movimientos — contiene la lógica más crítica del sistema.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MovimientoServiceImpl implements IMovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteRefRepository clienteRefRepository;
    private final CuentaMapper cuentaMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoResponseDTO> listarTodos(Pageable pageable) {
        return movimientoRepository.findAll(pageable)
                .map(cuentaMapper::toMovimientoDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoResponseDTO buscarPorId(Long id) {
        return movimientoRepository.findById(id)
                .map(cuentaMapper::toMovimientoDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Movimiento no encontrado con id: " + id));
    }

    @Override
    public MovimientoResponseDTO registrar(MovimientoRequestDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuenta())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada: " + dto.getNumeroCuenta()));

        BigDecimal valor = dto.getValor();
        String tipoMovimiento;

        if (valor.compareTo(BigDecimal.ZERO) >= 0) {
            tipoMovimiento = "CREDITO";
        } else {
            tipoMovimiento = "DEBITO";
            BigDecimal valorAbsoluto = valor.abs();
            if (cuenta.getSaldoDisponible().compareTo(valorAbsoluto) < 0) {
                log.warn("Saldo insuficiente en cuenta {}. Disponible: {}, Solicitado: {}",
                        dto.getNumeroCuenta(), cuenta.getSaldoDisponible(), valorAbsoluto);
                throw new SaldoInsuficienteException();
            }
        }

        BigDecimal nuevoSaldo = cuenta.getSaldoDisponible().add(valor);
        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        Movimiento movimiento = Movimiento.builder()
                .tipoMovimiento(tipoMovimiento)
                .valor(valor)
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .fecha(LocalDateTime.now())
                .build();

        Movimiento guardado = movimientoRepository.save(movimiento);
        log.info("Movimiento {} registrado en cuenta {}. Nuevo saldo: {}",
                tipoMovimiento, dto.getNumeroCuenta(), nuevoSaldo);

        return cuentaMapper.toMovimientoDTO(guardado);
    }

    @Override
    public MovimientoResponseDTO actualizar(Long id, MovimientoRequestDTO dto) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Movimiento no encontrado con id: " + id));

        Cuenta cuenta = movimiento.getCuenta();

        // Revertir efecto del movimiento anterior
        BigDecimal saldoRevertido = cuenta.getSaldoDisponible().subtract(movimiento.getValor());
        cuenta.setSaldoDisponible(saldoRevertido);

        BigDecimal nuevoValor = dto.getValor();
        if (nuevoValor.compareTo(BigDecimal.ZERO) < 0) {
            if (saldoRevertido.compareTo(nuevoValor.abs()) < 0) {
                throw new SaldoInsuficienteException();
            }
        }

        BigDecimal nuevoSaldo = saldoRevertido.add(nuevoValor);
        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        movimiento.setValor(nuevoValor);
        movimiento.setSaldo(nuevoSaldo);
        movimiento.setTipoMovimiento(
                nuevoValor.compareTo(BigDecimal.ZERO) >= 0 ? "CREDITO" : "DEBITO");

        return cuentaMapper.toMovimientoDTO(movimientoRepository.save(movimiento));
    }

    /**
     * PATCH — actualización parcial del valor de un movimiento.
     * Solo modifica el valor si viene no-null en el DTO.
     * Reutiliza la lógica de actualizar (revierte + aplica nuevo valor).
     */
    @Override
    public MovimientoResponseDTO actualizarParcial(Long id, MovimientoPatchDTO dto) {
        if (dto.getValor() == null) {
            // Nada que cambiar, devolvemos el movimiento actual
            return buscarPorId(id);
        }
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Movimiento no encontrado con id: " + id));

        // Reutilizar lógica PUT con el nuevo valor y la cuenta existente
        MovimientoRequestDTO req = new MovimientoRequestDTO(
                movimiento.getCuenta().getNumeroCuenta(),
                dto.getValor());

        return actualizar(id, req);
    }

    @Override
    public void eliminar(Long id) {
        Movimiento movimiento = movimientoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Movimiento no encontrado con id: " + id));

        Cuenta cuenta = movimiento.getCuenta();
        cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().subtract(movimiento.getValor()));
        cuentaRepository.save(cuenta);

        movimientoRepository.delete(movimiento);
        log.info("Movimiento {} eliminado y saldo revertido", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReporteMovimientoDTO> generarReporte(
            String clienteId, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {

        String nombreCliente = clienteRefRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cliente no encontrado con clienteId: " + clienteId))
                .getNombre();
        
        Page<Movimiento> movimientosPage = movimientoRepository
            .findByClienteIdAndFechaBetween(clienteId, fechaInicio, fechaFin, pageable);

        return movimientosPage.map(m -> ReporteMovimientoDTO.builder()
            .fecha(m.getFecha())
            .cliente(nombreCliente)
            .numeroCuenta(m.getCuenta().getNumeroCuenta())
            .tipo(m.getCuenta().getTipoCuenta())
            .saldoInicial(m.getCuenta().getSaldoInicial())
            .estado(m.getCuenta().getEstado())
            .movimiento(m.getValor())
            .saldoDisponible(m.getSaldo())
            .build());
    }
}
