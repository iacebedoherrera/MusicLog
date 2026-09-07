## Context

Consulta `proposal.md` para la motivación. El repositorio contiene una
integración generada completa bajo `.specify/`, diez skills bajo
`.agents/skills/speckit-*`, documentación que prescribe sus comandos y
`tools/review-plan.sh`, que todavía lee `.specify/memory/constitution.md` y
espera la estructura `specs/<feature>`.

La constitución contiene reglas de calidad del proyecto que deben conservarse,
pero su ubicación actual forma parte de la integración que se va a retirar.
`openspec/config.yaml` ya identifica el esquema vigente y debe permanecer como
configuración del repositorio. La búsqueda de referencias no encontró usos en
el código de MusicLog ni en las pruebas.

## Goals / Non-Goals

**Goals:**

- Dejar `ai-flow` y OpenSpec como el único flujo documentado y utilizable para
  cambios asistidos por IA.
- Retirar por completo la integración generada anterior, incluidos sus paths,
  metadatos, plantillas, workflow y skills.
- Conservar la constitución del proyecto en una ubicación neutral y mantener la
  revisión técnica independiente con artefactos OpenSpec.
- Hacer verificable que no quedan nombres, comandos ni rutas heredadas fuera de
  los artefactos de este cambio.

**Non-Goals:**

- Cambiar el comportamiento del backend o frontend, APIs, datos, dependencias,
  Docker, CI, secretos o pruebas de producto.
- Crear una nueva implementación de `ai-flow` dentro de MusicLog.
- Crear o modificar requisitos en `openspec/specs/`; el cambio usa
  `skip_specs: true`.
- Alterar el contenido normativo de la constitución salvo las rutas y
  referencias al workflow que dejen de ser válidas.

## Decisions

1. **Eliminar la integración generada como una unidad.** Se eliminarán `.specify/`
   y todos los directorios `.agents/skills/speckit-*`, incluidos manifests,
   scripts, plantillas y registros. Renombrar solo algunos archivos dejaría
   metadatos y rutas capaces de reactivar el flujo retirado.

2. **Reubicar la constitución en `docs/development-constitution.md`.** Se
   conservará su contenido y se actualizarán los consumidores conocidos
   (`README.md` y el revisor). Borrarla perdería las reglas de calidad; dejarla
   bajo `.specify/` mantendría la dependencia que este cambio elimina.

3. **Adaptar `tools/review-plan.sh` a OpenSpec.** El script conservará la
   revisión independiente, pero aceptará el directorio de un cambio OpenSpec,
   leerá `proposal.md`, `design.md` y `tasks.md` cuando exista, y usará la
   constitución reubicada. Eliminarlo rompería una puerta de calidad que la
   documentación actual considera obligatoria; dejarlo igual produciría una
   herramienta inutilizable tras retirar `.specify/`.

4. **Actualizar la documentación para el flujo `ai-flow` + OpenSpec.**
   `README.md` conservará la explicación general del proyecto y sustituirá solo
   su sección de desarrollo asistido; `docs/ai-workflow.md` será la referencia
   detallada. La documentación describirá la secuencia de propuesta, delta de
   specs cuando haya cambios de comportamiento, diseño, tareas, validación y
   aprobación, sin conservar comandos o paths del flujo anterior.

5. **Mantener OpenSpec como fuente de planificación.** No se añadirán skills
   locales equivalentes ni se modificarán `openspec/specs/`; `openspec/config.yaml`
   seguirá definiendo `spec-driven` y el cambio actual permanecerá marcado con
   `skip_specs: true`.

## Risks / Trade-offs

- [Riesgo] Colaboradores podrían intentar usar comandos eliminados → [Mitigación]
  actualizar ambas referencias documentales y comprobar con una búsqueda
  exhaustiva fuera de `openspec/changes/<change-name>`.
- [Riesgo] Algún consumidor local podría depender de la ruta antigua de la
  constitución → [Mitigación] buscar todas las referencias antes de retirar
  `.specify/` y comprobar la ayuda y la sintaxis del revisor adaptado.
- [Riesgo] El revisor podría interpretar incorrectamente artefactos OpenSpec →
  [Mitigación] definir explícitamente sus archivos de entrada y ejecutar una
  comprobación sintáctica junto con una validación del cambio.
- [Trade-off] La constitución deja de estar junto a la integración de skills →
  [Mitigación] mantenerla versionada en `docs/` y enlazarla desde la
  documentación de contribución.

## Migration Plan

1. Reubicar la constitución y actualizar sus referencias.
2. Adaptar y comprobar `tools/review-plan.sh` contra las rutas de OpenSpec.
3. Reescribir `README.md` y `docs/ai-workflow.md` con el flujo vigente.
4. Eliminar `.specify/` y `.agents/skills/speckit-*` después de que no queden
   consumidores de sus rutas.
5. Ejecutar la búsqueda de referencias, validaciones documentales y
   `openspec validate` para el cambio.

La reversión es de bajo riesgo: restaurar las rutas eliminadas desde el control
de versiones y revertir los cambios de documentación y del script. No hay
migraciones de datos ni cambios desplegables.
