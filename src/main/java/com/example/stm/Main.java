package com.example.stm;

import com.example.stm.parser.CsvPacketParser;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java -jar stm.jar <csv-file>");
            System.exit(1);
        }
        new CsvPacketParser(Path.of(args[0]))
                .stream()
                .limit(5)                      // stop after 5 rows so huge files don't flood console
                .forEach(System.out::println);
    }
}