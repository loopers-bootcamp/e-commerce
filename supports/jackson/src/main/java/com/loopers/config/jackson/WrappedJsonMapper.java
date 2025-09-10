package com.loopers.config.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class WrappedJsonMapper {

    @Delegate
    private final JsonMapper jsonMapper;

    public WrappedJsonMapper() {
        this.jsonMapper = new JsonMapper();
    }

    public <T> T readMap(Map<String, String> jsonMap, TypeReference<T> typeRef) {
        ObjectNode node = jsonMapper.createObjectNode();
        jsonMap.forEach((k, json) -> {
            try {
                node.set(k, jsonMapper.readTree(json));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        try {
            return jsonMapper.treeToValue(node, typeRef);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, String> writeValueAsMap(Object o) {
        JsonNode node = jsonMapper.valueToTree(o);
        Map<String, String> jsonMap = new LinkedHashMap<>();

        node.fields().forEachRemaining(e -> {
            JsonNode v = e.getValue();
            jsonMap.put(e.getKey(), v.toString());
        });

        return jsonMap;
    }

    public <T> T readValue(String json, TypeReference<T> typeRef) {
        try {
            return jsonMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String writeValueAsString(Object value) {
        try {
            return jsonMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
