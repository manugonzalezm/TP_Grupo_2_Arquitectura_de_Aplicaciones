# UADE – Arquitectura de Aplicaciones

## Trabajo Práctico

**Primer cuatrimestre de 2026**

---

# Contenido

* Trabajo Práctico
* Arquitectura de Aplicaciones
* Primer cuatrimestre de 2026
* Objetivo
* Alcance Mínimo
* Entregables

---

# Objetivo

**Objetivo:** Extender el ecosistema de microservicios provisto agregando un nuevo servicio simple y demostrando su integración arquitectónica mediante un **PoC (Proof of Concept)**.

---

# Alcance Mínimo

Para el desarrollo del Trabajo Práctico se requiere:

* Clonar y ejecutar el **ecosistema base** con perfil `rabbitmq`.
* Crear un **nuevo microservicio** (por ejemplo `order-service`) con **1–2 endpoints**.
* Integrar el nuevo servicio con:

    * **Eureka** (service discovery)
    * **API Gateway**
* Asegurar los endpoints utilizando **JWT emitido por `auth-service`**.
* Publicar un **evento de dominio**.
* Consumir el evento desde:

    * `notification-service`, o
    * un nuevo servicio.
* Demostrar el flujo completo utilizando:

    * **Zipkin** para trazas
    * **logs con traceId** (si se activa ELK).

---

# Entregables

1. **SAD (Software Architecture Document)**
   Documento de **15 a 25 páginas**.

2. **PoC funcionando**

3. **Nuevo microservicio integrado al ecosistema**

---

## Contenido requerido dentro del SAD

El documento SAD debe incluir:

* **C1 – Context Diagram**
* **C2 – Containers Diagram**
* **C3 – Components Diagram**
* **ASRs (Architecturally Significant Requirements)**
* **Decisiones arquitectónicas**
* **Riesgos**

---

## Requisitos del nuevo microservicio

El microservicio desarrollado debe cumplir con:

* Registrarse en **Eureka**
* Exponer **1 endpoint REST**
* **Consumir 1 evento**
* **Publicar 1 evento**
* Tener **1 entidad persistida**

---

# Niveles de arquitectura

## Nivel obligatorio (TP) – carga manual

Los siguientes componentes deben ejecutarse obligatoriamente:

* `config-server`
* `eureka`
* `gateway`
* `auth`
* `inventory`
* `notification`
* `RabbitMQ`

---

## Componentes opcionales

Se pueden incluir adicionalmente:

* **Docker**
* **Zipkin**
* **ELK (Elasticsearch, Logstash, Kibana)**

---

# Condiciones de aprobación del Trabajo Práctico

Para aprobar el TP **solo es obligatorio implementar**:

* **RabbitMQ**
* **Microservicios**
* **API Gateway**
* **Eureka**

Los siguientes componentes **son opcionales**:

* Docker
* Zipkin
* ELK
