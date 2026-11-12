# StudyMatch — Backend

Backend de **StudyMatch**, una red académica universitaria para encontrar compañeros de estudio y grupos de trabajo por asignatura, horario y afinidad académica.

El proyecto adapta la arquitectura y el modelo de negocio de una plataforma de streaming (StreamFlix) a un dominio de colaboración académica, siguiendo el diseño de Figma del proyecto **POO 2** y los lineamientos de arquitectura entregados. **Solo se implementa el backend**; no incluye frontend.

---

## 1. Alcance

- API REST para estudiantes, perfiles académicos, disponibilidad, asignaturas, grupos de estudio, solicitudes de ingreso, notificaciones, recomendaciones e historial de acciones.
- Autenticación con JWT HS256 y contraseñas con BCrypt.
- Persistencia en MySQL mediante JPA/Hibernate.
- Arquitectura hexagonal (puertos y adaptadores) en cuatro módulos Maven.
- Patrones tácticos DDD y patrones de diseño: State, Strategy + Composite, Observer, Command con undo, Factory, Repository, CQRS-lite y DTO Assemblers.
- Pruebas unitarias con JUnit 5 + Mockito.

---

## 2. Stack tecnológico

| Componente | Versión / tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Quarkus 3.8.2 |
| Build | Maven multi-módulo (Maven 3.9+) |
| Persistencia | Hibernate ORM / JPA + MySQL 8+ |
| Seguridad | JWT HS256 propio + BCrypt (`jbcrypt`) |
| Pruebas | JUnit 5, Mockito, Surefire |
| Índice de beans | Jandex + `beans.xml` |
| Contenedor de BD | Docker Compose (MySQL 8.4) |

---

## 3. Arquitectura

### 3.1 Módulos Maven

```
studymatch/
├── pom.xml                       # Parent POM: versiones y módulos
├── studymatch-domain/            # Núcleo: entidades, value objects, reglas, puertos
├── studymatch-application/       # Casos de uso: commands, handlers, queries, DTOs
├── studymatch-infrastructure/    # Adaptadores: JPA, BCrypt, JWT, notificaciones
├── studymatch-api/               # Adaptador de entrada: REST, seguridad, excepciones
└── sql/
    ├── schema.sql                # Esquema relacional MySQL
    └── seed.sql                  # Datos de demostración
```

Flujo de dependencias:

```
studymatch-api ──► studymatch-infrastructure ──► studymatch-application ──► studymatch-domain
       │                                                                          ▲
       └──────────────────────────────────────────────────────────────────────────┘
```

- **domain** no depende de Quarkus ni de infraestructura. Define agregados, value objects, reglas de negocio, eventos y puertos de repositorio/servicios.
- **application** orquesta casos de uso y depende solo de `domain`.
- **infrastructure** implementa los puertos con JPA, BCrypt, JWT, notificaciones y configuración.
- **api** expone los recursos JAX-RS, aplica seguridad JWT y traduce excepciones a respuestas HTTP.

### 3.2 Paquete base

```
co.ufps.studymatch
```

### 3.3 Patrones implementados

