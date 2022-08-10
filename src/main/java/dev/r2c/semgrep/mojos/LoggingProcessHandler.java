package dev.r2c.semgrep.mojos;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessHandler;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LoggingProcessHandler implements NuProcessHandler  {
    private static Stream<String> lineStream(CharBuffer cb) {
        Scanner scanner = new Scanner(cb);
        scanner.useDelimiter("\n");
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(scanner, Spliterator.ORDERED), false);
    }
    private final Consumer<String> logger;

    public LoggingProcessHandler(Consumer<String> logger) {
        this.logger = logger;
    }

    @Override
    public void onPreStart(NuProcess nuProcess) {

    }

    @Override
    public void onStart(NuProcess nuProcess) {

    }

    @Override
    public void onExit(int i) {
        logger.accept("Semgrep exited with code " + i);
    }

    @Override
    public void onStdout(ByteBuffer byteBuffer, boolean closed) {
        if (closed) {
            return;
        }
        lineStream(StandardCharsets.UTF_8.decode(byteBuffer)).forEach(logger);
    }

    @Override
    public void onStderr(ByteBuffer byteBuffer, boolean closed) {
        if (closed) {
            return;
        }
        lineStream(StandardCharsets.UTF_8.decode(byteBuffer)).forEach(logger);
    }

    @Override
    public boolean onStdinReady(ByteBuffer byteBuffer) {
        return false;
    }
}
