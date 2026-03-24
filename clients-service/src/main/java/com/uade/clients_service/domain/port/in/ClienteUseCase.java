package com.uade.clients_service.domain.port.in;

import com.uade.clients_service.domain.model.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteUseCase {

    List<Cliente> getAllClientes();

    Optional<Cliente> getClienteByDni(String dni);

    Cliente createCliente(Cliente cliente);

    Optional<Cliente> updateCliente(String dni, Cliente cliente);

    boolean deleteCliente(String dni);
}
