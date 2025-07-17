package com.example.stm.parser;

import com.example.stm.model.TelemetryPacket;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads a telemetry CSV and exposes it as a Java Stream<TelemetryPacket>.
 *
 * 1) Constructor takes the file path.
 * 2) stream() opens OpenCSV reader, parses header once,
 *    then lazily converts every line into an immutable TelemetryPacket.
 */
public class CsvPacketParser {

    /* ---------- 1) ctor ---------- */
    private final Path csvPath;
    public CsvPacketParser(Path csvPath) { this.csvPath = csvPath; }

    /* ---------- 2) public API ---------- */
    public Stream<TelemetryPacket> stream() throws IOException {
        CSVReader reader = new CSVReader(Files.newBufferedReader(csvPath));

        try {
            /* 2-A. read header row */
            String[] header = reader.readNext();                       // may throw CsvValidationException
            if (header == null) throw new IOException("Empty CSV: " + csvPath);

            /* 2-B. locate the mandatory columns */
            int tsIdx = indexOf(header, "timestamp");
            int idIdx = indexOf(header, "sat_id");

            /* 2-C. everything else = sensor columns */
            List<Integer> sensorIdx = new ArrayList<>();
            List<String>  sensorNames = new ArrayList<>();
            for (int i = 0; i < header.length; i++) {
                if (i != tsIdx && i != idIdx) {
                    sensorIdx.add(i);
                    sensorNames.add(header[i]);
                }
            }

            /* 2-D. wrap into an Iterator<TelemetryPacket> */
            Iterator<TelemetryPacket> it = new Iterator<>() {
                String[] next = advance();
                private String[] advance() {
                    try { return reader.readNext(); }
                    catch (IOException | CsvValidationException e) { throw new RuntimeException(e); }
                }
                @Override public boolean hasNext() { return next != null; }
                @Override public TelemetryPacket next() {
                    String[] row = next;
                    next = advance();
                    return parseRow(row, tsIdx, idIdx, sensorIdx, sensorNames);
                }
            };

            /* 2-E. turn iterator into Stream so caller controls terminal ops */
            Stream<TelemetryPacket> s = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(it, 0), false);

            /* 2-F. ensure underlying file closes when user closes the stream */
            return s.onClose(() -> { try { reader.close(); } catch (IOException ignored) {} });

        } catch (CsvValidationException e) {
            throw new IOException("CSV format error", e);
        }
    }

    /* ---------- helpers ---------- */
    private static int indexOf(String[] header, String col) {
        for (int i = 0; i < header.length; i++)
            if (header[i].equalsIgnoreCase(col)) return i;
        throw new IllegalArgumentException("Missing column: " + col);
    }

    private static TelemetryPacket parseRow(String[] row,
                                            int tsIdx, int idIdx,
                                            List<Integer> idx, List<String> names) {
        Instant ts = Instant.parse(row[tsIdx]);         // ISO-8601 -> Instant
        String  sid = row[idIdx];
        Map<String, Double> vals = new HashMap<>();
        for (int k = 0; k < idx.size(); k++)
            vals.put(names.get(k), Double.parseDouble(row[idx.get(k)]));
        return new TelemetryPacket(ts, sid, vals);
    }
}
