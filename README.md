# Running as Library
## Getting Started
#### To use MigrationDB in your project, add the following dependency to your `build.gradle` file:
```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'org.migrationDB:migration-library:1.0-SNAPSHOT'
}
```
#### Or Maven in `pom.xml`
```xml
<dependency>
    <groupId>org.migrationDB</groupId>
    <artifactId>migration-library</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Basic Usage
Here’s how to integrate MigrationDB into your Java application:
### Step 1: Configure Database Connection
Create an instance of `DatabaseConnection` with your database credentials and the path to the migration scripts:
```java
import org.migrationDB.Config.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection dbConnection = new DatabaseConnection(
            "com.mysql.cj.jdbc.Driver",         // driver to your database
            "jdbc:mysql://localhost:3306/test", // url to database
            "root",                             // username
            "",                                 // password
            "path/to/migrations/"               // path to migration scripts
        );
    }
}
```
### Step 2: Initialize Migration Components
Set up the necessary components for executing migrations:
```java
import org.migrationDB.Core.MigrationExecutor;
import org.migrationDB.Repository.VersionRepository;
import org.migrationDB.Service.FileService;
import org.migrationDB.Service.MigrationService;

public class Main {
    public static void main(String[] args) {
        // Database connection (from Step 1)
        DatabaseConnection dbConnection = ...;

        // Initialize components
        VersionRepository versionRepository = new VersionRepository();
        MigrationService migrationService = new MigrationService();
        FileService fileService = new FileService();

        MigrationExecutor migrationExecutor = new MigrationExecutor(
            versionRepository,
            new MigrationHandler(migrationService, fileService),
            new UndoHandler(migrationService, fileService)
        );

        // Run migrations
        migrationExecutor.makeMigration(dbConnection);

        // Show migration history
        migrationExecutor.showHistory(dbConnection);

        // Undoing migrations to 4th version (default 0)
        migrationExecutor.undoMigration(dbConnection, "4");
    }
}
```