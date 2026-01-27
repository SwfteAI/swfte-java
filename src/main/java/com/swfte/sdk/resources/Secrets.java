package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Secret;
import com.swfte.sdk.models.SecretListResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Secrets API resource for managing API keys, OAuth tokens, and MCP tokens.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SwfteClient client = SwfteClient.builder()
 *     .apiKey("sk-swfte-...")
 *     .workspaceId("my-workspace")
 *     .build();
 *
 * // Create an API key secret
 * Secret secret = client.secrets().create(
 *     Secret.builder()
 *         .name("openai-key")
 *         .secretType(Secret.SecretType.API_KEY)
 *         .provider("openai")
 *         .value("sk-...")
 *         .build()
 * );
 *
 * // Create an OAuth token
 * Secret oauth = client.secrets().createOAuth(
 *     "github-token",
 *     "GITHUB",
 *     "access-token",
 *     "refresh-token",
 *     3600
 * );
 *
 * // Retrieve secret value
 * String value = client.secrets().getValue(secret.getId());
 * }</pre>
 */
public class Secrets {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Secrets(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    /**
     * Get the base URL for secret endpoints.
     */
    private String getBaseUrl() {
        return "/v1/secrets";
    }

    /**
     * Create a new secret.
     *
     * @param secret the secret to create
     * @return the created secret
     */
    public Secret create(Secret secret) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", secret.getName());
        payload.put("secretType", secret.getSecretType().name());

        if (secret.getProvider() != null) {
            payload.put("provider", secret.getProvider());
        }
        if (secret.getValue() != null) {
            payload.put("value", secret.getValue());
        }
        if (secret.getDescription() != null) {
            payload.put("description", secret.getDescription());
        }
        if (secret.getExpiresAt() != null) {
            payload.put("expiresAt", secret.getExpiresAt());
        }
        if (secret.getMetadata() != null) {
            payload.put("metadata", secret.getMetadata());
        }
        if (client.getWorkspaceId() != null) {
            payload.put("workspaceId", client.getWorkspaceId());
        }

        return httpClient.postWithCustomBase(getBaseUrl(), payload, Secret.class);
    }

    /**
     * Create an OAuth token secret.
     *
     * @param name the secret name
     * @param provider the OAuth provider (GOOGLE, GITHUB, SLACK, etc.)
     * @param accessToken the access token
     * @param refreshToken the refresh token (optional)
     * @param expiresIn expiration time in seconds
     * @return the created secret
     */
    public Secret createOAuth(String name, String provider, String accessToken,
                               String refreshToken, Integer expiresIn) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("secretType", "OAUTH");
        payload.put("provider", provider);
        payload.put("accessToken", accessToken);
        if (refreshToken != null) {
            payload.put("refreshToken", refreshToken);
        }
        if (expiresIn != null) {
            payload.put("expiresIn", expiresIn);
        }
        if (client.getWorkspaceId() != null) {
            payload.put("workspaceId", client.getWorkspaceId());
        }

