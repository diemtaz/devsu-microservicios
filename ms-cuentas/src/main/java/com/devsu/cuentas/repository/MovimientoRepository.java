package com.devsu.cuentas.repository;

import com.devsu.cuentas.model.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("""
        SELECT m FROM Movimiento m
        JOIN FETCH m.cuenta c
        WHERE c.clienteId = :clienteId
          AND m.fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY m.fecha DESC
        """)
    Page<Movimiento> findByClienteIdAndFechaBetween(
            @Param("clienteId") String clienteId,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin,
            Pageable pageable);

    List<Movimiento> findByCuenta_NumeroCuenta(String numeroCuenta);
}
