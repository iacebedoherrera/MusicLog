## Purpose

Permite que cada usuario autenticado gestione de forma segura sus datos de cuenta desde una página de ajustes, con validaciones claras para identidad y contraseña.

## ADDED Requirements

### Requirement: Acceso autenticado a los ajustes de cuenta

El sistema SHALL ofrecer una página de ajustes de cuenta protegida para que el usuario autenticado consulte y edite su nombre mostrado y nombre de usuario. La página SHALL mostrar los valores actuales y SHALL dejar vacíos los campos de contraseña al abrirse; nunca SHALL precargar ni devolver una contraseña.

#### Scenario: El usuario autenticado abre sus ajustes

- **WHEN** un usuario autenticado accede a la página de ajustes de cuenta
- **THEN** el sistema muestra su nombre mostrado y nombre de usuario actuales, junto con los campos de contraseña vacíos

#### Scenario: Un usuario no autenticado intenta acceder

- **WHEN** una persona sin una sesión válida accede a la página de ajustes de cuenta
- **THEN** el sistema impide el acceso a los datos de la cuenta y la dirige al inicio de sesión

### Requirement: Actualización del nombre y del nombre de usuario

El sistema SHALL permitir actualizar el nombre mostrado y el nombre de usuario del usuario autenticado. El nombre mostrado SHALL ser obligatorio y tener como máximo 100 caracteres; el nombre de usuario SHALL ser obligatorio y tener como máximo 50 caracteres. La disponibilidad del nombre de usuario SHALL comprobarse sin distinguir mayúsculas y minúsculas, excluyendo la cuenta que realiza la petición.

#### Scenario: Se actualiza un nombre mostrado válido

- **WHEN** el usuario envía un nombre mostrado no vacío y válido
- **THEN** el sistema guarda el nuevo nombre y devuelve los datos actualizados sin exponer la contraseña

#### Scenario: Se cambia a un nombre de usuario disponible

- **WHEN** el usuario envía un nombre de usuario que no pertenece a otra cuenta, ignorando mayúsculas y minúsculas
- **THEN** el sistema guarda el nuevo nombre de usuario y lo muestra como el valor actual

#### Scenario: El nombre de usuario ya está ocupado

- **WHEN** el usuario envía un nombre de usuario que ya pertenece a otra cuenta, aunque cambie su combinación de mayúsculas y minúsculas
- **THEN** el sistema rechaza la actualización con un error de conflicto asociado al campo y conserva sin cambios los datos de la cuenta

#### Scenario: Los datos de identidad no superan la validación

- **WHEN** el usuario envía un nombre mostrado o nombre de usuario vacío o que supera su longitud máxima
- **THEN** el sistema rechaza la petición con errores de validación y no guarda ningún cambio

### Requirement: Cambio de contraseña con confirmación

El sistema SHALL permitir cambiar la contraseña solo cuando se envíen una nueva contraseña y su confirmación, ambas con entre 8 y 100 caracteres y exactamente iguales. Si ambos campos están vacíos, el sistema SHALL conservar la contraseña actual. Las contraseñas SHALL almacenarse únicamente como hash y nunca SHALL aparecer en respuestas, logs ni mensajes de error.

#### Scenario: Se actualiza una contraseña confirmada

- **WHEN** el usuario envía una nueva contraseña válida y una confirmación idéntica
- **THEN** el sistema guarda el hash de la nueva contraseña, la siguiente autenticación acepta la nueva contraseña y la respuesta no contiene ninguna contraseña

#### Scenario: Falta uno de los campos de contraseña

- **WHEN** el usuario envía solo la nueva contraseña o solo su confirmación
- **THEN** el sistema rechaza la petición con un error de validación y conserva la contraseña actual

#### Scenario: Las contraseñas no coinciden

- **WHEN** el usuario envía dos valores de contraseña distintos
- **THEN** el sistema rechaza la petición con un error de confirmación y no modifica la contraseña ni los demás datos enviados

#### Scenario: La nueva contraseña no cumple la longitud requerida

- **WHEN** el usuario envía dos valores iguales con menos de 8 o más de 100 caracteres
- **THEN** el sistema rechaza la petición con un error de validación y conserva la contraseña actual

### Requirement: Actualización atómica de los ajustes

El sistema SHALL aplicar una solicitud de ajustes como una única operación: si falla la validación de cualquier campo o la comprobación de disponibilidad del nombre de usuario, SHALL conservar todos los valores anteriores y no SHALL aplicar solo una parte de la solicitud.

#### Scenario: Una solicitud con un error no produce cambios parciales

- **WHEN** una solicitud contiene un nombre válido pero un nombre de usuario ocupado o una confirmación de contraseña inválida
- **THEN** el sistema rechaza la solicitud y conserva el nombre, nombre de usuario y contraseña anteriores

### Requirement: Alternancia visible de los campos de contraseña

La página SHALL mostrar un control con icono de ojo para cada campo de contraseña. Cada control SHALL alternar el campo entre entrada ofuscada y texto visible, SHALL conservar exactamente el valor introducido y SHALL ser operable con teclado y tecnologías de asistencia.

#### Scenario: El usuario muestra y oculta una contraseña

- **WHEN** el usuario activa el icono de ojo de un campo de contraseña y lo activa de nuevo
- **THEN** el campo pasa de ofuscado a visible y vuelve a ofuscado, sin cambiar su valor ni enviar la contraseña durante la alternancia
