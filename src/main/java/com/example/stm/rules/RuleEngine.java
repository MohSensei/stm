package com.example.stm.rules;

import com.example.stm.model.TelemetryPacket;

import java.util.*;

public class RuleEngine {

    private final Map<String, LimitRule> rulesBySensor;
    private final List<AlertSink> sinks;

    public RuleEngine(List<LimitRule> rules, List<AlertSink> sinks) {
        Map<String, LimitRule> map = new HashMap<>();
        for (LimitRule r : rules) map.put(r.sensor(), r);
        this.rulesBySensor = Map.copyOf(map);
        this.sinks = List.copyOf(sinks);
    }

    public void evaluate(TelemetryPacket packet) {
        for (var entry : packet.getValues().entrySet()) {
            String sensor = entry.getKey();
            double value  = entry.getValue();
            LimitRule rule = rulesBySensor.get(sensor);
            if (rule == null) continue;
            String level = rule.evaluate(value);
            if (level != null) {
                Alert alert = new Alert(packet.getTimestamp(),
                                        packet.getSatId(),
                                        sensor,
                                        value,
                                        level,
                                        rule);
                sinks.forEach(s -> s.accept(alert));
            }
        }
    }
}