package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Deployment;
import com.swfte.sdk.models.DeploymentListResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deployments API resource for managing model deployments via RunPod.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SwfteClient client = SwfteClient.builder()
 *     .apiKey("sk-swfte-...")
 *     .build();
 * 
 * // Create a deployment
 * Deployment deployment = client.deployments().create(
 *     Deployment.builder()
 *         .modelName("mistralai/Mistral-7B-Instruct-v0.2")
 *         .modelType("llm")
 *         .build()
 * );
 * 
 * // Wait for it to be ready
 * Deployment ready = client.deployments().waitForReady(deployment.getId());
 * 
 * // Get the endpoint URL
 * String endpointUrl = ready.getEndpointUrl();
 * }</pre>
 */
public class Deployments {
    
    private final HttpClient httpClient;
    private final SwfteClient client;
    
    public Deployments(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }
    
    /**
     * Get the base URL for deployment endpoints.
     */
    private String getBaseUrl() {
        return "/v1/inference";
    }
    
    /**
     * Create a new deployment.
     *
     * @param deployment the deployment to create
     * @return the created deployment
     */
    public Deployment create(Deployment deployment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("modelName", deployment.getModelName());
        if (deployment.getModelType() != null) {
            payload.put("modelType", deployment.getModelType());
        } else {
            payload.put("modelType", "llm");
        }
        if (deployment.getParameters() != null) {
            payload.put("parameters", deployment.getParameters());
        }
        if (deployment.getEnvironment() != null) {
            payload.put("environment", deployment.getEnvironment());
        }
        if (client.getWorkspaceId() != null) {
            payload.put("workspaceId", client.getWorkspaceId());
        }
        
        return httpClient.postWithCustomBase(getBaseUrl() + "/models/deploy", payload, Deployment.class);
    }
    
    /**
     * Get a deployment by ID.
     *
     * @param deploymentId the deployment ID
     * @return the deployment
     */
    public Deployment get(String deploymentId) {
        return httpClient.getWithCustomBase(getBaseUrl() + "/deployments/" + deploymentId, Deployment.class);
    }
    
    /**
     * Delete a deployment.
     *
     * @param deploymentId the deployment ID
     */
    public void delete(String deploymentId) {
        httpClient.deleteWithCustomBase(getBaseUrl() + "/deployments/" + deploymentId);
    }
    
    /**
     * List all deployments.
     *
     * @return list of deployments
     */
    public List<Deployment> list() {
        return list(0, 20);
    }

    /**
     * List deployments with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return list of deployments
     */
    public List<Deployment> list(int page, int size) {
        String url = getBaseUrl() + "/deployments?page=" + page + "&size=" + size;
        DeploymentListResponse response = httpClient.getWithCustomBase(url, DeploymentListResponse.class);
        return response.getDeploymentList();
    }

    /**
     * List all deployments and return full response.
     *
     * @return deployment list response with metadata
     */
    public DeploymentListResponse listResponse() {
        return listResponse(0, 20);
    }

    /**
     * List deployments with pagination and return full response.
     *
     * @param page the page number
     * @param size the page size
     * @return deployment list response with metadata
     */
    public DeploymentListResponse listResponse(int page, int size) {
        String url = getBaseUrl() + "/deployments?page=" + page + "&size=" + size;
        return httpClient.getWithCustomBase(url, DeploymentListResponse.class);
    }
    
    /**
     * Get deployment status.
     *
     * @param deploymentId the deployment ID
     * @return the deployment with current status
     */
    public Deployment getStatus(String deploymentId) {
        return httpClient.getWithCustomBase(getBaseUrl() + "/deployments/" + deploymentId, Deployment.class);
    }
    
    /**
     * Start a stopped deployment.
     *
     * @param deploymentId the deployment ID
     * @return the started deployment
     */
    public Deployment start(String deploymentId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/deployments/" + deploymentId + "/start",
            new HashMap<>(),
            Deployment.class
        );
    }
    
    /**
     * Stop a running deployment.
     *
     * @param deploymentId the deployment ID
     * @return the stopped deployment
     */
    public Deployment stop(String deploymentId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/deployments/" + deploymentId + "/stop",
            new HashMap<>(),
            Deployment.class
        );
    }
    
    /**
     * Restart a deployment.
     *
     * @param deploymentId the deployment ID
     * @return the restarted deployment
     */
    public Deployment restart(String deploymentId) {
        return httpClient.postWithCustomBase(
            getBaseUrl() + "/deployments/" + deploymentId + "/restart",
            new HashMap<>(),
            Deployment.class
        );
    }
    
    /**
     * Get deployment health.
     *
     * @param deploymentId the deployment ID
     * @return health status as a map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getHealth(String deploymentId) {
        return httpClient.getWithCustomBase(
            getBaseUrl() + "/deployments/" + deploymentId + "/health",
            Map.class
        );
    }
    
    /**
     * Analyze deployment logs.
     *
     * @param deploymentId the deployment ID
     * @return log analysis as a string
     */
    public String analyzeLogs(String deploymentId) {
        return httpClient.getWithCustomBase(
            getBaseUrl() + "/deployments/" + deploymentId + "/analyze-logs",
            String.class
        );
    }
    
    /**
     * Wait for a deployment to be ready.
     *
     * @param deploymentId the deployment ID
     * @return the ready deployment
     * @throws RuntimeException if timeout or deployment fails
     */
    public Deployment waitForReady(String deploymentId) {
        return waitForReady(deploymentId, 600000, 10000);
    }
    
    /**
     * Wait for a deployment to be ready with custom timeout.
     *
     * @param deploymentId the deployment ID
     * @param timeoutMs timeout in milliseconds
     * @param pollIntervalMs poll interval in milliseconds
     * @return the ready deployment
     * @throws RuntimeException if timeout or deployment fails
     */
    public Deployment waitForReady(String deploymentId, long timeoutMs, long pollIntervalMs) {
        long startTime = System.currentTimeMillis();
        
        while (true) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > timeoutMs) {
                throw new RuntimeException("Deployment " + deploymentId + " did not become ready within " + timeoutMs + "ms");
            }
            
            Deployment deployment = getStatus(deploymentId);
            
            if (deployment.getState() == Deployment.State.RUNNING) {
                return deployment;
            } else if (deployment.getState() == Deployment.State.FAILED) {
                throw new RuntimeException("Deployment " + deploymentId + " failed: " + deployment.getStatusMessage());
            } else if (deployment.getState() == Deployment.State.TERMINATED) {
                throw new RuntimeException("Deployment " + deploymentId + " was terminated");
            }
            
            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for deployment", e);
            }
        }
    }
    
    
    /**
     * Check if deployment endpoint is healthy.
     *
     * @param deploymentId the deployment ID
     * @return true if healthy
     */
    public boolean isHealthy(String deploymentId) {
        try {
            Map<String, Object> health = getHealth(deploymentId);
            Object status = health.get("status");
            return "healthy".equals(status) || "ok".equals(status);
        } catch (Exception e) {
            return false;
        }
    }
}







