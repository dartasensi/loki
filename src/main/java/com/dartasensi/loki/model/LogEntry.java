package com.dartasensi.loki.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Lightweight POJO for log entries stored in OpenSearch.
 * - Include the most common fields you expect (timestamp, level, message).
 * - Unknown properties are ignored so the mapping won't fail when documents contain other fields.
 *
 * If your documents have different fields, add them here with proper types.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {

    private String timestamp;
    private String level;
    private String message;

    /**
     * If mapping to concrete fields fails, we keep the raw JSON here as a fallback.
     */
    private ObjectNode rawJson;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ObjectNode getRawJson() {
        return rawJson;
    }

    public void setRawJson(ObjectNode rawJson) {
        this.rawJson = rawJson;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "timestamp='" + timestamp + '\'' +
                ", level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", rawJson=" + (rawJson != null ? rawJson.toString() : "null") +
                '}';
    }
}