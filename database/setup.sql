-- ExpenseFlow database bootstrap for MySQL 8+
-- The application can also create the database automatically because DB_URL
-- contains createDatabaseIfNotExist=true. This script is useful when you want
-- to create the database explicitly before starting Spring Boot.

CREATE DATABASE IF NOT EXISTS expense_tracker
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE expense_tracker;

-- Tables are intentionally created by Hibernate (spring.jpa.hibernate.ddl-auto=update).
-- Do not add a second hand-maintained table definition here; Hibernate keeps the
-- schema synchronized with the Java entities.
