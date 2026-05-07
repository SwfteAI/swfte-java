package com.swfte.sdk.models;

import java.util.List;
import java.util.Map;

/**
 * Response payload from a RAG hybrid-search call.
 */
public class RagSearchResponse {

    private List<Result> results;
    private String strategy;
    private Long latencyMs;
    private Map<String, Object> meta;

    public RagSearchResponse() {}

    public List<Result> getResults() { return results; }
    public void setResults(List<Result> results) { this.results = results; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public Map<String, Object> getMeta() { return meta; }
    public void setMeta(Map<String, Object> meta) { this.meta = meta; }

    /**
     * A single retrieved segment with its similarity score.
     */
    public static class Result {
        private String id;
        private String documentId;
        private String datasetId;
        private String content;
        private Double score;
        private Map<String, Object> metadata;

        public Result() {}

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public String getDatasetId() { return datasetId; }
        public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public Double getScore() { return score; }
        public void setScore(Double score) { this.score = score; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
