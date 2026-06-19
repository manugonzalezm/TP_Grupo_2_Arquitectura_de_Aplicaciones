package com.uade.clients_service.domain.event;

import java.time.LocalDateTime;

public class ClienteCreatedEvent {

    private Long clienteId;
    private String dni;
    private String nombre;
    private String apellido;
    private String email;
    private LocalDateTime timestamp;

    public ClienteCreatedEvent() {
    }

    public ClienteCreatedEvent(Long clienteId, String dni, String nombre, String apellido, String email) {
        this.clienteId = clienteId;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.timestamp = LocalDateTime.now();
    }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
