# Digital Loan Processing Platform (sample repo)

Cloud-native personal loan system for a CI/CD + GitOps + Istio + Dynatrace workshop.

```
Customer -> Frontend (nginx) -> Loan Service -+-> Customer Service  -> PostgreSQL
                                              +-> Document Service  -> PostgreSQL
                                              +-> Notification Service (simulated email/SMS)
```

## Repo layout
| Path | Purpose |
|---|---|
| `services/<name>-service/` | Spring Boot 3 / Java 17 service: `pom.xml`, `Dockerfile`, `src/main`, `src/test` |
| `frontend/` | Static portal + nginx reverse proxy, `Dockerfile` |
| `helm/microservice/` | Generic Helm chart reused by every service |
| `helm/postgres/` | PostgreSQL StatefulSet + credentials Secret |
| `helm/values/*.yaml` | Per-service values (image repo/tag) - **the files CI/ArgoCD care about** |
| `docker-compose.yml` | Local smoke test |

> In the later GitOps step, `helm/` moves into its own `loan-gitops-repo`; app code stays here.

## Build and run locally
```bash
docker compose up --build
# open http://localhost:8080
```
Seeded customers: `1` Asha (strong), `2` Ravi (credit score 610 -> rejected), `3` Meena (borderline).
Upload a KYC doc for the customer first, otherwise the loan is rejected ("KYC documents not uploaded").

## Build one image
```bash
cd services/loan-service
mvn -B test                              # unit tests
docker build -t loan-service:1.2.0 .
```

## Helm (manual render / install, before ArgoCD)
```bash
helm template loan-service helm/microservice -f helm/values/loan-service.yaml
kubectl create namespace loan-platform
helm install postgres helm/postgres -n loan-platform
for s in customer-service document-service notification-service loan-service frontend; do
  helm install $s helm/microservice -n loan-platform -f helm/values/$s.yaml
done
```
Replace `YOUR_DOCKERHUB_USER` in `helm/values/*.yaml` first.

## API quick reference
- `POST /api/customers`, `GET /api/customers/{id}`
- `POST /api/documents` (multipart: customerId, docType, file), `GET /api/documents/customer/{id}`
- `POST /api/loans` `{customerId, amount, tenureMonths, purpose}`, `GET /api/loans/{id}`
- `POST /api/notifications`
- All services: `/actuator/health`, `/actuator/prometheus`
