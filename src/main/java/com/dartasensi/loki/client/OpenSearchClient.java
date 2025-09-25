package com.dartasensi.loki.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "opensearch", url = "${opensearch.scheme}://${opensearch.host}:${opensearch.port}")
public interface OpenSearchClient {
    
    @GetMapping("/{index}/_search")
    String search(@PathVariable("index") String index);
}