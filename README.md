# Running as Library
## Getting Started
### Download project from repository
Download the project [here]([https://github.com/KarolWojnar/migrationDB.git](https://github.com/KarolWojnar/migrationDB/archive/refs/heads/master.zip))

### Create library
After open project use `./gradlew publishToMavenLocal` to create and publish library locally

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
public class Main {
    public static void main(String[] args) {
        // Database connection (from Step 1)
        DatabaseConnection dbConnection = ...;

        // Initialize components
        VersionRepository versionRepository = new VersionRepository();
        MigrationService migrationService = new MigrationService();
        FileService fileService = new FileService();
        MigrationHandler migrationHandler = new MigrationHandler(migrationService, fileService);
        UndoHandler undoHandler = new UndoHandler(migrationService, fileService);

        MigrationExecutor me = new MigrationExecutor(versionRepository, migrationHandler, undoHandler);
        MigrationLibrary migrationLibrary = new MigrationLibrary(me, db);
        // show history of migrations
        migrationLibrary.showHistory();
        
        // run migrations
        migrationLibrary.runMigrations();
        
        // undo migrations to 6th version
        migrationLibrary.undoMigration("6");
    }
}
```
