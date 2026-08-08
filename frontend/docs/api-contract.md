# Contrato de API consumido por el frontend

La especificación viva es `GET /v3/api-docs` y se puede explorar en
`/swagger-ui.html` cuando el backend está en marcha. Este documento fija el contrato
que usa el primer vertical web y recoge los cambios mínimos necesarios para una UI
correcta.

## Convenciones transversales

- La URL base de producción es relativa: `/api`. El frontend y la API se sirven bajo
  el mismo dominio mediante un reverse proxy. El proxy de Vite solo existe para el
  desarrollo local.
- Las rutas protegidas reciben `Authorization: Bearer <JWT>`.
- Los errores siguen `ApiError`: `timestamp`, `status`, `error`, `message`, `path` y
  `fieldErrors`. El cliente expone esos datos mediante `ApiClientError`.
- La paginación uniforme usa `page` con índice base 0 y `size` entre 1 y 50. Sus
  respuestas tienen siempre esta forma:

  ```json
  {
    "items": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "hasNext": false
  }
  ```

## Cambios de contrato incorporados

1. `GET /api/users/me` autenticado recupera el perfil actual y permite restaurar la
   sesión tras recargar la página.
2. `ReviewResponse` contiene `author` (`id`, `username`, `displayName`, `avatarUrl`)
   además de `userId`. Las colecciones de reseñas se enriquecen en lote, evitando una
   petición HTTP adicional por reseña.
3. Feed, búsqueda, reseñas e historial de escuchas usan el envoltorio de paginación
   uniforme anterior. Búsqueda propaga `page` y `size` a MusicBrainz mediante
   `offset` y `limit`.

## Endpoints actuales

| Área                | Rutas                                                                                                                               | Cuerpo o respuesta relevante                                                                                                                                  |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Autenticación       | `POST /api/auth/register`, `POST /api/auth/login`                                                                                   | Registro: username, email, password, displayName. Login: usernameOrEmail, password; devuelve token, userId y username.                                        |
| Usuario             | `GET /api/users/me`, `GET /api/users/{username}`, `PUT /api/users/me`                                                               | Perfil: id, username, displayName, bio, avatarUrl, createdAt.                                                                                                 |
| Social de usuario   | `POST`/`DELETE /api/users/{username}/follow`, `GET .../followers`, `GET .../following`, `GET .../activity`                          | Follow protegido. Activity usa paginación uniforme.                                                                                                           |
| Catálogo            | `GET /api/catalog/search?q=&type=&page=&size=`, `GET /artists/{mbid}`, `/albums/{mbid}`, `/tracks/{mbid}`, `/artists/{mbid}/albums` | `type`: artist, album o track. Las fichas incluyen MBID y relaciones MusicBrainz.                                                                             |
| Reseñas             | `POST /api/reviews`, `GET`/`PUT`/`DELETE /api/reviews/{id}`, `GET /api/reviews/me`                                                  | Crear: targetMbid, targetType (ALBUM/ARTIST/TRACK), rating opcional 1–10, reviewText opcional y containsSpoilers. Crear y editar requieren autor autenticado. |
| Reseñas de catálogo | `GET /api/catalog/albums/{mbid}/reviews`, `GET /api/catalog/artists/{mbid}/reviews`                                                 | Paginación uniforme y autor embebido.                                                                                                                         |
| Listening log       | `POST /api/listening-log`, `GET /api/listening-log/me`                                                                              | Alta: trackMbid y listenedAt ISO-8601 opcional. Historial paginado.                                                                                           |
| Feed y likes        | `GET /api/feed?page=&size=`, `POST`/`DELETE /api/reviews/{id}/like`, `GET /api/reviews/{id}/likes`                                  | Feed y actividad pública paginados; acciones protegidas.                                                                                                      |
| Spotify             | `GET /api/spotify/connect`, callback, status, tops; `POST /sync`; `DELETE /disconnect`                                              | Todas salvo callback requieren JWT. El callback redirige a `/settings/integrations`.                                                                          |

## Límites que no se simulan en este vertical

- El backend aún no expone reseñas de una pista; se puede crear una reseña para una
  pista y abrir su detalle por id, pero no hay una colección pública de reseñas de
  pista.
- Las fichas de álbum y pista solo incluyen MBIDs de sus relaciones. La UI resuelve
  los nombres con las fichas correspondientes cuando resulta necesario.
