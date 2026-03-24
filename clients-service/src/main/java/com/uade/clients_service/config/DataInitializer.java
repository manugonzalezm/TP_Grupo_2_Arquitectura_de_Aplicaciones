package com.uade.clients_service.config;

import com.uade.clients_service.model.Cliente;
import com.uade.clients_service.repository.ClienteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(ClienteRepository repository) {
        return args -> {
            repository.save(new Cliente("12345678", "Juan", "Pérez", "juan.perez@email.com", "011-4567-8901", "Av. Corrientes 1234, CABA"));
            repository.save(new Cliente("23456789", "María", "González", "maria.gonzalez@email.com", "011-4567-8902", "Av. Santa Fe 5678, CABA"));
            repository.save(new Cliente("34567890", "Carlos", "Rodríguez", "carlos.rodriguez@email.com", "011-4567-8903", "Calle Florida 910, CABA"));
        };
    }
}
