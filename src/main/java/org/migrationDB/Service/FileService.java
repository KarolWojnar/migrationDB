package org.migrationDB.Service;

import org.migrationDB.Exception.MigrationFileException;
import org.migrationDB.Data.MigrationSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileService {

    public List<String> getFilesFromDirectory(String pathToMigrationFiles, List<MigrationSchema> migrationSchemas) {
        List<String> fileNames = new ArrayList<>();

        try {
            List<Path> files = findFilesInDirectory(pathToMigrationFiles);

            for (Path file : files) {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".sql")) {
                    boolean isAlreadyMigrated = migrationSchemas.stream()
                                .anyMatch(schema -> (schema.script_name().equals(fileName)));

                    String checksum = "";
                    if (fileName.startsWith("R")) {
                        checksum = calculateCheckSumFromInputStream(getInputFile(file, pathToMigrationFiles));
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

        } catch (URISyntaxException | IOException e) {
            throw new MigrationFileException("Can't get files from directory: " + pathToMigrationFiles);
        }

        return fileNames;
    }

    private InputStream getInputFile(Path file, String pathToDirectory) {
        String fileName = file.getFileName().toString();
        return getClass().getClassLoader().getResourceAsStream(pathToDirectory + fileName);
    }

    private String calculateCheckSumFromInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new MigrationFileException("Cannot calculate checksum: input stream is null");
        }
        return MigrationsCompatibilityService.calculateCheckSum(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
    }

    public String getUndoFileNameByVersion(String pathToUndo, String versionFile) {
        try {
            List<Path> files = findFilesInDirectory(pathToUndo);

            for (Path file : files) {
                String fileName = file.getFileName().toString();
                if (fileName.startsWith(versionFile)) {
                    return fileName;
                }
            }
            throw new MigrationFileException("Can't find " + versionFile + "... version of undo.\nMigration files must be rolled back sequentially.");
        } catch (URISyntaxException | IOException e) {
            throw new MigrationFileException("Can't get files from directory: " + pathToUndo, e);
        }
    }

    public List<String> sortFilesByVersion(List<String> fileNames) {
        return fileNames.stream()
                .sorted((f1, f2) -> {
                    int v1 = Integer.parseInt(f1.split("__")[0].substring(1));
                    int v2 = Integer.parseInt(f2.split("__")[0].substring(1));
                    return Integer.compare(v1, v2);
                })
                .toList();
    }

    public String readScriptsFromFile(String fileName, String pathToDirectory) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(pathToDirectory + fileName)) {

            if (is == null) {
                throw new MigrationFileException("Migration file not found: " + fileName);
            }

            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MigrationFileException("Migration file not found: " + fileName, e);
        }
    }

    private List<Path> findFilesInDirectory(String directoryPath) throws URISyntaxException, IOException {
        URL resourceUrl = getClass().getClassLoader().getResource(directoryPath);
        if (resourceUrl == null) {
            throw new MigrationFileException("Can't find directory: " + directoryPath);
        }

        if (resourceUrl.getProtocol().equals("jar")) {
            return findFilesInJar(resourceUrl, directoryPath);
        }

        Path directory = Paths.get(resourceUrl.toURI());
        if (!Files.isDirectory(directory)) {
            throw new MigrationFileException("Not a directory: " + directoryPath);
        }

        try (Stream<Path> paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .collect(Collectors.toList());
        }
    }

    private List<Path> findFilesInJar(URL resourceUrl, String directoryPath) throws IOException {
        List<Path> files = new ArrayList<>();
        String jarPath = resourceUrl.getPath().substring(5, resourceUrl.getPath().indexOf("!"));

        if (jarPath.startsWith("/")) {
            jarPath = jarPath.substring(1);
        }
        jarPath = jarPath.replace("/", "\\");

        try (FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(jarPath), Collections.emptyMap())) {
            Path directory = fileSystem.getPath(directoryPath);
            try (Stream<Path> paths = Files.list(directory)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".sql"))
                        .forEach(files::add);
            }
        }
        return files;
    }


}
