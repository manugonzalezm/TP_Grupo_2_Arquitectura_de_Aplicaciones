# Guía: Cómo agregar RestTemplate al ecosistema de microservicios

## ¿Qué vamos a construir?

Partiendo del proyecto base (`microservicios`), vamos a agregar comunicación sincrónica entre
microservicios usando **RestTemplate**.

Para demostrarlo creamos dos servicios nuevos:

- **`productos-service`** (puerto `8085`) — expone una API REST de productos
- **`ordenes-service`** (puerto `8086`) — crea órdenes y llama a `productos-service` via RestTemplate

```
ordenes-service  ──── HTTP GET ────►  productos-service
                      RestTemplate
                     (@LoadBalanced)
                           │
                      Eureka Server
                    (resuelve la IP real)
```

---

## ¿Qué es RestTemplate?

`RestTemplate` es el cliente HTTP sincrónico clásico de Spring. Permite que un microservicio
llame a otro a través de HTTP.

| Característica | Detalle |
|---|---|
| Tipo de comunicación | Sincrónica (bloquea el hilo hasta obtener respuesta) |
| Integración con Eureka | Sí, con `@LoadBalanced` |
| Serialización automática | Sí, usa Jackson para JSON ↔ objetos Java |
| Manejo de errores | Via excepciones (`HttpClientErrorException`, etc.) |

> **Nota:** RestTemplate está en modo mantenimiento desde Spring 5. Para proyectos nuevos se
> recomienda `WebClient`. Sin embargo, RestTemplate sigue siendo válido y es más simple de
> enseñar como introducción.

---

## Paso 1 — Registrar los módulos nuevos en el `pom.xml` raíz

En el proyecto base, el `pom.xml` de la raíz tiene estos módulos:

```xml
<modules>
    <module>config-server</module>
    <module>eureka-server</module>
    <module>auth-service</module>
    <module>api-gateway</module>
    <module>inventory-service</module>
    <module>notification-service</module>
</modules>
```

**Agregar los dos módulos nuevos al final:**

```xml
<modules>
    <module>config-server</module>
    <module>eureka-server</module>
    <module>auth-service</module>
    <module>api-gateway</module>
    <module>inventory-service</module>
    <module>notification-service</module>
    <module>productos-service</module>   <!-- NUEVO -->
    <module>ordenes-service</module>     <!-- NUEVO -->
</modules>
```

---

## Paso 2 — Crear `productos-service`

Este servicio es el **proveedor**: expone endpoints REST que `ordenes-service` va a consultar.

### Estructura de carpetas

```
productos-service/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/uade/productos/
    │   ├── ProductosServiceApplication.java
    │   ├── model/
    │   │   └── Producto.java              ← @Entity — tabla "productos" en H2
    │   ├── repository/
    │   │   └── ProductoRepository.java    ← JpaRepository<Producto, Long>
    │   ├── service/
    │   │   └── ProductoService.java       ← @Service — lógica de negocio
    │   ├── controller/
    │   │   └── ProductoController.java    ← @RestController — endpoints HTTP
    │   └── config/
    │       └── DataInitializer.java       ← carga datos de prueba al iniciar
    └── resources/
        └── bootstrap.yml
```

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.uade.microservicios</groupId>
        <artifactId>microservicios-ecosystem</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>productos-service</artifactId>
    <name>Productos Service</name>
    <description>Microservicio de productos (consumido por ordenes-service via RestTemplate)</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

### `bootstrap.yml`

```yaml
server:
  port: 8085

spring:
  application:
    name: productos-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: false

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

> El `name: productos-service` es clave: es el nombre con el que este servicio
> se registra en Eureka. `ordenes-service` lo usa para encontrarlo.

### `ProductosServiceApplication.java`

```java
package com.uade.productos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductosServiceApplication.class, args);
    }
}
```

### `Producto.java` — entidad JPA

> La clase `Producto` es directamente el `@Entity`. No hay una clase de dominio separada
> ni una clase JPA separada — son la misma cosa.

```java
package com.uade.productos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private Integer stock;

    public Producto() {}

    public Producto(String nombre, String descripcion, Double precio, Integer stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Double getPrecio() { return precio; }
    public Integer getStock() { return stock; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public void setStock(Integer stock) { this.stock = stock; }
}
```

### `ProductoRepository.java`

> Extiende `JpaRepository` directamente sobre la entidad `Producto`.
> Spring Data JPA genera automáticamente la implementación.

```java
package com.uade.productos.repository;

import com.uade.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
```

### `ProductoService.java` — lógica de negocio

```java
package com.uade.productos.service;

import com.uade.productos.model.Producto;
import com.uade.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listarTodos() {
        return repository.findAll();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public Producto crear(Producto producto) {
        return repository.save(producto);
    }

    public Producto actualizar(Long id, Producto producto) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con id: " + id);
        }
        producto.setId(id);
        return repository.save(producto);
    }

    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