| Patrón | Dónde | Propósito |
|---|---|---|
| **Hexagonal / Ports & Adapters** | Módulos y paquetes | Aislar el dominio de MySQL, HTTP y JWT. |
| **DDD táctico** | `student`, `group`, `subject`, `notification`, `action` | Agregados, entidades, value objects y reglas invariantes. |
| **State** | `AvailabilityState`, `AvailableState`, `BusyState`, `InClassState`, `OfflineState` | Comportamiento de disponibilidad del estudiante. |
| **State / máquina de estados** | `GroupStatus` | Transiciones válidas de un grupo. |
| **Strategy** | `SubjectAffinityStrategy`, `ScheduleOverlapStrategy`, `CareerDiversityStrategy`, `AcademicLevelStrategy` | Cálculo intercambiable de afinidad. |
| **Composite** | `CompositeRecommendationStrategy` | Combina estrategias con pesos configurables. |
| **Observer** | `DomainEventPublisher`, `NotificationDispatcher`, `DatabaseNotificationObserver`, `ConsoleNotificationObserver` | Notificaciones desacopladas ante eventos de dominio. |
| **Command + undo** | `ReversibleCommand`, `CommandInvoker`, acciones en `application/action/commands` | Acciones reversibles con historial. |
| **Factory** | `AvailabilityStateFactory` | Creación del estado de disponibilidad. |
| **CQRS-lite** | Handlers de escritura y objetos `*Query` de lectura | Separar comandos de consultas sin sobreingeniería. |
| **Repository** | `IStudentRepository`, `IStudyGroupRepository`, etc. | Persistencia detrás de puertos del dominio. |
| **DTO Assembler** | `StudentDtoAssembler`, `GroupDtoAssembler` | Convertir dominio a DTOs de salida. |

### 3.4 Flujo típico de una petición

```
HTTP request
  → JwtAuthenticationFilter (valida Bearer token)
  → Resource JAX-RS (api)
  → Command/Query (application)
  → Handler + dominio (domain)
  → Repositorio (puerto)
  → Adaptador JPA (infrastructure)
  → MySQL
  → DTO de respuesta
```

Los errores de dominio se traducen a HTTP 400/401/403/404/409 mediante los mappers de `api/config`.

---

## 4. Modelo de dominio

### 4.1 Agregados y entidades

| Concepto | Descripción |
|---|---|
| **Student** | Estudiante: cuenta, perfil académico, disponibilidad, asignaturas y grupos activos. |
| **AcademicProfile** | Perfil académico: biografía, asignaturas (máximo 5) y horarios de disponibilidad. |
| **Subject** | Asignatura: código, nombre, área, créditos y semestre. |
| **StudyGroup** | Grupo de estudio: asignatura, creador, modalidad, capacidad, horario, miembros y solicitudes. |
| **GroupMembership** | Pertenencia de un estudiante a un grupo con rol `LEADER` o `MEMBER`. |
| **JoinRequest** | Solicitud de ingreso con estado `PENDING`, `ACCEPTED`, `REJECTED` o `CANCELLED`. |
| **Notification** | Notificación del estudiante, con tipo, mensaje, lectura y referencia relacionada. |
| **ActionHistoryEntry** | Registro persistido de acciones reversibles ejecutadas por un estudiante. |

### 4.2 Reglas destacadas

- Un estudiante puede tener como máximo **5 asignaturas activas** y **5 grupos activos**.
- Un grupo permite entre **2 y 50** miembros.
- Solo el líder puede aceptar, rechazar o consultar solicitudes.
- Un grupo solo acepta solicitudes en estado `OPEN`.
- No se permiten solicitudes duplicadas ni ingreso de miembros ya pertenecientes.
- Un grupo lleno pasa a `FULL`; al liberarse un cupo vuelve a `OPEN`.
- Las transiciones de estado del grupo son validadas por `GroupStatus`.

### 4.3 Estados de grupo

```
DRAFT → OPEN ⇄ FULL
DRAFT / OPEN / FULL → IN_PROGRESS → COMPLETED → ARCHIVED
DRAFT / OPEN / FULL → ARCHIVED
```

Reglas principales:

| Estado | Permite |
|---|---|
| `DRAFT` | Estado inicial de un grupo nuevo; puede publicarse (`OPEN`) o archivarse. |
| `OPEN` | Recibir solicitudes y pasar a `FULL`, `IN_PROGRESS` o `ARCHIVED`. |
| `FULL` | Volver a `OPEN`, iniciar (`IN_PROGRESS`) o archivar. |
| `IN_PROGRESS` | Completar (`COMPLETED`) o archivar. |
| `COMPLETED` | Archivar. |
| `ARCHIVED` | Estado final. |

