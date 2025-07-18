package com.example.stm.rules;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

public class FileAlertSink implements AlertSink, AutoCloseable {

    private final PrintWriter out;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    public FileAlertSink(Path path) throws IOException {
        Files.createDirectories(path.getParent() == null ? Path.of(".") : path.getParent());
        this.out = new PrintWriter(Files.newBufferedWriter(path, java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND));
    }

    @Override public void accept(Alert alert) {
        out.printf("%s,%s,%s,%s,%.3f%n",
                ISO.format(alert.timestamp()),
                alert.satId(),
                alert.level(),
                alert.sensor(),
                alert.value());
        out.flush();
    }

    @Override public void close() {
        out.close();
    }
}