# SOAP/REST Case Study

A Spring Boot application that integrates with a public SOAP web service (CountryInfoService) to look up country information by name, persists the results to a database, and exposes a REST API for CRUD operations.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Structure](#project-structure)
3. [Running Locally (H2 in-memory DB)](#running-locally)
4. [Running with Docker (PostgreSQL)](#running-with-docker)
5. [REST API Reference](#rest-api-reference)
6. [SOAP Endpoints Used](#soap-endpoints-used)
7. [Testing Examples (curl)](#testing-examples)

---

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17+ |
| Maven | 3.9+ |
| Docker & Docker Compose | Latest |

---

## Project Structure

```
soap-rest-case-study/
├── src/
│   ├── main/
│   │   ├── java/com/ncba/casestudy/
│   │   │   ├── CaseStudyApplication.java
│   │   │   ├── config/         # RestTemplate bean & timeouts
│   │   │   ├── controller/     # REST endpoints
│   │   │   ├── dto/            # Request/Response DTOs
│   │   │   ├── exception/      # Custom exceptions & global handler
│   │   │   ├── model/          # JPA entities (Country, CountryLanguage)
│   │   │   ├── repository/     # Spring Data JPA repositories
│   │   │   └── service/        # Business logic & SOAP client
│   │   └── resources/
│   │       ├── application.properties        # Local / H2 defaults
│   │       └── application-prod.properties   # Production / PostgreSQL
│   └── test/
│       ├── java/com/ncba/casestudy/
│       └── resources/application-test.properties
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Running Locally

```bash
# 1. Clone the repository
git clone <repo-url>
cd soap-rest-case-study

# 2. Build the project
mvn clean package -DskipTests

# 3. Run the application
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

---

## Running with Docker

> Uses **PostgreSQL** via Docker Compose.

```bash
# 1. Build and start all services (app + postgres)
docker-compose up --build

# 2. To stop and remove containers
docker-compose down

# 3. To remove volumes as well (wipes the database)
docker-compose down -v
```

The application starts on **http://localhost:8080**.

### Build the Docker image only

```bash
docker build -t soap-rest-case-study .
```

---

## REST API Reference

Base URL: `http://localhost:8080/api/v1/countries`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/countries` | Fetch country info via SOAP & persist |
| `GET` | `/api/v1/countries` | Retrieve all saved countries |
| `GET` | `/api/v1/countries/{id}` | Retrieve a country by ID |
| `PUT` | `/api/v1/countries/{id}` | Update a saved country |
| `DELETE` | `/api/v1/countries/{id}` | Delete a country by ID |

### Request / Response Format

#### POST `/api/v1/countries`

**Request body:**
```json
{
  "name": "Tanzania"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Country information retrieved and saved successfully",
  "data": {
    "id": 1,
    "isoCode": "TZ",
    "name": "Tanzania",
    "capitalCity": "Dodoma",
    "phoneCode": "255",
    "continentCode": "AF",
    "currencyIsoCode": "TZS",
    "countryFlag": "http://www.oorsprong.org/WebSamples.CountryInfo/Flags/Tanzania.jpg",
    "languages": [
      { "isoCode": "SW", "name": "Swahili" }
    ],
    "createdAt": "2026-03-18T10:00:00",
    "updatedAt": "2026-03-18T10:00:00"
  }
}
```

#### PUT `/api/v1/countries/{id}`

**Request body (all fields optional):**
```json
{
  "capitalCity": "Dar es Salaam",
  "phoneCode": "255"
}
```

---

## SOAP Endpoints Used

WSDL: `http://webservices.oorsprong.org/websamples.countryinfo/CountryInfoService.wso?WSDL`

| Operation | Input | Output |
|-----------|-------|--------|
| `CountryISOCode` | `sCountryName` | `CountryISOCodeResult` |
| `FullCountryInfo` | `sCountryISOCode` | `FullCountryInfoResult` |

---

## Testing Examples

```bash
# Fetch & persist a country
curl -X POST http://localhost:8080/api/v1/countries \
  -H "Content-Type: application/json" \
  -d '{"name": "kenya"}'

# Get all countries
curl http://localhost:8080/api/v1/countries

# Get country by ID
curl http://localhost:8080/api/v1/countries/1

# Update country
curl -X PUT http://localhost:8080/api/v1/countries/1 \
  -H "Content-Type: application/json" \
  -d '{"capitalCity": "Nairobi"}'

# Delete country
curl -X DELETE http://localhost:8080/api/v1/countries/1

# Application health check
curl http://localhost:8080/actuator/health
```

---

## Interact with PostgreSQL DB

```bash
docker exec -it soap_rest_postgres psql -U ${username}$ -d ${db}$
```

## Notes

- **Logging**: All service calls and errors are logged via SLF4J / Logback at `DEBUG` level for application code and `INFO` for Spring internals.
- **Error handling**: A global `@RestControllerAdvice` handler returns consistent JSON error responses for validation failures, not-found errors, SOAP connectivity issues, and unexpected exceptions.
- **Security**: XML parsing uses a hardened `DocumentBuilderFactory` (XXE prevention); user input is XML-escaped before being embedded in SOAP payloads.
- **Idempotency**: Re-posting the same country name will update the existing record rather than creating a duplicate (upsert by ISO code).
