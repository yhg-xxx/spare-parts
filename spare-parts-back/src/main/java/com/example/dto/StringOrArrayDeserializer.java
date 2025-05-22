package com.example.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;


public class StringOrArrayDeserializer extends JsonDeserializer<String> {
    private final boolean returnFirstElement; // 控制是否取数组首元素

    public StringOrArrayDeserializer() {
        this(true); // 默认取首元素（兼容原逻辑）
    }

    public StringOrArrayDeserializer(boolean returnFirstElement) {
        this.returnFirstElement = returnFirstElement;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isArray()) {
            if (returnFirstElement && node.size() > 0) {
                return node.get(0).asText(); // 取第一个元素
            }
            return null; // 空数组或配置不取首元素时返回null
        } else if (node.isTextual()) {
            return node.asText();
        }
        return null;
    }
}