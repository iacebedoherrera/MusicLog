# Research: Health Endpoint

## Decision 1: Place the endpoint in `shared.web`

**Decision**: Add the controller and its one-field response record to the existing `com.musiclog.shared.web` package.

**Rationale**: The endpoint has no business-domain ownership, persistent state, event, or external dependency. `shared.web` is already an explicitly named shared web interface, while the constitution limits backend code to the existing business modules or `shared`.

**Alternatives considered**:

- Create a new `health` module — rejected because it adds a top-level module for one cross-cutting route and conflicts with the declared module boundaries.
- Place it in a business module — rejected because health has no dependency on user, catalog, review, social, or Spotify behavior.

## Decision 2: Preserve the existing authentication policy

**Decision**: Do not change the security configuration. `GET /health` remains protected by the existing default rule and returns its successful response only after JWT authentication succeeds. The protected controller obtains `AuthenticatedUser` through the established mechanism without using or exposing identity data.

**Rationale**: The feature clarification selects authenticated callers, the existing final security rule already requires authentication for all non-exempted routes, and the constitution requires protected operations to use `AuthenticatedUser`. This preserves the explicit scope restriction against authentication changes while retaining the project's security convention.

**Alternatives considered**:

- Add an unauthenticated exception for `/health` — rejected because it changes access policy and contradicts the clarified requirement.
- Add a role-specific rule — rejected because no role distinction is required and it would alter security behavior.

## Decision 3: Use a focused HTTP integration test with a real test JWT

**Decision**: Add a Spring Boot HTTP integration test with MockMvc. Generate a bearer token through the existing `JwtService` and send it through the existing security filter; verify the exact success contract and that an unauthenticated request is not successful.

**Rationale**: This exercises the route, response serialization, and the unchanged JWT security path together without a running server or new dependency. MockMvc covers the Spring MVC request/response pipeline, and Spring Security documents MockMvc support for authenticated request testing.

**Alternatives considered**:

- Unit-test the controller alone — rejected because it would not demonstrate that the existing authentication policy admits the success response.
- Use a mocked security context only — rejected because it would bypass the application JWT filter rather than validate the required bearer-token path.
- Run a live server test — rejected because it adds startup and infrastructure requirements without additional value for this route.

## Decision 4: Do not add a persistent data model or dependency check

**Decision**: The endpoint returns a constant availability response and does not query databases, caches, or external services.

**Rationale**: The specification defines a basic application availability signal and explicitly excludes internal and infrastructure details. Checking dependencies would broaden the contract into diagnostics and can disclose operational state.

**Alternatives considered**:

- Include database, cache, or external-service status — rejected because it violates the minimal-response and no-infrastructure-details requirements.
- Persist health history — rejected because the feature requires no user or persistent data.

## Decision 5: Keep OpenAPI and frontend contract documentation synchronized

**Decision**: Document the protected operation and constrained `UP` response with the project's existing OpenAPI annotations, assert the generated `/v3/api-docs` document in the HTTP integration test, and update `frontend/docs/api-contract.md` with the same contract.

**Rationale**: The project identifies generated OpenAPI as its live specification and the constitution requires the frontend API-contract document to change with every compatible backend contract. Testing the generated document prevents the implementation annotations from drifting from the design contract.

**Alternatives considered**:

- Update only `contracts/health.openapi.yaml` — rejected because it would leave the runtime-generated document and frontend contract documentation divergent.
- Add a frontend API client or screen — rejected because no frontend consumer needs this operational endpoint; documentation preserves traceability without expanding behavior.

## Decision 6: Treat authenticated POST as a method-not-allowed request

**Decision**: Test authenticated `POST /health` as HTTP 405 and verify that it never returns `{"status":"UP"}`.

**Rationale**: The existing security chain disables CSRF, accepts a valid JWT through `JwtAuthFilter`, and applies `anyRequest().authenticated()` without a method-specific block for `/health`. An authenticated POST therefore reaches Spring MVC; because the planned controller exposes only `GET`, Spring MVC's existing unmatched-method behavior is HTTP 405. No security or exception-handler change is required.

**Alternatives considered**:

- Expect a security rejection before MVC — rejected because CSRF is disabled and a valid JWT satisfies the existing authorization rule.
- Add a POST mapping or a security matcher — rejected because either option changes the requested GET-only contract or `SecurityConfig`.

## Sources

- Local architecture, security, and OpenAPI: `src/main/java/com/musiclog/shared/security/SecurityConfig.java`, `src/main/java/com/musiclog/shared/security/JwtAuthFilter.java`, `src/main/java/com/musiclog/shared/security/JwtService.java`, `src/main/java/com/musiclog/shared/web/package-info.java`, and `src/main/java/com/musiclog/shared/config/OpenApiConfig.java`.
- Local test support: `pom.xml`, `src/test/java/com/musiclog/SpringModulithIntegrationTest.java`, and `src/test/resources/application-test.yml`.
- Frontend contract and quality commands: `frontend/docs/api-contract.md` and `frontend/package.json`.
- [Spring Framework MockMvc reference](https://docs.spring.io/spring-framework/reference/testing/mockmvc.html).
- [Spring Security MockMvc authentication testing](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/authentication.html).
