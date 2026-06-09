package com.devsu.cuentas.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.queue.cliente-creado}")
    private String queueClienteCreado;

    @Value("${rabbitmq.queue.cliente-eliminado}")
    private String queueClienteEliminado;

    @Value("${rabbitmq.queue.cliente-actualizado}")
    private String queueClienteActualizado;

    @Value("${rabbitmq.routing-key.cliente-creado}")
    private String rkClienteCreado;

    @Value("${rabbitmq.routing-key.cliente-eliminado}")
    private String rkClienteEliminado;

    @Value("${rabbitmq.routing-key.cliente-actualizado}")
    private String rkClienteActualizado;

    @Bean
    public TopicExchange devsuExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue queueClienteCreado() {
        return QueueBuilder.durable(queueClienteCreado).build();
    }

    @Bean
    public Queue queueClienteEliminado() {
        return QueueBuilder.durable(queueClienteEliminado).build();
    }

    @Bean
    public Binding bindingClienteCreado() {
        return BindingBuilder.bind(queueClienteCreado())
                .to(devsuExchange())
                .with(rkClienteCreado);
    }

    @Bean
    public Binding bindingClienteEliminado() {
        return BindingBuilder.bind(queueClienteEliminado())
                .to(devsuExchange())
                .with(rkClienteEliminado);
    }

    @Bean
    public Queue queueClienteActualizado() {
        return QueueBuilder.durable(queueClienteActualizado).build();
    }

    @Bean
    public Binding bindingClienteActualizado() {
        return BindingBuilder.bind(queueClienteActualizado())
                .to(devsuExchange())
                .with(rkClienteActualizado);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
