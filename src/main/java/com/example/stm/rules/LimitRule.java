package com.example.stm.rules;

public record LimitRule(
        String sensor,
        Double yellowLow,
        Double yellowHigh,
        Double redLow,
        Double redHigh) {

    /** Returns "RED", "YELLOW", or null when nominal */
    public String evaluate(double value) {
        if (redLow  != null && value < redLow  || redHigh  != null && value > redHigh)  return "RED";
        if (yellowLow != null && value < yellowLow || yellowHigh != null && value > yellowHigh) return "YELLOW";
        return null;
    }
}