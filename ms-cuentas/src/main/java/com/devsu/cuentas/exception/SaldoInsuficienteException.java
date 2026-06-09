package com.devsu.cuentas.exception;

/**
 * F3: Excepción específica cuando no hay saldo suficiente para un retiro.
 * Mensaje exacto requerido por el enunciado: "Saldo no disponible".
 */
public class SaldoInsuficienteException extends RuntimeException {
    public SaldoInsuficienteException() {
        super("Saldo no disponible");
    }
    public SaldoInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
