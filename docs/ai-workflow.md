# Flujo de desarrollo asistido por IA

## Regla principal

MusicLog usa `ai-flow` para coordinar cambios OpenSpec desde la propuesta hasta
la aplicación y la verificación. El cambio es la unidad de trabajo y vive en
`openspec/changes/<change-name>/`.

No se implementa una funcionalidad hasta que los artefactos estén completos,
la revisión técnica independiente esté aprobada y exista aprobación humana
explícita para aplicar el cambio.

La referencia normativa del proyecto es la
[constitución de desarrollo](development-constitution.md).

## Artefactos de un cambio

| Artefacto | Propósito | Cuándo se usa |
| --- | --- | --- |
| `proposal.md` | Explica el problema, el alcance, el impacto y las capacidades afectadas. | Siempre. |
| `specs/**/*.md` | Describe deltas de requisitos con formato OpenSpec. | Cuando cambia el comportamiento o un contrato del producto. |
| `design.md` | Documenta la solución técnica, sus decisiones, riesgos y límites. | Cuando el cambio necesita diseño técnico; es obligatorio para este flujo antes de generar tareas. |
| `tasks.md` | Divide la implementación en tareas ordenadas y verificables. | Siempre antes de aplicar un cambio. |
| `review.md` | Registra el veredicto y las validaciones del revisor independiente. | Antes de pedir aprobación para aplicar. |

Un cambio de documentación, tooling o limpieza que no altere requisitos puede
declarar `skip_specs: true` en su `.openspec.yaml`. No se crea un delta para
satisfacer la validación cuando no cambia el comportamiento.

## Flujo por cambio

### 1. Preparar la rama y la ejecución

Partir de `develop` actualizado y usar una rama dedicada. `ai-flow` puede crear
la ejecución y la rama con un identificador persistente:

```bash
git switch develop
git pull --ff-only
ai-flow start "Descripción breve del cambio" --branch-name ai-flow/nombre-cambio
```

Guardar el `run-id` que devuelve `ai-flow` y consultar su estado cuando sea
necesario:

```bash
ai-flow status <run-id>
```

### 2. Registrar la propuesta y resolver ambigüedades

Crear o completar:

```text
openspec/changes/<change-name>/proposal.md
```

La propuesta debe definir el problema, el alcance, las capacidades nuevas o
modificadas y el impacto. Resolver antes de continuar cualquier ambigüedad
material de producto, seguridad, datos, compatibilidad o arquitectura.

### 3. Añadir deltas de especificación cuando proceda

Si cambia un requisito o un contrato observable, añadir el delta bajo:

```text
openspec/changes/<change-name>/specs/<capability>.md
```

El delta debe expresar los requisitos modificados en el formato de OpenSpec y
mantener trazabilidad con `proposal.md` y `design.md`. Para cambios sin
comportamiento, mantener `skip_specs: true` y no modificar `openspec/specs/`.

### 4. Elaborar el diseño técnico

Completar:

```text
openspec/changes/<change-name>/design.md
```

El diseño debe cubrir módulos afectados, compatibilidad de API y datos,
eventos, seguridad, persistencia y migraciones, dependencias, estrategia de
pruebas, validaciones, riesgos y elementos fuera de alcance. Las instrucciones
del artefacto se pueden consultar con:

```bash
openspec instructions design --change <change-name> --json
```

### 5. Revisar de forma independiente

Ejecutar el revisor desde la raíz del repositorio:

```bash
./tools/review-plan.sh openspec/changes/<change-name>
REVIEW_STATUS=$?
grep -E '^VERDICT: ' openspec/changes/<change-name>/review.md
```

El revisor inspecciona la propuesta, los deltas disponibles, el diseño, las
tareas y la constitución en modo solo lectura. Los veredictos son:

| Veredicto | Acción |
| --- | --- |
| `APPROVED` | Continuar a la aprobación humana. |
| `CHANGES_REQUIRED` | Resolver los bloqueantes en los artefactos y repetir la revisión. |
| `HUMAN_DECISION_REQUIRED` | Detenerse y obtener la decisión material antes de continuar. |

No se solicita aprobación ni se aplica el cambio mientras haya bloqueantes o
decisiones materiales pendientes.

### 6. Generar y revisar las tareas

Completar:

```text
openspec/changes/<change-name>/tasks.md
```

Cada tarea debe usar una casilla `- [ ]`, indicar el alcance concreto y tener
una verificación observable. Consultar las instrucciones del artefacto si hace
falta:

```bash
openspec instructions tasks --change <change-name> --json
```

Comprobar el estado global del cambio antes de pedir autorización:

```bash
openspec status --change <change-name> --json
```

### 7. Aprobar y aplicar

La aprobación humana es una puerta explícita entre la planificación y la
implementación. `ai-flow` conserva esa decisión y solo permite continuar una
ejecución aprobada:

```bash
ai-flow approve <run-id>
ai-flow resume <run-id>
```

La operación `resume` ejecuta el `apply` de OpenSpec para el cambio aprobado.
Para inspeccionar las instrucciones que se aplicarán:

```bash
openspec instructions apply --change <change-name> --json
```

Durante la aplicación se implementan únicamente las tareas aprobadas. Si la
ejecución debe detenerse sin aplicar el cambio:

```bash
ai-flow cancel <run-id>
```

### 8. Validar y cerrar

Validar los artefactos y ejecutar los controles definidos por la constitución:

```bash
openspec validate "<change-name>" --type change --no-interactive
mvn test
```

Si el cambio toca `frontend/`, ejecutar también desde ese directorio:

```bash
npm run typecheck
npm run lint
npm run test
npm run build
```

Antes de abrir o actualizar el PR, revisar `git diff --check`, la lista de
archivos modificados, el resultado de cada comando y los riesgos pendientes.
El PR debe enlazar la propuesta, resumir la solución, registrar la aprobación,
las pruebas y cualquier limitación. El cierre o archivado del cambio se hace
solo después de la integración aprobada.

## Límites

- El revisor técnico es independiente y trabaja en modo solo lectura.
- La aprobación humana es obligatoria antes de `apply`.
- Los cambios de producto deben conservar módulos, contratos, seguridad,
  migraciones y pruebas según la constitución.
- No se modifican secretos, `.env`, Docker, CI/CD ni infraestructura de
  producción como parte de este flujo sin la aprobación específica exigida.
