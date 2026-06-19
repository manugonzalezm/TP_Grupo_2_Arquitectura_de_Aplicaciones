package com.uade.clients_service.application.service;

import com.uade.clients_service.domain.event.ClienteCreatedEvent;
import com.uade.clients_service.domain.model.Cliente;
import com.uade.clients_service.domain.port.in.ClienteUseCase;
import com.uade.clients_service.domain.port.out.ClienteRepositoryPort;
import com.uade.clients_service.domain.port.out.EventPublisherPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService implements ClienteUseCase {

    private final ClienteRepositoryPort repositoryPort;
    private final EventPublisherPort eventPublisherPort;

    public ClienteService(ClienteRepositoryPort repositoryPort, EventPublisherPort eventPublisherPort) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Override
    public List<Cliente> getAllClientes() {
        return repositoryPort.findAll();
    }

    @Override
    public Optional<Cliente> getClienteByDni(String dni) {
        return repositoryPort.findByDni(dni);
    }

    @Override
    public Cliente createCliente(Cliente cliente) {
        Cliente saved = repositoryPort.save(cliente);
        eventPublisherPort.publishClienteCreated(
                new ClienteCreatedEvent(saved.getId(), saved.getDni(), saved.getNombre(),
                        saved.getApellido(), saved.getEmail())
        );
        return saved;
    }

    @Override
    public Optional<Cliente> updateCliente(String dni, Cliente cliente) {
        return repositoryPort.findByDni(dni).map(existing -> {
            Cliente updated = new Cliente(
                    existing.getId(),
                    cliente.getDni(),
                    cliente.getNombre(),
                    cliente.getApellido(),
                    cliente.getEmail(),
                    cliente.getTelefono(),
                    cliente.getDireccion()
            );
            return repositoryPort.save(updated);
        });
    }

    @Override
    public boolean deleteCliente(String dni) {
        return repositoryPort.findByDni(dni).map(existing -> {
            repositoryPort.deleteById(existing.getId());
            return true;
        }).orElse(false);
    }
}
