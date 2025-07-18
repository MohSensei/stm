package com.example.stm.rules;

import com.example.stm.model.TelemetryPacket;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RuleEngineTest {

    static class CapturingSink implements AlertSink {
        final List<Alert> alerts = new ArrayList<>();
        @Override public void accept(Alert alert) { alerts.add(alert); }
    }

    @Test
    void emitsYellowAndRed() {
        LimitRule tempRule = new LimitRule("board_temp", null, 45.0, null, 50.0);
        CapturingSink sink = new CapturingSink();
        RuleEngine engine = new RuleEngine(List.of(tempRule), List.of(sink));

        TelemetryPacket nominal = new TelemetryPacket(Instant.parse("2025-07-16T18:00:00Z"),
                "SAT-42", Map.of("board_temp", 40.0));
        TelemetryPacket yellow  = new TelemetryPacket(Instant.parse("2025-07-16T18:01:00Z"),
                "SAT-42", Map.of("board_temp", 46.0));
        TelemetryPacket red     = new TelemetryPacket(Instant.parse("2025-07-16T18:02:00Z"),
                "SAT-42", Map.of("board_temp", 51.0));

        engine.evaluate(nominal);
        engine.evaluate(yellow);
        engine.evaluate(red);

        assertEquals(2, sink.alerts.size());
        assertEquals("YELLOW", sink.alerts.get(0).level());
        assertEquals("RED", sink.alerts.get(1).level());
    }
}