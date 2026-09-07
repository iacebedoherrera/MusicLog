## Context

Consulta `proposal.md` para la motivación y `specs/user-settings/spec.md` para el contrato observable. El módulo `user` ya autentica con JWT, obtiene el usuario mediante `AuthenticatedUser`, conserva las credenciales con `PasswordEncoder` y expone `GET /api/users/me` junto con un `PUT /api/users/me` orientado al perfil existente. Ese `PUT` actualiza `displayName`, `bio` y `avatarUrl`, por lo que no conviene mezclar credenciales con su semántica actual de campos opcionales.

El esquema actual tiene una restricción única sensible a mayúsculas para `users.username` y un índice no único sobre `LOWER(username)`. El frontend ya dispone de `RequireAuth`, `UserProfile`, el cliente HTTP con `ApiClientError` y un `AuthProvider`, pero todavía no tiene ruta ni navegación para ajustes de cuenta. Las pruebas de backend usan JUnit/Spring Modulith y las de frontend Vitest con Testing Library.

## Goals / Non-Goals

**Goals:**

- Añadir un contrato específico y aditivo para actualizar nombre mostrado, nombre de usuario y contraseña del usuario autenticado.
- Garantizar la unicidad de nombre de usuario sin distinguir mayúsculas y minúsculas tanto en la validación de servicio como en la base de datos.
- Mantener la operación atómica, el hash BCrypt existente y la compatibilidad del endpoint de perfil actual.
- Añadir una página protegida `/settings/account`, enlazada desde la navegación autenticada, con confirmación y alternancia accesible de contraseña.
- Actualizar el contrato documentado y cubrir backend, persistencia y frontend con pruebas automatizadas.

**Non-Goals:**

- Cambiar el correo electrónico, la biografía, el avatar, el borrado de cuenta, la autenticación multifactor o el flujo de registro.
- Exigir una contraseña actual adicional: la especificación limita la confirmación a repetir la nueva contraseña y la operación seguirá requiriendo una sesión JWT válida.
- Rotar el JWT ni introducir eventos, cachés o un índice de búsqueda nuevos por el cambio de nombre de usuario; la autorización continuará basándose en el identificador estable del usuario.
- Añadir una librería de iconos o modificar infraestructura, secretos, Docker o CI.

## Decisions

1. **Crear un endpoint de ajustes separado.** Se añadirá `PUT /api/users/me/account`, autenticado, con `displayName`, `username`, `newPassword` y `newPasswordConfirmation`, y devolverá el `UserProfileResponse` ya utilizado por el frontend. Se conserva `PUT /api/users/me` sin ampliar su semántica para no hacer que clientes existentes envíen o borren accidentalmente campos de perfil al cambiar una credencial. Se evaluó ampliar el endpoint actual, pero separar identidad/credenciales reduce el riesgo de compatibilidad y hace explícito el límite de seguridad.

2. **Validar antes de mutar y confirmar dentro del servicio.** El DTO aplicará las restricciones de presencia y longitud; el servicio exigirá que los dos campos de contraseña estén ambos vacíos o ambos presentes y sean idénticos, y comprobará `username` ignorando mayúsculas y excluyendo el propio `id`. Todas las comprobaciones se ejecutarán antes de modificar la entidad dentro de una transacción. Solo se codificará `newPassword`; `newPasswordConfirmation` nunca se persistirá ni se incluirá en la respuesta. Un conflicto de nombre devolverá HTTP 409 con un error asociable al campo `username`; los errores de validación conservarán el formato común `ApiError`/`fieldErrors`.

3. **Reforzar la unicidad en PostgreSQL.** Se añadirá una migración Flyway aditiva que cree un índice único sobre `LOWER(username)`, sin editar `V1__create_users_and_follows.sql` ni renombrar datos existentes. El prechequeo de servicio mejora el mensaje normal y el índice cubre carreras entre solicitudes concurrentes. Si una base ya contiene dos nombres iguales ignorando mayúsculas, la migración deberá fallar de forma explícita para resolver esos datos antes del despliegue; no se normalizarán ni eliminarán cuentas automáticamente.

4. **Conservar la identidad de la sesión.** El endpoint usará el `id` proveniente de `AuthenticatedUser`; cambiar el nombre no cambiará el sujeto del JWT ni revocará la sesión actual. El perfil devuelto actualizará el estado de usuario del frontend para que el encabezado refleje el nuevo nombre. El claim de nombre existente no se usará para autorizar operaciones, por lo que no se añade una rotación de token en este cambio.

5. **Integrar la pantalla con las piezas existentes.** La ruta `/settings/account` quedará bajo `RequireAuth` y utilizará `GET /api/users/me` para sus valores actuales. La navegación mostrará un enlace de cuenta solo con sesión iniciada. El formulario mantendrá las contraseñas únicamente en estado React transitorio, las inicializará vacías y las limpiará tras un guardado correcto. Cada campo de contraseña tendrá un botón `type="button"` con nombre accesible y estado pulsado; el icono se implementará con SVG local para evitar dependencias nuevas.

6. **No publicar eventos de dominio.** Los módulos existentes referencian usuarios por UUID y las respuestas públicas se construyen leyendo el usuario actual; no hay una proyección o índice que requiera sincronización por este cambio. No se añade un evento de actualización hasta que exista un consumidor que lo necesite.

7. **Mantener la documentación como contrato.** La tabla de `frontend/docs/api-contract.md` se actualizará con el endpoint, sus campos y sus errores, y las anotaciones OpenAPI del controlador describirán el cuerpo sin incluir ejemplos de contraseñas reales. No se modificarán rutas públicas existentes.

## Risks / Trade-offs

- [Riesgo] Dos solicitudes concurrentes podrían pasar el prechequeo de disponibilidad → [Mitigación] índice único sobre `LOWER(username)`, transacción y conversión del conflicto de integridad a un error 409 sin cambios parciales.
- [Riesgo] Una migración de índice único puede encontrar datos históricos que solo difieren en mayúsculas → [Mitigación] detectar el conflicto durante la migración y detener el despliegue sin corregir cuentas de forma automática.
- [Riesgo] El JWT activo puede conservar temporalmente el claim de nombre anterior → [Mitigación] usar el UUID como autoridad, refrescar el perfil en el frontend y dejar explícito que el claim no autoriza ni se muestra como fuente de verdad.
- [Riesgo] La contraseña permanece temporalmente en memoria del navegador durante la edición → [Mitigación] no persistirla en almacenamiento web, no incluirla en logs ni errores, limpiar el formulario tras éxito y usar siempre HTTPS fuera del desarrollo local.
- [Trade-off] El endpoint adicional duplica parte del concepto de actualización de perfil → [Mitigación] mantiene aislados los cambios de credenciales y evita alterar la compatibilidad y semántica de `PUT /api/users/me`.

## Migration Plan

1. Añadir el índice único funcional mediante una migración Flyway nueva y verificarla con una prueba de integración de persistencia.
2. Implementar el contrato de cuenta en el módulo `user`, el manejo de conflictos y las pruebas de servicio/API.
3. Actualizar el contrato documental y añadir el cliente, la ruta protegida, la navegación, el formulario y sus pruebas en el frontend.
4. Ejecutar la suite backend y los controles `typecheck`, `lint`, `test` y `build` del frontend. La migración es aditiva y no requiere migración de valores; si el código debe revertirse, el índice puede permanecer porque refuerza la invariancia requerida.
