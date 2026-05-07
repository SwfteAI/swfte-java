package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.FileMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Files API resource — upload and manage files used by datasets, agents,
 * voice playback, and chatflows.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * FileMetadata uploaded = client.files().upload("invoice.pdf", "application/pdf", bytes);
 * String url = client.files().download(uploaded.getId());
 * }</pre>
 */
public class Files {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Files(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/api/v2/files"; }

    /** Get the upload-config (size limits, allowed mime types). */
    @SuppressWarnings("unchecked")
    public Map<String, Object> config() {
        return httpClient.getWithCustomBase(base() + "/config", Map.class);
    }

    /**
     * Upload a single file.
     */
    public FileMetadata upload(String filename, String mimeType, byte[] data) {
        Map<String, Object> fields = new HashMap<>();
        fields.put("file", data);
        if (filename != null) fields.put("filename", filename);
        if (mimeType != null) fields.put("mimeType", mimeType);
        return httpClient.postMultipart(base() + "/upload", fields, FileMetadata.class);
    }

    /** Upload a batch of files. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> uploadBatch(Map<String, Object> fields) {
        return httpClient.postMultipart(base() + "/upload-batch", fields, Map.class);
    }

    /** List uploaded files. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> list() { return list(0, 20); }

    @SuppressWarnings("unchecked")
    public Map<String, Object> list(int page, int size) {
        return httpClient.getWithCustomBase(base() + "?page=" + page + "&size=" + size, Map.class);
    }

    /** Get file metadata. */
    public FileMetadata get(String fileId) {
        return httpClient.getWithCustomBase(base() + "/" + fileId, FileMetadata.class);
    }

    /** Get a download URL or redirect for a file. */
    public String download(String fileId) {
        return httpClient.getWithCustomBase(base() + "/" + fileId + "/download", String.class);
    }

    /** Get a preview URL for a file. */
    public String preview(String fileId) {
        return httpClient.getWithCustomBase(base() + "/" + fileId + "/preview", String.class);
    }

    /** Delete a file. */
    public void delete(String fileId) {
        httpClient.deleteWithCustomBase(base() + "/" + fileId);
    }

    /** Update usage information for a file. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> updateUsage(String fileId, Map<String, Object> usage) {
        return httpClient.putWithCustomBase(base() + "/" + fileId + "/usage", usage, Map.class);
    }

    /** Run a cleanup pass for orphaned files. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> cleanup() {
        return httpClient.postWithCustomBase(base() + "/cleanup", new HashMap<>(), Map.class);
    }
}
