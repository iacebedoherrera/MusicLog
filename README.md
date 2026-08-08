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

Este proyecto usa **Spec Kit + Codex** para desarrollar funcionalidades de forma guiada por especificaciones.

La regla principal es:

> No se implementa una funcionalidad hasta que exista una especificación,
> un plan técnico, tareas accionables, una revisión técnica independiente
> aprobada y una aprobación humana explícita.

La constitución del proyecto está en:

```text
.specify/memory/constitution.md
```

Debe respetarse durante todas las fases.

### Flujo resumido

```text
Rama de funcionalidad
  -> Especificación
  -> Aclaraciones
  -> Plan técnico
  -> Revisión independiente del plan
  -> Tareas
  -> Análisis de coherencia
  -> Aprobación humana
  -> Implementación
  -> Tests, revisión final y PR
```

---

### 0. Crear una rama de funcionalidad

Partir siempre de `develop` actualizado:

```bash
git switch develop
git pull --ff-only
git switch -c feature/nombre-corto
```

Comprobar que el árbol de trabajo está limpio:

```bash
git status --short
```

---

### 1. Crear la especificación

Abrir Codex desde la raíz del repositorio:

```bash
codex
```

Crear la funcionalidad con:

```text
$speckit-specify
```

En esta fase se debe describir:

- Qué problema resuelve la funcionalidad.
- Para quién está pensada.
- Criterios de aceptación verificables.
- Casos de error relevantes.
- Restricciones de producto.
- Límites explícitos de alcance.

No se deben decidir todavía clases, tablas, controladores, dependencias ni detalles de implementación.

Spec Kit creará una carpeta similar a:

```text
specs/00N-nombre-funcionalidad/
└── spec.md
```

---

### 2. Aclarar ambigüedades

Ejecutar:

```text
$speckit-clarify
```

Responder únicamente las preguntas que afecten materialmente a:

- Comportamiento funcional.
- Seguridad o autorización.
- Contratos de API.
- Persistencia o migraciones.
- Compatibilidad.
- Experiencia de usuario.
- Límites de alcance.

Si una decisión cambia qué debe hacer el producto, debe quedar registrada en `spec.md`.

No continuar mientras haya ambigüedades materiales sin resolver.

---

### 3. Crear el plan técnico

Cuando `spec.md` esté clara, ejecutar:

```text
$speckit-plan
```

El plan debe inspeccionar el repositorio y generar, normalmente:

```text
specs/00N-nombre-funcionalidad/
├── plan.md
├── research.md
├── data-model.md
├── contracts/
├── quickstart.md
└── checklists/
```

El plan debe incluir como mínimo:

- Módulos y archivos afectados.
- Decisiones de arquitectura.
- Compatibilidad de API y datos.
- Seguridad, autorización y validación.
- Persistencia, eventos y migraciones cuando apliquen.
- Dependencias nuevas, si son imprescindibles.
- Estrategia de pruebas.
- Comandos de validación.
- Riesgos y elementos deliberadamente fuera de alcance.

No ejecutar todavía `$speckit-tasks` ni `$speckit-implement`.

---

### 4. Revisar el plan con un segundo agente

Ejecutar el revisor independiente desde la raíz del repositorio:

```bash
./tools/review-plan.sh specs/00N-nombre-funcionalidad
REVIEW_STATUS=$?

echo "Review exit code: $REVIEW_STATUS"
grep -E '^VERDICT: ' specs/00N-nombre-funcionalidad/review.md
```

El script ejecuta un segundo proceso de Codex en modo de solo lectura y crea:

```text
specs/00N-nombre-funcionalidad/review.md
```

Los posibles veredictos son:

| Veredicto | Significado | Acción |
|---|---|---|
| `APPROVED` | El plan no tiene bloqueantes | Continuar a tareas |
| `CHANGES_REQUIRED` | El plan tiene hallazgos bloqueantes | Actualizar el plan y repetir la revisión |
| `HUMAN_DECISION_REQUIRED` | Falta una decisión de producto, seguridad, compatibilidad o arquitectura | Detenerse y decidir antes de continuar |

El script devuelve:

```text
0   -> APPROVED
10  -> CHANGES_REQUIRED o HUMAN_DECISION_REQUIRED
```

> Aunque Codex sugiera ejecutar `$speckit-tasks`, no se debe hacer hasta que
> `review.md` contenga `VERDICT: APPROVED`.

---

### 5. Resolver hallazgos del revisor

Si la revisión devuelve `CHANGES_REQUIRED`:

1. Abrir Codex.
2. Pedir que actualice los artefactos de planificación afectados.
3. No implementar código.
4. No generar tareas todavía.
5. Ejecutar de nuevo el revisor.

Ejemplo:

```text
Actualiza el plan existente de la funcionalidad activa.

Lee y resuelve todos los hallazgos de:
`specs/00N-nombre-funcionalidad/review.md`

Actualiza únicamente los artefactos de planificación necesarios.
No implementes código ni generes tareas todavía.
```

