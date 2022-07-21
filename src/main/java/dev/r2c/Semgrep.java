package dev.r2c;

import com.google.common.io.Resources;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.logging.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class Semgrep {
    public enum SemgrepExtractor {
        INSTANCE;
        private Optional<Path> extractedPath = Optional.empty();

        public synchronized Path extract() throws IOException {
            if (extractedPath.isPresent()) {
                return extractedPath.get();
            }
            Path semgrepArchive = Files.createTempFile("semgrep", ".tar.gz");
            try (OutputStream os = Files.newOutputStream(semgrepArchive)) {
                Resources.copy(Resources.getResource("semgrep.tar.gz"), os);
            }

            Path installDir = Files.createTempDirectory("semgrep-install");
            TarGZipUnArchiver unArchiver = new TarGZipUnArchiver();
            unArchiver.setSourceFile(semgrepArchive.toFile());
            unArchiver.setDestDirectory(installDir.toFile());
            unArchiver.enableLogging(new NoopLogger());
            unArchiver.extract();

            extractedPath = Optional.of(installDir);
            return installDir;
        }
    }

    private Semgrep() {
    }

    public static NuProcessBuilder processBuilder() throws IOException {
        Path extractedPath = SemgrepExtractor.INSTANCE.extract();
        return new NuProcessBuilder(Arrays.asList(Paths.get(extractedPath.toString(), "bin/python").toString(), "-c", "import semgrep.cli; semgrep.cli.cli()"));
    }



    private static class NoopLogger implements Logger {
        @Override
        public void debug(String s) {

        }

        @Override
        public void debug(String s, Throwable throwable) {

        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void info(String s) {
        }

        @Override
        public void info(String s, Throwable throwable) {

        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public void warn(String s) {

        }

        @Override
        public void warn(String s, Throwable throwable) {

        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void error(String s) {

        }

        @Override
        public void error(String s, Throwable throwable) {

        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public void fatalError(String s) {

        }

        @Override
        public void fatalError(String s, Throwable throwable) {

        }

        @Override
        public boolean isFatalErrorEnabled() {
            return false;
        }

        @Override
        public int getThreshold() {
            return 0;
        }

        @Override
        public void setThreshold(int i) {

        }

        @Override
        public Logger getChildLogger(String s) {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
