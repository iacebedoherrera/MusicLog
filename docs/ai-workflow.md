# Flujo de desarrollo asistido por IA

## Regla principal

No se implementa una funcionalidad hasta que Spec Kit haya generado
especificación, plan y tareas, y una revisión técnica independiente haya
devuelto `VERDICT: APPROVED`.

## Flujo por funcionalidad

1. Crear una rama desde `develop`.
2. Ejecutar `$speckit-specify` en Codex.
3. Resolver ambigüedades con `$speckit-clarify`.
4. Generar diseño técnico con `$speckit-plan`.
5. Ejecutar:

   ```bash
   ./tools/review-plan.sh specs/<numero-feature>
   ```

6. Si `review.md` contiene `CHANGES_REQUIRED`, actualizar el plan y repetir
   la revisión. Si contiene `HUMAN_DECISION_REQUIRED`, detenerse y decidir
   antes de seguir.
7. Solo con `APPROVED`, ejecutar `$speckit-tasks`.
8. Ejecutar `$speckit-analyze`.
9. Revisar `spec.md`, `plan.md`, `tasks.md` y `review.md`.
10. Con aprobación humana, ejecutar `$speckit-implement`.
11. Ejecutar los controles obligatorios definidos por la constitución y abrir
    un PR hacia `develop`.

## Límites

- El revisor se ejecuta con Codex en modo de solo lectura.
- La implementación requiere aprobación humana explícita.
- Los artefactos de cada funcionalidad dentro de `specs/` se versionan.
- Las evidencias de tests y el resumen de cambios se incluyen en el PR.
