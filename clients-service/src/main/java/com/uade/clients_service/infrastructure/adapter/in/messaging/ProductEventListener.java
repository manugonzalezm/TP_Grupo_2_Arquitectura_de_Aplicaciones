package com.uade.clients_service.infrastructure.adapter.in.messaging;

import com.uade.clients_service.infrastructure.adapter.out.messaging.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumidor de eventos ProductCreated publicados por el inventory-service.
 * Recibe el mensaje como Map genérico para evitar acoplamiento al DTO de inventory.
 */
@Component
@Profile("rabbitmq")
public class ProductEventListener {

    private static final Logger log = LoggerFactory.getLogger(ProductEventListener.class);

    @RabbitListener(queues = RabbitMQConfig.PRODUCT_CREATED_CLIENTS_QUEUE)
    public void handleProductCreated(Map<String, Object> event) {
        log.info("=== [clients-service] PRODUCTO RECIBIDO ===");
        log.info("  ID:       {}", event.get("productId"));
        log.info("  Nombre:   {}", event.get("name"));
        log.info("  Cantidad: {}", event.get("quantity"));
        log.info("  Precio:   ${}", event.get("price"));
        log.info("===========================================");
    }
}
