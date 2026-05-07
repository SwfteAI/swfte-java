package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.VoiceCall;

import java.util.Map;

/**
 * Voice Calls API resource — list and inspect voice calls handled by the
 * Swfte voice runtime (Twilio + WebRTC).
 *
 * <p>See <a href="https://www.swfte.com/products/voice">swfte.com/products/voice</a>.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Map<String, Object> calls = client.voiceCalls().list();
 * VoiceCall call = client.voiceCalls().get("CA1234567890abcdef");
 * Map<String, Object> transcript = client.voiceCalls().transcript(call.getCallSid());
 * }</pre>
 */
public class VoiceCalls {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public VoiceCalls(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    private String base() { return "/v2/voice/calls"; }

    /** List voice calls. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> list() { return list(0, 20); }

    @SuppressWarnings("unchecked")
    public Map<String, Object> list(int page, int size) {
        return httpClient.getWithCustomBase(base() + "?page=" + page + "&size=" + size, Map.class);
    }

    /** List in-progress voice calls. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> inProgress() {
        return httpClient.getWithCustomBase(base() + "/in-progress", Map.class);
    }

    /** Get a single call. */
    public VoiceCall get(String callSid) {
        return httpClient.getWithCustomBase(base() + "/" + callSid, VoiceCall.class);
    }

    /** Get the transcript for a call. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> transcript(String callSid) {
        return httpClient.getWithCustomBase(base() + "/" + callSid + "/transcript", Map.class);
    }

    /** Get the recording metadata for a call. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> recording(String callSid) {
        return httpClient.getWithCustomBase(base() + "/" + callSid + "/recording", Map.class);
    }

    /** Get the audit envelope for a call (consent, retention, jurisdiction). */
    @SuppressWarnings("unchecked")
    public Map<String, Object> audit(String callSid) {
        return httpClient.getWithCustomBase(base() + "/" + callSid + "/audit", Map.class);
    }

    /** List calls associated with a chatflow. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> forChatFlow(String chatFlowId) {
        return httpClient.getWithCustomBase("/v2/chatflows/" + chatFlowId + "/calls", Map.class);
    }
}
