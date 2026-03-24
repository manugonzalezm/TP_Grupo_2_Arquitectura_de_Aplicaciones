# clients-service — Explicación

Este microservicio gestiona los clientes del sistema. Está implementado siguiendo la **arquitectura hexagonal** (también llamada Ports & Adapters), que separa claramente la lógica de negocio de los detalles técnicos (base de datos, HTTP, etc.).

---

## Estructura general

```
clients-service/
├── domain/               → Núcleo del negocio (sin dependencias externas)
│   ├── model/
│   └── port/
│       ├── in/           → Qué puede hacer el servicio (casos de uso)
│       └── out/          → Qué necesita el servicio del exterior (repositorio)
├── application/          → Implementación de los casos de uso
│   └── service/
└── infrastructure/       → Detalles técnicos (HTTP, base de datos, seguridad)
    ├── adapter/
    │   ├── in/web/       → Controller REST
    │   └── out/persistence/ → Acceso a la base de datos
    └── config/           → Configuración de seguridad y datos iniciales
```

---

## Capa de Dominio (`domain/`)

### `Cliente.java`
Es el modelo de negocio puro. Representa un cliente con sus atributos: `id`, `dni`, `nombre`, `apellido`, `email`, `telefono`, `direccion`. No tiene ninguna anotación de Spring ni de JPA — es un objeto Java puro. La igualdad entre clientes se determina por `id`.

### `ClienteUseCase.java` (puerto de entrada)
Interface que define **qué operaciones** expone el servicio hacia afuera:
- `getAllClientes()` — listar todos
- `getClienteByDni(dni)` — buscar por DNI
- `createCliente(cliente)` — crear
- `updateCliente(dni, cliente)` — actualizar
- `deleteCliente(dni)` — eliminar

Es el contrato que el controller usa para hablar con la lógica de negocio. Al ser una interface, el controller no sabe nada de cómo está implementado.

### `ClienteRepositoryPort.java` (puerto de salida)
Interface que define **qué necesita** la lógica de negocio de la base de datos: `findAll`, `findByDni`, `save`, `deleteById`, `count`. La lógica de negocio habla con esta interface, no con JPA directamente.

---

## Capa de Aplicación (`application/`)

### `ClienteService.java`
Implementa `ClienteUseCase`. Es el **orquestador de la lógica de negocio**:
- Para listar/buscar/crear, delega directamente al repositorio.
- Para actualizar: busca el cliente existente por DNI, construye un nuevo objeto `Cliente` con el `id` original y los datos nuevos, y lo guarda. Esto asegura que no se crea un registro nuevo accidentalmente.
- Para eliminar: busca por DNI para obtener el `id`, luego elimina por `id`. Devuelve `false` si no existe.

---

## Capa de Infraestructura (`infrastructure/`)

### `ClienteController.java`
Es el **adaptador de entrada HTTP**. Expone la API REST en `/api/clientes`:

| Método | Path | Descripción |
|--------|------|-------------|
| GET | `/api/clientes` | Lista todos los clientes |
| GET | `/api/clientes/dni/{dni}` | Busca un cliente por DNI |
| POST | `/api/clientes` | Crea un nuevo cliente |
| PUT | `/api/clientes/dni/{dni}` | Actualiza un cliente |
| DELETE | `/api/clientes/dni/{dni}` | Elimina un cliente |

Recibe las requests HTTP, llama al `ClienteUseCase`, y devuelve la respuesta. No contiene lógica de negocio.

### `ClienteJpaEntity.java`
Es la **representación de la tabla en la base de datos**. Tiene anotaciones JPA (`@Entity`, `@Table`, `@Column`). Existe separada del modelo de dominio `Cliente` para que los cambios en la base de datos no afecten al dominio y viceversa.

### `ClienteJpaRepository.java`
Extiende `JpaRepository` de Spring Data. Spring genera automáticamente las queries SQL. Agrega el método `findByDni(String dni)` para buscar por DNI.

### `ClienteMapper.java`
Convierte entre `Cliente` (dominio) y `ClienteJpaEntity` (persistencia):
- `toDomain(entity)` → convierte de JPA a dominio
- `toJpaEntity(cliente)` → convierte de dominio a JPA (preserva el `id` si existe, para que JPA haga un UPDATE en lugar de INSERT)

### `ClientePersistenceAdapter.java`
Implementa `ClienteRepositoryPort`. Es el **adaptador de salida** hacia la base de datos. Usa el `ClienteJpaRepository` para ejecutar las queries y el `ClienteMapper` para traducir los objetos. Es el único lugar del sistema donde se usa JPA directamente.

### `SecurityConfig.java`
Configura la seguridad del servicio:
- Deshabilita CSRF (innecesario en APIs REST stateless).
- Permite acceso libre a `/h2-console` y `/actuator`.
- Requiere JWT válido para cualquier endpoint de `/api/clientes/**`.
- Actúa como **Resource Server OAuth2**: valida los tokens JWT usando HMAC-SHA384 con la misma clave secreta que el auth-service.

### `DataInitializer.java`
Se ejecuta al arrancar la aplicación. Si la tabla está vacía, carga 3 clientes de ejemplo (Juan Pérez, María González, Carlos Rodríguez). Evita cargar duplicados verificando `count() == 0`.

---

## Flujo de una request

```
HTTP Request
    ↓
ClienteController       (infraestructura - adaptador de entrada)
    ↓
ClienteUseCase          (puerto de entrada - interface)
    ↓
ClienteService          (aplicación - lógica de negocio)
    ↓
ClienteRepositoryPort   (puerto de salida - interface)
    ↓
ClientePersistenceAdapter (infraestructura - adaptador de salida)
    ↓
ClienteJpaRepository    (Spring Data JPA)
    ↓
Base de datos
```

La ventaja de este diseño es que se puede cambiar cualquier capa (por ejemplo, reemplazar JPA por un cliente HTTP, o cambiar el controller REST por un consumer de mensajes) sin tocar la lógica de negocio.