Después, repetir:

```bash
./tools/review-plan.sh specs/00N-nombre-funcionalidad
```

No continuar hasta obtener:

```text
VERDICT: APPROVED
```

---

### 6. Generar tareas

Con el plan aprobado, ejecutar:

```text
$speckit-tasks
```

Esto crea:

```text
specs/00N-nombre-funcionalidad/tasks.md
```

Las tareas deben:

- Tener identificadores únicos.
- Indicar archivos concretos.
- Respetar el orden de dependencias.
- Incluir pruebas antes o junto a la implementación.
- Incluir documentación de contratos cuando cambie una API.
- Incluir los comandos de validación requeridos.
- Mantener explícitamente fuera de alcance los cambios no aprobados.

Revisar el archivo generado:

```bash
sed -n '1,360p' specs/00N-nombre-funcionalidad/tasks.md
```

---

### 7. Analizar coherencia de artefactos

Ejecutar:

```text
$speckit-analyze
```

Este paso comprueba coherencia entre:

```text
spec.md
plan.md
tasks.md
research.md
data-model.md
contracts/
quickstart.md
.specify/memory/constitution.md
```

El análisis debe detectar, entre otros:

- Requisitos sin tareas.
- Tareas sin requisito.
- Conflictos entre contrato, plan y tareas.
- Incumplimientos de la constitución.
- Ambigüedades pendientes.
- Cobertura insuficiente de pruebas.
- Cambios de API sin documentación.
- Cambios protegidos que no siguen los patrones de autenticación existentes.

Si el análisis detecta problemas:

- Si cambia el plan, repetir la revisión independiente.
- Si cambian las tareas, volver a ejecutar `$speckit-analyze`.
- No implementar hasta que no haya problemas críticos.

---

### 8. Aprobar la implementación

Antes de permitir cambios de código, revisar:

```bash
git status --short
find specs/00N-nombre-funcionalidad -maxdepth 3 -type f | sort
sed -n '1,320p' specs/00N-nombre-funcionalidad/spec.md
sed -n '1,360p' specs/00N-nombre-funcionalidad/plan.md
sed -n '1,360p' specs/00N-nombre-funcionalidad/tasks.md
sed -n '1,260p' specs/00N-nombre-funcionalidad/review.md
```

La implementación requiere una aprobación humana explícita:

```text
Apruebo implementar 00N-nombre-funcionalidad según spec.md, plan.md,
tasks.md y review.md aprobados.
```

Requieren especial revisión humana previa:

- Cambios de autenticación, permisos o criptografía.
- Migraciones de base de datos.
- Eliminación o modificación incompatible de contratos.
- Nuevas dependencias.
- Secretos, `.env`, Docker, CI/CD o infraestructura.
- Integraciones externas, pagos o datos sensibles.

---

### 9. Implementar

Solo después de aprobar explícitamente:

```text
$speckit-implement
```

Codex debe implementar exclusivamente las tareas aprobadas.

Al terminar debe informar de:

- Archivos modificados.
- Tareas completadas.
- Pruebas y comandos ejecutados.
- Resultado de cada validación.
- Limitaciones o riesgos pendientes.
- Cambios que no se han realizado por estar fuera de alcance.

---

### 10. Validar la implementación

Revisar el diff:

```bash
git status
git diff
```

Ejecutar los comandos exigidos por `tasks.md`, `plan.md` y la constitución.

Como mínimo, para cambios backend:

```bash
mvn test
```

Si se ha modificado `frontend/`, incluidos contratos bajo `frontend/docs/`:

```bash
npm --prefix frontend run typecheck
npm --prefix frontend run lint
npm --prefix frontend run test
npm --prefix frontend run build
```

Revisar además que no se hayan modificado archivos fuera de alcance:

```bash
git diff --name-only
```

Comprobar especialmente que no existan cambios inesperados en:

```text
SecurityConfig.java
.env
Docker
CI/CD
Flyway
Redis
pom.xml
package.json
infraestructura
```

---

### 11. Cerrar la funcionalidad

Versionar los artefactos y la implementación:

```bash
git add specs/ src/ frontend/  # Ajustar a los archivos reales modificados
git status
git diff --cached
git commit -m "feat: add nombre de funcionalidad"
```

Abrir un PR desde la rama de funcionalidad hacia `develop`.

El PR debe incluir:

- Objetivo de la funcionalidad.
- Enlace o ruta a `spec.md`.
- Resumen de la solución.
- Archivos o módulos modificados.
- Compatibilidad de API y datos.
- Migraciones, si existen.
- Pruebas ejecutadas y sus resultados.
- Riesgos, limitaciones o comprobaciones pendientes.
- Confirmación de que `review.md` fue aprobado.

## Licencia

Este proyecto no incluye una licencia todavía.
