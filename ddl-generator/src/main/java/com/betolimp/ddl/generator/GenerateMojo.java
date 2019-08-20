package com.betolimp.ddl.generator;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateMojo extends AbstractMojo {

    private static final int EMBEDDED_POSTGRES_PORT = 5433;

    @Parameter(property = "migrationsPath")
    private String migrationsPath;

    public void execute(){
        getLog().info("************************************************************************* " + migrationsPath);
        EmbeddedPostgres embeddedPostgres = null;
        try {
            embeddedPostgres = EmbeddedPostgres.builder()
                    .setPort(EMBEDDED_POSTGRES_PORT).start();
            DataSource dataSource = embeddedPostgres.getPostgresDatabase();
            getLog().info(dataSource.getConnection().getMetaData().getURL());

            Flyway flyway = new Flyway();
            flyway.setDataSource(dataSource.getConnection().getMetaData().getURL(), "postgres", "postgres");
            flyway.setLocations("filesystem:" + migrationsPath);
            flyway.migrate();

            final Statement statement = flyway.getConfiguration().getDataSource().getConnection().createStatement();

            statement.execute("insert into sports values (1, 'sport1')");
            statement.execute("insert into champs values (1, 1, 'champ1')");
            statement.execute("select * from champs");

            getLog().info("@@@ " + statement.getResultSet().next() + " " + statement.getResultSet().getString("name"));

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }

    }

    public String getMigrationsPath() {
        return migrationsPath;
    }

    public void setMigrationsPath(String migrationsPath) {
        this.migrationsPath = migrationsPath;
    }
}
