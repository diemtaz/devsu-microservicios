# devsu — Prueba Técnica Microservicios

## Arquitectura

```
Cliente HTTP
    ├── MS-Clientes (Puerto 8081) ──► db_clientes (PostgreSQL)
    │       └── Publica eventos ──► RabbitMQ ──► MS-Cuentas
    └── MS-Cuentas  (Puerto 8082) ──► db_cuentas (PostgreSQL)
```

### Comunicación asíncrona (RabbitMQ)
| Evento             | Cola                    | Productor   | Consumidor  |
|--------------------|-------------------------|-------------|-------------|
| Cliente creado     | `cliente.creado.queue`  | MS-Clientes | MS-Cuentas  |
| Cliente eliminado  | `cliente.eliminado.queue` | MS-Clientes | MS-Cuentas  |
| Cliente actualizado| `cliente.actualizado.queue` | MS-Clientes | MS-Cuentas  |

---

## Despliegue con Docker

```bash
# 1. Clonar el repositorio
git clone https://github.com/diemtaz/devsu-microservicios
cd devsu-microservicios

# 2. Construir y levantar todos los servicios
docker-compose up --build

# 3. Verificar servicios activos
docker-compose ps

# 4. Ver logs de un servicio
docker-compose logs -f ms-clientes
docker-compose logs -f ms-cuentas
```

### URLs disponibles
| Servicio            | URL                                    |
|---------------------|----------------------------------------|
| MS-Clientes API     | http://localhost:8081/api/clientes     |
| MS-Cuentas API      | http://localhost:8082/api/cuentas      |
| MS-Cuentas Movimientos | http://localhost:8082/api/movimientos |
| MS-Cuentas Reportes | http://localhost:8082/api/reportes     |
| RabbitMQ Management | http://localhost:15672 (guest/guest)   |

---

## Persistencia y Base de Datos
El proyecto utiliza PostgreSQL 15 con una estrategia de inicialización automática y persistencia de datos:

Inicialización automática: Se utiliza el archivo BaseDatos.sql montado en el directorio /docker-entrypoint-initdb.d/ de los contenedores de base de datos.

Lógica Condicional: El script SQL detecta en qué base de datos se está ejecutando (db_clientes o db_cuentas) para crear únicamente las tablas correspondientes a cada microservicio. Esto permite mantener un único archivo de definición de esquema para todo el ecosistema.

Volúmenes de Docker: Se han configurado volúmenes nombrados (clientes-data y cuentas-data) para garantizar que la información no se pierda al reiniciar o detener los contenedores.

```yaml

volumes:
  - clientes-data:/var/lib/postgresql/data
  - ./BaseDatos.sql:/docker-entrypoint-initdb.d/BaseDatos.sql
```

## Ejecución de pruebas

```bash
# MS-Clientes
cd ms-clientes
mvn test

# MS-Cuentas
cd ms-cuentas
mvn test
```

---

## Pruebas con Postman
En la raíz del proyecto se encuentra el archivo `devsu-Microservicios.postman_collection.json`. Puedes importarlo en Postman para probar todos los flujos (Clientes, Cuentas, Movimientos y Reportes) de forma preconfigurada.

## Ejemplos de uso (Casos del enunciado)

### 1. Crear clientes
```bash
curl -X POST http://localhost:8081/api/clientes \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Jose Lema","genero":"Masculino","edad":30,
       "identificacion":"0101010101","direccion":"Otavalo sn y principal",
       "telefono":"098254785","clienteId":"jose123","contrasena":"1234","estado":true}'
```

### 2. Crear cuenta
```bash
curl -X POST http://localhost:8082/api/cuentas \
  -H "Content-Type: application/json" \
  -d '{"numeroCuenta":"478758","tipoCuenta":"Ahorro","saldoInicial":2000,
       "estado":true,"clienteId":"jose123"}'
```

### 3. Registrar movimiento (retiro)
```bash
curl -X POST http://localhost:8082/api/movimientos \
  -H "Content-Type: application/json" \
  -d '{"numeroCuenta":"478758","valor":-575}'
```

### 4. Reporte de estado de cuenta (F4)
```bash
curl "http://localhost:8082/api/reportes?fecha=2022-01-01,2022-12-31&clienteId=marianela456"
```

### 5. Error F3 — Saldo insuficiente
```bash
curl -X POST http://localhost:8082/api/movimientos \
  -H "Content-Type: application/json" \
  -d '{"numeroCuenta":"495878","valor":-999}'
# Respuesta: 422 Unprocessable Entity
# {"mensaje": "Saldo no disponible"}
```
---

## Manejo Global de Excepciones y Validaciones

Se implementó una arquitectura de manejo de errores centralizada utilizando @RestControllerAdvice, lo que garantiza que todas las respuestas de error sigan un formato consistente y semántico.

### Estrategia de Manejo de Errores (HTTP Mapping)

Se implementó un `@RestControllerAdvice` para estandarizar las respuestas de error del sistema, mapeando excepciones de negocio a códigos de estado HTTP semánticos:

| Escenario | Excepción | Código HTTP |
| :--- | :--- | :--- |
| **Regla de Negocio (Saldo)** | `SaldoInsuficienteException` | **422** Unprocessable Entity |
| **Recurso No Encontrado** | `RecursoNoEncontradoException` | **404** Not Found |
| **Conflicto / Duplicados** | `RecursoDuplicadoException` | **409** Conflict |
| **Errores de Validación** | `MethodArgumentNotValidException` | **400** Bad Request |
| **Errores Inesperados** | `Exception` (Genérica) | **500** Internal Server Error |



### Formato de Respuesta de Error

Todas las excepciones devuelven un cuerpo JSON estructurado para facilitar el consumo desde el Frontend o servicios externos:

```JSON
{
  "timestamp": "2026-03-25T23:15:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "mensaje": "Saldo no disponible"
}
```

### Validaciones de Datos

Se utiliza Spring Boot Validation (@Valid) en los DTOs de entrada. Cuando una validación falla (ej. campos nulos o formatos incorrectos), el GlobalExceptionHandler intercepta el error y devuelve un detalle específico de cada campo afectado, mejorando la experiencia del desarrollador que consume la API.

---

## Arquitectura Orientada a Eventos (Consistencia Eventual)

Para garantizar el desacoplamiento entre los microservicios de **Clientes** y **Cuentas**, se implementó un patrón de **Consistencia Eventual** utilizando **RabbitMQ**:

1. **Publicación:** Cuando un cliente es creado o eliminado en el `MS-Clientes`, se publica un evento en el exchange correspondiente.
2. **Sincronización:** El `MS-Cuentas` consume estos eventos para mantener una referencia local (`ClienteRef`) de los datos mínimos necesarios (ID y nombre).
3. **Beneficio:** Esta arquitectura permite que el microservicio de Cuentas funcione de manera independiente y resiliente, sin necesidad de realizar consultas síncronas (Feign/Rest) hacia el microservicio de Clientes durante la creación de cuentas o generación de reportes, eliminando el acoplamiento fuerte y mejorando el tiempo de respuesta.

---

## Tecnologías
- Java 17 + Spring Boot 3.2
- Spring Data JPA / Hibernate
- PostgreSQL 15 (con scripts de inicialización y volúmenes persistentes)
- RabbitMQ 3.12
- Docker + Docker Compose
- JUnit 5 + Mockito
- Lombok