### 4.4 Disponibilidad

Estados: `AVAILABLE`, `BUSY`, `IN_CLASS`, `OFFLINE`. El estado se representa con el patrón State y se crea mediante `AvailabilityStateFactory`. La disponibilidad se combina con los horarios de los grupos para calcular coincidencias.

### 4.5 Recomendaciones

Estrategias disponibles:

| Nombre | Descripción |
|---|---|
| `subject` | Asignaturas en común. |
| `schedule` | Coincidencia de horarios. |
| `career` | Diversidad de carreras. |
| `academic` | Proximidad de semestre. |
| `composite` | Promedio ponderado de las anteriores (estrategia por defecto). |

Pesos configurables por variables de entorno:

```
subject 0.35 · schedule 0.30 · career 0.20 · academic 0.15
```

Los workers en segundo plano (virtual threads) recalculan la matriz de afinidad y los top de recomendaciones en una caché concurrente en memoria. Los intervalos son configurables.

---

## 5. Requisitos

- JDK 21.
- Maven 3.9 o superior.
- MySQL 8 o superior (o Docker para levantar MySQL 8.4).
- Opcional: Docker + Docker Compose.

---

## 6. Puesta en marcha

### 6.1 Base de datos con Docker

```bash
docker compose up -d
```

Esto levanta MySQL 8.4 en `localhost:3306`, crea la base `studymatch` y ejecuta automáticamente `sql/schema.sql` y `sql/seed.sql`.

### 6.2 Base de datos manual

```bash
mysql -u root -p -e "CREATE DATABASE studymatch CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p studymatch < sql/schema.sql
mysql -u root -p studymatch < sql/seed.sql
```

### 6.3 Compilar y ejecutar

```bash
# Compilar todos los módulos y ejecutar pruebas
mvn clean test

# Empaquetar
mvn clean package

# Ejecutar la API
java -jar studymatch-api/target/quarkus-app/quarkus-run.jar
```

La API queda disponible en `http://localhost:8080`.

### 6.4 Modo desarrollo con recarga

```bash
# Instalar módulos en el repositorio local
mvn clean install -DskipTests

# Ejecutar Quarkus en modo dev
mvn -f studymatch-api/pom.xml quarkus:dev
```

### 6.5 Perfil de producción

Al empaquetar con Maven, Quarkus usa el perfil `prod` por defecto. En ese perfil Hibernate no modifica el esquema (`quarkus.hibernate-orm.database.generation=none`), por lo que la base debe estar creada con `sql/schema.sql` antes de arrancar.

```bash
java -jar studymatch-api/target/quarkus-app/quarkus-run.jar
```

La configuración de ejecución se ajusta con variables de entorno (ver sección 7).

### 6.6 Usuarios de demostración

Todas las cuentas del seed usan la contraseña **`StudyMatch123`**:

| Correo | Nombre | Carrera |
|---|---|---|
| `laura@ufps.edu.co` | Laura Gómez | Ingeniería de Sistemas |
| `carlos@ufps.edu.co` | Carlos Pérez | Ingeniería de Sistemas |
| `valentina@ufps.edu.co` | Valentina Rodríguez | Ingeniería Industrial |
| `andres@ufps.edu.co` | Andrés Martínez | Ingeniería Electrónica |
| `daniela@ufps.edu.co` | Daniela Sánchez | Ingeniería de Sistemas |
| `sofia@ufps.edu.co` | Sofía Ramírez | Ingeniería de Sistemas |

---

## 7. Configuración

Las credenciales y secretos se inyectan por variables de entorno. Los valores por defecto son solo para desarrollo local.

