package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

/**
 * Response of {@code GET /v2/catalog/search}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CatalogSearchResponse {
    private List<CatalogEntry> items;
    private String nextCursor;
    private List<String> degraded;

    public List<CatalogEntry> getItems() { return items == null ? Collections.emptyList() : items; }
    /** Pass to {@link CatalogSearchParams.Builder#cursor(String)} for the next page; {@code null} at the end. */
    public String getNextCursor() { return nextCursor; }
    /** Subsystems that were unavailable (e.g. {@code jev_rerank}); the search still answered. */
    public List<String> getDegraded() { return degraded == null ? Collections.emptyList() : degraded; }
}
