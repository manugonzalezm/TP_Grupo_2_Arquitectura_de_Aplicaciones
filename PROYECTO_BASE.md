# SAD – Proyecto Base de Microservicios (Spring Boot 3.4 / Java 21)

Este documento describe la arquitectura del ecosistema de microservicios provisto para la materia **Arquitectura de Aplicaciones**. Sirve como base para el **Trabajo Práctico (TP)** y como referencia para extender el sistema con un nuevo microservicio sencillo.

---

# 1. Introducción y alcance

**Alcance:** ejecutar localmente un ecosistema de microservicios con:

* autenticación JWT
* API Gateway
* Service Discovery
* configuración centralizada
* mensajería asincrónica (RabbitMQ o Kafka por perfiles)
* trazas distribuidas (Zipkin)
* logging centralizable (ELK opcional)

El objetivo es demostrar **decisiones arquitectónicas clave** y su viabilidad mediante un **PoC (Proof of Concept)**.

---

# 2. Stakeholders y restricciones

## Stakeholders

* Docente (arquitecto/a del curso)
* Auxiliar(es)
* Estudiantes (equipos de 4–6)

## Restricciones técnicas

* ejecución en notebooks personales
* RAM recomendada **8GB** (especialmente para RabbitMQ)
* evitar levantar componentes pesados si no son necesarios (Zipkin / ELK)

## Sistemas operativos

* Windows
* macOS
* Linux

Con **Docker Desktop** o ejecución manual.

---

# 3. Atributos de calidad (ASRs)

Escenarios mínimos de calidad:

### Seguridad

Todo endpoint de negocio expuesto por Gateway requiere **JWT válido**.
Debe responder **401** ante ausencia o expiración del token.

### Observabilidad

Cada request debe poder correlacionarse mediante:

* `traceId`
* `spanId`

Visibles en:

* trazas (Zipkin)
* logs centralizados (Kibana si está activo)

### Modificabilidad

`inventory-service` adopta **Arquitectura Hexagonal** para desacoplar:

* dominio
* infraestructura

Esto permite cambiar persistencia o mensajería sin afectar los casos de uso.

### Disponibilidad

* Service discovery con **Eureka**
* Gateway tolera reinicios de servicios
* healthchecks y reintentos básicos

### Portabilidad

Posibilidad de alternar broker:

* RabbitMQ
* Kafka

mediante **profiles**, sin cambios en dominio.

---

# 4. Arquitectura propuesta (C1, C2, C3)

## C1 – Contexto

El **usuario (alumno/docente)** interactúa con el sistema vía **API Gateway**.

Servicios internos gestionan:

* autenticación
* catálogo de productos
* notificaciones

Componentes de observabilidad y mensajería forman parte del **contexto de ejecución local**.

---

## C2 – Contenedores (servicios y herramientas)

| Componente           | Puerto | Descripción                     |
| -------------------- | ------ | ------------------------------- |
| config-server        | 8888   | Configuración centralizada      |
| eureka-server        | 8761   | Service discovery               |
| api-gateway          | 8080   | Ruteo y políticas transversales |
| auth-service         | 8083   | Autenticación y emisión de JWT  |
| inventory-service    | 8082   | Dominio de productos            |
| notification-service | 8084   | Consumidor de eventos           |

### Infraestructura

* RabbitMQ (5672 / 15672) o Kafka (9092)
* Zipkin (9411)
* Elasticsearch (9200)
* Logstash (5044)
* Kibana (5601)

ELK es **opcional**.

---

## C3 – Componentes (inventory-service)

### Dominio

* POJOs sin anotaciones de framework

### Puertos

Entrada:

* `ProductUseCase`

Salida:

* `ProductRepositoryPort`
* `EventPublisherPort`

### Adaptadores

Entrada:

* REST

Salida:

* JPA
* Mensajería

Activados mediante **profiles**.

### Application Service

Orquesta:

* casos de uso
* publicación de eventos de dominio

---

# 5. Decisiones arquitectónicas justificadas

* **Microservicios + API Gateway** → mejor aislamiento y escalado
* **JWT** para autenticación
* **BCrypt** para contraseñas
* **RabbitMQ / Kafka intercambiables** mediante profiles
* **Arquitectura Hexagonal** en `inventory-service`
* **Micrometer Tracing + Zipkin** para observabilidad
* **Logs JSON compatibles con ELK**
* **H2 en memoria** para desarrollo
* **Config Server file-based**
* **Java 21 + Spring Boot 3.4 + Maven multi-módulo**

