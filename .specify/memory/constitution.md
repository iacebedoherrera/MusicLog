<!--
Sync Impact Report
- Version change: 1.0.0 -> 1.0.1.
- Modified principles: III. Protección de secretos, entrega e infraestructura (aclaración de
  cambios permitidos en Docker Compose e infraestructura de desarrollo local).
- Added sections: ninguna.
- Removed sections: ninguna.
- Follow-up TODOs: ninguno.
-->

# Constitución de MusicLog

## Principios fundamentales

### I. Cambios pequeños, cohesionados y compatibles

Cada cambio DEBE resolver una única necesidad identificable y limitarse a los módulos, contratos y
archivos necesarios. Las rutas REST, DTOs públicos, eventos, esquemas de PostgreSQL y contratos del
frontend DEBEN conservar compatibilidad hacia atrás. Una eliminación, renombrado, cambio semántico o
migración no compatible exige aprobación humana explícita y un plan de transición documentado en el
plan y en el PR. Las migraciones Flyway nuevas DEBEN ser aditivas e inmutables una vez aplicadas;
no se editan migraciones `V*` ya existentes.

### II. Pruebas y controles obligatorios

Todo comportamiento nuevo o modificado DEBE incluir pruebas automatizadas que cubran el resultado
esperado y los casos de error relevantes. Los cambios de servicio se prueban con JUnit/Mockito; los
límites de módulos, persistencia, eventos, seguridad o integración se prueban con las pruebas de
integración y Spring Modulith existentes; los cambios de frontend se prueban con Vitest cuando
alteren comportamiento observable.

Antes de declarar terminada una tarea, se DEBEN ejecutar los controles aplicables ya disponibles:
`mvn test` para el backend, y, cuando cambie `frontend/`, `npm run typecheck`, `npm run lint`,
`npm run test` y `npm run build` desde `frontend/`. Si un control no puede ejecutarse, el resumen
final DEBE indicar el comando, el motivo y el riesgo pendiente; no se puede presentar como aprobado.

### III. Protección de secretos, entrega e infraestructura

Queda prohibido modificar, crear o exponer secretos, archivos `.env`, credenciales, configuraciones
de CI/CD, despliegues o infraestructura de producción sin aprobación humana explícita y previa. Los
cambios en Docker Compose u otra infraestructura de desarrollo local solo se permiten si son
necesarios para la funcionalidad, están incluidos en el plan, no incorporan secretos ni reducen
controles de seguridad, y se documentan en el PR. Nunca se registran ni incluyen en código, pruebas,
documentación o PR contraseñas, JWT, cabeceras de autorización, secretos de Spotify, claves de
cifrado, variables de base de datos ni datos personales innecesarios. Los valores de configuración se
obtienen por variables de entorno y los secretos de producción no pueden sustituirse por valores
incrustados.

### IV. Dependencias mínimas y justificadas

No se añade una dependencia de Maven, npm ni una herramienta de construcción si las dependencias y
capacidades existentes resuelven razonablemente el problema. Toda excepción DEBE justificarse en el
plan y en el PR, indicando necesidad concreta, alternativas evaluadas, licencia o mantenimiento,
impacto de seguridad y cómo se valida. Una dependencia nueva no se considera aceptada hasta recibir
la revisión técnica requerida por esta constitución.

### V. Validación, errores y privacidad por defecto

Las entradas HTTP DEBEN modelarse en DTOs, validarse con restricciones de Jakarta Validation y
`@Valid`, y las reglas de negocio DEBEN validarse también en el servicio. Los errores de API DEBEN
usar el formato común `ApiError` a través de `GlobalExceptionHandler`, con códigos HTTP coherentes y
sin revelar detalles internos. Las operaciones protegidas DEBEN respetar Spring Security y obtener el
usuario mediante el mecanismo `AuthenticatedUser` ya establecido.

Los logs JSON DEBEN incluir solo datos operativos necesarios y excluir tokens, secretos, contraseñas,
datos de pago y contenido personal que no sea imprescindible para diagnóstico. Las integraciones con
MusicBrainz, Cover Art Archive y Spotify DEBEN validar respuestas y fallos de red, preservar sus
contratos configurados y no degradar silenciosamente la seguridad ni la privacidad.

### VI. Arquitectura y convenciones existentes

MusicLog se mantiene como monolito modular con Spring Modulith. El código backend DEBE permanecer
dentro de los módulos `user`, `catalog`, `review`, `social`, `spotifyconnector` o `shared`, respetar
sus límites y pasar la verificación `ApplicationModules`. La comunicación entre módulos DEBE usar
eventos de aplicación cuando existe consistencia eventual; no se introducen dependencias directas que
rompan esos límites sin aprobación técnica explícita.

