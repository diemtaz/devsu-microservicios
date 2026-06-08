package com.devsu.clientes.exception;

/**
 * Excepción de negocio cuando un recurso no se encuentra.
 * Extiende RuntimeException para no obligar al llamador a capturarla
 * (unchecked exception), simplificando el código de los servicios.
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
