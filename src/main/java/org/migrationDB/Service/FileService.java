package org.migrationDB.Service;

import org.migrationDB.Exception.MigrationFileException;
import org.migrationDB.Data.MigrationSchema;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
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

        } catch (IOException e) {
            throw new MigrationFileException("Can't get files from directory: " + pathToMigrationFiles);
        }

        return fileNames;
    }

    private Path getInputFile(Path file, String pathToDirectory) {
        String fileName = file.getFileName().toString();
        return Paths.get(pathToDirectory, fileName);
    }

    private String calculateCheckSumFromInputStream(Path path) throws IOException {
        if (path == null) {
            throw new MigrationFileException("Cannot calculate checksum: input stream is null");
        }
        return MigrationsCompatibilityService.calculateCheckSum(Files.readString(path, StandardCharsets.UTF_8));
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
        } catch (IOException e) {
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
        Path filePath = Paths.get(pathToDirectory, fileName);
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MigrationFileException("Migration file not found: " + fileName, e);
        }
    }

    private List<Path> findFilesInDirectory(String directoryPath) throws IOException {
        Path directory = Paths.get(directoryPath);
        if (!Files.isDirectory(directory)) {
            throw new MigrationFileException("Not a directory: " + directoryPath);
        }
        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .collect(Collectors.toList());
        }
    }

}