PostgreSQL es la fuente de escritura y Redis se usa para caché y la proyección de lectura del módulo
`social`; el CQRS ligero no se extiende a otros módulos sin una decisión arquitectónica documentada.
El backend DEBE conservar Java 21, Spring Boot, JPA, Flyway y las convenciones de paquetes, DTOs,
controladores y paginación existentes. El frontend DEBE conservar React, TypeScript estricto, Vite,
ESLint y la organización por `features`, `components`, `pages` y `api` ya presente.

### VII. Especificación, plan, tareas y revisión técnica antes de implementar

No se implementa funcionalidad hasta que existan una especificación aprobada (`spec.md`), un plan
(`plan.md`), tareas dependientes y accionables (`tasks.md`) y una revisión técnica registrada del
plan. La revisión DEBE comprobar al menos límites de módulo, compatibilidad de API y datos, eventos,
seguridad, migraciones, dependencias y estrategia de pruebas. Las excepciones a esta puerta de entrada
requieren aprobación humana explícita y su alcance debe quedar escrito.

### VIII. Ramas dedicadas, PR y cierre trazable

Toda implementación se realiza en una rama dedicada, basada en `develop` salvo instrucción humana
distinta, y se integra mediante PR hacia `develop`. Cada PR o entrega DEBE incluir: objetivo y enlace
a la especificación, lista de archivos modificados, compatibilidad o migraciones, pruebas y controles
ejecutados con su resultado, y limitaciones, riesgos o comprobaciones pendientes. No se mezclan en la
misma rama cambios de funcionalidad independientes ni se realiza un commit directo en `develop` para
evitar el flujo de revisión.

### IX. Ambigüedades materiales se resuelven antes de actuar

Ante una ambigüedad material de producto, seguridad, datos, contrato de API, diseño, persistencia o
experiencia de usuario, el trabajo DEBE detenerse y formular preguntas concretas. No se permite asumir
requisitos que cambien alcance, exposición de datos, compatibilidad, autorización o diseño
arquitectónico. Las respuestas y decisiones resultantes DEBEN incorporarse a la especificación o al
plan antes de continuar.

## Restricciones técnicas del proyecto

El backend usa Java 21, Spring Boot 3.3, Spring Modulith, Maven, PostgreSQL 16, Redis 7, Flyway,
Spring Security con JWT y OpenAPI. MusicBrainz es la fuente de catálogo y exige un `User-Agent`
identificable y un máximo de una solicitud por segundo; Spotify se limita a OAuth y sincronización,
con tokens cifrados. Las modificaciones que afecten a estos límites o a las configuraciones de
integración se tratan como decisiones de arquitectura y se someten a revisión técnica.

El frontend es una aplicación independiente en `frontend/`, construida con React 19, TypeScript,
Vite y Tailwind. Los contratos consumidos se mantienen en `frontend/docs/api-contract.md`; cualquier
cambio de contrato del backend DEBE actualizar su documentación y las pruebas consumidoras dentro del
mismo cambio compatible.

## Flujo de desarrollo y calidad

1. Registrar la necesidad mediante `$speckit-specify` y resolver las ambigüedades materiales con
   `$speckit-clarify`.
2. Elaborar el diseño con `$speckit-plan`, incluyendo impacto en módulos, API, datos, eventos,
   seguridad, dependencias y pruebas; completar su revisión técnica.
3. Generar tareas con `$speckit-tasks`, crear una rama dedicada y ejecutar solo las tareas aprobadas
   con `$speckit-implement`.
4. Verificar la coherencia con `$speckit-analyze` cuando existan especificación, plan y tareas, y
   ejecutar los controles exigidos por el principio II antes de abrir o actualizar el PR.
5. Revisar el PR contra esta constitución. Cualquier incumplimiento exige una excepción humana
   explícita, documentada con alcance, motivo, mitigación y fecha de revisión.

## Gobernanza

Esta constitución prevalece sobre prácticas implícitas y sobre instrucciones de menor nivel. Las
especificaciones, planes, tareas, revisiones y PR DEBEN demostrar su cumplimiento; quien revisa debe
rechazar cambios que no aporten las evidencias exigidas o que no documenten una excepción aprobada.

Las enmiendas se proponen mediante PR dedicado que describa el principio afectado, la motivación, los
impactos en plantillas o flujos y la aprobación humana. La versión sigue SemVer: MAJOR para retirar o
redefinir de forma incompatible principios, MINOR para añadir principios o obligaciones materiales y
PATCH para aclaraciones sin cambio de obligación. Toda enmienda actualiza la fecha de modificación y
el informe de impacto al inicio del documento.

Las revisiones técnicas y de PR comprueban explícitamente arquitectura modulith, compatibilidad,
validación y errores, protección de datos, pruebas, dependencias, alcance de rama y estado de los
controles. Una entrega no se considera terminada hasta que esa comprobación quede registrada.

**Versión**: 1.0.1 | **Ratificada**: 2026-08-08 | **Última modificación**: 2026-08-08
