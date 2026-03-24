package com.uade.clients_service.controller;

import com.uade.clients_service.model.Cliente;
import com.uade.clients_service.service.ClienteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> getAll() {
        return clienteService.findAll();
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Cliente> getByDni(@PathVariable String dni) {
        return clienteService.findByDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Cliente create(@RequestBody Cliente cliente) {
        return clienteService.save(cliente);
    }

    @PutMapping("/dni/{dni}")
    public ResponseEntity<Cliente> update(@PathVariable String dni, @RequestBody Cliente cliente) {
        return clienteService.findByDni(dni)
                .map(existing -> {
                    existing.setDni(cliente.getDni());
                    existing.setNombre(cliente.getNombre());
                    existing.setApellido(cliente.getApellido());
                    existing.setEmail(cliente.getEmail());
                    existing.setTelefono(cliente.getTelefono());
                    existing.setDireccion(cliente.getDireccion());
                    return ResponseEntity.ok(clienteService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/dni/{dni}")
    public ResponseEntity<Void> delete(@PathVariable String dni) {
        return clienteService.findByDni(dni)
                .map(c -> {
                    clienteService.deleteById(c.getId());
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
