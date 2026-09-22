package com.swfte.sdk.resources;

import com.swfte.sdk.HttpClient;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.models.CatalogContract;
import com.swfte.sdk.models.CatalogEntry;
import com.swfte.sdk.models.CatalogSearchParams;
import com.swfte.sdk.models.CatalogSearchResponse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalog API ({@code /v2/catalog}) — find proven artifacts across kinds, read
 * their evidence and the contract for calling them.
 *
 * <pre>{@code
 * CatalogSearchResponse page = client.catalog().search(
 *     CatalogSearchParams.builder().q("invoice").kinds("workflow").minEvidence("corroborated").build());
 * CatalogEntry detail = client.catalog().get("workflow", page.getItems().get(0).getId());
 * CatalogContract contract = client.catalog().contract("workflow", page.getItems().get(0).getId());
 * System.out.println(contract.getInvoke().getMethod() + " " + contract.getInvoke().getPath());
 * }</pre>
 */
public class Catalog {

    private final HttpClient httpClient;

    public Catalog(SwfteClient client) {
        this.httpClient = new HttpClient(client);
    }

    /** {@code GET /v2/catalog/search} with no filters. */
    public CatalogSearchResponse search() {
        return search(CatalogSearchParams.builder().build());
    }

    /** {@code GET /v2/catalog/search}. */
    public CatalogSearchResponse search(CatalogSearchParams params) {
        CatalogSearchParams p = params != null ? params : CatalogSearchParams.builder().build();
        Map<String, String> query = new LinkedHashMap<>();
        put(query, "q", p.getQ());
        if (!p.getKinds().isEmpty()) {
            put(query, "kinds", String.join(",", p.getKinds()));
        }
        put(query, "scope", p.getScope());
        put(query, "domain", p.getDomain());
        put(query, "capability", p.getCapability());
        put(query, "industry", p.getIndustry());
        put(query, "minEvidence", p.getMinEvidence());
        put(query, "limit", p.getLimit() == null ? null : String.valueOf(p.getLimit()));
        put(query, "cursor", p.getCursor());

        StringBuilder path = new StringBuilder("/v2/catalog/search");
        char sep = '?';
        for (Map.Entry<String, String> e : query.entrySet()) {
            path.append(sep).append(encode(e.getKey())).append('=').append(encode(e.getValue()));
            sep = '&';
        }
        CatalogSearchResponse res = httpClient.apiRequest("GET", path.toString(), null, CatalogSearchResponse.class);
        return res != null ? res : new CatalogSearchResponse();
    }

    /** {@code GET /v2/catalog/{kind}/{id}} — evidence records, dependencies, reviews. */
    public CatalogEntry get(String kind, String id) {
        return httpClient.apiRequest("GET", entryPath(kind, id), null, CatalogEntry.class);
    }

    /** {@code GET /v2/catalog/{kind}/{id}/contract} — invoke method/path, schemas, snippets. */
    public CatalogContract contract(String kind, String id) {
        return httpClient.apiRequest("GET", entryPath(kind, id) + "/contract", null, CatalogContract.class);
    }

    private static String entryPath(String kind, String id) {
        if (kind == null || kind.isEmpty()) {
            throw new SwfteException("kind is required");
        }
        if (id == null || id.isEmpty()) {
            throw new SwfteException("id is required");
        }
        return "/v2/catalog/" + encode(kind) + "/" + encode(id);
    }

    private static void put(Map<String, String> query, String key, String value) {
        if (value != null && !value.isEmpty()) {
            query.put(key, value);
        }
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
