package com.uade.clients_service.infrastructure.adapter.out.messaging;

import com.uade.clients_service.domain.event.ClienteCreatedEvent;
import com.uade.clients_service.domain.port.out.EventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Adaptador NoOp: se activa cuando no hay perfil rabbitmq ni kafka.
 * No hace nada, pero permite que ClienteService funcione sin mensajería.
 */
@Component
@Profile("!rabbitmq")
public class NoOpEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(NoOpEventPublisherAdapter.class);

    @Override
    public void publishClienteCreated(ClienteCreatedEvent event) {
        log.debug("NoOp: evento ClienteCreated ignorado (sin perfil de mensajería activo)");
    }
}
