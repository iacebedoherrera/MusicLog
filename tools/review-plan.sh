#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Uso:
  ./tools/review-plan.sh <directorio-de-feature>

Ejemplo:
  ./tools/review-plan.sh specs/001-export-csv

El directorio debe contener spec.md y plan.md.
El script genera o sobrescribe review.md.
USAGE
}

if [[ $# -ne 1 ]]; then
  usage
  exit 2
fi

FEATURE_DIR="$1"
SPEC_FILE="$FEATURE_DIR/spec.md"
PLAN_FILE="$FEATURE_DIR/plan.md"
TASKS_FILE="$FEATURE_DIR/tasks.md"
REVIEW_FILE="$FEATURE_DIR/review.md"

for required_file in "$SPEC_FILE" "$PLAN_FILE"; do
  if [[ ! -f "$required_file" ]]; then
    echo "Error: falta el archivo requerido: $required_file" >&2
    exit 1
  fi
done

TASKS_CONTEXT="No existe todavía tasks.md; revisa únicamente especificación y plan."
if [[ -f "$TASKS_FILE" ]]; then
  TASKS_CONTEXT="$(cat "$TASKS_FILE")"
fi

PROMPT="$(cat <<EOF_PROMPT
Actúas como revisor técnico independiente y adversarial para MusicLog.

Tu objetivo es determinar si el plan puede pasar a generación de tareas e
implementación. No implementes código, no modifiques archivos y no reescribas
la especificación ni el plan. Inspecciona el repositorio en modo solo lectura.

Aplica obligatoriamente `.specify/memory/constitution.md`.

Comprueba como mínimo:
- Cumplimiento de requisitos y criterios de aceptación de spec.md.
- Límites de módulos de Spring Modulith y dependencias entre módulos.
- Compatibilidad de API, DTOs, eventos y persistencia.
- Cambios de Flyway y compatibilidad de PostgreSQL.
- Autorización, validación, gestión de errores y privacidad.
- Uso correcto de integraciones MusicBrainz, Cover Art Archive y Spotify.
- Necesidad y justificación de dependencias nuevas.
- Cobertura de pruebas: JUnit/Mockito, integración/Spring Modulith y Vitest.
- Comandos reales que deben ejecutarse para validar el cambio.
- Riesgos, decisiones arquitectónicas y preguntas abiertas.

Un problema BLOCKING solo puede ser algo que pueda provocar un fallo funcional,
vulnerabilidad, regresión, incompatibilidad, incumplimiento de la constitución
o que impida implementar correctamente. No uses BLOCKING para preferencias de
estilo.

Devuelve EXCLUSIVAMENTE Markdown y usa exactamente esta estructura:

# Revisión técnica del plan

## Veredicto
VERDICT: APPROVED
o
VERDICT: CHANGES_REQUIRED
o
VERDICT: HUMAN_DECISION_REQUIRED

## Resumen
[Máximo cinco líneas.]

## Hallazgos
- [BLOCKING|WARNING|INFO] ID: título
  - Evidencia: archivo, módulo, contrato o requisito concreto.
  - Impacto: consecuencia concreta.
  - Cambio requerido: modificación mínima necesaria del plan.

Si no hay hallazgos, escribe: "Sin hallazgos".

## Validaciones requeridas
- [ ] comando real y motivo

## Preguntas o decisiones humanas
- [Pregunta concreta, solo si es material.]

Reglas de veredicto:
- APPROVED exige cero hallazgos BLOCKING y cero preguntas materiales.
- CHANGES_REQUIRED exige uno o más BLOCKING.
- HUMAN_DECISION_REQUIRED exige una decisión de producto, seguridad,
  compatibilidad o arquitectura que no pueda deducirse del repositorio.

--- CONSTITUCIÓN ---
$(cat .specify/memory/constitution.md)

--- ESPECIFICACIÓN ---
$(cat "$SPEC_FILE")

--- PLAN ---
$(cat "$PLAN_FILE")

--- TAREAS, SI EXISTEN ---
$TASKS_CONTEXT
EOF_PROMPT
)"

TMP_FILE="$(mktemp "${REVIEW_FILE}.tmp.XXXXXX")"
trap 'rm -f "$TMP_FILE"' EXIT

codex exec \
  --ephemeral \
  --sandbox read-only \
  --output-last-message "$TMP_FILE" \
  - <<< "$PROMPT"

if ! grep -qE '^VERDICT: (APPROVED|CHANGES_REQUIRED|HUMAN_DECISION_REQUIRED)$' "$TMP_FILE"; then
  echo "Error: el revisor no emitió un veredicto válido." >&2
  echo "Salida temporal conservada en: $TMP_FILE" >&2
  trap - EXIT
  exit 1
fi

mv "$TMP_FILE" "$REVIEW_FILE"
trap - EXIT

VERDICT="$(grep -E '^VERDICT: ' "$REVIEW_FILE" | head -1 | cut -d' ' -f2)"

echo "Revisión guardada en: $REVIEW_FILE"
echo "Veredicto: $VERDICT"

if [[ "$VERDICT" != "APPROVED" ]]; then
  exit 10
fi
