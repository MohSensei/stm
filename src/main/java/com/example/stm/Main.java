package com.example.stm;

import com.example.stm.model.TelemetryPacket;
import com.example.stm.parser.CsvPacketParser;
import com.example.stm.rules.ConsoleAlertSink;
import com.example.stm.rules.RuleEngine;
import com.example.stm.util.JsonConfigLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -jar stm.jar <csv-file> [--limits limits.json]");
            System.exit(1);
        }

        Path csv = Path.of(args[0]);
        RuleEngine engine = buildEngineFromArgs(args);

        try (var stream = new CsvPacketParser(csv).stream()) {
            final RuleEngine eng = engine; // effectively final copy
            stream.forEach(pkt -> {
                System.out.println(pkt);
                if (eng != null) {
                    eng.evaluate(pkt);
                }
            });
        }
    }

    private static RuleEngine buildEngineFromArgs(String[] args) {
        for (int i = 1; i < args.length - 1; i++) {
            if ("--limits".equals(args[i])) {
                Path limitsFile = Path.of(args[i + 1]);
                try {
                    var rules = JsonConfigLoader.load(limitsFile);
                    return new RuleEngine(rules, List.of(new ConsoleAlertSink()));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to load limits file: " + limitsFile, e);
                }
            }
        }
        return null;
    }
}