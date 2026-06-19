package com.uade.clients_service.infrastructure.adapter.out.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("rabbitmq")
public class RabbitMQConfig {

    // ── Exchange propio de clients-service (para publicar) ──────────────────
    public static final String CLIENTS_EXCHANGE          = "clients.exchange";
    public static final String CLIENTE_CREATED_QUEUE     = "cliente.created.queue";
    public static final String CLIENTE_CREATED_ROUTING_KEY = "cliente.created";

    // ── Exchange de inventory-service (para consumir ProductCreatedEvent) ───
    public static final String INVENTORY_EXCHANGE             = "inventory.exchange";
    public static final String PRODUCT_CREATED_CLIENTS_QUEUE  = "product.created.clients.queue";
    public static final String PRODUCT_CREATED_ROUTING_KEY    = "product.created";

    // ── Beans: publicación ──────────────────────────────────────────────────
    @Bean
    public TopicExchange clientsExchange() {
        return new TopicExchange(CLIENTS_EXCHANGE);
    }

    @Bean
    public Queue clienteCreatedQueue() {
        return QueueBuilder.durable(CLIENTE_CREATED_QUEUE).build();
    }

    @Bean
    public Binding clienteCreatedBinding(Queue clienteCreatedQueue, TopicExchange clientsExchange) {
        return BindingBuilder.bind(clienteCreatedQueue).to(clientsExchange).with(CLIENTE_CREATED_ROUTING_KEY);
    }

    // ── Beans: consumo de eventos de inventory ──────────────────────────────
    @Bean
    public TopicExchange inventoryExchangeRef() {
        return new TopicExchange(INVENTORY_EXCHANGE);   // mismo nombre → RabbitMQ lo referencia, no lo duplica
    }

    @Bean
    public Queue productCreatedClientsQueue() {
        return QueueBuilder.durable(PRODUCT_CREATED_CLIENTS_QUEUE).build();
    }

    @Bean
    public Binding productCreatedClientsBinding(Queue productCreatedClientsQueue,
                                                TopicExchange inventoryExchangeRef) {
        return BindingBuilder.bind(productCreatedClientsQueue)
                .to(inventoryExchangeRef)
                .with(PRODUCT_CREATED_ROUTING_KEY);
    }

    // ── Converter JSON ──────────────────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
