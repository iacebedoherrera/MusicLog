## 1. Persistencia y contrato backend

- [x] 1.1 Añadir una migración Flyway aditiva que cree un índice único sobre `LOWER(username)` sin modificar las migraciones existentes. Verificación: ejecutar la migración contra la base de pruebas y comprobar que impide dos nombres que solo difieren en mayúsculas, sin alterar las filas válidas.
- [x] 1.2 Crear el DTO del ajuste de cuenta con `displayName`, `username`, `newPassword` y `newPasswordConfirmation`, sus límites de validación y la documentación OpenAPI correspondiente. Verificación: pruebas de validación que cubran campos obligatorios, longitudes, pareja de contraseña vacía y pareja incompleta.
- [x] 1.3 Añadir la operación de dominio y repositorio para actualizar identidad y contraseña de forma transaccional, comprobando el nombre de usuario sin distinguir mayúsculas y excluyendo al usuario actual, codificando solo la nueva contraseña y evitando cambios parciales. Verificación: pruebas unitarias del servicio para actualización válida, nombre ocupado, confirmación distinta, longitud inválida, nombre sin cambios y conservación de la contraseña cuando la pareja está vacía.
- [x] 1.4 Exponer `PUT /api/users/me/account` protegido con JWT, devolver el perfil actualizado y mapear conflictos de `username` y validaciones al formato `ApiError` sin incluir contraseñas. Verificación: prueba de integración HTTP que compruebe autenticación, respuesta correcta, HTTP 409 para un nombre ocupado y ausencia de valores de contraseña en la respuesta y el error.

## 2. Verificación del módulo user

- [x] 2.1 Completar las pruebas de integración del módulo `user` para confirmar que una solicitud inválida conserva nombre, nombre de usuario y hash anteriores, y que una solicitud válida permite autenticarse con la nueva contraseña. Verificación: la prueba de integración pasa con una cuenta autenticada y cubre también la carrera o restricción de unicidad sensible a mayúsculas disponible en el entorno de pruebas.
- [x] 2.2 Actualizar las pruebas de estructura o contrato necesarias para mantener los límites de Spring Modulith y el endpoint documentado. Verificación: `mvn test` termina correctamente y la verificación de `ApplicationModules` sigue aprobada.

## 3. Contrato consumido por el frontend

- [x] 3.1 Documentar en `frontend/docs/api-contract.md` el endpoint de ajustes, el cuerpo, la respuesta y los errores de validación/conflicto, sin ejemplos de secretos. Verificación: el documento contiene el nuevo endpoint y coincide campo por campo con el DTO y la respuesta de OpenAPI.
- [x] 3.2 Añadir el tipo de entrada y el cliente API de ajustes usando el cliente HTTP existente, conservando `ApiClientError.fieldErrors`. Verificación: prueba del cliente que compruebe método, ruta, cuerpo JSON y propagación de errores de API.

## 4. Página protegida de ajustes

- [x] 4.1 Incorporar al estado de autenticación una forma de reemplazar el `UserProfile` después de guardar, sin exponer un setter mutable ni persistir datos sensibles. Verificación: prueba del proveedor o de la página que confirme que el encabezado refleja el nombre actualizado.
- [x] 4.2 Crear la página `/settings/account` bajo `RequireAuth`, inicializar nombre y usuario con el perfil actual y dejar vacíos los campos de contraseña. Verificación: prueba de renderizado autenticado que muestre los valores actuales y prueba de acceso no autenticado que dirija a `/login` sin realizar una petición de guardado.
- [x] 4.3 Implementar el formulario de guardado con estado pendiente, errores generales y errores por campo, envío atómico de los cuatro campos y limpieza de las contraseñas tras éxito. Verificación: prueba de interacción que no llame al API con contraseñas distintas, muestre el conflicto de `username` y envíe una pareja coincidente solo una vez.
- [x] 4.4 Añadir el icono de ojo como control de teclado accesible para cada campo de contraseña, alternando `password`/texto sin modificar el valor ni enviar el formulario. Verificación: prueba de Testing Library que compruebe el tipo inicial, `aria-pressed`, etiqueta accesible, alternancia dos veces y ausencia de una petición durante la alternancia.
- [x] 4.5 Añadir el enlace de cuenta a la navegación autenticada y registrar la ruta sin cambiar las rutas públicas existentes. Verificación: prueba de navegación o smoke test que abra `/settings/account` desde el enlace y confirme que el enlace no aparece para una sesión cerrada.

## 5. Controles finales del cambio

- [x] 5.1 Ejecutar la suite backend completa y revisar que no haya contraseñas o tokens en logs, respuestas, documentación ni pruebas. Verificación: `mvn test` termina correctamente y una búsqueda focalizada no encuentra secretos en los archivos modificados.
- [x] 5.2 Ejecutar los controles del frontend para typecheck, lint, pruebas y build. Verificación: desde `frontend/`, `npm run typecheck`, `npm run lint`, `npm run test` y `npm run build` terminan correctamente.
- [x] 5.3 Validar el cambio OpenSpec en modo no interactivo y comprobar que solo contiene los cuatro artefactos de planificación previstos y el metadato local existente. Verificación: `openspec validate "como-usuario-quiero-poder-tener-una-run7f4b8" --type change --no-interactive` termina correctamente y `git diff --check` no reporta errores.
