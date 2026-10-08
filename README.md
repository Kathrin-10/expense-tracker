# ExpenseFlow — Java Full-Stack Expense Tracker

ExpenseFlow is a Java 21 + Spring Boot + MySQL + Thymeleaf expense-management application with customer authentication and an administrator customer-management panel.

## Features

### Customer
- Registration and login
- Forgot/reset password flow
- Active/inactive account handling
- Personal profile editing while authenticated
- Password change while authenticated
- Customer-owned expenses
- Category management
- Expense CRUD, search, filtering, sorting and pagination
- Filter-aware expense total, e.g. `Sum ₹14,450.00` for the currently selected expense set
- Dashboard with total, transaction count, highest/lowest and current-month spending
- Category-wise spending
- Monthly reports and CSV export

### Administrator
- Separate administrator login page
- View all customer accounts
- Filter active/inactive customers
- Edit customer name, email and phone
- Activate/deactivate customers
- Deactivated customers are blocked from subsequent requests

## Stack
- Java 21
- Spring Boot 3.5.16
- Spring MVC / REST
- Spring Security
- Spring Data JPA / Hibernate
- MySQL 8+
- Thymeleaf
- HTML / CSS / JavaScript
- Maven

## Architecture

Browser → Thymeleaf/JavaScript → Controller → Service → Repository → JPA/Hibernate → MySQL

Authentication uses Spring Security with BCrypt password hashing and session-based login. Expense records are linked to the authenticated customer through `user_id` so customers cannot edit another customer's expenses.

## Database setup

ExpenseFlow uses **MySQL**, not an embedded database file. The project stores data in the `expense_tracker` MySQL database through Spring Data JPA/Hibernate. On first startup, the JDBC URL can create the database automatically because it contains `createDatabaseIfNotExist=true`, and Hibernate creates/updates the tables with `JPA_DDL_AUTO=update`.

If you prefer to create the database manually, run `database/setup.sql` in MySQL first. Then copy `.env.example` to `.env`, set `DB_USERNAME` and `DB_PASSWORD`, and start the application.

The main persisted tables are `users`, `categories`, `expenses`, and `password_reset_tokens`.

## Configuration

Copy `.env.example` to `.env` and update the MySQL credentials.

Important variables:

```text
DB_URL=jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=Asia/Kolkata
DB_USERNAME=root
DB_PASSWORD=your_mysql_password
ADMIN_EMAIL=admin@expenseflow.local
ADMIN_PASSWORD=change_this_admin_password
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=your_smtp_username
MAIL_PASSWORD=your_smtp_password
MAIL_FROM=no-reply@example.com
APP_BASE_URL=https://your-expenseflow-host.example.com
```

`.env` is ignored by Git. Never commit real credentials.

## Local admin

On first startup, the application creates the single administrator using `ADMIN_EMAIL` and `ADMIN_PASSWORD`. These values are required; there is no built-in administrator password. The administrator password is BCrypt-hashed before storage.

## Password reset

The forgot-password flow emails a one-time reset link that expires after 30 minutes and can only be used once. Configure `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, and `MAIL_FROM` for your SMTP provider. Set `APP_BASE_URL` to the public URL where customers can reach the app; it defaults to the local server URL for development. Reset links are never printed to the application console. If SMTP is not configured or email delivery fails, the recovery page displays an error instead of claiming that a message was sent.

## Run

```bash
mvn clean test
mvn spring-boot:run
```

Open:

- `http://localhost:8080/login`
- `http://localhost:8080/register`
- `http://localhost:8080/admin/login`

## API highlights

- `GET /api/expenses`
- `POST /api/expenses`
- `PUT /api/expenses/{id}`
- `DELETE /api/expenses/{id}`
- `GET /api/categories`
- `GET /api/dashboard`
- `GET /api/reports/monthly`
- `GET /api/expenses/export/csv`
- `GET /api/admin/customers`
- `PUT /api/admin/customers/{id}`
- `PUT /api/profile`
- `POST /api/profile/password`

The expense-list response includes `totalAmount`, which is calculated using the same active filters as the displayed transaction set. Expenses created before customer authentication may have a null `user_id`; they are intentionally not assigned automatically to a customer, because silently transferring another person's historical data would be unsafe. If you need to migrate legacy expenses, assign them explicitly before using the authenticated customer dashboard.
