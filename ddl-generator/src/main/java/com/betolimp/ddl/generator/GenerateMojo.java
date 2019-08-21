package com.betolimp.ddl.generator;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.flywaydb.core.Flyway;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.reflections.Reflections;

import javax.persistence.Entity;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateMojo extends AbstractMojo {

    private static final int EMBEDDED_POSTGRES_PORT = 5433;

    @Parameter(property = "migrationsPath")
    private String migrationsPath;

    @Parameter(property = "outputPath")
    private File outputPath;

    @Parameter(property = "entities")
    private List<String> entitiesPath;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    private PluginDescriptor descriptor;

    private URL mapPathToURL(String path) {
        try {
            return Paths.get(path).toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public void execute(){

        List<String> compileSourceRoots = project.getCompileSourceRoots();
        compileSourceRoots.stream().map(this::mapPathToURL).forEach(url -> descriptor.getClassRealm().addURL(url));

        try {
            project.getCompileClasspathElements().stream().map(this::mapPathToURL).forEach(url -> descriptor.getClassRealm().addURL(url));
        } catch (DependencyResolutionRequiredException e) {
            throw new IllegalStateException(e);
        }
        EmbeddedPostgres embeddedPostgres = null;
        Connection connection = null;
        try {
            embeddedPostgres = EmbeddedPostgres.builder()
                    .setPort(EMBEDDED_POSTGRES_PORT).start();
            DataSource dataSource = embeddedPostgres.getPostgresDatabase();
            connection = dataSource.getConnection();
            String dbUrl = connection.getMetaData().getURL();
            getLog().info(dbUrl);

            Flyway flyway = new Flyway();
            flyway.setDataSource(dbUrl, "postgres", "postgres");
            flyway.setLocations("filesystem:" + migrationsPath);
            flyway.migrate();

            final Statement statement = flyway.getConfiguration().getDataSource().getConnection().createStatement();

            statement.execute("insert into sports values (1, 'sport1')");
            statement.execute("insert into champs values (1, 1, 'champ1')");
            statement.execute("select * from champs");

            getLog().info("@@@ " + statement.getResultSet().next() + " " + statement.getResultSet().getString("name"));

            Map<String, String> settings = new HashMap<>();

            settings.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL95Dialect");
            settings.put("hibernate.connection.url","" + dbUrl + "?useSSL=false");
            settings.put("hibernate.connection.username", "postgres");
            settings.put("hibernate.connection.password", "postgres");
            settings.put("hibernate.hbm2ddl.auto", "update");
            settings.put("hibernate.show_sql", "true");

            MetadataSources metadata = new MetadataSources(
                    new StandardServiceRegistryBuilder()
                            .applySettings(settings)
                            .build());

            new Reflections(entitiesPath)
                    .getTypesAnnotatedWith(Entity.class)
                    .forEach(metadata::addAnnotatedClass);

            EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT);
            SchemaExport export = new SchemaExport();
            export.setHaltOnError(true);
            export.setFormat(true);
            export.setDelimiter(";");
            export.setOutputFile(outputPath+"/example.sql");
            export.execute(targetTypes, SchemaExport.Action.BOTH, metadata.buildMetadata());

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

    public List<String> getEntitiesPath() {
        return entitiesPath;
    }

}