| Variable | Default | Descripción |
|---|---|---|
| `PORT` | `8080` | Puerto HTTP. |
| `DB_URL` | `jdbc:mysql://localhost:3306/studymatch?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | URL JDBC. |
| `DB_USER` | `studymatch` | Usuario de MySQL. |
| `DB_PASSWORD` | `studymatch` | Contraseña de MySQL. |
| `CORS_ORIGINS` | `*` | Orígenes permitidos, separados por coma. |
| `JWT_SECRET` | secreto de desarrollo | Clave HS256; **obligatorio cambiarla en producción**. |
| `JWT_ISSUER` | `studymatch` | Emisor del token. |
| `JWT_TTL_SECONDS` | `86400` | Vigencia del token en segundos (24 h). |
| `WEIGHT_SUBJECT` | `0.35` | Peso de asignaturas en la estrategia compuesta. |
| `WEIGHT_SCHEDULE` | `0.30` | Peso de horarios. |
| `WEIGHT_CAREER` | `0.20` | Peso de diversidad de carreras. |
| `WEIGHT_ACADEMIC` | `0.15` | Peso de nivel académico. |
| `WORKER_AFFINITY_SECONDS` | `60` | Intervalo del worker de afinidad. |
| `WORKER_RECOMMENDATIONS_SECONDS` | `90` | Intervalo del worker de recomendaciones. |

Los pesos deben sumar `1.0`.

---

## 8. API REST

- Base URL: `http://localhost:8080/api/v1`
- Formato: JSON (`application/json`).
- Rutas protegidas: enviar `Authorization: Bearer <token>`.
- El token se obtiene con `POST /auth/login`.

### 8.1 Rutas públicas

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Registrar estudiante. |
| `POST` | `/auth/login` | Iniciar sesión y obtener JWT. |
| `GET` | `/subjects` | Listar/buscar asignaturas. |
| `GET` | `/recommendations/strategies` | Listar estrategias de recomendación. |
| `GET` | `/meta/options` | Catálogos y opciones para formularios. |
| `GET` | `/health` | Estado del servicio. |

### 8.2 Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/register` | Crea la cuenta, perfil y disponibilidad inicial; devuelve JWT y estudiante. |
| `POST` | `/auth/login` | Valida credenciales y devuelve JWT, expiración y estudiante. |

Ejemplo de login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"laura@ufps.edu.co","password":"StudyMatch123"}'
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresAt": "2026-11-10T18:00:00Z",
  "student": {
    "studentId": "22222222-2222-2222-2222-222222222201",
    "email": "laura@ufps.edu.co",
    "fullName": "Laura Gómez",
    "career": "Ingeniería de Sistemas",
    "semester": 5,
    "status": "ACTIVE"
  }
}
```

Ejemplo de registro:

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "email": "nuevo@ufps.edu.co",
    "password": "StudyMatch123",
    "fullName": "Nuevo Estudiante",
    "career": "Ingeniería de Sistemas",
    "semester": 3,
    "bio": "Buscando grupo de estructuras de datos",
    "subjectIds": ["11111111-1111-1111-1111-111111111105"],
    "availabilityStatus": "AVAILABLE",
    "availabilitySlots": [
      {"day": "MONDAY", "startTime": "14:00", "endTime": "16:00"}
    ]
  }'
```

### 8.3 Estudiantes

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/students/me` | Perfil completo del estudiante autenticado. |
| `GET` | `/students/me/dashboard` | Dashboard: grupos activos, recomendaciones, notificaciones y solicitudes pendientes. |
| `GET` | `/students/me/groups` | Grupos del estudiante autenticado. |
| `PUT` | `/students/me/profile` | Actualizar carrera, semestre, biografía y asignaturas. |
| `PUT` | `/students/me/availability` | Cambiar disponibilidad y horarios. |
| `GET` | `/students/{studentId}` | Consultar un estudiante por ID. |
| `GET` | `/students/{studentId}/groups` | Grupos de un estudiante (solo el propio). |
| `PUT` | `/students/{studentId}/profile` | Actualizar perfil (solo el propio). |
| `PUT` | `/students/{studentId}/availability` | Actualizar disponibilidad (solo el propio). |

Ejemplo de actualización de disponibilidad:

```bash
curl -X PUT http://localhost:8080/api/v1/students/me/availability \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "availabilityStatus": "BUSY",
    "availabilitySlots": [
      {"day": "MONDAY", "startTime": "14:00", "endTime": "16:00"},
      {"day": "WEDNESDAY", "startTime": "14:00", "endTime": "16:00"}
    ]
  }'
