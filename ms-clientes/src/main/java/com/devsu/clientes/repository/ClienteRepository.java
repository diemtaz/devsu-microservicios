package com.devsu.clientes.repository;

import com.devsu.clientes.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Patrón Repository: abstrae la capa de persistencia.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByClienteId(String clienteId);

    boolean existsByClienteId(String clienteId);

    boolean existsByIdentificacion(String identificacion);
}
