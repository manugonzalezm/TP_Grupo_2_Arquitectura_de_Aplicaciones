package com.uade.clients_service.domain.port.out;

import com.uade.clients_service.domain.event.ClienteCreatedEvent;

public interface EventPublisherPort {

    void publishClienteCreated(ClienteCreatedEvent event);
}
