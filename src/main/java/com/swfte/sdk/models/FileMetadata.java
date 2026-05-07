package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a file uploaded to Swfte's file store. Files can be referenced
 * by datasets, documents, agents, voice playback, and chatflows.
 */
public class FileMetadata {

    private String id;
    private String name;
    private String mimeType;
    private Long size;
    private String workspaceId;
    private String purpose;
    private String url;
    private String checksum;
    private String storageBucket;
    private String storageKey;
    private Map<String, Object> usage;
    private LocalDateTime createdAt;

    public FileMetadata() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }
    public String getStorageBucket() { return storageBucket; }
    public void setStorageBucket(String storageBucket) { this.storageBucket = storageBucket; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public Map<String, Object> getUsage() { return usage; }
    public void setUsage(Map<String, Object> usage) { this.usage = usage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final FileMetadata f = new FileMetadata();
        public Builder id(String v) { f.id = v; return this; }
        public Builder name(String v) { f.name = v; return this; }
        public Builder mimeType(String v) { f.mimeType = v; return this; }
        public Builder size(Long v) { f.size = v; return this; }
        public Builder purpose(String v) { f.purpose = v; return this; }
        public FileMetadata build() { return f; }
    }
}
