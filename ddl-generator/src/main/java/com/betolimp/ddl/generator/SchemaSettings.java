package com.betolimp.ddl.generator;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SchemaSettings {

    private String migrationsPath;
    private File outputPath;
    private List<String> entityPackages;
    private Map<String, String> hibernateSettings;


    public SchemaSettings(String migrationsPath, File outputPath, List<String> entityPackages, Map<String, String> hibernateSettings) {
        this.migrationsPath = migrationsPath;
        this.outputPath = outputPath;
        this.entityPackages = entityPackages;
        this.hibernateSettings = hibernateSettings;
    }

    public String getMigrationsPath() {
        return migrationsPath;
    }

    public File getOutputPath() {
        return outputPath;
    }

    public List<String> getEntityPackages() {
        return entityPackages;
    }

    public Map<String, String> getHibernateSettings() {
        return hibernateSettings;
    }
}
