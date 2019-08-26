package com.betolimp.ddl.generator;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE)
public class GenerateMojo extends AbstractMojo {

    private static final int EMBEDDED_POSTGRES_PORT = 5433;

    @Parameter(property = "migrationsPath")
    private String migrationsPath;

    @Parameter(property = "outputPath")
    private File outputPath;

    @Parameter(property = "entities")
    private List<String> entityPackages;

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

        SchemaUpdater schemaUpdater = new SchemaUpdater();
        schemaUpdater.updateSchema(new SchemaSettings(migrationsPath, outputPath, entityPackages, new HashMap<>()));
    }

}
