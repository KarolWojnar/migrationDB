# MigrationDB - Database Migration Tool
## Introduction
MigrationDB is a lightweight Java-based tool for managing database schema changes using SQL scripts. Inspired by tools like Liquibase and Flyway, MigrationDB provides versioning, checksum validation, and undo functionality to ensure smooth and reliable database migrations. It supports multiple databases, including MySQL, PostgreSQL, SQLite, Oracle, and MSSQL Server.

Whether used as a library in your Java application or as a standalone Command-Line Interface (CLI) tool, MigrationDB simplifies the process of maintaining database consistency across environments.

## Features
- `Version Control` : Manage database migrations with versioned SQL scripts.
- `Checksum Validation` : Ensure script integrity by comparing checksums before execution.
- `Undo Migrations` : Roll back migrations to a specific version.
- `Repeatable Migrations` : Execute repeatable scripts when their content changes.
- `Cross-Database Support` : Works with MySQL, PostgreSQL, SQLite, Oracle, and MSSQL Server.
- `CLI Interface` : Run migrations directly from the command line ([check](https://github.com/KarolWojnar/migrationDB/tree/migration-cli)).
- `Library Integration` : Easily integrate into Java applications ([check](https://github.com/KarolWojnar/migrationDB/tree/migration-library)).

## How It Works
MigrationDB operates by executing SQL scripts stored in a specified folder. The tool follows these steps:

### 1.  Initialization :
   - Connects to the target database using provided credentials.
   - Creates a `version_control` table if it does not exist. This table tracks executed migrations.
### 2.  Migrating :
   - Scans the migration folder for SQL scripts named according to the convention:
     - ##### Versioned Migrations : `V<version>__<description>.sql`
     - ##### Repeatable Migrations : `R<version>__<description>.sql`
     - ##### Undo Scripts : `U<version>__<description>.sql`
   - Executes pending migrations in order, recording each migration in the `version_control` table.
### 3. Checksum Validation :
   - Before executing a migration, MigrationDB calculates its checksum and compares it with the recorded checksum in the `version_control` table.
   - If a mismatch is detected, the tool throws an error to prevent accidental execution of modified scripts.
### 4. Undoing Migrations :
   - Uses undo scripts (`U<version>__<description>.sql`) to roll back specific versions of migrations.
   - Deletes the corresponding entry from the `version_control` table after successful undo.
### 5. History Tracking :
   Maintains a history of all executed migrations, including timestamps and success status.

## Example Migration Script Structure
Place your migration scripts in the specified folder. Scripts should follow this naming convention:
 - ### Versioned Migrations:
   - `V<version>__<description>.sql`, 
   - Example: `V1__create_users_table.sql`, `V2__add_email_column.sql`
 - ### Repeatable Migrations :
   - `R<version>__<description>.sql`,
   - Example: `R1__check_date.sql`
 - ### Undo Scripts :
   - `U<version>__<description>.sql`
   - Example: `U1__drop_users_table.sql`

### Example folder structure:
```angular2html
migrations/
├── V1__create_users_table.sql
├── V2__add_email_column.sql
├── R1__update_statistics.sql
└── U1__drop_users_table.sql
```
