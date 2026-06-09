package com.devsu.cuentas.repository;

import com.devsu.cuentas.model.entity.ClienteRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRefRepository extends JpaRepository<ClienteRef, Long> {
    Optional<ClienteRef> findByClienteId(String clienteId);
    boolean existsByClienteId(String clienteId);
    void deleteByClienteId(String clienteId);
}
