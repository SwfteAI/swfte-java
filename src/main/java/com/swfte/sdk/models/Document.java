package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a document inside a Swfte dataset.
 */
public class Document {

    private String id;
    private String datasetId;
    private String name;
    private String dataSourceType;
    private String content;
    private String url;
    private String fileId;
    private String status;
    private Integer wordCount;
    private Integer segmentCount;
    private Boolean enabled;
    private Map<String, Object> metadata;
    private Map<String, Object> processingRule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Document() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDataSourceType() { return dataSourceType; }
    public void setDataSourceType(String dataSourceType) { this.dataSourceType = dataSourceType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getWordCount() { return wordCount; }
    public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
    public Integer getSegmentCount() { return segmentCount; }
    public void setSegmentCount(Integer segmentCount) { this.segmentCount = segmentCount; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Map<String, Object> getProcessingRule() { return processingRule; }
    public void setProcessingRule(Map<String, Object> processingRule) { this.processingRule = processingRule; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Document d = new Document();
        public Builder id(String v) { d.id = v; return this; }
        public Builder datasetId(String v) { d.datasetId = v; return this; }
        public Builder name(String v) { d.name = v; return this; }
        public Builder dataSourceType(String v) { d.dataSourceType = v; return this; }
        public Builder content(String v) { d.content = v; return this; }
        public Builder url(String v) { d.url = v; return this; }
        public Builder fileId(String v) { d.fileId = v; return this; }
        public Builder status(String v) { d.status = v; return this; }
        public Builder enabled(Boolean v) { d.enabled = v; return this; }
        public Builder metadata(Map<String, Object> v) { d.metadata = v; return this; }
        public Builder processingRule(Map<String, Object> v) { d.processingRule = v; return this; }
        public Document build() { return d; }
    }
}
