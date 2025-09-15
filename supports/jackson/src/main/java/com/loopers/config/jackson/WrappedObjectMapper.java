package com.loopers.config.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class WrappedObjectMapper {

    @Delegate
    private final ObjectMapper mapper;

    public WrappedObjectMapper() {
        this.mapper = new ObjectMapper();
    }

    public <T> T readMap(Map<String, String> jsonMap, TypeReference<T> typeRef) {
        ObjectNode node = mapper.createObjectNode();
        jsonMap.forEach((k, json) -> {
            try {
                node.set(k, mapper.readTree(json));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            return mapper.treeToValue(node, typeRef);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, String> writeValueAsMap(Object o) {
        JsonNode node = mapper.valueToTree(o);
        Map<String, String> jsonMap = new LinkedHashMap<>();

        node.fields().forEachRemaining(e -> {
            JsonNode v = e.getValue();
            jsonMap.put(e.getKey(), v.toString());
        });

        return jsonMap;
    }

    public <T> T readValue(String json, TypeReference<T> typeRef) {
        try {
            return mapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String writeValueAsString(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
