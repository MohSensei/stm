package com.example.stm.util;

import com.example.stm.rules.LimitRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class JsonConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static List<LimitRule> load(Path json) throws IOException {
        return MAPPER.readValue(json.toFile(), new TypeReference<>() {});
    }
}