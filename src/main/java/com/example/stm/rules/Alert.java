package com.example.stm.rules;

import java.time.Instant;

public record Alert(
        Instant timestamp,
        String satId,
        String sensor,
        double value,
        String level,        // "RED" or "YELLOW"
        LimitRule rule) {

    @Override public String toString() {
        return "%s ALERT sat=%s sensor=%s value=%.3f (rule:%s)".formatted(
                level, satId, sensor, value,
                summarizeRule());
    }

    private String summarizeRule() {
        return "Y[" + rule.yellowLow() + "," + rule.yellowHigh() + "]"
             + " R[" + rule.redLow() + "," + rule.redHigh() + "]";
    }
}