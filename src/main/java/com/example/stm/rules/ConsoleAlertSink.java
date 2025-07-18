package com.example.stm.rules;

public class ConsoleAlertSink implements AlertSink {
    @Override public void accept(Alert alert) {
        System.out.println(alert);
    }
}