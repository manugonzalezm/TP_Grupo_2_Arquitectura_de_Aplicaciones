package com.uade.clients_service.domain.port.out;

import com.uade.clients_service.domain.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepositoryPort {

    List<Cliente> findAll();

    Optional<Cliente> findByDni(String dni);

    Cliente save(Cliente cliente);

    void deleteById(Long id);

    long count();
}
