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
- NgRx 20+
- @angular/material 20+
- STOMP
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
UI: http://localhost:4200

## Запуск через Docker Compose

```bash
docker compose build
docker compose up
```

API: http://localhost:8080/api
UI: http://localhost:8081
H2 Console: http://localhost:8080/h2-console

## Оптимизация

Минимальное значение параметра -Xmx, при котором приложение успешно обрабатывает файл объёмом 50 МБ
равно 27МБ. При меньшем значении docker не может поднять контейнер.