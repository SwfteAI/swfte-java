package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a published module on the Swfte marketplace.
 */
public class Publication {

    private String id;
    private String moduleId;
    private String publisherWorkspaceId;
    private String name;
    private String description;
    private String category;
    private List<String> tags;
    private String visibility;
    private Double price;
    private String currency;
    private Map<String, Object> metadata;
    private Integer installCount;
    private Double rating;
    private LocalDateTime publishedAt;

    public Publication() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }
    public String getPublisherWorkspaceId() { return publisherWorkspaceId; }
    public void setPublisherWorkspaceId(String publisherWorkspaceId) { this.publisherWorkspaceId = publisherWorkspaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Integer getInstallCount() { return installCount; }
    public void setInstallCount(Integer installCount) { this.installCount = installCount; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Publication p = new Publication();
        public Builder id(String v) { p.id = v; return this; }
        public Builder moduleId(String v) { p.moduleId = v; return this; }
        public Builder name(String v) { p.name = v; return this; }
        public Builder description(String v) { p.description = v; return this; }
        public Builder category(String v) { p.category = v; return this; }
        public Builder tags(List<String> v) { p.tags = v; return this; }
        public Builder visibility(String v) { p.visibility = v; return this; }
        public Builder price(Double v) { p.price = v; return this; }
        public Publication build() { return p; }
    }
}
