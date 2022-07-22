package dev.r2c.semgrep.mojos;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import com.zaxxer.nuprocess.NuProcessHandler;
import dev.r2c.semgrep.Argumentable;
import dev.r2c.semgrep.SemgrepArchive;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractSemgrepMojo extends AbstractMojo {
    protected NuProcessBuilder semgrepProcessBuilder(String command, Argumentable[] args, String... additionalArgs) throws IOException {
        Path extractedPath = SemgrepArchive.extract();
        NuProcessBuilder pb = new NuProcessBuilder(Arrays.asList(Paths.get(extractedPath.toString(), "bin/python").toString(), "-m", "semgrep"));
        pb.command().add(command);

        for (Argumentable arg : args) {
            Optional<String> prop = Optional.ofNullable(System.getProperty(arg.getPropertyName()));
            pb.command().addAll(prop.map(arg.getTransformer()).orElse(Collections.emptyList()));
        }

        pb.command().addAll(Arrays.asList(additionalArgs));
        pb.setProcessListener(new NuProcessHandler() {
            @Override
            public void onPreStart(NuProcess nuProcess) {

            }

            @Override
            public void onStart(NuProcess nuProcess) {

            }

            @Override
            public void onExit(int i) {
                getLog().debug("Semgrep exited with code " + i);
            }

            @Override
            public void onStdout(ByteBuffer byteBuffer, boolean closed) {
                if (closed) {
                    return;
                }
                lineStream(StandardCharsets.UTF_8.decode(byteBuffer)).forEach(getLog()::info);
            }

            @Override
            public void onStderr(ByteBuffer byteBuffer, boolean closed) {
                if (closed) {
                    return;
                }
                lineStream(StandardCharsets.UTF_8.decode(byteBuffer)).forEach(getLog()::info);
            }

            @Override
            public boolean onStdinReady(ByteBuffer byteBuffer) {
                return false;
            }
        });
        return pb;
    }

    private static Stream<String> lineStream(CharBuffer cb) {
        Scanner scanner = new Scanner(cb);
        scanner.useDelimiter("\n");
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(scanner, Spliterator.ORDERED), false);
    }

    protected abstract NuProcessBuilder buildSemgrepProcess() throws IOException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            NuProcessBuilder pb = buildSemgrepProcess();
            getLog().debug("Running command: " + pb.command());
            int exitCode = pb.start().waitFor(0, TimeUnit.MINUTES);
            if (exitCode != 0) {
                throw new MojoFailureException("Semgrep exited with code " + exitCode);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }
}
