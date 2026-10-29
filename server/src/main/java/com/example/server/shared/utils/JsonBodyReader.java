package com.example.server.shared.utils;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;

import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

public class JsonBodyReader {
    private static final StreamReadConstraints CONSTRAINTS = StreamReadConstraints
        .builder()
        .maxNestingDepth(5)
        .maxTokenCount(30)
        .maxDocumentLength(1000)
        .build();

    private static final JsonFactory FACTORY = JsonFactory.builder().streamReadConstraints(CONSTRAINTS).build();

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder(FACTORY).build();

    public static Map<String, Object> readObject(HttpServletRequest req) {
        try (InputStream in = req.getInputStream()) {
            return OBJECT_MAPPER.readValue(in, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return null;
        }
    }
}