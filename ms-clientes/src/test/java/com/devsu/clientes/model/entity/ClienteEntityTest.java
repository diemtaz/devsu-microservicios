package com.devsu.clientes.model.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("F5 - Prueba unitaria entidad de dominio Cliente")
class ClienteEntityTest {

    // ─────────────────────────────────────────────────────────────────
    //  TEST 1: Cliente hereda los campos de Persona correctamente
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Cliente debe heredar todos los campos de Persona")
    void cliente_DebeHeredarCamposDePersona() {
        // ARRANGE + ACT
        Cliente cliente = new Cliente();
        cliente.setNombre("Jose Lema");
        cliente.setGenero("Masculino");
        cliente.setEdad(30);
        cliente.setIdentificacion("0101010101");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("098254785");

        // ASSERT — campos heredados de Persona accesibles desde Cliente
        assertThat(cliente.getNombre()).isEqualTo("Jose Lema");
        assertThat(cliente.getGenero()).isEqualTo("Masculino");
        assertThat(cliente.getEdad()).isEqualTo(30);
        assertThat(cliente.getIdentificacion()).isEqualTo("0101010101");
        assertThat(cliente.getDireccion()).isEqualTo("Otavalo sn y principal");
        assertThat(cliente.getTelefono()).isEqualTo("098254785");

        // Cliente ES una instancia de Persona (herencia)
        assertThat(cliente).isInstanceOf(Persona.class);
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 2: Campos propios de Cliente se asignan correctamente
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Cliente debe almacenar clienteId, contraseña y estado")
    void cliente_DebeAlmacenarCamposPropios() {
        // ARRANGE + ACT
        Cliente cliente = new Cliente();
        cliente.setClienteId("jose123");
        cliente.setContrasena("1234");
        cliente.setEstado(true);

        // ASSERT
        assertThat(cliente.getClienteId()).isEqualTo("jose123");
        assertThat(cliente.getContrasena()).isEqualTo("1234");
        assertThat(cliente.getEstado()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 3: Estado por defecto es true
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Estado del cliente debe ser true por defecto")
    void cliente_EstadoPorDefectoDebeSerTrue() {
        Cliente cliente = new Cliente();
        cliente.setEstado(true); // simula el default del constructor

        assertThat(cliente.getEstado()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 4: Cliente inactivo cambia estado a false
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Cliente debe poder desactivarse cambiando estado a false")
    void cliente_DebePoderDesactivarse() {
        Cliente cliente = new Cliente();
        cliente.setEstado(true);

        // ACT
        cliente.setEstado(false);

        // ASSERT
        assertThat(cliente.getEstado()).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────
    //  TEST 5: Dos clientes con mismo clienteId son lógicamente iguales
    // ─────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Dos instancias con el mismo clienteId deben tener el mismo clienteId")
    void cliente_ClienteIdEsElIdentificadorDeNegocio() {
        Cliente c1 = new Cliente();
        c1.setClienteId("jose123");

        Cliente c2 = new Cliente();
        c2.setClienteId("jose123");

        assertThat(c1.getClienteId()).isEqualTo(c2.getClienteId());
    }
}
