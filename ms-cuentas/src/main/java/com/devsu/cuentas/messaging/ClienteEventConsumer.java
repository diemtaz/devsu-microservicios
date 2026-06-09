package com.devsu.cuentas.messaging;

import com.devsu.cuentas.model.entity.ClienteRef;
import com.devsu.cuentas.repository.ClienteRefRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Consumidor de eventos del broker en MS-Cuentas.
 *
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClienteEventConsumer {

    private final ClienteRefRepository clienteRefRepository;

    @RabbitListener(queues = "${rabbitmq.queue.cliente-creado}")
    @Transactional
    public void onClienteCreado(Map<String, Object> mensaje) {
        String clienteId = (String) mensaje.get("clienteId");
        String nombre    = (String) mensaje.get("nombre");

        log.info("Evento CLIENTE_CREADO recibido para clienteId: {}", clienteId);

        if (clienteRefRepository.existsByClienteId(clienteId)) {
            log.warn("ClienteRef ya existe para clienteId: {}. Ignorando.", clienteId);
            return;
        }

        clienteRefRepository.save(ClienteRef.builder()
                .clienteId(clienteId)
                .nombre(nombre)
                .build());

        log.info("ClienteRef creado para clienteId: {}", clienteId);
    }

    @RabbitListener(queues = "${rabbitmq.queue.cliente-eliminado}")
    @Transactional
    public void onClienteEliminado(Map<String, Object> mensaje) {
        String clienteId = (String) mensaje.get("clienteId");

        log.info("Evento CLIENTE_ELIMINADO recibido para clienteId: {}", clienteId);

        clienteRefRepository.deleteByClienteId(clienteId);

        log.info("ClienteRef eliminado para clienteId: {}", clienteId);
    }

    @RabbitListener(queues = "${rabbitmq.queue.cliente-actualizado}")
    @Transactional
    public void onClienteActualizado(Map<String, Object> mensaje) {
        String clienteId = (String) mensaje.get("clienteId");
        String nombre    = (String) mensaje.get("nombre");

        log.info("Evento CLIENTE_ACTUALIZADO recibido para clienteId: {}", clienteId);

        clienteRefRepository.findByClienteId(clienteId).ifPresent(ref -> {
            ref.setNombre(nombre);
            clienteRefRepository.save(ref);
            log.info("ClienteRef actualizado para clienteId: {}", clienteId);
        });
    }
}
