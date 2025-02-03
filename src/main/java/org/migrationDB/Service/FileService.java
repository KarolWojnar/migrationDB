package org.migrationDB.Service;

import org.migrationDB.Exception.MigrationFileException;
import org.migrationDB.Migrations.CheckSumCalculator;
import org.migrationDB.Migrations.MigrationSchema;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    public static List<String> getFilesFromDirectory(String pathToMigrationFiles, List<MigrationSchema> migrationSchemas) {
        List<String> fileNames = new ArrayList<>();

        try {
            URL resourceUrl = FileService.class.getClassLoader().getResource(pathToMigrationFiles);
            File[] files = findFilesFromPath(pathToMigrationFiles, resourceUrl);

            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.endsWith(".sql")) {
                    boolean isAlreadyMigrated = migrationSchemas.stream()
                                .anyMatch(schema -> (schema.script_name().equals(fileName)));

                    String checksum = "";
                    if (file.getName().startsWith("R")) {
                        checksum = CheckSumCalculator.calculateCheckSumFromFile(file);
                    }

                    String checkSumFromList = migrationSchemas.stream()
                            .filter(schema -> schema.script_name().equals(fileName))
                            .findFirst()
                            .map(MigrationSchema::checksum)
                            .orElse("");

                    if (!isAlreadyMigrated || (fileName.startsWith("R") && (checkSumFromList.isEmpty() || !checksum.equals(checkSumFromList)))) {
                        fileNames.add(fileName);
                    }
                }
            }

            if (!fileNames.isEmpty()) {
                fileNames = sortFilesByVersion(fileNames);
            }

        } catch (URISyntaxException e) {
            throw new MigrationFileException("Can't get files from directory: " + pathToMigrationFiles);
        }

        return fileNames;
    }

    private static File[] findFilesFromPath(String pathToMigrationFiles, URL resourceUrl) throws URISyntaxException {
        if (resourceUrl == null) {
            throw new MigrationFileException("Can't find directory: " + pathToMigrationFiles);
        }

        File directory = new File(resourceUrl.toURI());
        if (!directory.isDirectory()) {
            throw new MigrationFileException("Not a directory: " + pathToMigrationFiles);
        }

        File[] files = directory.listFiles();
        if (files == null) {
            throw new MigrationFileException("Can't get files from directory: " + pathToMigrationFiles);
        }
        return files;
    }

    static List<String> sortFilesByVersion(List<String> fileNames) {
        return fileNames.stream()
                .sorted((f1, f2) -> {
                    int v1 = Integer.parseInt(f1.split("__")[0].substring(1));
                    int v2 = Integer.parseInt(f2.split("__")[0].substring(1));
                    return Integer.compare(v1, v2);
                })
                .toList();
    }

    public static String readScriptsFromFile(String fileName, String pathToDirectory) throws IOException {
        try (InputStream is = FileService.class.getClassLoader().getResourceAsStream(pathToDirectory + fileName)) {
            if (is == null) {
                throw new FileNotFoundException("Migration file not found: " + fileName);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