---

# 6. Modelo de dominio y contratos

## Dominio base (Inventory)

Entidad **Product**

```
Product {
 id
 name
 quantity
 price
}
```

### Caso de uso

`ProductUseCase`

Operaciones CRUD básicas.

---

## Endpoints (vía Gateway)

### Login

```
POST /auth/login
```

Response:

```
{
 token,
 type,
 expiresIn
}
```

---

### Obtener productos

```
GET /api/inventory/products
```

Requiere **JWT**

---

### Crear producto

```
POST /api/inventory/products
```

* crea producto
* publica evento

Requiere **JWT**

---

## Evento de dominio

```
ProductCreatedEvent
```

* publicado por `inventory-service`
* consumido por `notification-service`

---

# 7. Comunicación y mensajería

### Profiles

* `rabbitmq`
* `kafka`

Activación mediante `@Profile`.

---

## RabbitMQ

Configuración de:

* exchange
* queue
* bindings

---

## Kafka

Topic:

```
product-created
```

Configuración de:

* producers
* consumers

según profile.

---

# 8. Seguridad (JWT, gateway, roles)

* **BCrypt** para hash de contraseñas en `auth-service`
* **JWT firmado HS384**
* validación en Gateway y servicios downstream
* propagación del header

```
Authorization: Bearer <token>
```

---

## Usuarios de prueba

| Usuario | Password | Roles       |
| ------- | -------- | ----------- |
| admin   | admin123 | ADMIN, USER |
| user    | user123  | USER        |

**Nota:** en producción se debe:

* externalizar secreto JWT
* usar variables de entorno o secret manager
* ajustar expiración del token

---

# 9. Observabilidad (logs, métricas, trazas)

### Trazas

* **Micrometer Tracing**
* bridge **Brave**
* reporter a **Zipkin**

Endpoint:

```
http://localhost:9411/api/v2/spans
```

Sampling en desarrollo:

```
probability: 1.0
```

---

### Logs

Incluyen correlación:

```
[service, traceId, spanId]
```

---

### ELK (opcional)

* Logstash recibe logs JSON en **5044**
* Kibana visualiza con index pattern:

```
logs-*
```

---

# 10. Riesgos y mitigaciones

| Riesgo                          | Mitigación              |
| ------------------------------- | ----------------------- |
| RAM limitada                    | desactivar ELK / Zipkin |
| Desalineación de secretos JWT   | unificar configuración  |
| Cambio de profile sin servicios | documentar arranque     |
| Acoplamiento al framework       | mantener dominio limpio |
| Credenciales por defecto        | rotar usuarios          |

---

# 11. Plan de comunicaciones y RACI

## Canales

* Aula virtual
* Repositorio Git del equipo
* issues / board

## Frecuencia

* stand-up semanal **5–10 minutos**

---

## RACI

| Rol             | Responsabilidad |
| --------------- | --------------- |
| Docente         | A / R           |
| Auxiliar        | C / I           |
| Líder de equipo | R               |
| Desarrolladores | R               |
| QA Observador   | C               |

---

# 12. Control de cambios y cronograma

Los cambios de:

* alcance
* tecnología

se solicitan mediante **issue** y requieren aprobación del **docente**.

El seguimiento se realizará mediante **hitos del cronograma de la cátedra**.

---

# 13. Consideraciones éticas y privacidad

* No registrar **PII ni secretos** en logs
* Enmascarar tokens y credenciales
* Limitar retención de logs
* Evitar compartir capturas con información sensible
* Asegurar accesibilidad básica de endpoints
* Mensajes de error claros para pruebas

---

# 14. Requisitos de ejecución y perfiles

### Opción recomendada

Docker Desktop + **docker compose**

---

### Alternativa manual

Instalar:

* RabbitMQ o Kafka
* Zipkin (opcional)

y ejecutar servicios con **Maven**.

---

### Profiles disponibles

```
rabbitmq (default)
kafka
```

---

# 15. Pruebas y script de demo

### 1️⃣ Login

```
POST /auth/login
```

Obtener **token JWT**.

---

### 2️⃣ Obtener productos

```
GET /api/inventory/products
Authorization: Bearer <token>
```

---

### 3️⃣ Crear producto

```
POST /api/inventory/products
```

* dispara evento
* verificar consumo en `notification-service`

---

### 4️⃣ Observabilidad

**Trazas:** visualizar en **Zipkin**

**Logs (opcional):** revisar en **Kibana**

```
index: logs-*
```
