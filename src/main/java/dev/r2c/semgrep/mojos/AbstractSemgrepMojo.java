package dev.r2c.semgrep.mojos;

import com.zaxxer.nuprocess.NuProcessBuilder;
import dev.r2c.semgrep.Argumentable;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSemgrepMojo extends AbstractMojo {
    protected abstract Path getSemgrepInstallPath() throws IOException;

    protected abstract Optional<String> getSemgrepVersion();

    protected abstract String getPythonCommand();

    protected Path install() throws IOException, InterruptedException, MojoExecutionException {
        String installPath = getSemgrepInstallPath().toString();
        String packageName = getSemgrepVersion().map(v -> "semgrep==" + v).orElse("semgrep");
        if (getSemgrepVersion().map(v -> Files.exists(Paths.get(installPath, String.format("semgrep-%s.dist-info/METADATA", getSemgrepVersion().get())))).orElseGet(() -> Files.exists(Paths.get(installPath, "bin/semgrep")))) {
            getLog().info(String.format("Found preexisting %s at %s", packageName, installPath));
            return Paths.get(installPath, "bin/semgrep");
        }
        getLog().info(String.format("Installing %s", packageName));
        NuProcessBuilder pb = new NuProcessBuilder(Arrays.asList(getPythonCommand(), "-m", "pip", "install", "-U", "--target", installPath, packageName));
        pb.environment().put("PYTHONPATH", installPath);
        pb.setProcessListener(new LoggingProcessHandler(getLog()::info));
        int exitCode = pb.start().waitFor(0, TimeUnit.DAYS);
        if (exitCode != 0) {
            throw new MojoExecutionException("Semgrep install exited with code " + exitCode);
        }
        return Paths.get(installPath, "bin/semgrep");
    }

    protected NuProcessBuilder semgrepProcessBuilder(String command, Argumentable[] args, String... additionalArgs) throws IOException, InterruptedException, MojoExecutionException {
        Path executable = install();
        NuProcessBuilder pb = new NuProcessBuilder(Collections.singletonList(executable.toString()));
        pb.command().add(command);
        pb.environment().put("PYTHONPATH", getSemgrepInstallPath().toString());

        for (Argumentable arg : args) {
            Optional<String> prop = Optional.ofNullable(System.getProperty(arg.getPropertyName()));
            pb.command().addAll(prop.map(arg.getTransformer()).orElse(Collections.emptyList()));
        }

        pb.command().addAll(Arrays.asList(additionalArgs));
        pb.setProcessListener(new LoggingProcessHandler(getLog()::info));
        return pb;
    }

    protected abstract NuProcessBuilder buildSemgrepProcess() throws IOException, InterruptedException, MojoExecutionException;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            NuProcessBuilder pb = buildSemgrepProcess();
            getLog().debug("Launching semgrep with arguments: " + pb.command().subList(1, pb.command().size()));
            int exitCode = pb.start().waitFor(0, TimeUnit.MINUTES);
            if (exitCode != 0) {
                throw new MojoFailureException("Semgrep exited with code " + exitCode);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }
}
