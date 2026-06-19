package com.uade.clients_service.infrastructure.adapter.out.persistence;

import com.uade.clients_service.domain.model.Cliente;
import com.uade.clients_service.domain.port.out.ClienteRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ClientePersistenceAdapter implements ClienteRepositoryPort {

    private final ClienteJpaRepository jpaRepository;
    private final ClienteMapper mapper;

    public ClientePersistenceAdapter(ClienteJpaRepository jpaRepository, ClienteMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Cliente> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Cliente> findByDni(String dni) {
        return jpaRepository.findByDni(dni)
                .map(mapper::toDomain);
    }

    @Override
    public Cliente save(Cliente cliente) {
        ClienteJpaEntity entity = mapper.toJpaEntity(cliente);
        ClienteJpaEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
