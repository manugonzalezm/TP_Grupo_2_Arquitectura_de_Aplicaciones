package com.uade.clients_service.infrastructure.config;

import com.uade.clients_service.domain.model.Cliente;
import com.uade.clients_service.domain.port.out.ClienteRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ClienteRepositoryPort clienteRepositoryPort;

    public DataInitializer(ClienteRepositoryPort clienteRepositoryPort) {
        this.clienteRepositoryPort = clienteRepositoryPort;
    }

    @Override
    public void run(String... args) {
        if (clienteRepositoryPort.count() == 0) {
            clienteRepositoryPort.save(new Cliente("12345678", "Juan", "Pérez", "juan.perez@email.com", "011-4567-8901", "Av. Corrientes 1234, CABA"));
            clienteRepositoryPort.save(new Cliente("23456789", "María", "González", "maria.gonzalez@email.com", "011-4567-8902", "Av. Santa Fe 5678, CABA"));
            clienteRepositoryPort.save(new Cliente("34567890", "Carlos", "Rodríguez", "carlos.rodriguez@email.com", "011-4567-8903", "Calle Florida 910, CABA"));
        }
    }
}
