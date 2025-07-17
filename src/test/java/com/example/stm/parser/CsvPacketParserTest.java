package com.example.stm.parser;

import com.example.stm.model.TelemetryPacket;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Happy-path unit test for CsvPacketParser.
 * Adjust ROW_COUNT if you add or remove lines in sample.csv.
 */
class CsvPacketParserTest {

    private static final int ROW_COUNT = 3;   // update if sample.csv has a different number of rows

    @Test
    void parsesSampleCsv() throws Exception {
        // 1. Parse the sample file
        List<TelemetryPacket> packets =
                new CsvPacketParser(Path.of("telemetry-samples/sample.csv"))
                        .stream()
                        .collect(Collectors.toList());

        // 2. Validate row count
        assertEquals(ROW_COUNT, packets.size(), "row count");

        // 3. Validate first row contents
        TelemetryPacket first = packets.get(0);
        assertEquals(Instant.parse("2025-07-16T18:00:01Z"), first.getTimestamp(), "timestamp");
        assertEquals("SAT-42", first.getSatId(), "satId");
        assertEquals(34.8, first.get("power_bus_voltage"), 1e-6, "power bus");
        assertEquals(41.3, first.get("board_temp"),       1e-6, "board temp");
        assertEquals(78.1, first.get("cpu_load"),         1e-6, "cpu load");
    }
}