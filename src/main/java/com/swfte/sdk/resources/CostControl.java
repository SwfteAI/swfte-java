package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.RoutingRule;
import com.swfte.sdk.models.UsageCap;

import java.util.HashMap;
import java.util.Map;

/**
 * Cost Control API resource — model routing rules, per-workspace and per-model
 * usage caps, scaling configuration for self-hosted deployments.
 *
 * <p>See <a href="https://www.swfte.com/products/cost-control">swfte.com/products/cost-control</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * RoutingRule rule = client.costControl().createRoutingRule(
 *     RoutingRule.builder()
 *         .name("Cap GPT-4 latency")
 *         .matchModel("openai:gpt-4")
 *         .fallbackModel("anthropic:claude-haiku")
 *         .priority(10)
 *         .enabled(true)
 *         .build()
 * );
 *
 * client.costControl().setWorkspaceCap(
 *     UsageCap.builder().budgetUsd(2500.0).period("MONTHLY").enforced(true).build()
 * );
 * }</pre>
 */
public class CostControl {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public CostControl(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/v2/cost-control"; }

    // -------- Routing rules --------

    @SuppressWarnings("unchecked")
    public Map<String, Object> listRoutingRules() {
        return httpClient.getWithCustomBase(base() + "/routing-rules", Map.class);
    }

    public RoutingRule createRoutingRule(RoutingRule rule) {
        return httpClient.postWithCustomBase(base() + "/routing-rules", rule, RoutingRule.class);
    }

    public RoutingRule getRoutingRule(String ruleId) {
        return httpClient.getWithCustomBase(base() + "/routing-rules/" + ruleId, RoutingRule.class);
    }

    public RoutingRule updateRoutingRule(String ruleId, RoutingRule updates) {
        return httpClient.putWithCustomBase(
            base() + "/routing-rules/" + ruleId,
            updates,
            RoutingRule.class
        );
    }

    public void deleteRoutingRule(String ruleId) {
        httpClient.deleteWithCustomBase(base() + "/routing-rules/" + ruleId);
    }

    public RoutingRule toggleRoutingRule(String ruleId, boolean enabled) {
        Map<String, Object> body = new HashMap<>();
        body.put("enabled", enabled);
        return httpClient.patchWithCustomBase(
            base() + "/routing-rules/" + ruleId + "/toggle",
            body,
            RoutingRule.class
        );
    }

    // -------- Usage caps --------

    @SuppressWarnings("unchecked")
    public Map<String, Object> listUsageCaps() {
        return httpClient.getWithCustomBase(base() + "/usage-caps", Map.class);
    }

    public UsageCap setWorkspaceCap(UsageCap cap) {
        return httpClient.putWithCustomBase(base() + "/usage-caps/workspace", cap, UsageCap.class);
    }

    public UsageCap setModelCap(String modelId, UsageCap cap) {
        return httpClient.putWithCustomBase(
            base() + "/usage-caps/model/" + modelId,
            cap,
            UsageCap.class
        );
    }

    public void deleteCap(String capId) {
        httpClient.deleteWithCustomBase(base() + "/usage-caps/" + capId);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> usageStats() {
        return httpClient.getWithCustomBase(base() + "/usage-stats", Map.class);
    }

    // -------- Scaling config --------

    @SuppressWarnings("unchecked")
    public Map<String, Object> scalingConfig(String deploymentId) {
        return httpClient.getWithCustomBase(base() + "/scaling/" + deploymentId, Map.class);
    }
}
