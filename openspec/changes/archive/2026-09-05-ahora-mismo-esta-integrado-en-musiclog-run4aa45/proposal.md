## Why

MusicLog conserva una integración heredada de Spec Kit que deja comandos, skills,
plantillas y documentación apuntando a un flujo que ya no es el mantenido. Ahora
que `ai-flow` centraliza la ejecución de cambios con OpenSpec, retirar esa
integración evita que agentes y colaboradores sigan iniciando el workflow
obsoleto o mantengan dos fuentes de verdad.

## What Changes

- Retirar el árbol `.specify/`, incluidos sus metadatos de integración, scripts,
  plantillas, workflow y registro heredados.
- Retirar las skills generadas bajo `.agents/skills/speckit-*` y cualquier
  referencia de integración asociada.
- Actualizar `README.md` y `docs/ai-workflow.md` para describir el flujo vigente
  basado en OpenSpec y `ai-flow`, conservando las puertas de revisión y
  aprobación que sigan aplicando.
- Retirar o adaptar `tools/review-plan.sh` para que no dependa de la integración
  retirada ni de rutas `.specify`.
- Mantener sin cambios el código de MusicLog, sus pruebas, APIs, dependencias,
  Docker, CI y configuración de secretos.

## Capabilities

### New Capabilities

Ninguna.

### Modified Capabilities

Ninguna. Es un cambio de documentación, tooling y limpieza de integración sin
cambios en requisitos o comportamiento observable del producto; por ello el
cambio declara `skip_specs: true` en `.openspec.yaml`.

## Impact

- Afecta exclusivamente la configuración local del flujo asistido por IA, las
  skills y plantillas heredadas, la documentación de contribución y el script
  auxiliar de revisión.
- No cambia módulos backend/frontend, contratos HTTP, persistencia, eventos,
  dependencias ni despliegues.
- La verificación se centrará en que no queden referencias al flujo retirado
  fuera de los artefactos de este cambio y en que la documentación apunte solo
  a OpenSpec/`ai-flow`.
