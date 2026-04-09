
![Vera Logo](frontend/src/assets/vera.png)

Semester-prosjekt IDATT2105 – Fullstack-appsutvikling (2026)

Et digitalt internkontrollsystem for restauranter, barer og serveringssteder. Vera hjelper virksomheter å forenkle compliance med helse-, sikkerhets- og alkoholreguleringer ved å digitalisere sjekklister, temperaturlogging og rutiner for avviksoppfølging.

## Rask oppstart

### Forutsetninger
- Docker Desktop (inkludert Docker Compose)
- Git

### Kjør appen lokalt med docker
**Start:**
Fra repo-roten:

```bash
docker compose up --build
```

**Stoppe:**

```bash
docker compose down
```

Fjern alle data (inkludert databasen):

```bash
docker compose down -v
```

### Kjøre program for utvikling: ###
**Start:**
```bash
chmod +x dev-up.sh
./dev-up.sh
```

**Stoppe:**
```bash
Ctrl + c
```

Applikasjonen vil starte på:
- **Frontend**: http://localhost (Nginx + Vue app)
- **Backend API (mat)**: http://localhost:8081
- **Backend API (alkohol)**: http://localhost:8082
- **Database**: mysql:3306

## Testbrukere

Default testdata er satt opp automatisk ved oppstart. Bruk disse credentials for å logge inn:

| Email | Passord | Rolle | Organisasjon |
|-------|---------|-------|--------------|
| `admin@iksystem.local` | `password` | ADMIN | Everest Sushi & Fusion AS + Nordvik Bar & Kjøkken AS |
| `manager@iksystem.local` | `password` | MANAGER | Everest Sushi & Fusion AS |
| `employee@iksystem.local` | `password` | EMPLOYEE | Everest Sushi & Fusion AS |

## Prosjektstruktur

```
FULLSTACK-V2026/
├── backend/           # Spring Boot API (Java 21+)
│   ├── ik-common/           # Felles tjenester
│   ├── ik-food-service/     # IK-Mat modul
│   └── ik-alcohol-service/  # IK-Alkohol modul
├── frontend/          # Vue.js 3 + Vite
├── docker-compose.yml # Container orchestration
└── docs/              # Dokumentasjon
```

## Tech Stack

| Område | Teknologi | Versjon |
|--------|-----------|---------|
| **Frontend** | Vue.js | 3.x |
| | Vite | 7.x |
| | TypeScript | 5.x |
| | Node.js | 20.19+ eller 22.12+ |
| **Backend** | Java | 21 |
| | Kotlin | 2.1+ |
| | Spring Boot | 3.x |
| | Spring Framework | 6.x |
| | Spring Security | 6.x |
| | JPA/Hibernate | Latest |
| **Database** | MySQL | 8.4 |
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

- Se `docs/docs.md`

---