```

### `ProductoController.java` — API REST

> Este endpoint `GET /api/productos/{id}` es el que `ordenes-service` va a llamar via RestTemplate.

```java
package com.uade.productos.controller;

import com.uade.productos.model.Producto;
import com.uade.productos.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    // Este endpoint es el que va a llamar ordenes-service via RestTemplate
    @GetMapping("/{id}")
    public ResponseEntity<Producto> buscarPorId(@PathVariable Long id) {
        return productoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.crear(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id, @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizar(id, producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

### `DataInitializer.java` — datos de prueba al iniciar

```java
package com.uade.productos.config;

import com.uade.productos.model.Producto;
import com.uade.productos.service.ProductoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductoService productoService;

    public DataInitializer(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Override
    public void run(String... args) {
        productoService.crear(new Producto("Laptop Pro 15", "Laptop de alto rendimiento 15 pulgadas", 1500.00, 10));
        productoService.crear(new Producto("Mouse Inalámbrico", "Mouse ergonómico sin cable", 35.00, 50));
        productoService.crear(new Producto("Teclado Mecánico", "Teclado mecánico retroiluminado", 89.99, 30));
        productoService.crear(new Producto("Monitor 27\"", "Monitor Full HD 27 pulgadas", 320.00, 15));
        productoService.crear(new Producto("Auriculares Bluetooth", "Auriculares inalámbricos con cancelación de ruido", 120.00, 25));
    }
}
```

### `Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/productos-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Paso 3 — Crear `ordenes-service` (el que usa RestTemplate)

Este es el servicio **consumidor**: cuando se crea una orden, llama a `productos-service`
via RestTemplate para obtener el nombre, precio y validar el stock.

### Estructura de carpetas

```
ordenes-service/
├── pom.xml
├── Dockerfile
└── src/main/
    ├── java/com/uade/ordenes/
    │   ├── OrdenesServiceApplication.java
    │   ├── model/
    │   │   ├── Orden.java              ← @Entity — tabla "ordenes" en H2
    │   │   ├── EstadoOrden.java        ← enum con los estados posibles
    │   │   └── ProductoDTO.java        ← representa la respuesta JSON de productos-service
    │   ├── repository/
    │   │   └── OrdenRepository.java    ← JpaRepository<Orden, Long>
    │   ├── service/
    │   │   └── OrdenService.java       ← @Service — lógica + llamada RestTemplate
    │   ├── controller/
    │   │   └── OrdenController.java    ← @RestController — endpoints HTTP
    │   └── config/
    │       └── RestTemplateConfig.java ← define el Bean RestTemplate con @LoadBalanced
    └── resources/
        └── bootstrap.yml
```

### `pom.xml` — dependencia clave: `spring-cloud-starter-loadbalancer`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.uade.microservicios</groupId>
        <artifactId>microservicios-ecosystem</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>ordenes-service</artifactId>
    <name>Ordenes Service</name>
    <description>Microservicio de órdenes — comunica con productos-service via RestTemplate</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!--
            DEPENDENCIA CLAVE para RestTemplate con Eureka:
            Permite usar @LoadBalanced sobre el RestTemplate, lo que hace que Spring
            resuelva "productos-service" como nombre de servicio en Eureka en lugar
            de intentar una resolución DNS convencional.
        -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
    </dependencies>
</project>
```

### `bootstrap.yml`

```yaml
server:
  port: 8086
spring:
  application:
    name: ordenes-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: false

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### `OrdenesServiceApplication.java`

```java
package com.uade.ordenes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OrdenesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdenesServiceApplication.class, args);
    }
}
```

---

## Paso 4 — Las tres clases centrales de RestTemplate
 
Estas son las clases que implementan la comunicación entre servicios. Son el corazón de la guía.

---

### 4.1 — `RestTemplateConfig.java` — definir el Bean

> **Esta es la clase más importante.** Aquí se configura RestTemplate como un Bean de Spring
> y se le agrega `@LoadBalanced` para que funcione con Eureka.

```java
package com.uade.ordenes.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    /**
     * @LoadBalanced le indica a Spring Cloud que este RestTemplate debe
     * resolver los nombres de servicio (ej: "productos-service") consultando
     * el registro de Eureka en lugar de hacer una resolución DNS normal.
     *
     * Sin @LoadBalanced: la URL debe ser http://localhost:8085/...
     * Con @LoadBalanced:  la URL puede ser http://productos-service/...
     *                     Spring resuelve la IP real a través de Eureka.
     */
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

### 4.2 — `ProductoDTO.java` — objeto que representa la respuesta JSON

> RestTemplate deserializa automáticamente la respuesta JSON de `productos-service`
> en este objeto Java. Los nombres de los campos deben coincidir con el JSON recibido.

```java
package com.uade.ordenes.model;

public class ProductoDTO {

    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;

    public ProductoDTO() {}

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Double getPrecio() { return precio; }
    public Integer getStock() { return stock; }

    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(Double precio) { this.precio = precio; }
    public void setStock(Integer stock) { this.stock = stock; }
}
```

---

### 4.3 — `OrdenService.java` — la llamada HTTP real con RestTemplate

> Acá vive el código de RestTemplate. El servicio inyecta directamente el `RestTemplate`
> y llama a `productos-service` cuando se crea una nueva orden.

```java
package com.uade.ordenes.service;

import com.uade.ordenes.model.EstadoOrden;
import com.uade.ordenes.model.Orden;
import com.uade.ordenes.model.ProductoDTO;
import com.uade.ordenes.repository.OrdenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;

@Service
public class OrdenService {

    private static final Logger log = LoggerFactory.getLogger(OrdenService.class);

    private final OrdenRepository ordenRepository;
    private final RestTemplate restTemplate;
    private final String productosServiceUrl;

    public OrdenService(
            OrdenRepository ordenRepository,
            RestTemplate restTemplate,
            @Value("${productos.service.url:http://productos-service}") String productosServiceUrl) {
        this.ordenRepository = ordenRepository;
        this.restTemplate = restTemplate;
        this.productosServiceUrl = productosServiceUrl;
    }

    public Orden crear(Long productoId, Integer cantidad) {
        String url = productosServiceUrl + "/api/productos/" + productoId;
        log.info("Consultando productos-service: GET {}", url);

        ProductoDTO producto;
        try {
            // getForObject: hace GET y deserializa el JSON de la respuesta a ProductoDTO
            producto = restTemplate.getForObject(url, ProductoDTO.class);
        } catch (HttpClientErrorException.NotFound e) {
            // El servidor devolvió 404 → el producto no existe
            throw new RuntimeException("Producto no encontrado con id: " + productoId);
        } catch (Exception e) {
            // Cualquier otro error: timeout, servicio caído, etc.
            throw new RuntimeException("productos-service no disponible: " + e.getMessage(), e);
        }

        if (producto == null) {
            throw new RuntimeException("Producto no encontrado con id: " + productoId);
        }

        if (producto.getStock() < cantidad) {
            throw new RuntimeException(
                "Stock insuficiente. Disponible: " + producto.getStock() + ", solicitado: " + cantidad
            );
        }

        Orden orden = new Orden(productoId, cantidad);
        orden.setNombreProducto(producto.getNombre());
        orden.setPrecioUnitario(producto.getPrecio());
        orden.setPrecioTotal(producto.getPrecio() * cantidad);

        return ordenRepository.save(orden);
    }

    public List<Orden> listarTodas() {
        return ordenRepository.findAll();
    }

    public Optional<Orden> buscarPorId(Long id) {
        return ordenRepository.findById(id);
    }

    public Orden confirmar(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id: " + id));
        orden.setEstado(EstadoOrden.CONFIRMADA);
        return ordenRepository.save(orden);
    }

    public Orden cancelar(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id: " + id));
        orden.setEstado(EstadoOrden.CANCELADA);
        return ordenRepository.save(orden);
    }
}
```

---

## Paso 5 — Resto de clases de `ordenes-service`

### `EstadoOrden.java`

```java
package com.uade.ordenes.model;

public enum EstadoOrden {
    PENDIENTE,
    CONFIRMADA,
    CANCELADA
}
```

### `Orden.java` — entidad JPA

> `Orden` también es directamente `@Entity`. El campo `estado` usa `@Enumerated(EnumType.STRING)`
> para guardar el nombre del enum como texto en la base de datos.

```java
package com.uade.ordenes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productoId;

    private String nombreProducto;

    @Column(nullable = false)
    private Integer cantidad;

    private Double precioUnitario;
    private Double precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOrden estado;

    private LocalDateTime fechaCreacion;

    public Orden() {}

    public Orden(Long productoId, Integer cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.estado = EstadoOrden.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProductoId() { return productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public Integer getCantidad() { return cantidad; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public Double getPrecioTotal() { return precioTotal; }
    public EstadoOrden getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public void setId(Long id) { this.id = id; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public void setPrecioTotal(Double precioTotal) { this.precioTotal = precioTotal; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
```

### `OrdenRepository.java`

```java
package com.uade.ordenes.repository;

import com.uade.ordenes.model.Orden;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdenRepository extends JpaRepository<Orden, Long> {
}
```

### `OrdenController.java`

```java
package com.uade.ordenes.controller;

import com.uade.ordenes.model.Orden;
import com.uade.ordenes.service.OrdenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping
    public ResponseEntity<List<Orden>> listarTodas() {
        return ResponseEntity.ok(ordenService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orden> buscarPorId(@PathVariable Long id) {
        return ordenService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea una nueva orden.
     * Internamente llama a productos-service via RestTemplate para obtener
     * el nombre y precio del producto y validar el stock disponible.
     *
     * Body esperado: { "productoId": 1, "cantidad": 2 }
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Long productoId = Long.valueOf(body.get("productoId").toString());
            Integer cantidad = Integer.valueOf(body.get("cantidad").toString());
            return ResponseEntity.ok(ordenService.crear(productoId, cantidad));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Orden> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.confirmar(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Orden> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenService.cancelar(id));
    }
}
```

### `Dockerfile`

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/ordenes-service-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Resumen: los 3 elementos clave para agregar RestTemplate

| # | Elemento | Rol |
|---|---------|-----|
| 1 | `RestTemplateConfig.java` | Define el Bean `RestTemplate` con `@LoadBalanced` |
| 2 | `ProductoDTO.java` | Objeto que mapea la respuesta JSON del otro servicio |
| 3 | `restTemplate.getForObject(url, ProductoDTO.class)` | La línea que hace la llamada HTTP |

---

## Cómo probar el flujo completo

### 1. Levantar los servicios (en orden)

```bash
# Primero
mvn spring-boot:run -pl config-server

# Segundo
mvn spring-boot:run -pl eureka-server

# Luego (en cualquier orden)
mvn spring-boot:run -pl productos-service
mvn spring-boot:run -pl ordenes-service
```

O con Docker:

```bash
docker compose --profile rabbitmq up --build
```

### 2. Verificar que ambos servicios estén en Eureka

Abrir: http://localhost:8761

Deben aparecer `PRODUCTOS-SERVICE` y `ORDENES-SERVICE` en la lista.

### 3. Probar la llamada RestTemplate

```http
### Crear una orden (llama internamente a productos-service via RestTemplate)
POST http://localhost:8086/api/ordenes
Content-Type: application/json

{
  "productoId": 1,
  "cantidad": 2
}
```

**Respuesta esperada:**
```json
{
  "id": 1,
  "productoId": 1,
  "nombreProducto": "Laptop Pro 15",
  "cantidad": 2,
  "precioUnitario": 1500.0,
  "precioTotal": 3000.0,
  "estado": "PENDIENTE",
  "fechaCreacion": "2026-03-22T10:00:00"
}
```

Los campos `nombreProducto`, `precioUnitario` y `precioTotal` vienen de `productos-service`,
obtenidos via RestTemplate en tiempo de creación de la orden.

### 4. Probar casos de error

```http
### Producto inexistente → devuelve 400 con mensaje de error
POST http://localhost:8086/api/ordenes
Content-Type: application/json

{ "productoId": 999, "cantidad": 1 }

###

### Stock insuficiente → devuelve 400 con mensaje de error
POST http://localhost:8086/api/ordenes
Content-Type: application/json

{ "productoId": 1, "cantidad": 9999 }
```

---

## Métodos principales de RestTemplate

| Método | Descripción | Ejemplo |
|--------|-------------|---------|
| `getForObject(url, Clase.class)` | GET y deserializa la respuesta | `restTemplate.getForObject(url, ProductoDTO.class)` |
| `getForEntity(url, Clase.class)` | GET y devuelve `ResponseEntity` (incluye status code) | `restTemplate.getForEntity(url, ProductoDTO.class)` |
| `postForObject(url, body, Clase.class)` | POST con body y deserializa respuesta | `restTemplate.postForObject(url, orden, OrdenDTO.class)` |
| `exchange(url, método, entity, Clase.class)` | Llamada genérica con control total (headers, método, etc.) | `restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class)` |
| `delete(url)` | DELETE | `restTemplate.delete(url)` |
