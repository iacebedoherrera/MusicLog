## 1. Preservar la gobernanza del repositorio

- [x] 1.1 Reubicar `.specify/memory/constitution.md` en `docs/development-constitution.md`, conservando sus principios y actualizando únicamente rutas o referencias al workflow retirado; verificar que el nuevo archivo no está vacío, contiene los principios I–IX y no contiene `.specify` ni comandos `speckit`.
- [x] 1.2 Actualizar las referencias a la constitución en `README.md`, `docs/ai-workflow.md` y `tools/review-plan.sh`; verificar con `rg -n '\.specify/memory/constitution\.md' README.md docs tools` que la ruta antigua no aparece.

## 2. Migrar la revisión técnica auxiliar

- [x] 2.1 Adaptar `tools/review-plan.sh` para recibir un cambio bajo `openspec/changes/<change-name>` y leer `proposal.md`, `design.md` y `tasks.md` cuando exista; verificar con `bash -n tools/review-plan.sh` y su salida de uso que no depende de `.specify` ni de la estructura `specs/<feature>`.
- [x] 2.2 Actualizar el prompt y los mensajes del revisor para usar la constitución reubicada y la terminología de OpenSpec; verificar que `rg -n -i '(spec[ -]?kit|speckit|\.specify)' tools/review-plan.sh` no devuelve resultados.

## 3. Actualizar la documentación del workflow

- [x] 3.1 Reescribir `docs/ai-workflow.md` para documentar `ai-flow` con OpenSpec, sus artefactos `proposal.md`, deltas de specs cuando procedan, `design.md` y `tasks.md`, además de las puertas de revisión y aprobación; verificar que el documento es no vacío y no contiene nombres, comandos ni rutas del workflow retirado.
- [x] 3.2 Actualizar en `README.md` la sección de desarrollo asistido, sus ejemplos y rutas para apuntar solo a `ai-flow`/OpenSpec y enlazar la constitución reubicada; verificar que `rg -n -i '(spec[ -]?kit|speckit|\.specify)' README.md` no devuelve resultados.

## 4. Retirar los artefactos generados heredados

- [x] 4.1 Eliminar el árbol `.specify/` completo después de migrar sus consumidores, incluidos metadatos, manifests, scripts, plantillas y registros; verificar que `test ! -e .specify` se cumple.
- [x] 4.2 Eliminar todas las skills `.agents/skills/speckit-*` y sus directorios vacíos resultantes; verificar que `find .agents/skills -maxdepth 1 -type d -name 'speckit-*' -print` no produce salida.

## 5. Validación final del cambio

- [x] 5.1 Ejecutar una búsqueda exhaustiva de referencias heredadas fuera de este cambio con `rg -n -i --hidden --glob '!**/.git/**' --glob '!**/node_modules/**' --glob '!openspec/changes/ahora-mismo-esta-integrado-en-musiclog-run4aa45/**' '(spec[ -]?kit|speckit|\.specify)' .`; verificar que no devuelve coincidencias.
- [x] 5.2 Validar los artefactos OpenSpec con `openspec validate "ahora-mismo-esta-integrado-en-musiclog-run4aa45" --type change --no-interactive`; verificar que la validación termina correctamente, reconoce `skip_specs: true` y no exige un delta de specs.
- [x] 5.3 Comprobar que la limpieza no toca el producto ni introduce errores de formato; verificar `git diff --check` y que la lista de cambios solo contiene documentación, tooling de workflow y artefactos de planificación permitidos.
