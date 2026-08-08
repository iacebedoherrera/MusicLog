# Data Model: Health Endpoint

## Persistent entities

None. The feature does not create, read, update, or delete persistent data.

## Response model

### HealthResponse

| Field | Type | Required | Rules |
| --- | --- | --- | --- |
| `status` | string | Yes | Fixed value: `UP` |

The serialized successful response is exactly `{"status":"UP"}`. It has no identifier, relationships, lifecycle, storage mapping, user data, configuration values, or infrastructure diagnostics.

## State transitions

None. The response is produced per authenticated request and is not stored or transitioned between states.
