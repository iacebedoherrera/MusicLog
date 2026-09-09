## Why

Los usuarios autenticados no disponen de una página de autoservicio para mantener sus datos de cuenta. La API actual permite actualizar algunos datos de perfil, pero no ofrece un flujo para cambiar el nombre de usuario o la contraseña con las validaciones y la experiencia necesarias.

## What Changes

- Añadir una página protegida de ajustes de cuenta accesible desde la aplicación.
- Permitir editar el nombre mostrado y el nombre de usuario del usuario autenticado.
- Validar que el nombre de usuario no esté ocupado por otra cuenta, sin distinguir mayúsculas y minúsculas, y mostrar el error sin guardar cambios parciales.
- Permitir cambiar la contraseña solicitando la nueva contraseña dos veces y rechazando el envío si no coinciden.
- Incorporar un icono de ojo para alternar entre contraseña ofuscada y visible en los campos de contraseña.
- Mantener las contraseñas fuera de las respuestas, logs y estados persistidos en claro.

## Capabilities

### New Capabilities

- `user-settings`: Gestión autenticada del nombre mostrado, nombre de usuario y contraseña, incluida la comprobación de disponibilidad del nombre de usuario y el control de visibilidad de los campos de contraseña.

### Modified Capabilities

Ninguna.

## Impact

- Afecta al módulo backend `user`, sus DTOs, servicio, controlador, persistencia y pruebas para ampliar el contrato de actualización del usuario autenticado.
- Afecta al frontend React en la navegación, rutas protegidas, cliente API, tipos y página de ajustes.
- Requiere conservar el hash de contraseña existente y el manejo común de errores; no introduce dependencias externas ni modifica otros módulos, infraestructura o secretos.