```

Valores válidos de `availabilityStatus`: `AVAILABLE`, `BUSY`, `IN_CLASS`, `OFFLINE`.
Valores válidos de `day`: `MONDAY` … `SUNDAY`. Formato de horas: `HH:mm` o `HH:mm:ss`.

### 8.4 Asignaturas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/subjects?q=` | Lista todas las asignaturas o filtra por texto. |

### 8.5 Grupos de estudio

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/groups` | Crear grupo (el creador queda como `LEADER`). |
| `GET` | `/groups` | Buscar grupos con filtros y paginación. |
| `GET` | `/groups/{groupId}` | Detalle del grupo, miembros y solicitudes visibles. |
| `PUT` | `/groups/{groupId}/status` | Cambiar estado del grupo (solo líder). |
| `GET` | `/groups/{groupId}/requests` | Listar solicitudes (solo líder). |
| `POST` | `/groups/{groupId}/requests` | Enviar solicitud de ingreso. |
| `POST` | `/groups/{groupId}/requests/{requestId}/accept` | Aceptar solicitud (solo líder). |
| `POST` | `/groups/{groupId}/requests/{requestId}/reject` | Rechazar solicitud (solo líder). |
| `DELETE` | `/groups/{groupId}/requests/{requestId}` | Cancelar solicitud propia. |
| `DELETE` | `/groups/{groupId}/members/{studentId}` | Salir del grupo (solo el propio estudiante). |

Parámetros de búsqueda de grupos:

| Parámetro | Descripción |
|---|---|
| `subject` o `subjectId` | ID (UUID) de asignatura. |
| `status` | Estado del grupo (por defecto `OPEN`; `DRAFT` no se lista). |
| `q` | Texto libre en nombre o descripción. |
| `modality` | `IN_PERSON`, `VIRTUAL` o `HYBRID`. |
| `page` | Página (desde 1; por defecto 1). |
| `size` | Tamaño (1–50; por defecto 10). |

Ejemplo de creación:

```bash
curl -X POST http://localhost:8080/api/v1/groups \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{
    "subjectId": "11111111-1111-1111-1111-111111111105",
    "name": "Algoritmos en equipo",
    "description": "Talleres y repaso para el parcial",
    "maxMembers": 5,
    "modality": "HYBRID",
    "schedule": [
      {"day": "MONDAY", "startTime": "14:00", "endTime": "16:00"}
    ]
  }'
```

Ejemplo de solicitud de ingreso:

```bash
curl -X POST http://localhost:8080/api/v1/groups/66666666-6666-6666-6666-666666666301/requests \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"message":"Quiero preparar el parcial con ustedes"}'
```

Ejemplo de cambio de estado:

```bash
curl -X PUT http://localhost:8080/api/v1/groups/66666666-6666-6666-6666-666666666301/status \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"status":"OPEN"}'
```

### 8.6 Recomendaciones

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/recommendations/students` | Estudiantes recomendados para el estudiante autenticado. |
| `GET` | `/recommendations/groups` | Grupos recomendados. |
| `GET` | `/recommendations/strategies` | Estrategias disponibles y pesos (público). |
| `GET` | `/recommendations/cache` | Estado de la caché de recomendaciones. |

Parámetros:

| Parámetro | Descripción |
|---|---|
| `studentId` | Opcional; solo se permite el propio estudiante. |
| `strategy` | `subject`, `schedule`, `career`, `academic` o `composite` (por defecto). |
| `limit` | Cantidad máxima (por defecto 10, máximo 50). |

