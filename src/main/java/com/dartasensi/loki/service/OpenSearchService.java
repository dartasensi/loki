package com.dartasensi.loki.service;

import com.dartasensi.loki.client.OpenSearchClient;
import com.dartasensi.loki.model.LogEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple service that formats a basic query, calls the OpenSearch client,
 * and parses the response into a list of LogEntry POJOs (or raw JsonNodes if mapping fails).
 *
 * Notes:
 * - This implementation calls the Feign client method that accepts a single "q" request param.
 * - For more advanced queries (JSON body, pagination, size), extend the client to accept body/params.
 */
@Service
public class OpenSearchService {

    private final OpenSearchClient client;
    private final ObjectMapper mapper;

    public OpenSearchService(OpenSearchClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    /**
     * Run a simple query against the given index. The query string should be an OpenSearch
     * simple query string (e.g. "message:error OR level:WARN").
     *
     * Returns a list of LogEntry objects mapped from each hit._source. If mapping to LogEntry
     * fails for a hit, the raw JsonNode for _source will still be converted into a LogEntry
     * with its rawJson filled.
     */
    public List<LogEntry> searchLogs(String index, String q) throws IOException {
        String rawResponse = client.searchWithQuery(index, q == null ? "" : q);
        JsonNode root = mapper.readTree(rawResponse);

        List<LogEntry> results = new ArrayList<>();
        JsonNode hitsArray = root.path("hits").path("hits");
        if (hitsArray.isArray()) {
            for (JsonNode hit : hitsArray) {
                JsonNode source = hit.path("_source");
                try {
                    // Try to map to known POJO
                    LogEntry entry = mapper.treeToValue(source, LogEntry.class);
                    results.add(entry);
                } catch (Exception ex) {
                    // Fallback: put raw JSON into LogEntry.rawJson
                    LogEntry fallback = new LogEntry();
                    if (source.isObject()) {
                        fallback.setRawJson((ObjectNode) source);
                    }
                    results.add(fallback);
                }
            }
        }
        return results;
    }
}