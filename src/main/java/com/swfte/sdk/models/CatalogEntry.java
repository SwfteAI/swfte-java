package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A catalog entry. Search returns the summary fields; {@code catalog().get(kind, id)}
 * also fills {@link #getRationale()}, {@link #getEvidenceRecords()},
 * {@link #getDependencies()} and {@link #getReviews()}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CatalogEntry {

    /** A classification facet (domain, capability, industry, pattern, risk). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Facet {
        private String key;
        private String value;
        private Double confidence;
        private String status;
        private String source;

        public String getKey() { return key; }
        public String getValue() { return value; }
        /** 0..1, or {@code null} when a human set it. */
        public Double getConfidence() { return confidence; }
        /** PROPOSED, CONFIRMED or DISPUTED. */
        public String getStatus() { return status; }
        /** jev, human or rule. */
        public String getSource() { return source; }
    }

    /** How proven the entry is, and why. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Evidence {
        private String level;
        private Map<String, Integer> runs;
        private Double successRate;
        private String lastRunAt;
        private Integer evals;
        private Map<String, Integer> reviews;
        private List<String> reasons;

        /** unmeasured, observed, corroborated, validated, verified, stale or disputed. */
        public String getLevel() { return level; }
        /** {@code total}, {@code succeeded}, {@code failed}. */
        public Map<String, Integer> getRuns() { return runs == null ? Collections.emptyMap() : runs; }
        public Double getSuccessRate() { return successRate; }
        public String getLastRunAt() { return lastRunAt; }
        public Integer getEvals() { return evals; }
        /** {@code approve}, {@code reject}. */
        public Map<String, Integer> getReviews() { return reviews == null ? Collections.emptyMap() : reviews; }
        /** Why the level is what it is. */
        public List<String> getReasons() { return reasons == null ? Collections.emptyList() : reasons; }
    }

    private String catalogRef;
    private String kind;
    private String id;
    private String workspaceId;
    private String scope;
    private String name;
    private String description;
    private String source;
    private String listingId;
    private List<Facet> facets;
    private Evidence evidence;
    private String updatedAt;
    private String shapeHash;

    // Detail-only fields.
    private Map<String, Object> rationale;
    private List<Map<String, Object>> evidenceRecords;
    private List<Map<String, Object>> dependencies;
    private List<Map<String, Object>> reviews;

    /** {@code "<kind>:<id>"}. */
    public String getCatalogRef() { return catalogRef; }
    public String getKind() { return kind; }
    public String getId() { return id; }
    public String getWorkspaceId() { return workspaceId; }
    /** workspace or public. */
    public String getScope() { return scope; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getSource() { return source; }
    public String getListingId() { return listingId; }
    public List<Facet> getFacets() { return facets == null ? Collections.emptyList() : facets; }
    public Evidence getEvidence() { return evidence; }
    public String getUpdatedAt() { return updatedAt; }
    public String getShapeHash() { return shapeHash; }
    public Map<String, Object> getRationale() { return rationale; }
    /** Latest 20: {@code {type, refId, status, at}}. */
    public List<Map<String, Object>> getEvidenceRecords() { return evidenceRecords == null ? Collections.emptyList() : evidenceRecords; }
    /** {@code {catalogRef, relation}}. */
    public List<Map<String, Object>> getDependencies() { return dependencies == null ? Collections.emptyList() : dependencies; }
    /** {@code {id, verdict, role, note, reviewerId, at}}. */
    public List<Map<String, Object>> getReviews() { return reviews == null ? Collections.emptyList() : reviews; }
}
