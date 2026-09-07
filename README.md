# MusicLog

Backend de una aplicación social para registrar, valorar y descubrir música, inspirada en Letterboxd. Permite gestionar perfiles, seguir a otros usuarios, consultar el catálogo musical, publicar reseñas y mantener un historial de escuchas.

## Tecnologías

- Java 21 y Spring Boot 3.3
- Spring Modulith para organizar el monolito modular
- PostgreSQL 16 + Flyway para persistencia y migraciones
- Redis 7 para caché y proyecciones de lectura del feed
- Spring Security con JWT
- MusicBrainz y Cover Art Archive para el catálogo
- Spotify OAuth 2.0 para sincronizar artistas y pistas más escuchadas
- OpenAPI/Swagger UI para explorar la API

## Módulos

| Módulo | Responsabilidad |
| --- | --- |
| `user` | Registro, autenticación JWT, perfiles y seguimientos. |
| `catalog` | Búsqueda y detalle de artistas, álbumes y pistas mediante MusicBrainz; usa caché local. |
| `review` | Reseñas con puntuación de 1 a 10 y listening log manual. |
| `social` | Feed de actividad, actividad pública y likes de reseñas. Mantiene una proyección de lectura en Redis. |
| `spotifyconnector` | Conexión OAuth con Spotify, cifrado de tokens y sincronización periódica de top artists/tracks. |
| `shared` | Seguridad, configuración de clientes, OpenAPI y tratamiento común de errores. |

Los módulos se comunican mediante eventos de aplicación, aceptando consistencia eventual donde corresponde. El módulo social aplica un CQRS ligero: PostgreSQL para escritura y Redis para la lectura del feed.

## Requisitos

- JDK 21
- Maven 3.9 o superior
- Docker y Docker Compose
- Una aplicación de Spotify Developer (solo para la integración con Spotify)

## Arranque local

1. Inicia PostgreSQL y Redis:

   ```bash
   docker compose up -d
   ```

2. Configura las variables de entorno necesarias. Para desarrollo básico, las credenciales de base de datos ya tienen valores por defecto. Genera un secreto JWT seguro antes de desplegar fuera de local.

   ```bash
   export JWT_SECRET='un-secreto-largo-y-aleatorio-de-al-menos-32-caracteres'
   export SPOTIFY_CLIENT_ID='tu-client-id'
   export SPOTIFY_CLIENT_SECRET='tu-client-secret'
   export SPOTIFY_TOKEN_ENCRYPTION_KEY='una-clave-local-segura-de-32-bytes'
   ```

3. Ejecuta la aplicación:

   ```bash
   mvn spring-boot:run
   ```

La API quedará disponible en `http://localhost:8080`. Flyway aplicará las migraciones automáticamente al arrancar.

Para detener la infraestructura:

```bash
docker compose down
```

> `docker compose down -v` también elimina los datos locales de PostgreSQL y Redis.

## Configuración

| Variable | Valor por defecto | Descripción |
| --- | --- | --- |
| `PORT` | `8080` | Puerto HTTP de la aplicación. |
| `DB_URL` | `jdbc:postgresql://localhost:5432/musiclog` | URL de PostgreSQL. |
| `DB_USERNAME` / `DB_PASSWORD` | `musiclog` / `musiclog` | Credenciales de PostgreSQL local. |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Conexión a Redis. |
| `JWT_SECRET` | — | Secreto para firmar tokens JWT. Obligatorio con un valor seguro fuera de desarrollo. |
| `JWT_EXPIRATION` | `PT24H` | Duración del JWT en formato ISO-8601. |
| `MUSICBRAINZ_USER_AGENT` | `MusicLog/0.1 (contacto@example.com)` | User-Agent requerido por MusicBrainz; sustituye el contacto. |
| `SPOTIFY_CLIENT_ID` / `SPOTIFY_CLIENT_SECRET` | — | Credenciales de tu aplicación de Spotify. |
| `SPOTIFY_REDIRECT_URI` | `http://localhost:8080/api/spotify/callback` | Callback registrado en Spotify. |
| `SPOTIFY_TOKEN_ENCRYPTION_KEY` | — | Clave para cifrar los tokens de Spotify en reposo. |
| `SPOTIFY_FRONTEND_REDIRECT_URI` | `http://localhost:3000/settings/integrations` | Destino tras completar la autorización. |

No incluyas secretos reales en el repositorio ni uses los valores por defecto de JWT o cifrado en producción.

## Documentación de la API

Con la aplicación en ejecución:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Especificación OpenAPI: <http://localhost:8080/v3/api-docs>

Las rutas protegidas requieren la cabecera:

```http
Authorization: Bearer <token>
```

### Rutas principales

