package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Represents a secret.
 */
public class Secret {

    public enum SecretType {
        MANUAL, OAUTH, MCP
    }

    public enum SecretStatus {
        ACTIVE, EXPIRED, REVOKED, PENDING
    }

    private String id;
    private String name;

    @JsonProperty("workspaceId")
    private String workspaceId;

    @JsonProperty("secretType")
    private SecretType secretType;

    private SecretStatus status;
    private String description;
    private String category;
    private String environment;

    @JsonProperty("toolId")
    private String toolId;

    private String provider;

    @JsonProperty("maskedValue")
    private String maskedValue;

    // For creating secrets - not returned by API
    private String value;

    @JsonProperty("expiresAt")
    private String expiresAt;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("lastUsedAt")
    private String lastUsedAt;

    private Map<String, Object> metadata;

    // Default constructor
    public Secret() {}

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public SecretType getSecretType() {
        return secretType;
    }

    public void setSecretType(SecretType secretType) {
        this.secretType = secretType;
    }

    public SecretStatus getStatus() {
        return status;
    }

    public void setStatus(SecretStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getToolId() {
        return toolId;
    }

    public void setToolId(String toolId) {
        this.toolId = toolId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getMaskedValue() {
        return maskedValue;
    }

    public void setMaskedValue(String maskedValue) {
        this.maskedValue = maskedValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(String lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Builder for Secret.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Secret secret = new Secret();

        public Builder name(String name) {
            secret.name = name;
            return this;
        }

        public Builder secretType(SecretType secretType) {
            secret.secretType = secretType;
            return this;
        }

        public Builder value(String value) {
            secret.value = value;
            return this;
        }

        public Builder description(String description) {
            secret.description = description;
            return this;
        }

        public Builder category(String category) {
            secret.category = category;
            return this;
        }

        public Builder environment(String environment) {
            secret.environment = environment;
            return this;
        }

        public Builder toolId(String toolId) {
            secret.toolId = toolId;
            return this;
        }

        public Builder provider(String provider) {
            secret.provider = provider;
            return this;
        }

        public Builder expiresAt(String expiresAt) {
            secret.expiresAt = expiresAt;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            secret.metadata = metadata;
            return this;
        }

        public Secret build() {
            return secret;
        }
    }
}
