package com.dartasensi.loki.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Suggested Feign client interface. If your repository already has a similar file,
 * ensure the method signature below matches it. This client calls the OpenSearch
 * _search endpoint using the simple "q" query parameter.
 *
 * Optional enhancement: add a size param or a POST-based search method that accepts
 * the full JSON query DSL for richer queries.
 */
@FeignClient(name = "opensearch", url = "${opensearch.scheme}://${opensearch.host}:${opensearch.port}")
public interface OpenSearchClient {

    @GetMapping("/{index}/_search")
    String searchWithQuery(@PathVariable("index") String index, @RequestParam("q") String q);

    // Example enhancement:
    // @GetMapping("/{index}/_search")
    // String searchWithQueryAndSize(@PathVariable("index") String index,
    //                               @RequestParam("q") String q,
    //                               @RequestParam(value = "size", required = false) Integer size);
}