| Área | Endpoints destacados |
| --- | --- |
| Autenticación | `POST /api/auth/register`, `POST /api/auth/login` |
| Usuarios | `GET /api/users/{username}`, `PUT /api/users/me`, `POST/DELETE /api/users/{username}/follow` |
| Catálogo | `GET /api/catalog/search?q={texto}&type={artist|album|track}`, `GET /api/catalog/artists/{mbid}`, `GET /api/catalog/albums/{mbid}` |
| Reseñas | `POST /api/reviews`, `GET/PUT/DELETE /api/reviews/{id}`, `GET /api/reviews/me` |
| Listening log | `POST /api/listening-log`, `GET /api/listening-log/me` |
| Social | `GET /api/feed`, `POST/DELETE /api/reviews/{id}/like` |
| Spotify | `GET /api/spotify/connect`, `GET /api/spotify/status`, `POST /api/spotify/sync` |

Ejemplo de registro:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "ana",
    "email": "ana@example.com",
    "password": "contraseña-segura",
    "displayName": "Ana"
  }'
```

Ejemplo de creación de una reseña autenticada:

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{
    "targetMbid": "mbid-del-album-o-artista",
    "targetType": "ALBUM",
    "rating": 9,
    "reviewText": "Un disco excelente.",
    "containsSpoilers": false
  }'
```

## Integraciones externas

MusicBrainz es la fuente de catálogo. Respeta su límite de una petición por segundo y configura un `MUSICBRAINZ_USER_AGENT` identificable con un contacto real. Cover Art Archive aporta las portadas de álbumes.

Spotify se usa únicamente para OAuth y para sincronizar el contenido reciente y los artistas/pistas más escuchados; no es la fuente principal del catálogo. Registra exactamente la URL de callback configurada en `SPOTIFY_REDIRECT_URI` en el panel de Spotify Developer.

## Pruebas

La suite usa H2 en memoria y no necesita levantar Docker:

```bash
mvn test
```

Incluye pruebas unitarias, de integración de módulos y una verificación de la estructura de Spring Modulith.

## Estructura

```text
src/main/java/com/musiclog/
├── user/
├── catalog/
├── review/
├── social/
├── spotifyconnector/
└── shared/

src/main/resources/
├── application.yml
└── db/migration/
```

## Flujo de desarrollo asistido por IA

MusicLog usa `ai-flow` para coordinar cambios OpenSpec desde la propuesta hasta
la aplicación y la verificación. La unidad de trabajo es un cambio bajo
`openspec/changes/<change-name>/`; la guía completa está en
[`docs/ai-workflow.md`](docs/ai-workflow.md).

La regla principal es:

> No se implementa una funcionalidad hasta que la propuesta, los deltas de
> especificación que procedan, el diseño y las tareas estén completos, la
> revisión técnica independiente esté aprobada y exista aprobación humana
> explícita.

La referencia normativa del proyecto es la
[`constitución de desarrollo`](docs/development-constitution.md).

### Flujo resumido

```text
Rama dedicada
  -> ai-flow start
  -> proposal.md
  -> Deltas de especificación, si aplican
  -> design.md
  -> Revisión técnica independiente
  -> tasks.md
  -> Aprobación humana
  -> ai-flow resume / OpenSpec apply
  -> Tests y validación
  -> PR
```

### Iniciar un cambio

Partir de `develop` actualizado y crear una ejecución con `ai-flow`:

```bash
git switch develop
git pull --ff-only
ai-flow start "Descripción breve del cambio" --branch-name ai-flow/nombre-cambio
ai-flow status <run-id>
```

Guardar el `run-id` para las operaciones de aprobación, reanudación o
cancelación. Los artefactos se mantienen en:

```text
openspec/changes/<change-name>/
├── proposal.md
├── specs/       # solo si cambia un requisito o contrato observable
├── design.md
└── tasks.md
```

Para un cambio de documentación, tooling o limpieza sin cambio de
comportamiento, declarar `skip_specs: true` en `.openspec.yaml` y no crear un
delta artificial.

### Revisar, aprobar y aplicar

Completar `proposal.md`, los deltas aplicables, `design.md` y `tasks.md`. Antes
de aplicar, ejecutar la revisión independiente:

```bash
./tools/review-plan.sh openspec/changes/<change-name>
grep -E '^VERDICT: ' openspec/changes/<change-name>/review.md
```

Resolver todos los bloqueantes y repetir la revisión hasta obtener
`VERDICT: APPROVED`. Comprobar después el estado OpenSpec:

```bash
openspec status --change <change-name> --json
ai-flow approve <run-id>
ai-flow resume <run-id>
```

`ai-flow resume` ejecuta `apply` para la ejecución aprobada. La aplicación solo
debe ejecutar las tareas aprobadas; las instrucciones de la operación se
pueden inspeccionar con:

```bash
openspec instructions apply --change <change-name> --json
```

### Validar y cerrar

Ejecutar la validación del cambio, las pruebas exigidas por la constitución y
los controles específicos de los artefactos:

```bash
openspec validate "<change-name>" --type change --no-interactive
mvn test
```

Si se modifica `frontend/`, ejecutar también desde ese directorio:

```bash
npm run typecheck
npm run lint
npm run test
npm run build
```

Antes del PR, revisar `git diff --check`, la lista de archivos modificados,
los resultados de cada control y los riesgos pendientes. No modificar secretos,
`.env`, Docker, CI/CD o infraestructura de producción fuera de las aprobaciones
exigidas por la constitución.

## Licencia

Este proyecto no incluye una licencia todavía.