Ejemplo:

```bash
curl "http://localhost:8080/api/v1/recommendations/groups?strategy=composite&limit=5" \
  -H "Authorization: Bearer $TOKEN"
```

Cada resultado incluye `affinityScore` y `reasons` con la explicación del puntaje.

### 8.7 Notificaciones

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/notifications` | Listar notificaciones; `unreadOnly=true` filtra no leídas. |
| `GET` | `/notifications/unread-count` | Cantidad de notificaciones no leídas. |
| `PUT` | `/notifications/{notificationId}/read` | Marcar una notificación como leída. |
| `POST` | `/notifications/read-all` | Marcar todas como leídas. |

Las notificaciones se generan automáticamente por eventos: registro, creación de grupo, solicitud enviada/aceptada/rechazada, cambio de disponibilidad y salida de un grupo.

### 8.8 Historial de acciones y undo

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/actions/history` | Historial de acciones del estudiante autenticado. |
| `POST` | `/actions/{actionId}/undo` | Deshacer una acción reversible. |

Tipos de acción disponibles: `CHANGE_AVAILABILITY`, `SEND_JOIN_REQUEST`, `CANCEL_JOIN_REQUEST`, `ACCEPT_JOIN_REQUEST`, `REJECT_JOIN_REQUEST`, `LEAVE_GROUP`.

Ejemplo:

```bash
# Consultar historial
curl http://localhost:8080/api/v1/actions/history \
  -H "Authorization: Bearer $TOKEN"

# Deshacer la acción indicada
curl -X POST http://localhost:8080/api/v1/actions/<actionId>/undo \
  -H "Authorization: Bearer $TOKEN"
```

> **Importante:** las acciones reversibles se mantienen en una pila en memoria por estudiante. El historial se persiste en `action_history`, pero solo se puede deshacer una acción ejecutada en la **misma ejecución del servidor**. Las acciones del seed aparecen con `canUndo=false`.

