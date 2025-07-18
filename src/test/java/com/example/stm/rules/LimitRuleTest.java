package com.example.stm.rules;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LimitRuleTest {

    @Test
    void evaluatesCorrectly() {
        LimitRule r = new LimitRule("sensor", 90.0, 110.0, 80.0, 120.0);

        assertNull(r.evaluate(100.0));
        assertEquals("YELLOW", r.evaluate(85.0));
        assertEquals("RED",    r.evaluate(75.0));
        assertEquals("YELLOW", r.evaluate(115.0));
        assertEquals("RED",    r.evaluate(130.0));
    }
}