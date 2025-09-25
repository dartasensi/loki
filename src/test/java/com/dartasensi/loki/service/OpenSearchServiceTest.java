package com.dartasensi.loki.service;

import com.dartasensi.loki.client.OpenSearchClient;
import com.dartasensi.loki.model.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OpenSearchService.
 * - Mocks the Feign OpenSearchClient to return canned JSON responses.
 * - Asserts that mapping to LogEntry works for normal documents.
 * - Asserts that when mapping fails, the service falls back to storing rawJson.
 */
class OpenSearchServiceTest {

    @Test
    void mapsHitsToLogEntry() throws Exception {
        OpenSearchClient client = mock(OpenSearchClient.class);
        ObjectMapper mapper = new ObjectMapper();
        OpenSearchService service = new OpenSearchService(client, mapper);

        String responseJson =
                "{\n" +
                "  \"hits\": {\n" +
                "    \"total\": { \"value\": 1, \"relation\": \"eq\" },\n" +
                "    \"hits\": [\n" +
                "      {\n" +
                "        \"_index\": \"logs\",\n" +
                "        \"_id\": \"1\",\n" +
                "        \"_score\": 1.0,\n" +
                "        \"_source\": {\n" +
                "          \"timestamp\": \"2025-01-01T00:00:00Z\",\n" +
                "          \"level\": \"INFO\",\n" +
                "          \"message\": \"hello\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        when(client.searchWithQuery("logs", "q")).thenReturn(responseJson);

        List<LogEntry> results = service.searchLogs("logs", "q");

        assertNotNull(results);
        assertEquals(1, results.size());

        LogEntry entry = results.get(0);
        assertEquals("2025-01-01T00:00:00Z", entry.getTimestamp());
        assertEquals("INFO", entry.getLevel());
        assertEquals("hello", entry.getMessage());
        assertNull(entry.getRawJson());

        verify(client, times(1)).searchWithQuery("logs", "q");
    }

    @Test
    void fallsBackToRawJsonWhenMappingFails() throws Exception {
        OpenSearchClient client = mock(OpenSearchClient.class);
        ObjectMapper mapper = new ObjectMapper();
        OpenSearchService service = new OpenSearchService(client, mapper);

        // timestamp is an object (not a string) which should cause mapping to LogEntry.timestamp (String) to fail
        // but _source remains an object node so the fallback can store the raw JSON
        String responseJson =
                "{\n" +
                "  \"hits\": {\n" +
                "    \"hits\": [\n" +
                "      {\n" +
                "        \"_source\": {\n" +
                "          \"timestamp\": { \"nested\": \"value\" },\n" +
                "          \"level\": \"WARN\",\n" +
                "          \"extra\": { \"a\": 1 }\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";

        when(client.searchWithQuery("logs", "bad")).thenReturn(responseJson);

        List<LogEntry> results = service.searchLogs("logs", "bad");

        assertNotNull(results);
        assertEquals(1, results.size());

        LogEntry entry = results.get(0);
        // POJO mapping should have failed, so the typed fields remain null
        assertNull(entry.getTimestamp());
        // level may also be null because mapping failed; rely on rawJson for verification
        ObjectNode raw = entry.getRawJson();
        assertNotNull(raw, "Expected rawJson fallback to be present");
        assertTrue(raw.has("timestamp"));
        assertTrue(raw.get("timestamp").isObject());
        assertTrue(raw.get("timestamp").has("nested"));
        assertEquals("value", raw.get("timestamp").get("nested").asText());
        assertEquals("WARN", raw.get("level").asText());

        verify(client, times(1)).searchWithQuery("logs", "bad");
    }
}