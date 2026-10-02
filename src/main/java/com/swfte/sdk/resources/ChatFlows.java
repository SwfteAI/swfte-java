package com.swfte.sdk.resources;

import com.swfte.sdk.CallSite;
import com.swfte.sdk.exceptions.SwfteException;
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.ChatFlow;
import com.swfte.sdk.models.ChatFlowSession;
import com.swfte.sdk.models.ChatFlowVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * ChatFlows API resource — conversational forms (onboarding, lead-qualification,
 * support, surveys) with field extraction, validation, branching, and multi-channel
 * delivery (web, WhatsApp, Telegram, voice).
 *
 * <p>See <a href="https://www.swfte.com/products/chatflows">swfte.com/products/chatflows</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ChatFlow flow = client.chatflows().create(
 *     ChatFlow.builder()
 *         .name("Lead Capture")
 *         .description("Qualify inbound leads")
 *         .build()
 * );
 *
 * client.chatflows().deploy(flow.getId());
 * ChatFlowSession session = client.chatflows().startSession(flow.getId(), Map.of("channel", "web"));
 * }</pre>
 */
public class ChatFlows {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public ChatFlows(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }


    private static String routeSegment(String value) {
        if (value == null || value.isEmpty() || value.equals(".") || value.equals("..")) throw new SwfteException("identifier must be a nonempty raw route identity");
        for (int i=0;i<value.length();i++) {
            char code=value.charAt(i);
            if (code < 32 || code == 127) throw new SwfteException("identifier contains control characters");
            if (Character.isHighSurrogate(code)) {
                if (++i >= value.length() || !Character.isLowSurrogate(value.charAt(i))) throw new SwfteException("identifier contains malformed Unicode");
            } else if (Character.isLowSurrogate(code)) throw new SwfteException("identifier contains malformed Unicode");
        }
        return encode(value);
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String base() { return "/v2/chatflows"; }
    private String builderBase() { return "/v2/chatflows/builder"; }

    /** Access the chatflow builder sub-resource. */
    public Builder builder() { return new Builder(); }

    /** Access the chatflow versions sub-resource. */
    public Versions versions(String chatFlowId) { return new Versions(chatFlowId); }

    /** Access the chatflow publishing sub-resource. */
    public Publishing publishing() { return new Publishing(); }

    /** Create a new chatflow. */
    public ChatFlow create(ChatFlow flow) {
        return httpClient.postWithCustomBase(base(), flow, ChatFlow.class);
    }

    /** Get a chatflow by ID. */
    public ChatFlow get(String chatFlowId) {
        return httpClient.getWithCustomBase(base() + "/" + chatFlowId, ChatFlow.class);
    }

    /** List chatflows in the current workspace. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> list() { return list(0, 20); }

    /** List chatflows with pagination. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> list(int page, int size) {
        return httpClient.getWithCustomBase(base() + "?page=" + page + "&size=" + size, Map.class);
    }

    /** Update an existing chatflow (PUT). */
    public ChatFlow update(String chatFlowId, ChatFlow updates) {
        return httpClient.putWithCustomBase(base() + "/" + chatFlowId, updates, ChatFlow.class);
    }

    /** Delete a chatflow. */
    public void delete(String chatFlowId) {
        httpClient.deleteWithCustomBase(base() + "/" + chatFlowId);
    }

    /** Validate a chatflow definition. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> validate(String chatFlowId) {
        return httpClient.postWithCustomBase(base() + "/" + chatFlowId + "/validate", new HashMap<>(), Map.class);
    }

    /** Deploy a chatflow. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deploy(String chatFlowId) {
        return httpClient.postWithCustomBase(base() + "/" + chatFlowId + "/deploy", new HashMap<>(), Map.class);
    }

    /** Undeploy a chatflow. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> undeploy(String chatFlowId) {
        return httpClient.postWithCustomBase(base() + "/" + chatFlowId + "/undeploy", new HashMap<>(), Map.class);
    }

    /** Start a runtime session against a deployed chatflow. */
    public ChatFlowSession startSession(String chatFlowId, Map<String, Object> context) {
        return startSession(chatFlowId, context, null);
    }

    /**
     * {@link #startSession(String, Map)} attributed to a code-map call site.
     *
     * @param callsite sent as {@value CallSite#HEADER} when valid; {@code null} falls back to
     *                 opt-in stack capture (see {@link com.swfte.sdk.CallsiteResolver})
     */
    public ChatFlowSession startSession(String chatFlowId, Map<String, Object> context, CallSite callsite) {
        return httpClient.requestWithCustomBase(
            "POST",
            base() + "/" + routeSegment(chatFlowId) + "/sessions",
            context != null ? context : new HashMap<>(),
            ChatFlowSession.class,
            client.getCallsiteResolver().resolve(callsite)
        );
    }

    /** List sessions for a chatflow. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> listSessions(String chatFlowId) {
        return httpClient.getWithCustomBase(base() + "/" + routeSegment(chatFlowId) + "/sessions", Map.class);
    }

    /** Get aggregate session stats for a chatflow. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> stats(String chatFlowId) {
        return httpClient.getWithCustomBase(base() + "/" + routeSegment(chatFlowId) + "/stats", Map.class);
    }

    /** Get a single session by id. */
    public ChatFlowSession getSession(String sessionId) {
        return httpClient.getWithCustomBase(base() + "/sessions/" + routeSegment(sessionId), ChatFlowSession.class);
    }

    /**
     * ChatFlow builder sub-resource — templates, field/action types, previews.
     */
    public class Builder {
        @SuppressWarnings("unchecked")
        public Map<String, Object> fieldTypes() {
            return httpClient.getWithCustomBase(builderBase() + "/field-types", Map.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> actionTypes() {
            return httpClient.getWithCustomBase(builderBase() + "/action-types", Map.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> pressStrategies() {
            return httpClient.getWithCustomBase(builderBase() + "/press-strategies", Map.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> templates() {
            return httpClient.getWithCustomBase(builderBase() + "/templates", Map.class);
        }

        public ChatFlow fromTemplate(String templateId, Map<String, Object> overrides) {
            return httpClient.postWithCustomBase(
                builderBase() + "/from-template/" + templateId,
                overrides != null ? overrides : new HashMap<>(),
                ChatFlow.class
            );
        }

        public ChatFlow fromTemplateDynamic(String templateId, Map<String, Object> body) {
            return httpClient.postWithCustomBase(
                builderBase() + "/from-template/" + templateId + "/dynamic",
                body != null ? body : new HashMap<>(),
                ChatFlow.class
            );
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> previewPrompt(String templateId, Map<String, Object> body) {
            return httpClient.postWithCustomBase(
                builderBase() + "/from-template/" + templateId + "/preview-prompt",
                body != null ? body : new HashMap<>(),
                Map.class
            );
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> regeneratePrompt(String chatFlowId, Map<String, Object> body) {
            return httpClient.postWithCustomBase(
                builderBase() + "/" + chatFlowId + "/regenerate-prompt",
                body != null ? body : new HashMap<>(),
                Map.class
            );
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> preview(Map<String, Object> draft) {
            return httpClient.postWithCustomBase(builderBase() + "/preview", draft, Map.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> test(String chatFlowId, Map<String, Object> input) {
            return test(chatFlowId, input, null);
        }
        @SuppressWarnings("unchecked")
        public Map<String, Object> test(String chatFlowId, Map<String, Object> input, CallSite callsite) {
            return httpClient.apiRequest("POST",
                builderBase() + "/" + routeSegment(chatFlowId) + "/test",
                input != null ? input : new HashMap<>(),
                Map.class, client.getCallsiteResolver().resolve(callsite)
            );
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> export(String chatFlowId) {
            return httpClient.getWithCustomBase(builderBase() + "/" + chatFlowId + "/export", Map.class);
        }

        public ChatFlow importJson(Map<String, Object> definition) {
            return httpClient.postWithCustomBase(builderBase() + "/import", definition, ChatFlow.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> previewVoice(Map<String, Object> body) {
            return httpClient.postWithCustomBase(builderBase() + "/preview-voice", body, Map.class);
        }
    }

    /**
     * ChatFlow versions sub-resource for a single chatflow.
     */
    public class Versions {
        private final String chatFlowId;
        Versions(String chatFlowId) { this.chatFlowId = chatFlowId; }

        @SuppressWarnings("unchecked")
        public Map<String, Object> list() {
            return httpClient.getWithCustomBase(base() + "/" + chatFlowId + "/versions", Map.class);
        }

        public ChatFlowVersion get(String version) {
            return httpClient.getWithCustomBase(
                base() + "/" + chatFlowId + "/versions/" + version,
                ChatFlowVersion.class
            );
        }

        public ChatFlowVersion create(ChatFlowVersion version) {
            return httpClient.postWithCustomBase(
                base() + "/" + chatFlowId + "/versions",
                version,
                ChatFlowVersion.class
            );
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> promote(String version) {
            return httpClient.postWithCustomBase(
                base() + "/" + chatFlowId + "/versions/" + version + "/promote",
                new HashMap<>(),
                Map.class
            );
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> archive(String version) {
            return httpClient.postWithCustomBase(
                base() + "/" + chatFlowId + "/versions/" + version + "/archive",
                new HashMap<>(),
                Map.class
            );
        }
    }

    /**
     * ChatFlow publishing sub-resource.
     */
    public class Publishing {
        @SuppressWarnings("unchecked")
        public Map<String, Object> publish(String chatFlowId, Map<String, Object> body) {
            return httpClient.postWithCustomBase(
                base() + "/" + chatFlowId + "/publish",
                body != null ? body : new HashMap<>(),
                Map.class
            );
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getPublished(String chatFlowId) {
            return httpClient.getWithCustomBase(base() + "/" + chatFlowId + "/published", Map.class);
        }
    }
}
