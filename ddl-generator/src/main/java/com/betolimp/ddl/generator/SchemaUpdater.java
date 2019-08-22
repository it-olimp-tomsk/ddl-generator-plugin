package com.betolimp.ddl.generator;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;

public class SchemaUpdater {

    private static final int EMBEDDED_POSTGRES_PORT = 5433;

    public void updateSchema(SchemaSettings schemaSettings){

        EmbeddedPostgres embeddedPostgres = null;
        Connection connection = null;
        try {
            FlywayMigrate flywayMigrate = new FlywayMigrate();

            embeddedPostgres = EmbeddedPostgres.builder()
                    .setPort(EMBEDDED_POSTGRES_PORT).start();
            DataSource dataSource = embeddedPostgres.getPostgresDatabase();
            connection = dataSource.getConnection();
            String dbUrl = connection.getMetaData().getURL();
            flywayMigrate.migrate(dbUrl, schemaSettings.getMigrationsPath());

            schemaSettings.getHibernateSettings().put("hibernate.connection.driver_class", "org.postgresql.Driver");
            schemaSettings.getHibernateSettings().put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
            schemaSettings.getHibernateSettings().put("hibernate.connection.url","" + dbUrl + "?useSSL=false");
            schemaSettings.getHibernateSettings().put("hibernate.connection.username", "postgres");
            schemaSettings.getHibernateSettings().put("hibernate.connection.password", "postgres");
            schemaSettings.getHibernateSettings().put("hibernate.hbm2ddl.auto", "update");
            schemaSettings.getHibernateSettings().put("hibernate.show_sql", "true");

            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(schemaSettings.getHibernateSettings())
                    .build();

            MetadataSources sources = new MetadataSources(standardRegistry);

            new Reflections(schemaSettings.getEntityPackages())
                    .getTypesAnnotatedWith(Entity.class)
                    .forEach(sources::addAnnotatedClass);

            MetadataImplementor metadata = (MetadataImplementor) sources
                    .getMetadataBuilder()
                    .build();

            EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT);

            SchemaUpdate schemaUpdate = new SchemaUpdate();
            schemaUpdate.setHaltOnError(true);
            schemaUpdate.setFormat(true);
            schemaUpdate.setDelimiter(";");
            schemaUpdate.setOutputFile(FileResolver.resolveNextMigrationFile(schemaSettings.getOutputPath()).getAbsolutePath());
            schemaUpdate.execute(targetTypes, metadata, standardRegistry);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (embeddedPostgres != null) {
                    embeddedPostgres.close();
                }
            } catch (SQLException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}
