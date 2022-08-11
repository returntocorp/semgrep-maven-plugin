package dev.r2c.semgrep.mojos;

import com.zaxxer.nuprocess.NuProcessBuilder;
import dev.r2c.semgrep.Argumentable;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Mojo(name = "scan")
public class ScanMojo extends AbstractSemgrepMojo {
    @Parameter(defaultValue = "${semgrep.install.path}", readonly = true)
    private String installPath;

    @Parameter(defaultValue = "${semgrep.url}", readonly = true)
    private String url = "https://semgrep.dev";

    @Parameter(defaultValue= "${semgrep.version}", readonly = true)
    private String version;

    @Parameter(defaultValue = "${semgrep.python.command}", required = true, readonly = true)
    private String pythonCommand;

    @Override
    protected Path getSemgrepInstallPath() throws IOException {
        if (installPath == null || installPath.isEmpty()) {
            Path tempDir = Files.createTempDirectory("semgrep_" + version + "_");
            tempDir.toFile().deleteOnExit();
            installPath = tempDir.toString();
        }
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

    protected NuProcessBuilder buildSemgrepProcess() throws IOException, InterruptedException, MojoExecutionException {
        NuProcessBuilder pb = semgrepProcessBuilder("scan", ScanArgument.values());
        pb.environment().put("SEMGREP_URL", url);
        return pb;
    }

    enum ScanArgument implements Argumentable {
        REPLACEMENT("semgrep.replacement", Argumentable.arg("--replacement")),
        CONFIG("semgrep.config", Argumentable.arg("--config")),
        PATTERN("semgrep.pattern", Argumentable.arg("--pattern")),
        LANG("semgrep.lang", Argumentable.arg("--lang")),
        DRY_RUN("semgrep.dry.run", Argumentable.switchArg("--dry-run")),
        SEVERITY("semgrep.severity", Argumentable.arg("--severity")),
        TEST("semgrep.test", Argumentable.switchArg("--test")),
        TEST_IGNORE_TODO("semgrep.test.ignore.todo", Argumentable.switchArg("--test-ignore-todo")),
        DUMP_AST("semgrep.dump.ast", Argumentable.switchArg("--dump-ast")),
        ERROR("semgrep.error", Argumentable.switchArg("--error")),
        STRICT("semgrep.strict", Argumentable.switchArg("--strict")),
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
        ;

        private final String propertyName;
        private final Function<String, List<String>> transformer;

        ScanArgument(String propertyName, Function<String, List<String>> transformer) {
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
