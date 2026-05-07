package com.swfte.sdk.models;

import java.util.List;
import java.util.Map;

/**
 * Request payload for hybrid RAG search.
 */
public class RagSearchRequest {

    private String query;
    private List<String> datasetIds;
    private Integer topK;
    private String strategy;
    private Boolean rerank;
    private String rerankerModel;
    private Map<String, Object> filters;
    private Double scoreThreshold;
    private Boolean includeMetadata;

    public RagSearchRequest() {}

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public List<String> getDatasetIds() { return datasetIds; }
    public void setDatasetIds(List<String> datasetIds) { this.datasetIds = datasetIds; }
    public Integer getTopK() { return topK; }
    public void setTopK(Integer topK) { this.topK = topK; }
    public String getStrategy() { return strategy; }
    public void setStrategy(String strategy) { this.strategy = strategy; }
    public Boolean getRerank() { return rerank; }
    public void setRerank(Boolean rerank) { this.rerank = rerank; }
    public String getRerankerModel() { return rerankerModel; }
    public void setRerankerModel(String rerankerModel) { this.rerankerModel = rerankerModel; }
    public Map<String, Object> getFilters() { return filters; }
    public void setFilters(Map<String, Object> filters) { this.filters = filters; }
    public Double getScoreThreshold() { return scoreThreshold; }
    public void setScoreThreshold(Double scoreThreshold) { this.scoreThreshold = scoreThreshold; }
    public Boolean getIncludeMetadata() { return includeMetadata; }
    public void setIncludeMetadata(Boolean includeMetadata) { this.includeMetadata = includeMetadata; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RagSearchRequest r = new RagSearchRequest();
        public Builder query(String v) { r.query = v; return this; }
        public Builder datasetIds(List<String> v) { r.datasetIds = v; return this; }
        public Builder topK(Integer v) { r.topK = v; return this; }
        public Builder strategy(String v) { r.strategy = v; return this; }
        public Builder rerank(Boolean v) { r.rerank = v; return this; }
        public Builder rerankerModel(String v) { r.rerankerModel = v; return this; }
        public Builder filters(Map<String, Object> v) { r.filters = v; return this; }
        public Builder scoreThreshold(Double v) { r.scoreThreshold = v; return this; }
        public Builder includeMetadata(Boolean v) { r.includeMetadata = v; return this; }
        public RagSearchRequest build() { return r; }
    }
}
