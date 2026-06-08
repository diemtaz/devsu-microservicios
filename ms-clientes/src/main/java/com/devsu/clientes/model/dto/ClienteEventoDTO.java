package com.devsu.clientes.model.dto;

import lombok.*;
import java.io.Serializable;

/**
 * Evento que MS-Clientes publica a RabbitMQ cuando se crea o elimina un cliente.
 * MS-Cuentas consume este evento para mantener una referencia local del clienteId.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteEventoDTO implements Serializable {
    private String clienteId;
    private String nombre;
    private String tipoEvento; // CREADO | ELIMINADO | ACTUALIZADO
}
