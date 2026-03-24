package com.uade.clients_service.service;

import com.uade.clients_service.model.Cliente;
import com.uade.clients_service.repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> findById(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente save(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Optional<Cliente> findByDni(String dni) {
        return clienteRepository.findByDni(dni);
    }

    public void deleteById(Long id) {
        clienteRepository.deleteById(id);
    }
}