### 8.9 Metadatos y salud

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/meta/options` | Días, modalidades, estados, transiciones, tipos de notificación, áreas, asignaturas y estrategias. |
| `GET` | `/health` | Estado, versión y hora de inicio del servicio. |

### 8.10 Formato de error

```json
{
  "timestamp": "2026-11-09T18:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "El estudiante ya pertenece al grupo",
  "path": "api/v1/groups/.../requests"
}
```

Códigos usados:

| Código | Situación |
|---|---|
| `400` | Validación o transición inválida. |
| `401` | Token faltante, inválido o expirado; credenciales incorrectas. |
| `403` | Operación reservada al líder u otro estudiante. |
| `404` | Estudiante, grupo, solicitud, notificación o acción no encontrada. |
| `409` | Conflicto: correo duplicado, miembro duplicado, grupo lleno, etc. |
| `500` | Error no controlado. |

---

## 9. Base de datos

### 9.1 Tablas

| Tabla | Contenido |
|---|---|
| `subjects` | Asignaturas. |
| `students` | Cuentas de estudiante. |
| `academic_profiles` | Perfil académico y estado de disponibilidad. |
| `availability_slots` | Horarios de disponibilidad del estudiante. |
| `student_subjects` | Asignaturas del estudiante. |
| `study_groups` | Grupos de estudio. |
| `group_members` | Miembros y roles del grupo. |
| `group_schedule` | Horarios del grupo. |
| `join_requests` | Solicitudes de ingreso. |
| `notifications` | Notificaciones. |
| `action_history` | Auditoría de acciones reversibles. |

### 9.2 Persistencia

- Entidades JPA planas con columnas de clave foránea explícitas.
- Repositorios con JPQL y mapeo manual hacia el dominio.
- `flush()` + `clear()` después de escrituras para evitar lecturas obsoletas de la caché de primer nivel.
- En desarrollo, Hibernate puede ajustar el esquema (`update`); en producción se usa `sql/schema.sql` (`none`).

---

## 10. Pruebas

```bash
mvn test
```

Cobertura actual:

- **Dominio:** reglas de `Student`, `StudyGroup` y estrategias de recomendación.
- **Aplicación:** registro, login, envío/aceptación de solicitudes y `CommandInvoker` (undo).
- **Infraestructura:** `NotificationDispatcher` y `HmacTokenIssuer`.

Resultado de referencia: **38 pruebas, 0 fallos**.

Pruebas manuales realizadas contra MySQL:

- Login y consulta de perfil.
- Cambio de disponibilidad y undo (respuesta y BD consistentes).
- Aceptación de solicitud y undo (solicitud vuelve a `PENDING` y se retira el miembro).
- Búsqueda de grupos, dashboard, notificaciones y recomendaciones.

---

## 11. Estructura de paquetes

```
co.ufps.studymatch
├── student/              # Student, AcademicProfile, AvailabilityState, repositorios
├── group/                # StudyGroup, GroupMembership, JoinRequest, GroupStatus
├── subject/              # Subject y su repositorio
├── notification/         # Notification, NotificationType, observadores
├── action/               # ActionHistoryEntry y repositorio
├── recommendation/       # Estrategias, contexto y puntajes
├── shared/               # Eventos de dominio y publicador
├── application/
│   ├── auth/             # Registro y login
│   ├── student/          # Perfil, disponibilidad, dashboard
│   ├── group/            # Casos de uso de grupos y solicitudes
│   ├── notification/     # Consultas y marcado de notificaciones
│   ├── recommendation/   # Servicio, caché, workers y consultas
│   ├── action/           # CommandInvoker, historial y comandos reversibles
│   └── common/           # DTOs compartidos y paginación
├── infrastructure/
│   ├── persistence/      # Adaptadores JPA por agregado
│   ├── security/         # BCrypt y JWT HS256
│   ├── messaging/        # Dispatcher y observadores de notificaciones
│   └── config/           # Productor de pesos de recomendación
└── api/
    ├── auth/             # /auth
    ├── student/          # /students
    ├── group/            # /groups
    ├── subject/          # /subjects
    ├── recommendation/   # /recommendations
    ├── notification/     # /notifications
    ├── action/           # /actions
    ├── meta/             # /meta
    ├── health/           # /health
    ├── security/         # Filtro JWT y SecurityContext
    └── config/           # Mappers de excepciones y ApiError
```

---

## 12. Decisiones y limitaciones

- **JWT propio:** se implementa HS256 manualmente para mantener el stack sin proveedor de identidad externo. El secreto debe configurarse por entorno.
- **BCrypt:** las contraseñas se almacenan con hash BCrypt; nunca en texto plano.
- **Undo en memoria:** la pila de comandos es por estudiante y por ejecución del servidor. El historial persistido sirve de auditoría, pero no permite deshacer acciones de una ejecución anterior.
- **Caché de recomendaciones en memoria:** los workers usan virtual threads y una caché concurrente; no se comparte entre instancias. Para escalar horizontalmente habría que sustituirla por una caché distribuida.
- **Sin frontend:** el proyecto entrega exclusivamente la API y los scripts de base de datos.
- **Docker Compose provisto:** `docker-compose.yml` levanta MySQL 8.4 con esquema y seed; si no se usa Docker, se pueden ejecutar los scripts SQL manualmente.

---

## 13. Comandos rápidos

```bash
# Levantar MySQL
docker compose up -d

# Compilar y probar
mvn clean test

# Empaquetar y ejecutar
mvn clean package
java -jar studymatch-api/target/quarkus-app/quarkus-run.jar

# Login de demostración
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"laura@ufps.edu.co","password":"StudyMatch123"}'

# Health
curl http://localhost:8080/api/v1/health
```
