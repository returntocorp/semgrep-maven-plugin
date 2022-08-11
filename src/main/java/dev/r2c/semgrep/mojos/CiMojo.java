package dev.r2c.semgrep.mojos;

import com.zaxxer.nuprocess.NuProcessBuilder;
import dev.r2c.semgrep.Argumentable;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Mojo(name = "ci")
public class CiMojo extends AbstractSemgrepMojo {
    @Parameter(defaultValue = "${semgrep.install.path}", required=true, readonly = true)
    private String installPath;

    @Parameter(defaultValue = "${semgrep.url}", readonly = true)
    private String url = "https://semgrep.dev";

    @Parameter(defaultValue = "${semgrep.app.token}", required = true, readonly = true)
    private String appToken;

    @Parameter(defaultValue= "${semgrep.version}", required = true, readonly = true)
    private String version;

    @Parameter(defaultValue = "${semgrep.python.command}", required = true, readonly = true)
    private String pythonCommand;

    @Override
    protected Path getSemgrepInstallPath() {
        return Paths.get(installPath);
    }

    @Override
    protected Optional<String> getSemgrepVersion() {

        return (version == null || version.isEmpty()) ? Optional.empty() : Optional.of(version);
    }

    @Override
    protected String getPythonCommand() {
        return pythonCommand;
    }

    @Override
    protected NuProcessBuilder buildSemgrepProcess() throws IOException, InterruptedException, MojoExecutionException {
        NuProcessBuilder pb = semgrepProcessBuilder("ci", CiArgument.values());
        pb.environment().put("SEMGREP_URL", url);
        pb.environment().put("SEMGREP_APP_TOKEN", appToken);
        return pb;
    }

    enum CiArgument implements Argumentable {
        AUTOFIX("semgrep.autofix", Argumentable.booleanArg("--auto-fix", "--no-autofix")),
        BASELINE_COMMIT("semgrep.baseline.commit", Argumentable.arg("--baseline-commit")),
        METRICS("semgrep.metrics", Argumentable.arg("--metrics")),
        // TODO: EXCLUDE
        // TODO: INCLUDE
        MAX_TARGET_BYTES("semgrep.max.target.bytes", Argumentable.arg("--max-target-bytes")),
        USE_GIT_IGNORE("semgrep.use.gitignore", Argumentable.booleanArg("--use-git-ignore", "--no-git-ignore")),
        SCAN_UNKNOWN_EXTENSIONS("semgrep.scan.unknown.extensions", Argumentable.booleanArg("--scan-unknown-extensions", "--skip-unknown-extensions")),
        ENABLE_VERSION_CHECK("semgrep.enable.version.check", Argumentable.booleanArg("--enable-version-cheeck", "--disable-version-check")),
        JOBS("semgrep.jobs", Argumentable.arg("--jobs")),
        MAX_MEMORY("semgrep.max.memory", Argumentable.arg("--max-memory")),
        OPTIMIZATIONS("semgrep.optimizations", Argumentable.arg("--optimizations")),
        TIMEOUT("semgrep.timeout", Argumentable.arg("--timeout")),
        TIMEOUT_THRESHOLD("semgrep.timeout.threshold", Argumentable.arg("--timeout-threshold")),
        NOSEM("semgrep.nosem", Argumentable.booleanArg("--enable-nosem", "--disable-nosem")),
        FORCE_COLOR("semgrep.force.color", Argumentable.booleanArg("--force-color", "--no-force-color")),
        MAX_CHARS_PER_LINE("semgrep.max.chars.per.line", Argumentable.arg("--max-chars-per-line")),
        MAX_LINES_PER_FINDING("semgrep.max.lines.per.finding", Argumentable.arg("--max-lines-per-finding")),
        OUTPUT("semgrep.output", Argumentable.arg("--output")),
        REWRITE_RULE_IDS("semgrep.rewrite.rule.ids", Argumentable.booleanArg("--rewrite-rule-ids", "--no-rewrite-rule-ids")),
        TIME("semgrep.time", Argumentable.booleanArg("--time", "--no-time")),
        VERBOSE("semgrep.verbose", Argumentable.switchArg("--verbose")),
        DEBUG("semgrep.debug", Argumentable.switchArg("--debug")),
        // TODO: output formats
        CONFIG("semgrep.config", Argumentable.arg("--config")),
        DRY_RUN("semgrep.dry.run", Argumentable.switchArg("--dry-run")),
        ;

        private final String propertyName;
        private final Function<String, List<String>> transformer;

        CiArgument(String propertyName, Function<String, List<String>> transformer) {
            this.propertyName = propertyName;
            this.transformer = transformer;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public Function<String, List<String>> getTransformer() {
            return transformer;
        }


    }
}
