# Dokumentasjon

<img src="bilder/vera.png" width="400" />   

> [!IMPORTANT]
> **Målet med dette dokumentet** er å gjøre det enkelt for nye utviklere å forstå systemet, kjøre det lokalt, teste det og videreutvikle det.
>
> **Sist oppdatert:** 2026-04-09

> [!TIP]
> **For å sjekke og teste nettsiden med en gang:**
> Gå til **https://idatt2105-mappe-2026-production.up.railway.app/login**

---

## Innhold

- [Dokumentasjon](#dokumentasjon)
  - [Innhold](#innhold)
  - [1. Kort om prosjektet](#1-kort-om-prosjektet)
  - [2. Hva som kreves levert (krav → hvor det finnes)](#2-hva-som-kreves-levert-krav--hvor-det-finnes)
  - [3. Systemoversikt og arkitektur](#3-systemoversikt-og-arkitektur)
    - [3.1 Moduler og ansvar](#31-moduler-og-ansvar)
    - [3.2 Arkitekturdiagrammer](#32-arkitekturdiagrammer)
    - [3.3 Request-flyt (viktig for forståelse)](#33-request-flyt-viktig-for-forståelse)
  - [4. Kjøring lokalt med docker (Anbefalt)](#4-kjøring-lokalt-med-docker-anbefalt)
    - [4.1 Forutsetninger](#41-forutsetninger)
    - [4.2 Start alt](#42-start-alt)
    - [4.3 Stoppe / reset](#43-stoppe--reset)
  - [5. Kjøring lokalt uten docker (utvikling)](#5-kjøring-lokalt-uten-docker-utvikling)
    - [5.1 Backend](#51-backend)
    - [5.2 Frontend](#52-frontend)
  - [6. API-dokumentasjon (Swagger/OpenAPI)](#6-api-dokumentasjon-swaggeropenapi)
    - [6.1 Swagger UI (samlet)](#61-swagger-ui-samlet)
    - [6.2 Direkte OpenAPI JSON](#62-direkte-openapi-json)
    - [6.3 Forklaring av endepunkter](#63-forklaring-av-endepunkter)
  - [7. Autentisering og autorisasjon](#7-autentisering-og-autorisasjon)
    - [7.1 Mekanisme](#71-mekanisme)
    - [7.2 Viktige endepunkter](#72-viktige-endepunkter)
    - [7.3 Roller](#73-roller)
      - [7.3.1 Rolle-matrise (implementert tilgang)](#731-rolle-matrise-implementert-tilgang)
  - [8. Database, migreringer og testdata](#8-database-migreringer-og-testdata)
    - [8.1 Database (Docker)](#81-database-docker)
    - [8.2 Flyway migreringer](#82-flyway-migreringer)
    - [8.3 Testbrukere](#83-testbrukere)
    - [8.4 Eksterne integrasjoner (S3/Resend)](#84-eksterne-integrasjoner-s3resend)
  - [9. Testing og code coverage](#9-testing-og-code-coverage)
    - [9.1 Backend (JUnit/Kotest + JaCoCo)](#91-backend-junitkotest--jacoco)
    - [9.2 Frontend (Vitest)](#92-frontend-vitest)
    - [9.3 Coverage](#93-coverage)
  - [10. CI/CD](#10-cicd)
  - [11. OWASP og universell utforming](#11-owasp-og-universell-utforming)
    - [11.1 OWASP (hva som er implementert)](#111-owasp-hva-som-er-implementert)
    - [11.2 Universell utforming (tilgjengelighet)](#112-universell-utforming-tilgjengelighet)
  - [12. Vedlikehold og videreutvikling](#12-vedlikehold-og-videreutvikling)
    - [12.1 Vanlige utviklingsoppgaver](#121-vanlige-utviklingsoppgaver)

---

## 1. Kort om prosjektet

**IK System** er et digitalt internkontrollsystem for serveringsbransjen.
Systemet er delt i to toppnivå-tjenester:

- **IK-Mat**: temperatur, HACCP-oppsett, matrelaterte avvik, dashboard
- **IK-Alkohol**: aldersverifisering, alkoholpolicy, alkoholrelaterte avvik, prikkbelastning, dashboard

Begge deler **gjenbruker felles funksjonalitet** fra `ik-common` (f.eks. auth, organisasjoner/tenant, brukere, dokumenter, sjekklister, rapporter, varsler).

> [!NOTE]
> Oppgaveteksten krever at systemet støtter flere tenants. I dette prosjektet er det modellert som **organisasjoner** med **medlemskap/roller**.

---

## 2. Hva som kreves levert (krav → hvor det finnes)

Tabellen under speiler dokumentasjonskravene fra oppgaveteksten (pkt. 5–6) og viser hvor vi dekker det.

| Krav fra oppgave | Hvordan vi oppfyller | Hvor i repo | Status |
|---|---|---|---|
| API-endepunkt dokumentasjon (Swagger + forklaring) | OpenAPI JSON + samlet Swagger UI-side | `frontend/swagger.html` + OpenAPI på backend | ok |
| Kode dokumentert (javadoc/kdoc) | KDoc/Javadoc på sentrale klasser og konfig | `backend/**/src/main/**` | ok |
| Systemdokumentasjon (ny utvikler kan komme i gang) | Kjøring, arkitektur, moduler, dataflyt, testing | **Dette dokumentet** | ok |
| Testdata (testbrukere, DB creds) | Seed-data via Flyway + tabell her | `backend/ik-common/src/main/resources/db/migration/` | ok |
| Avhengigheter/prereqs dokumentert | Docker, Java/Node, secrets, S3/Resend | Seksjon 4–5 + `.env.example` | ok |
| Kjøring av system og tester “enkelt” | `docker compose up --build`, `./mvnw …`, `npm run …` | Rot-README + denne | ok |
| DB scripts (Flyway) + testdata | Flyway migreringer med seed | `backend/ik-common/src/main/resources/db/migration/` | ok |
| CI/CD brukt underveis | GitHub Actions workflow | `.github/workflows/ci.yml` | ok |
| OWASP + universell utforming | Sikkerhetstiltak + TODO-liste | Seksjon 11 | ok |

---

## 3. Systemoversikt og arkitektur

### 3.1 Moduler og ansvar

| Del | Mappe | Ansvar |
|---|---|---|
| Frontend (SPA) | `frontend/` | Vue 3-app, routing, UI, API-kall |
| Backend: felles | `backend/ik-common/` | Auth/JWT, organisasjoner, brukere, sjekklister, dokumenter, varsler, rapporter, DB migreringer |
| Backend: IK-Mat | `backend/ik-food-service/` | Temperatur + HACCP + matavvik + mat-dashboard |
| Backend: IK-Alkohol | `backend/ik-alcohol-service/` | Aldersverifisering + alkoholpolicy + alkoholavvik + prikkbelastning + alkohol-dashboard |
| Dokumentasjon | `docs/` | Systemdokumentasjon, diagrammer, bilder |

### 3.2 Arkitekturdiagrammer

**Klassediagram:**

<img src="diagrammer/classdiagram.png" width="500" />


**Container-arkitektur arkitektur:**

<img src="diagrammer/container-arkitektur.png" width="500" />


**Sekvensdiagram: innlogging + organisasjonsvalg:**

<img src="diagrammer/auth-sekvens.png" width="500" />


**System-kontekst:**

<img src="diagrammer/system-kontekst.png" width="500" />


### 3.3 Request-flyt (viktig for forståelse)

Frontend kjører bak Nginx og **proxyer API-kall** til riktig backend-tjeneste.

- Standard `/api/v1/**` går til **IK-Mat**
- Noen path-er rutes eksplisitt til **IK-Alkohol** (f.eks. alkoholavvik, age verification)

> [!NOTE]
> Denne rutingen er definert i `frontend/nginx.conf`.

---

## 4. Kjøring lokalt med docker (Anbefalt)

### 4.1 Forutsetninger

Prosjektet er avhengig av følgende for at det skal kunne kjøres, testes og bygges lokalt. Listen er samlet fra alle `pom.xml`-filene i backend og `frontend/package.json`.

| Grunnleggende forutsetning | Hvorfor den trengs | Kommentar |
|---|---|---|
| Docker Desktop | Starter database, backend og frontend via containere | Anbefalt måte å kjøre hele systemet på |
| Docker Compose | Orkestrerer alle tjenestene i `docker-compose.yml` | Inngår i Docker Desktop |
| Git | For å klone, oppdatere og jobbe med repoet | Nødvendig for utviklingsflyt |
| Java 21 | Backend er bygget med Spring Boot/Kotlin på Java 21 | Brukes ved lokal backend-kjøring uten Docker |
| Maven Wrapper (`./mvnw`) | Bygger og tester backend uten egen Maven-installasjon | Ligger i `backend/` |
| Node.js 20.19+ eller 22.12+ | Kjører frontend med Vite | Brukes ved lokal frontend-kjøring uten Docker |
| npm | Installerer og starter frontend-avhengigheter | Kommer med Node.js |
| MySQL 8.4 | Database for applikasjonen | Starter automatisk via Docker Compose |
| `.env` / `.env.example` | Inneholder nødvendige secrets og integrasjonsvariabler | Særlig viktig for S3 og Resend |
| AWS S3-konfigurasjon | Brukes til dokumentlagring | Kreves hvis du tester filopplasting mot ekte lagring |
| Resend-konfigurasjon | Brukes til e-postutsendelser | Kan ofte settes i dev-modus lokalt |

| Backend-avhengighet | Bruk / ansvar | Hvor det brukes |
|---|---|---|
| `org.springframework.boot:spring-boot-starter-parent` 3.5.12 | Parent for alle backend-moduler | `backend/pom.xml` |
| Kotlin 2.1.10 (`kotlin-stdlib`, `kotlin-reflect`, `kotlin-maven-plugin`) | Språk og bygg av Kotlin-koden | `backend/pom.xml`, alle backend-moduler |
| `com.iksystem:ik-common` | Felles backend-lag mellom mat og alkohol | `backend/ik-food-service/pom.xml`, `backend/ik-alcohol-service/pom.xml` |
| `org.springframework.boot:spring-boot-starter-data-jpa` | Database- og repository-lag | `backend/ik-common/pom.xml` |
| `org.springframework.boot:spring-boot-starter-security` | Autentisering og autorisasjon | `backend/ik-common/pom.xml` |
| `org.springframework.boot:spring-boot-starter-validation` | DTO- og inputvalidering | `backend/ik-common/pom.xml` |
| `org.springframework.boot:spring-boot-starter-web` | REST API og web-endepunkter | `backend/ik-common/pom.xml` |
| `org.flywaydb:flyway-core` | Database-migreringer | `backend/ik-common/pom.xml` |
| `org.flywaydb:flyway-mysql` | Flyway-støtte for MySQL | `backend/ik-common/pom.xml` |
| `com.mysql:mysql-connector-j` | JDBC-driver mot MySQL | `backend/ik-common/pom.xml` |
| `com.h2database:h2` | In-memory database for test og lokal bruk | `backend/ik-common/pom.xml` |
| `io.jsonwebtoken:jjwt-api` | JWT-kontrakter | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `io.jsonwebtoken:jjwt-impl` | JWT-implementasjon | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `io.jsonwebtoken:jjwt-jackson` | JWT + Jackson serialisering | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | OpenAPI/Swagger-dokumentasjon | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `software.amazon.awssdk:s3` | Dokumentlagring i AWS S3 | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `com.resend:resend-java` | E-postutsendelser | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `com.openhtmltopdf:openhtmltopdf-pdfbox` | PDF-generering | `backend/ik-common/pom.xml` |
| `org.thymeleaf:thymeleaf` | HTML-templating for PDF-generering | `backend/ik-common/pom.xml` |
| `org.springframework.boot:spring-boot-starter-test` | Backendtester | `backend/ik-common/pom.xml`, modul-pomene |
| `org.springframework.security:spring-security-test` | Testing av sikkerhet og autorisasjon | `backend/ik-common/pom.xml`, modul-pomene |
| `io.kotest:kotest-runner-junit5-jvm` | Kotest test-runner | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `io.kotest:kotest-assertions-core-jvm` | Kotest assertions | `backend/pom.xml`, `backend/ik-common/pom.xml` |
| `io.mockk:mockk-jvm` | Mocking i tester | `backend/pom.xml`, `backend/ik-common/pom.xml`, modul-pomene |
| `com.ninja-squad:springmockk` | Spring + MockK-integrasjon | `backend/ik-food-service/pom.xml`, `backend/ik-alcohol-service/pom.xml` |

| Frontend-avhengighet | Bruk / ansvar | Hvor det brukes |
|---|---|---|
| `vue` | UI-rammeverk | `frontend/package.json` |
| `vue-router` | Routing mellom sider | `frontend/package.json` |
| `pinia` | Global state management | `frontend/package.json` |
| `axios` | HTTP-klient mot backend | `frontend/package.json` |
| `@tanstack/vue-query` | Server state og caching | `frontend/package.json` |
| `@tanstack/vue-form` | Skjema og validering | `frontend/package.json` |
| `zod` | Valideringsschema og feilhåndtering | `frontend/package.json` |
| `@vueuse/core` | Gjenbrukbare Vue-komposables | `frontend/package.json` |
| `@internationalized/date` | Dato- og tidsverktøy | `frontend/package.json` |
| `chart.js` | Diagrammer og visualiseringer | `frontend/package.json` |
| `vue-chartjs` | Vue-tilpasning for Chart.js | `frontend/package.json` |
| `lucide-vue-next` | Ikoner i UI | `frontend/package.json` |
| `vue-sonner` | Toast-varsler | `frontend/package.json` |
| `@vitejs/plugin-vue` | Vite-plugin for Vue SFC-er | `frontend/package.json` |
| `vite` | Utviklingsserver og build | `frontend/package.json` |
| `vue-tsc` | Type-sjekk av Vue/TypeScript | `frontend/package.json` |
| `typescript` | Språkstøtte og typing | `frontend/package.json` |
| `eslint` | Linting av frontend-kode | `frontend/package.json` |
| `eslint-plugin-vue` | Vue-spesifikk linting | `frontend/package.json` |
| `eslint-plugin-oxlint` | Ekstra linting-regler | `frontend/package.json` |
| `oxlint` | Rask statisk kodeanalyse | `frontend/package.json` |
| `npm-run-all2` | Samler flere scripts i én kommando | `frontend/package.json` |
| `vitest` | Tester i frontend | `frontend/package.json` |
| `@vitest/coverage-v8` | Coverage for frontendtester | `frontend/package.json` |
| `@testing-library/vue` | Komponenttesting | `frontend/package.json` |
| `happy-dom` | DOM-miljø for tester | `frontend/package.json` |
| `vite-plugin-vue-devtools` | Utviklerverktøy for Vue | `frontend/package.json` |
| `@vue/eslint-config-typescript` | ESLint-oppsett for Vue + TypeScript | `frontend/package.json` |
| `@vue/tsconfig` | TypeScript-konfigurasjon for Vue | `frontend/package.json` |
| `@tsconfig/node24` | Node-spesifikk TypeScript-konfig | `frontend/package.json` |
| `@types/node` | Type-definisjoner for Node | `frontend/package.json` |
| `jiti` | Runtime-laster brukt av tooling | `frontend/package.json` |

| Bygg-/testplugin | Bruk / ansvar | Hvor det brukes |
|---|---|---|
| `kotlin-maven-plugin` | Kompilerer Kotlin-koden | `backend/pom.xml`, modul-pomene |
| `kotlin-maven-allopen` | Gjør Spring-annoterte Kotlin-klasser åpne | `backend/pom.xml` |
| `kotlin-maven-noarg` | Genererer no-arg konstruktører for JPA | `backend/pom.xml` |
| `spring-boot-maven-plugin` | Pakker og starter Spring Boot-modulene | `backend/ik-food-service/pom.xml`, `backend/ik-alcohol-service/pom.xml` |
| `jacoco-maven-plugin` | Måler testdekning | `backend/ik-common/pom.xml`, modul-pomene |

### 4.2 Start alt

Fra repo-roten:

```bash
docker compose up --build
```

Tjenester som starter:

| Tjeneste | URL lokalt | Notat |
|---|---:|---|
| Frontend (Nginx + SPA) | http://localhost | Proxyer `/api/*` videre |
| IK-Mat API | http://localhost:8081 | Kjøres internt på 8080 i container |
| IK-Alkohol API | http://localhost:8082 | Kjøres internt på 8080 i container |
| MySQL | `localhost:3306` | DB: `ik_system` |

### 4.3 Stoppe / reset

```bash
docker compose down
```

Fjern også DB-data:

```bash
docker compose down -v
```

---

## 5. Kjøring lokalt uten docker (utvikling)

### 5.1 Backend

Vi har et samlet dev-script som starter **alt** du trenger i utvikling:

- MySQL (Docker Compose, men kun for denne modulen)
- IK-Mat (port `8081`)
- IK-Alkohol (port `8082`)
- Frontend (Vite, port `5173`)

Kjør fra repo-roten:

```bash
chmod +x dev-up.sh
./dev-up.sh
```

Logger skrives til `.run-logs/` (i repo-roten).

### 5.2 Frontend

Frontend startes automatisk av `./dev-up.sh`.

---

## 6. API-dokumentasjon (Swagger/OpenAPI)

### 6.1 Swagger UI (samlet)

Swagger UI serveres som en statisk side i frontend-containeren:

- http://localhost/swagger.html

Den peker til:

- `/api/mat/v3/api-docs` (IK-Mat)
- `/api/alkohol/v3/api-docs` (IK-Alkohol)

### 6.2 Direkte OpenAPI JSON

- Docker (begge tjenester):
  - IK-Mat: http://localhost:8081/v3/api-docs
  - IK-Alkohol: http://localhost:8082/v3/api-docs
- Uten Docker (én modul lokalt):
  - Aktiv modul: http://localhost:8080/v3/api-docs

> [!NOTE]
> `springdoc.swagger-ui.enabled` er satt til `false` i backend sin `application.yml`. Det betyr at backend ikke serverer egen Swagger UI som standard, men OpenAPI JSON er tilgjengelig.

### 6.3 Forklaring av endepunkter


> [!TIP]
> For full detaljert spesifikasjon (schemas + prøving) bruk http://localhost/swagger.html.

---

## 7. Autentisering og autorisasjon

### 7.1 Mekanisme

- **JWT Bearer token** brukes for API-kall
- Passord er **BCrypt-hashet**
- API er konfigurert som **stateless** (ingen server-side session for API)

### 7.2 Viktige endepunkter

**Offentlige auth-endepunkter** (krever ikke JWT):

- `POST /api/v1/auth/register` (oppretter identitet → `preAuthToken`)
- `POST /api/v1/auth/login` (fase 1 → `preAuthToken` + memberships)
- `POST /api/v1/auth/refresh` (ny `accessToken` via `refreshToken`)

**Endepunkter som krever JWT**:

- `POST /api/v1/auth/select-org` (fase 2, org-scope)
- `POST /api/v1/auth/switch-org` (bytter org-kontekst)
- `GET /api/v1/auth/memberships` (org-switcher)
- `POST /api/v1/auth/logout` (revoker tokens/sessions)

Eksempel på auth-header:

```http
Authorization: Bearer <ACCESS_TOKEN>
```

> [!TIP]
> Swagger UI (http://localhost/swagger.html) støtter “Authorize” med JWT.

### 7.3 Roller

Systemet bruker roller per organisasjon (membership):

- `ADMIN`
- `MANAGER`
- `EMPLOYEE`

#### 7.3.1 Rolle-matrise (implementert tilgang)

Tabellen under er basert på `@PreAuthorize` i controllerne.

| Funksjon/område | EMPLOYEE | MANAGER | ADMIN |
|---|:---:|:---:|:---:|
| Logge inn + velge org | x | x | x |
| Se sjekklister + stats | x | x | x |
| Opprette/endre/slette sjekklister | - | x | x |
| Se training logs | x | x | x |
| Opprette/endre/slette training logs | - | x | x |
| Rapport preview/generate/export | - | x | x |
| Slette rapport | - | - | x |
| Dokument: hent URL | x | x | x |
| Dokument: upload/slett | - | x | x |
| Invitasjoner: send invite | - | x | x |
| Bruker/medlemskap: liste + administrere medlemmer | - | x | x |
| Fjerne medlem fra org | - | - | x |

> [!NOTE]
> Noen endepunkter uten `@PreAuthorize` kan likevel være begrenset i service-laget. Tabellen over viser det som er eksplisitt definert i controller-nivå.

---

## 8. Database, migreringer og testdata

**Databasetabelldiagram (ERD):**

<img src="diagrammer/databasetabelldiagram.png" width="900" />

### 8.1 Database (Docker)

- **DB**: `ik_system`
- **User**: `ik_user`
- **Pass**: `ik_password`
- **Port**: `3306`

Se `docker-compose.yml` og `.env.example`.

### 8.2 Flyway migreringer

Migreringer og seed-data ligger i:

- `backend/ik-common/src/main/resources/db/migration/`

Kritisk seed-fil:

- `V25__full_seed_data.sql` (full reset + realistiske testdata)

### 8.3 Testbrukere

Alle testbrukere har passord: `password`

| E-post | Rolle(r) | Organisasjon(er) | Kommentar |
|---|---|---|---|
| `admin@iksystem.local` | ADMIN | Everest + Nordvik | Systemadmin |
| `manager@iksystem.local` | MANAGER | Everest | Manager |
| `employee@iksystem.local` | EMPLOYEE | Everest | Ansatt |
| `kokk@iksystem.local` | EMPLOYEE | Everest | Kokk |
| `bartender@iksystem.local` | EMPLOYEE | Everest + Nordvik | Jobber i begge |
| `daglig@iksystem.local` | MANAGER | Nordvik | Daglig leder |
| `servitor@iksystem.local` | EMPLOYEE | Everest + Nordvik | Jobber i begge |
| `renhold@iksystem.local` | EMPLOYEE | Everest | Renhold |

> [!NOTE]
> Seed-data kan endres over tid. Denne tabellen speiler `V25__full_seed_data.sql`.

### 8.4 Eksterne integrasjoner (S3/Resend)

Prosjektet støtter:

- **AWS S3** (dokumentlagring)
- **Resend** (epost)

Konfigureres via `.env` (start med å kopiere `.env.example`).

> [!TIP]
> For lokal utvikling kan `RESEND_DEV_MODE=true` brukes for å unngå ekte utsendelser (avhengig av implementasjon).

---

## 9. Testing og code coverage

### 9.1 Backend (JUnit/Kotest + JaCoCo)

Kjør alt:

```bash
cd backend
./mvnw -B -ntp clean verify
```

eller
 
```bash
cd backend
mvn clean test 
```
coverage kan man se når man klikker på target - sites, og åpner index.html i browser

Coverage rapporter (JaCoCo) genereres per modul under `target/site/jacoco/`.

### 9.2 Frontend (Vitest)

Kjør tester for komponenter:

```bash
cd frontend/src/components
npx vitest run
```

Kjør coverage:

```bash
cd frontend/src/components
npx vitest run --coverage
```

Start backhand før gjennomføring av E2E testene:
```bash
docker compose up --build
```

kommandoen for å kjøre E2E testene:
```bash
cd frontend
npx playwright test
```

> [!NOTE]
> Frontend CI kjører lint + build i GitHub Actions. Tester kan legges til som ekstra steg hvis ønskelig.

### 9.3 Coverage
> [!IMPORTANT]
> Oppgaveteksten krever **minst 50% code coverage**.

Basert på JaCoCo-rapportene per backend-modul:

| Modul | Instruction coverage | Branch coverage | Oppfyller 50%-kravet |
|---|---:|---:|---:|
| IK-Mat (`ik-food-service`) | 81% | 43% | Ja (instruction) |
| IK-Alkohol (`ik-alcohol-service`) | 85% | 58% | Ja |
| IK-Common (`ik-common`) | 77% | 60% | Ja |

> [!NOTE]
> Oppgavekravet tolkes her som generell code coverage (instruction/line), der alle modulene er over 50%.

**IK-Alkohol (JaCoCo):**

<img src="bilder/IKAlkoholTest.png" width="900" />

**IK-Common (JaCoCo):**

<img src="bilder/IKCommonTest.png" width="900" />

**IK-Mat (JaCoCo):**

<img src="bilder/IKFoodtest.png" width="900" />

---

## 10. CI/CD

CI er definert i GitHub Actions:

- `.github/workflows/ci.yml`

Den kjører:

- Backend: `./mvnw clean verify`
- Frontend: `npm ci`, `npm run lint:ci`, `npm run build`

<img src="bilder/ci.png" width="300" />

---

## 11. OWASP og universell utforming

### 11.1 OWASP (hva som er implementert)

- Passord lagres som BCrypt hash
- JWT bearer tokens for API-autentisering
- Stateless API + CSRF deaktivert (API-scenario)
- CORS konfigurert for lokale origin-er
- OpenAPI security scheme (`bearerAuth`) definert

I tillegg (repo-verifisert):

- **Method-level authorization** via `@PreAuthorize` på kritiske endepunkter (admin/manager)
- **Refresh tokens** brukes for å hente ny access token (`/api/v1/auth/refresh`)
- **S3 presigned URL** for dokumenttilgang (typisk gyldig i 1 time)
- **Opplasting begrenset i proxy**: Nginx `client_max_body_size 10M`

### 11.2 Universell utforming (tilgjengelighet)

Dette er basert på komponentene som håndterer innlogging/registrering/orgvalg.

- **Skjemavalidering + feilmeldinger**: Frontend bruker `@tanstack/vue-form` + `zod` og viser feltspesifikke feil under input.
- **Synlig fokus**: Inputs har `:focus-visible` styling (ring/border) i UI-komponentene.
- **Semantikk**: Formularer bruker `form` + `label` og `button`.


---

## 12. Vedlikehold og videreutvikling

### 12.1 Vanlige utviklingsoppgaver

Legge til en ny feature (høy-nivå oppskrift)

1. Avklar hvilken tjeneste som eier domenet (mat vs alkohol vs common)
2. Legg til/endre DB via ny Flyway-migrering i `ik-common`
3. Implementer service/repository/controller
4. Oppdater OpenAPI (annotations) og legg til tester
5. Oppdater frontend (types, composables, views)
6. Verifiser med `docker compose up --build`

---
