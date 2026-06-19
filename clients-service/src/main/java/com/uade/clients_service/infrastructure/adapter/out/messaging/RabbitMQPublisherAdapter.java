package com.uade.clients_service.infrastructure.adapter.out.messaging;

import com.uade.clients_service.domain.event.ClienteCreatedEvent;
import com.uade.clients_service.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("rabbitmq")
public class RabbitMQPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishClienteCreated(ClienteCreatedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CLIENTS_EXCHANGE,
                RabbitMQConfig.CLIENTE_CREATED_ROUTING_KEY,
                event
        );
        log.info("Evento publicado: ClienteCreated [id={}, dni={}, nombre={} {}]",
                event.getClienteId(), event.getDni(), event.getNombre(), event.getApellido());
    }
}