        return httpClient.postWithCustomBase(getBaseUrl() + "/oauth", payload, Secret.class);
    }

    /**
     * Create an MCP token secret.
     *
     * @param name the secret name
     * @param mcpServer the MCP server identifier
     * @param token the MCP token
     * @param permissions list of permissions
     * @return the created secret
     */
    public Secret createMcp(String name, String mcpServer, String token, List<String> permissions) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("secretType", "MCP");
        payload.put("mcpServer", mcpServer);
        payload.put("token", token);
        if (permissions != null) {
            payload.put("permissions", permissions);
        }
        if (client.getWorkspaceId() != null) {
            payload.put("workspaceId", client.getWorkspaceId());
        }

        return httpClient.postWithCustomBase(getBaseUrl() + "/mcp", payload, Secret.class);
    }

    /**
     * Get a secret by ID.
     *
     * @param secretId the secret ID
     * @return the secret (value is masked)
     */
    public Secret get(String secretId) {
        return httpClient.getWithCustomBase(getBaseUrl() + "/" + secretId, Secret.class);
    }

    /**
     * List all secrets.
     *
     * @return list of secrets
     */
    public List<Secret> list() {
        return list(0, 20);
    }

    /**
     * List secrets with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return list of secrets
     */
    public List<Secret> list(int page, int size) {
        String url = getBaseUrl() + "?page=" + page + "&size=" + size;
        SecretListResponse response = httpClient.getWithCustomBase(url, SecretListResponse.class);
        return response.getSecrets();
    }

    /**
     * List secrets by type.
     *
     * @param secretType the secret type
     * @return list of secrets of that type
     */
    public List<Secret> listByType(Secret.SecretType secretType) {
        String url = getBaseUrl() + "?type=" + secretType.name();
        SecretListResponse response = httpClient.getWithCustomBase(url, SecretListResponse.class);
        return response.getSecrets();
    }

    /**
     * List secrets by provider.
     *
     * @param provider the provider name
     * @return list of secrets for that provider
     */
    public List<Secret> listByProvider(String provider) {
        String url = getBaseUrl() + "?provider=" + encodeParam(provider);
        SecretListResponse response = httpClient.getWithCustomBase(url, SecretListResponse.class);
        return response.getSecrets();
    }

    /**
     * Update a secret.
     *
     * @param secretId the secret ID
     * @param updates the updates to apply
     * @return the updated secret
     */
    public Secret update(String secretId, Secret updates) {
        Map<String, Object> payload = new HashMap<>();
        if (updates.getName() != null) {
            payload.put("name", updates.getName());
        }
        if (updates.getValue() != null) {
            payload.put("value", updates.getValue());
        }
        if (updates.getDescription() != null) {
            payload.put("description", updates.getDescription());
        }
        if (updates.getExpiresAt() != null) {
            payload.put("expiresAt", updates.getExpiresAt());
        }
        if (updates.getMetadata() != null) {
            payload.put("metadata", updates.getMetadata());
        }

        return httpClient.putWithCustomBase(getBaseUrl() + "/" + secretId, payload, Secret.class);
    }

    /**
     * Delete a secret.
     *
     * @param secretId the secret ID
     */
    public void delete(String secretId) {
        httpClient.deleteWithCustomBase(getBaseUrl() + "/" + secretId);
    }

    /**
     * Refresh an OAuth token.
     *
     * @param secretId the secret ID
     * @return the refreshed secret with new tokens
     */
    public Secret refreshOAuth(String secretId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + secretId + "/refresh",
            new HashMap<>(),
            Secret.class
        );
    }

    /**
     * Revoke a secret (mark as inactive).
     *
     * @param secretId the secret ID
     * @return the revoked secret
     */
    public Secret revoke(String secretId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + secretId + "/revoke",
            new HashMap<>(),
            Secret.class
        );
    }

    /**
     * Get the actual secret value.
     * Use with caution - this returns the unmasked value.
     *
     * @param secretId the secret ID
     * @return the secret value
     */
    @SuppressWarnings("unchecked")
    public String getValue(String secretId) {
        Map<String, Object> response = httpClient.getWithCustomBase(
            getBaseUrl() + "/" + secretId + "/value",
            Map.class
        );
        return (String) response.get("value");
    }

    /**
     * Rotate a secret (generate new value).
     *
     * @param secretId the secret ID
     * @return the rotated secret with new value
     */
    public Secret rotate(String secretId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + secretId + "/rotate",
            new HashMap<>(),
            Secret.class
        );
    }

    /**
     * Validate a secret is active and not expired.
     *
     * @param secretId the secret ID
     * @return true if valid
     */
    @SuppressWarnings("unchecked")
    public boolean validate(String secretId) {
        try {
            Map<String, Object> response = httpClient.getWithCustomBase(
                getBaseUrl() + "/" + secretId + "/validate",
                Map.class
            );
            Object valid = response.get("valid");
            return Boolean.TRUE.equals(valid);
        } catch (Exception e) {
            return false;
        }
    }

    private String encodeParam(String param) {
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return param;
        }
    }
}
