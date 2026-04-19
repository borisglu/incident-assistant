## Platform context (simplified)

Our payment platform consists of:
- **api-gateway**: external HTTP ingress and routing.
- **auth-service**: authentication and token issuance.
- **payment-service**: payment transaction creation and processing.
- **billing-service**: balances and invoicing.
- **notification-service**: email and SMS delivery.
- **reporting-service**: reports and exports (often heavy DB queries).

Operational notes:
- Centralized logging (ELK) for all services.
- PostgreSQL: separate DB instances for payment-service and billing-service.
- payment-service frequently hits external provider errors (timeouts, 5xx, credential issues).
- notification-service can degrade when SMTP/SMS providers fail.
- reporting-service can overload DB with long analytical queries.

## Past incidents (examples)

[INC-101] Customers cannot pay by card. payment-service logs show massive timeouts calling PayGate; started ~12:05 UTC; few other anomalies.

[INC-102] /payments/create latency spikes (5–7s). DB dashboards: high CPU and long-running queries from reporting-service. Some 504s from api-gateway.

[INC-103] Missing top-up confirmation emails; balances correct. notification-service logs: intermittent SMTP connection errors.

[INC-104] Mobile login failures; auth-service 401s; logs mention invalid token signatures; other services mostly normal.
