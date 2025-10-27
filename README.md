# LabInvent Test

Тестовый проект: **Spring Boot (Java 21) + Angular 20**  
Состоит из двух модулей:
- `api` — backend
- `ui` — frontend

---

## Требования

- Java 21
- Maven 3.9+
- Node.js 18+ и npm
- Angular CLI 20+
- Docker + Docker Compose (для контейнерного запуска)

---

## Запуск модулей по отдельности

### Backend (api)

```bash
cd api
mvn spring-boot:run
```

### Frontend (ui)

```bash
cd ui
npm install
ng serve
```

## Запуск через Docker Compose

```bash
docker compose build
docker compose up
```

API: http://localhost:8080/api/test
UI: http://localhost:8081
H2 Console: http://localhost:8080/h2-console