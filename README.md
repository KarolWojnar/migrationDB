# Running the CLI Tool
To use MigrationDB as a Command-Line Interface (CLI) tool, follow these steps:
1. **Build the Project**:
   - Download project from [here](https://github.com/KarolWojnar/migrationDB/archive/refs/heads/migration-cli.zip)
     - Use Gradle to build the project and generate the JAR file
        ```bash
        ./gradlew shadowJar
        ```
   This will create a fat JAR file in the `build/libs` directory.<br>
2. **Run the CLI Tool**:
   <br>Use the following command to execute the CLI tool
```bash
java -jar build/libs/migration-tool.jar --path=<migration_path> --action=<action> --version=<version> --driver=<driver> --user=<user> --url=<url> --password=<password>
```
**Options**<br>
`--path`: Path to the folder containing migration scripts (required).<br>
`--action`: Action to perform (`migrate` or `undo`) (required).<br>
`--version`: Version to undo (optional).<br>
`--driver`: Driver of database.<br>
`--user`: Database user.<br>
`--url`: Url to database.<br>
`--password`: Password to database (optional).<br>
**Example commands:**
```bash
java -jar build/libs/migration-tool.jar --path=C:/path/to/migration/ --action=migrate --driver=com.mysql.cj.jdbc.Driver --url=jdbc:mysql://localhost:3306/test --user=root
```
```bash
java -jar build/libs/migration-tool.jar --path=migrations/ --action=undo --version=4 --driver=com.mysql.cj.jdbc.Driver --url=jdbc:mysql://localhost:3306/test --user=root
```

# Usage Examples
Example Migration Script Structure<br>
Place your migration scripts in the specified folder. Scripts should follow this naming convention:

Versioned Migrations : `V<version>__<description>.sql`<br>
Repeatable Migrations : `R<version>__<description>.sql`<br>
Undo Scripts : `U<version>__<description>.sql`

# Example Folder Structure

```yaml
migrations
├── V1__create_users_table.sql
├── V2__add_email_column.sql
├── R__update_statistics.sql
└── U1__drop_users_table.sql
```