package com.devsu.cuentas.service;

import com.devsu.cuentas.exception.RecursoDuplicadoException;
import com.devsu.cuentas.exception.RecursoNoEncontradoException;
import com.devsu.cuentas.model.dto.CuentaPatchDTO;
import com.devsu.cuentas.model.dto.CuentaRequestDTO;
import com.devsu.cuentas.model.dto.CuentaResponseDTO;
import com.devsu.cuentas.model.entity.Cuenta;
import com.devsu.cuentas.model.mapper.CuentaMapper;
import com.devsu.cuentas.repository.ClienteRefRepository;
import com.devsu.cuentas.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CuentaServiceImpl implements ICuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRefRepository clienteRefRepository;
    private final CuentaMapper cuentaMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CuentaResponseDTO> listarTodas(Pageable pageable) {
        return cuentaRepository.findAll(pageable)
                .map(cuentaMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaResponseDTO buscarPorNumeroCuenta(String numeroCuenta) {
        return cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .map(cuentaMapper::toDTO)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada: " + numeroCuenta));
    }

    @Override
    public CuentaResponseDTO crear(CuentaRequestDTO dto) {
        if (!clienteRefRepository.existsByClienteId(dto.getClienteId())) {
            throw new RecursoNoEncontradoException(
                    "No existe un cliente registrado con clienteId: " + dto.getClienteId()
                            + ". Asegúrese de que fue creado en MS-Clientes.");
        }
        if (cuentaRepository.existsByNumeroCuenta(dto.getNumeroCuenta())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una cuenta con número: " + dto.getNumeroCuenta());
        }
        Cuenta cuenta = cuentaMapper.toEntity(dto);
        return cuentaMapper.toDTO(cuentaRepository.save(cuenta));
    }

    @Override
    public CuentaResponseDTO actualizar(String numeroCuenta, CuentaRequestDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada: " + numeroCuenta));
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setSaldoInicial(dto.getSaldoInicial());
        if (dto.getEstado() != null) cuenta.setEstado(dto.getEstado());
        return cuentaMapper.toDTO(cuentaRepository.save(cuenta));
    }

    @Override
    public CuentaResponseDTO actualizarParcial(String numeroCuenta, CuentaPatchDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada: " + numeroCuenta));
        if (dto.getTipoCuenta()   != null) cuenta.setTipoCuenta(dto.getTipoCuenta());
        if (dto.getEstado()       != null) cuenta.setEstado(dto.getEstado());
        if (dto.getSaldoInicial() != null) cuenta.setSaldoInicial(dto.getSaldoInicial());
        log.info("Cuenta {} actualizada parcialmente", numeroCuenta);
        return cuentaMapper.toDTO(cuentaRepository.save(cuenta));
    }

    @Override
    public void eliminar(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Cuenta no encontrada: " + numeroCuenta));
        cuentaRepository.delete(cuenta);
        log.info("Cuenta {} eliminada exitosamente", numeroCuenta);
    }
}
