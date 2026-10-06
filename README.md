# ExpenseFlow — Java Full-Stack Expense Tracker

ExpenseFlow is a complete portfolio-style expense management application built with **Java, Spring Boot, Spring MVC, Spring Data JPA/Hibernate, MySQL, Thymeleaf, HTML, CSS and JavaScript**.

## Technology
- Java 21
- Spring Boot 3.5.16
- Spring MVC + REST
- Spring Data JPA / Hibernate
- MySQL 8+
- Thymeleaf
- HTML5 / CSS3 / JavaScript
- Bean Validation
- OpenAPI / Swagger UI
- Spring Actuator
- Maven

Spring Boot 3.5.16 is the final OSS release in the 3.5.x generation; Spring's release notes recommend moving to a 4.x line for ongoing OSS support. This project intentionally stays on 3.5.x to keep the learning stack stable and widely encountered in Java trainee environments.

## Features
### Category management
- Create, read, update and delete categories
- Unique category names
- Maximum 50 characters
- Deletion blocked when expenses still reference the category

### Expense management
- Create, read, update and delete expenses
- Positive amount validation
- Future-date validation
- Required category
- Optional description up to 250 characters
- Search by title
- Filter by category
- Filter by month
- Filter by date range
- Pagination
- Sorting

### Dashboard
- Total expenses
- Transaction count
- Highest expense
- Lowest expense
- Current-month expense
- Category-wise totals

### Reports
- Monthly total expense
- Monthly transaction count
- Visual monthly comparison
- CSV export

### Engineering features
- DTOs
- Layered architecture
- Global exception handling
- Meaningful HTTP status codes
- Database constraints
- Indexed frequently queried columns
- REST API documentation
- Actuator health endpoint
- Unit tests
- Docker Compose
- Environment-based database configuration

## Architecture
```text
Browser
  ↓
Thymeleaf HTML + CSS + JavaScript
  ↓
Spring MVC / REST Controllers
  ↓
Service Layer
  ↓
Repository Layer
  ↓
JPA / Hibernate
  ↓
MySQL
```

The browser pages are rendered with Thymeleaf, while JavaScript uses `fetch()` to call the same REST API layer. This means the UI and API are both real and independently testable.

## Database relationship
```text
Category (1) ────────────< Expense (many)

category
---------
id
name

expense
-------
id
title
amount
expense_date
description
category_id
created_at
updated_at
```

The `Expense` entity owns the JPA relationship through `category_id`. The inverse collection was deliberately not added to `Category`, because the application never needs to load an entire category's expense collection and doing so would add unnecessary entity coupling.

## Environment configuration

The project includes `.env.example`. For local development, the ZIP also contains a safe development `.env` using `root/root`. **Change the password for your own MySQL installation. Never commit real credentials.**

Spring Boot loads the root `.env` through `spring.config.import`, while actual OS environment variables can still override the values.

Important variables:
```text
SERVER_PORT=8080
DB_URL=jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Kolkata
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
DB_POOL_SIZE=10
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
THYMELEAF_CACHE=false
```

## Run locally
### Option A — Maven
1. Install Java 21.
2. Install Maven 3.6.3+.
3. Install MySQL 8+.
4. Update `.env` with your MySQL credentials.
5. From the project root:
```bash
mvn clean test
mvn spring-boot:run
```
6. Open `http://localhost:8080`.

### Option B — Docker
With Docker Desktop running:
```bash
docker compose up --build
```
Then open `http://localhost:8080`.

## Useful URLs
- Application: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`
- Health: `http://localhost:8080/actuator/health`

## REST API
### Categories
- POST `/api/categories`
- GET `/api/categories`
- GET `/api/categories/{id}`
- PUT `/api/categories/{id}`
- DELETE `/api/categories/{id}`

### Expenses
- POST `/api/expenses`
- GET `/api/expenses`
- GET `/api/expenses/{id}`
- PUT `/api/expenses/{id}`
- DELETE `/api/expenses/{id}`
- GET `/api/expenses/export/csv`

Expense search parameters:
`title`, `categoryId`, `month=yyyy-MM`, `fromDate`, `toDate`, `page`, `size`, `sort`, `direction`.

### Dashboard and reports
- GET `/api/dashboard`
- GET `/api/reports/monthly`

## Validation and error handling
The API returns structured JSON errors for:
- 400 — validation/invalid request
- 404 — resource not found
- 409 — duplicate category, category-in-use or database conflict
- 500 — unexpected server error

## Testing
Run:
```bash
mvn clean test
```

The test suite covers core category and expense service behaviour.

## Portfolio note
This project is intended to be understood and defended in an interview. Do not simply submit it without learning the entity relationship, request flow, validation, service/repository responsibilities, JPQL aggregation, pagination and exception handling.
