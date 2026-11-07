package com.example.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class JacksonConfig {
    @Bean
    @Primary
    public JsonMapper jsonMapper() {
        StreamReadConstraints constraints = StreamReadConstraints.builder()
            .maxNestingDepth(5)
            .maxTokenCount(30)
            .maxDocumentLength(1000)
            .build();

        JsonFactory factory = JsonFactory.builder()
            .streamReadConstraints(constraints)
            .build();

        return JsonMapper.builder(factory).build();
    }
}