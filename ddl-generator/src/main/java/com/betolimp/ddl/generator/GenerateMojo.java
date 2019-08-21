package com.betolimp.ddl.generator;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateMojo extends AbstractMojo {

    private static final int EMBEDDED_POSTGRES_PORT = 5433;

    @Parameter(property = "migrationsPath")
    private String migrationsPath;

    @Parameter(property = "outputPath")
    private String outputPath;

    @Parameter(property = "entities")
    private List<String> entitiesPath;



    public void execute(){
        getLog().info("************************************************************************* " + migrationsPath);
        getLog().info("************************************************************************* " + outputPath);
        getLog().info("************************************************************************* " + entitiesPath);

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

           /* for (String packageName : entitiesPath) {
                getLog().info("!!!!!!!!!!!!! " + packageName);
                listClassNamesInPackage(packageName).forEach(metadata::addAnnotatedClassName);
                metadata.addPackage(packageName);
            }*/

            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setHaltOnError(true);
            schemaExport.setFormat(true);
            schemaExport.setDelimiter(";");
            schemaExport.setOutputFile(outputPath+"/someSql.sql");
            schemaExport.execute(EnumSet.of(TargetType.SCRIPT), SchemaExport.Action.CREATE, metadata.buildMetadata());

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



    private static List<String> listClassNamesInPackage(final String packageName) throws Exception {
        final List<String> classes = new ArrayList<>();
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(packageName.replace('.', File.separatorChar));
        if (!resources.hasMoreElements()) {
            throw new IllegalStateException("No package found: " + packageName);
        }
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:*.class");
        while (resources.hasMoreElements()) {
            final URL resource = resources.nextElement();
            Files.walkFileTree(Paths.get(resource.toURI()), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (pathMatcher.matches(path.getFileName())) {
                        try {
                            String className = Paths.get(resource.toURI()).relativize(path).toString().replace(File.separatorChar, '.');
                            classes.add(packageName + '.' + className.substring(0, className.length() - 6));
                        } catch (URISyntaxException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        return classes;
    }



    public List<String> getEntitiesPath() {
        return entitiesPath;
    }


}
