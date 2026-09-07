# MusicLog frontend

Aplicación web independiente para el backend MusicLog. El primer vertical cubre
autenticación, búsqueda y fichas de catálogo, y el ciclo completo de reseñas.

## Requisitos

- Node.js 20.19 o superior
- Backend MusicLog en marcha en `http://localhost:8080` para el desarrollo local

## Arranque local

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Vite sirve la aplicación por defecto en `http://localhost:5173` y redirige las
peticiones `/api` al backend indicado por `VITE_PROXY_TARGET`.

## Variables de entorno

| Variable            | Valor local por defecto | Uso                                                                                                        |
| ------------------- | ----------------------- | ---------------------------------------------------------------------------------------------------------- |
| `VITE_API_URL`      | `/api`                  | Prefijo público de la API. En producción debe mantenerse relativo cuando frontend y API comparten dominio. |
| `VITE_PROXY_TARGET` | `http://localhost:8080` | Destino del proxy de Vite, solo en desarrollo. No forma parte del bundle de producción.                    |

## Comandos

```bash
npm run dev           # servidor de desarrollo
npm run typecheck     # TypeScript estricto
npm run lint          # ESLint sin warnings
npm run test          # pruebas de Vitest
npm run build         # typecheck y bundle de producción
npm run format:check  # comprueba formato Prettier
```

## Despliegue

El build se genera con `npm run build` en `dist/`. Publícalo tras un reverse proxy
que entregue la SPA y reenvíe `/api`, `/v3/api-docs` y `/swagger-ui` al backend. De
este modo no hace falta habilitar CORS entre orígenes. El proxy configurado en Vite
no es una configuración de producción.

El despliegue completo del proyecto ya incluye este reverse proxy mediante
`docker compose up -d --build` desde la raíz. El frontend se sirve en
`http://localhost:3000` y reenvía las rutas de API y OpenAPI al servicio interno
`backend`.

La persistencia de JWT en `localStorage` está aislada en
`src/features/auth/authStorage.ts` y es temporal para desarrollo. Antes de publicar,
migrar a cookies `HttpOnly`, `Secure` y `SameSite`, junto con una estrategia de
renovación de sesión.

El contrato de API que usa la aplicación está documentado en
[docs/api-contract.md](docs/api-contract.md).
