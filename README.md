
![Vera Logo](frontend/src/assets/vera.png)

Semester-prosjekt IDATT2105 – Fullstack-appsutvikling (2026)

Et digitalt internkontrollsystem for restauranter, barer og serveringsteder. SafeServe hjelper virksomheter å forenkle compliance med helse-, sikkerhets- og alkoholreguleringer ved å digitalisere sjekklister, temperaturlogging og rutiner for avviksoppfølging.

## Rask oppstart

### Forutsetninger
- Docker Desktop (inkludert Docker Compose)
- Git

### Kjør appen lokalt

Fra repo-roten:

```bash
docker compose up --build
```

Applikasjonen vil starte på:
- **Frontend**: http://localhost:5173 (Vue.js + Vite)
- **Backend API**: http://localhost:8080 (Spring Boot)
- **Database**: mysql:3306

### Stopp appen

```bash
docker compose down
```

Fjern alle data (inkludert databasen):

```bash
docker compose down -v
```

## Testbrukere

Default testdata er satt opp automatisk ved oppstart. Bruk disse credentials for å logge inn:

| Email | Passord | Rolle | Organisasjon |
|-------|---------|-------|--------------|
| `admin@iksystem.local` | `password` | ADMIN | IK System |
| `manager@iksystem.local` | `password` | MANAGER | IK System |
| `employee@iksystem.local` | `password` | EMPLOYEE | IK System & Demo Org |

## Prosjektstruktur

```
SafeServe/
├── backend/           # Spring Boot API (Java 17+)
│   ├── ik-common/     # Felles tjenester
│   ├── ik-mat/        # IK-Mat modul
│   └── ik-alkohol/    # IK-Alkohol modul
├── frontend/          # Vue.js 3 + Vite
├── docker-compose.yml # Container orchestration
└── docs/              # Dokumentasjon
```

## Tech Stack

| Område | Teknologi | Versjon |
|--------|-----------|---------|
| **Frontend** | Vue.js | 3.x |
| | Vite | 5.x |
| | TypeScript | 5.x |
| | Node.js | 18+ |
| **Backend** | Java | 21 |
| | Kotlin | 2.1+ |
| | Spring Boot | 3.x |
| | Spring Framework | 6.x |
| | Spring Security | 6.x |
| | JPA/Hibernate | Latest |
| **Database** | MySQL | 8.0 / 9.0 |
| | Flyway | Latest |
| **DevOps** | Docker | Latest |
| | Docker Compose | Latest |
| **API** | Swagger/OpenAPI | 3.0 |
| **Testing** | JUnit 5 | Latest |
| | Jest/Vitest | Latest |

## Database

- **Database**: `ik_system`
- **Bruker**: `ik_user`
- **Passord**: `ik_password`
- **Port**: 3306

Backend bruker **Flyway** for databasemigrasjoner og oppretter/migrerer schema automatisk ved oppstart.

## Dokumentasjon

- **API referanse**: Se Swagger UI på `http://localhost:8080/swagger-ui.html`
- **Systemdokumentasjon**: Se `/docs/` for arkitektur, klasdiagrammer og setup-guide
- **Wiki**: Full teknisk dokumentasjon og arkitektur-notater

## Nyttige kommandoer

**Se logger fra alle tjenester:**
```bash
docker compose logs -f
```

**Se logger fra bare backend:**
```bash
docker compose logs -f backend
```

**Restart bare backend (for rask utvikling):**
```bash
docker compose restart backend
```

**Kjør bare backend lokalt (uten Docker):**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**Kjør bare frontend lokalt (uten Docker):**
```bash
cd frontend
pnpm run dev
```
---
**Innleveringsfrist**: Fredag 10. april 2026 kl. 14:00
