package com.betolimp.ddl.generator;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileResolver {

    private final static Pattern FILENAME_PATTERN = Pattern.compile("V([0-9]+)__.+\\.sql");
    private final static Pattern SCHEMA_FILENAME_PATTERN = Pattern.compile("V([0-9]+)__updatedSchema.*\\.sql");

    static File resolveNextMigrationFile(File migrationDir) {
        Optional<Path> lastFile = resolveExistingMigrations(migrationDir, true, false)
                .stream()
                .findFirst();

        Integer fileIndex = lastFile.map((Path input) -> FILENAME_PATTERN.matcher(input.getFileName().toString()))
                .map(matcher -> {
                    if (matcher.find()) {
                        return Integer.valueOf(matcher.group(1));
                    } else {
                        return 0;
                    }
                }).orElse(0);

        return migrationDir.toPath().resolve("V" + ++fileIndex + "__updatedSchema.sql").toFile();
    }

    private static List<Path> resolveExistingMigrations(File migrationsDir, boolean reversed, boolean onlySchemaMigrations) {
        if (!migrationsDir.exists()) {
            migrationsDir.mkdirs();
        }

        File[] files = migrationsDir.listFiles();

        if (files == null) {
            return Collections.emptyList();
        }

        Comparator<Path> pathComparator = Comparator.comparingInt(FileResolver::compareVersionedMigrations);
        if (reversed) {
            pathComparator = pathComparator.reversed();
        }
        return Arrays.stream(files)
                .map(File::toPath)
                .filter(path -> !onlySchemaMigrations || SCHEMA_FILENAME_PATTERN.matcher(path.getFileName().toString()).matches())
                .sorted(pathComparator)
                .collect(Collectors.toList());
    }

    private static Integer compareVersionedMigrations(Path path) {
        Matcher filenameMatcher = FILENAME_PATTERN.matcher(path.getFileName().toString());
        if (filenameMatcher.find()) {
            return Integer.valueOf(filenameMatcher.group(1));
        } else {
            return Integer.MIN_VALUE;
        }
    }
}
