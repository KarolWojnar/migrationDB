package org.migrationDB.Service;

import org.migrationDB.Migrations.MigrationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    public static List<String> getFilesFromDirectory(String pathToMigrationFiles, List<MigrationSchema> migrationSchemas) {
        List<String> fileNames = new ArrayList<>();

        try {
            URL resourceUrl = FileService.class.getClassLoader().getResource(pathToMigrationFiles);
            if (resourceUrl == null) {
                log.error("Can't find directory: {}", pathToMigrationFiles);
                return null;
            }

            File directory = new File(resourceUrl.toURI());
            if (!directory.isDirectory()) {
                log.error("Path is not a directory: {}", directory.getAbsolutePath());
                return null;
            }

            File[] files = directory.listFiles();
            if (files == null) {
                log.warn("Can't get files from directory: {}", directory.getAbsolutePath());
                return null;
            }

            for (File file : files) {
                String fileName = file.getName();
                if (file.isFile() && fileName.endsWith(".sql")) {
                    boolean isAlreadyMigrated = migrationSchemas.stream()
                            .anyMatch(schema -> schema.script_name().equals(fileName));

                    if (!isAlreadyMigrated) {
                        fileNames.add(fileName);
                    }
                }
            }

            if (!fileNames.isEmpty()) {
                fileNames = sortFilesByVersion(fileNames);
            }

        } catch (URISyntaxException e) {
            log.error("Invalid directory URI", e);
        } catch (SecurityException e) {
            log.error("Access denied to directory", e);
        }

        return fileNames;
    }

    private static List<String> sortFilesByVersion(List<String> fileNames) {
        return fileNames.stream()
                .sorted((f1, f2) -> {
                    int v1 = Integer.parseInt(f1.split("__")[0].substring(1));
                    int v2 = Integer.parseInt(f2.split("__")[0].substring(1));
                    return Integer.compare(v1, v2);
                })
                .toList();
    }

    public static String readScriptsFromFile(String fileName, String pathToDirectory) {
        try (InputStream is = FileService.class.getClassLoader().getResourceAsStream(pathToDirectory + fileName)) {
            if (is == null) {
                throw new FileNotFoundException("Migration file not found: " + fileName);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
