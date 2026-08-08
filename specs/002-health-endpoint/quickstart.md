# Quickstart: Health Endpoint Validation

## Prerequisites

- Java 21 and Maven 3.9 or later.
- No Docker, database, cache, external-service, dependency, or environment change is required to execute the automated validation; the existing test profile uses in-memory test configuration.

## Automated validation

From the repository root, run:

```bash
mvn test
```

Expected result:

1. The existing test suite passes, including the Modulith structure verification.
2. The new health HTTP integration test sends an existing-service-generated bearer token to `GET /health`.
3. That authenticated request receives HTTP 200 with `application/json` and exactly `{"status":"UP"}`; the literal one-field comparison proves no extra or sensitive fields are present.
4. A `GET /health` request without authentication does not receive a successful health response, demonstrating that the current access policy was not changed.
5. An authenticated `POST /health` receives HTTP 405 and never the body `{"status":"UP"}`. The current security configuration disables CSRF and permits the valid JWT to reach Spring MVC, where the GET-only mapping rejects POST.
6. The integration test reads `/v3/api-docs` and verifies that `/health` declares `bearerAuth` and a closed response schema with `additionalProperties: false`, required `status`, and the sole allowed value `UP`.

Because this feature updates the frontend API-contract documentation, also run the existing frontend checks from the repository root:

```bash
npm --prefix frontend run typecheck
npm --prefix frontend run lint
npm --prefix frontend run test
npm --prefix frontend run build
```

Expected result: all commands succeed. These checks validate the frontend workspace after its documentation-only change; no frontend runtime behavior or consumer test is added.

## Optional manual validation

Use the normal local startup described in [README.md](../../README.md) without changing its configuration. Obtain a valid JWT through the existing authentication flow, then request:

```bash
curl -i http://localhost:8080/health \
  -H 'Authorization: Bearer <valid-jwt>'
```

Expected result: HTTP 200 and the JSON body `{"status":"UP"}`. Do not place a real token in source control, documentation, test fixtures, or command history shared with others.

For the API definition, see [health.openapi.yaml](contracts/health.openapi.yaml). For the response shape and absence of persistence, see [data-model.md](data-model.md). The implementation also updates `frontend/docs/api-contract.md` with the identical protected route and response contract.
