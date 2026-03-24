package com.uade.clients_service.infrastructure.adapter.out.persistence;

import com.uade.clients_service.domain.model.Cliente;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toDomain(ClienteJpaEntity entity) {
        return new Cliente(
                entity.getId(),
                entity.getDni(),
                entity.getNombre(),
                entity.getApellido(),
                entity.getEmail(),
                entity.getTelefono(),
                entity.getDireccion()
        );
    }

    public ClienteJpaEntity toJpaEntity(Cliente cliente) {
        ClienteJpaEntity entity = new ClienteJpaEntity(
                cliente.getDni(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion()
        );
        if (cliente.getId() != null) {
            entity.setId(cliente.getId());
        }
        return entity;
    }
}
