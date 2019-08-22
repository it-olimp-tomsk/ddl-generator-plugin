package com.betolimp.ddl.generator;

import org.flywaydb.core.Flyway;

public class FlywayMigrate {

    public void migrate(String dbUrl, String migrationsPath){
        Flyway flyway = new Flyway();
        flyway.setDataSource(dbUrl, "postgres", "postgres");
        flyway.setLocations("filesystem:" + migrationsPath);
        flyway.migrate();
    }
}
