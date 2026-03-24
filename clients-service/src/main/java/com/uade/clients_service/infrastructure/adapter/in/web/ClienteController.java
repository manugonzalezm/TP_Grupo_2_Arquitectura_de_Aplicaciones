package com.uade.clients_service.infrastructure.adapter.in.web;

import com.uade.clients_service.domain.model.Cliente;
import com.uade.clients_service.domain.port.in.ClienteUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteUseCase clienteUseCase;

    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> getAllClientes() {
        return ResponseEntity.ok(clienteUseCase.getAllClientes());
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Cliente> getClienteByDni(@PathVariable String dni) {
        return clienteUseCase.getClienteByDni(dni)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Cliente> createCliente(@RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteUseCase.createCliente(cliente));
    }

    @PutMapping("/dni/{dni}")
    public ResponseEntity<Cliente> updateCliente(@PathVariable String dni, @RequestBody Cliente cliente) {
        return clienteUseCase.updateCliente(dni, cliente)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/dni/{dni}")
    public ResponseEntity<Void> deleteCliente(@PathVariable String dni) {
        if (clienteUseCase.deleteCliente(dni)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
