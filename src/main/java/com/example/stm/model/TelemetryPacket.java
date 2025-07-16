package com.example.stm.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public final class TelemetryPacket {
    private final Instant timestamp;
    private final String  satId;
    private final Map<String, Double> values;

    public TelemetryPacket(Instant timestamp, String satId, Map<String, Double> values) {
        this.timestamp = timestamp;
        this.satId     = satId;
        this.values    = Map.copyOf(values);
    }

    public Instant getTimestamp()          { return timestamp; }
    public String  getSatId()              { return satId; }
    public Map<String, Double> getValues() { return values; }
    public Double get(String name)         { return values.get(name); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TelemetryPacket)) return false;
        TelemetryPacket that = (TelemetryPacket) o;
        return timestamp.equals(that.timestamp)
            && satId.equals(that.satId)
            && values.equals(that.values);
    }
    @Override public int hashCode() { return Objects.hash(timestamp, satId, values); }
    @Override public String toString() {
        return "TelemetryPacket{" + timestamp + ',' + satId + ',' + values + '}';
    }
}
