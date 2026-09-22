package com.swfte.sdk.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Filters for {@code catalog().search(...)}. Unset fields are not sent.
 *
 * <pre>{@code
 * CatalogSearchParams.builder().q("invoice").kinds("workflow", "agent").minEvidence("corroborated").limit(10).build();
 * }</pre>
 */
public class CatalogSearchParams {
    private final String q;
    private final List<String> kinds;
    private final String scope;
    private final String domain;
    private final String capability;
    private final String industry;
    private final String minEvidence;
    private final Integer limit;
    private final String cursor;

    private CatalogSearchParams(Builder b) {
        this.q = b.q;
        this.kinds = b.kinds;
        this.scope = b.scope;
        this.domain = b.domain;
        this.capability = b.capability;
        this.industry = b.industry;
        this.minEvidence = b.minEvidence;
        this.limit = b.limit;
        this.cursor = b.cursor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getQ() { return q; }
    public List<String> getKinds() { return kinds == null ? Collections.emptyList() : kinds; }
    public String getScope() { return scope; }
    public String getDomain() { return domain; }
    public String getCapability() { return capability; }
    public String getIndustry() { return industry; }
    public String getMinEvidence() { return minEvidence; }
    public Integer getLimit() { return limit; }
    public String getCursor() { return cursor; }

    public static class Builder {
        private String q;
        private List<String> kinds;
        private String scope;
        private String domain;
        private String capability;
        private String industry;
        private String minEvidence;
        private Integer limit;
        private String cursor;

        /** Free-text query. */
        public Builder q(String q) { this.q = q; return this; }
        /** Kinds to include (sent comma-separated): workflow, agent, chatflow, widget, application, mcp-server, model, module, solution. */
        public Builder kinds(String... kinds) { this.kinds = Arrays.asList(kinds); return this; }
        public Builder kinds(List<String> kinds) { this.kinds = kinds; return this; }
        /** workspace, public or all. */
        public Builder scope(String scope) { this.scope = scope; return this; }
        public Builder domain(String domain) { this.domain = domain; return this; }
        public Builder capability(String capability) { this.capability = capability; return this; }
        public Builder industry(String industry) { this.industry = industry; return this; }
        /** Only entries at or above this evidence level. */
        public Builder minEvidence(String minEvidence) { this.minEvidence = minEvidence; return this; }
        /** Page size (server default 20). */
        public Builder limit(int limit) { this.limit = limit; return this; }
        /** {@code nextCursor} from the previous page. */
        public Builder cursor(String cursor) { this.cursor = cursor; return this; }

        public CatalogSearchParams build() {
            return new CatalogSearchParams(this);
        }
    }
}
