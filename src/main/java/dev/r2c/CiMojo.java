package dev.r2c;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import com.zaxxer.nuprocess.NuProcessHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

@Mojo(name = "ci")
public class CiMojo extends AbstractMojo {
    @Parameter(defaultValue = "${semgrep.install.dir}", readonly = true)
    private String installDir;

    @Parameter(defaultValue = "${semgrep.url}", readonly = true)
    private String semgrepUrl = "https://semgrep.dev";

    @Parameter(defaultValue = "${semgrep.app.token}", required = true, readonly = true)
    private String semgrepAppToken;

    @Parameter(defaultValue = "${semgrep.scan.path}", readonly = true)
    private String scanPath = ".";

    @Parameter(defaultValue = "${semgrep.scan.timeout.minutes}", readonly = true)
    private int scanTimeoutMinutes = 0;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            NuProcessBuilder pb = Semgrep.processBuilder();
            pb.command().add("ci");
            pb.command().add("--include=" + Paths.get(".").toAbsolutePath().relativize(Paths.get(scanPath)));
            pb.environment().put("SEMGREP_URL", semgrepUrl);
            pb.environment().put("SEMGREP_APP_TOKEN", semgrepAppToken);
            pb.setProcessListener(new NuProcessHandler() {
                @Override
                public void onPreStart(NuProcess nuProcess) {

                }

                @Override
                public void onStart(NuProcess nuProcess) {

                }

                @Override
                public void onExit(int i) {
                }

                @Override
                public void onStdout(ByteBuffer byteBuffer, boolean closed) {
                    if (closed) {
                        return;
                    }
                    Scanner scanner = new Scanner(StandardCharsets.UTF_8.decode(byteBuffer));
                    scanner.useDelimiter("\n");
                    while (scanner.hasNext()) {
                        getLog().info(scanner.next());
                    }
                }

                @Override
                public void onStderr(ByteBuffer byteBuffer, boolean closed) {
                    if (closed) {
                        return;
                    }
                    Scanner scanner = new Scanner(StandardCharsets.UTF_8.decode(byteBuffer));
                    scanner.useDelimiter("\n");
                    while (scanner.hasNext()) {
                        getLog().info(scanner.next());
                    }
                }

                @Override
                public boolean onStdinReady(ByteBuffer byteBuffer) {
                    return false;
                }
            });
            int exitCode = pb.start().waitFor(scanTimeoutMinutes, TimeUnit.MINUTES);
            if (exitCode == Integer.MIN_VALUE) {
                throw new MojoFailureException(String.format("Timed out waiting for semgrep (%d minutes)", scanTimeoutMinutes));
            } else if (exitCode != 0) {
                throw new MojoFailureException("semgrep ci exited with code " + exitCode);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e);
        }
    }
}
