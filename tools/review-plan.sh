#!/usr/bin/env bash
set -euo pipefail

usage() {
  cat <<'USAGE'
Uso:
  ./tools/review-plan.sh openspec/changes/<change-name>

Ejemplo:
  ./tools/review-plan.sh openspec/changes/export-csv

El directorio debe ser un cambio OpenSpec y contener proposal.md.
El script lee los deltas de specs/, design.md y tasks.md cuando existen.
Genera o sobrescribe review.md dentro del cambio.
USAGE
}

if [[ $# -ne 1 ]]; then
  usage
  exit 2
fi

CHANGE_DIR="$1"
PROPOSAL_FILE="$CHANGE_DIR/proposal.md"
DESIGN_FILE="$CHANGE_DIR/design.md"
TASKS_FILE="$CHANGE_DIR/tasks.md"
CONSTITUTION_FILE="docs/development-constitution.md"
REVIEW_FILE="$CHANGE_DIR/review.md"

if [[ ! -d "$CHANGE_DIR" ]]; then
  echo "Error: no existe el directorio del cambio: $CHANGE_DIR" >&2
  exit 1
fi

for required_file in "$PROPOSAL_FILE" "$CONSTITUTION_FILE"; do
  if [[ ! -f "$required_file" ]]; then
    echo "Error: falta el archivo requerido: $required_file" >&2
    exit 1
  fi
done

DESIGN_CONTEXT="No existe todavía design.md; revisa la propuesta y los demás artefactos disponibles."
if [[ -f "$DESIGN_FILE" ]]; then
  DESIGN_CONTEXT="$(cat "$DESIGN_FILE")"
fi

TASKS_CONTEXT="No existe todavía tasks.md; revisa la propuesta, el diseño y los deltas disponibles."
if [[ -f "$TASKS_FILE" ]]; then
  TASKS_CONTEXT="$(cat "$TASKS_FILE")"
fi

SPECS_CONTEXT="No existen deltas de especificación para este cambio."
if [[ -d "$CHANGE_DIR/specs" ]]; then
  DELTA_FILES=()
  while IFS= read -r delta_file; do
    DELTA_FILES+=("$delta_file")
  done < <(find "$CHANGE_DIR/specs" -type f -name '*.md' -print | sort)

  if [[ ${#DELTA_FILES[@]} -gt 0 ]]; then
    SPECS_CONTEXT=""
    for delta_file in "${DELTA_FILES[@]}"; do
      SPECS_CONTEXT+=$'\n--- DELTA: '"$delta_file"$' ---\n'
      SPECS_CONTEXT+="$(cat "$delta_file")"
      SPECS_CONTEXT+=$'\n'
    done
  fi
fi

PROMPT="$(cat <<EOF_PROMPT
Actúas como revisor técnico independiente y adversarial para MusicLog.

Tu objetivo es determinar si este cambio OpenSpec puede pasar a aprobación y
aplicación. No implementes código, no modifiques archivos y no reescribas los
artefactos. Inspecciona el repositorio en modo solo lectura.

Aplica obligatoriamente el archivo docs/development-constitution.md.

Comprueba como mínimo:
- Coherencia entre proposal.md, los deltas de especificación, design.md y tasks.md cuando existan.
- Cumplimiento de requisitos y criterios de aceptación del cambio.
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
o que impida aplicar correctamente el cambio. No uses BLOCKING para preferencias
de estilo.

Devuelve EXCLUSIVAMENTE Markdown y usa exactamente esta estructura:

# Revisión técnica del cambio

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
  - Cambio requerido: modificación mínima necesaria de los artefactos.

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
$(cat "$CONSTITUTION_FILE")

--- PROPUESTA ---
$(cat "$PROPOSAL_FILE")

--- DELTAS DE ESPECIFICACIÓN ---
$SPECS_CONTEXT

--- DISEÑO ---
$DESIGN_CONTEXT

--- TAREAS ---
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
