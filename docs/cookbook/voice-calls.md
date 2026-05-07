# Cookbook: Voice Calls

The `VoiceCalls` resource lists and inspects voice calls handled by Swfte's voice runtime (Twilio + WebRTC). Endpoints live under `/v2/voice/calls`. See [swfte.com/products/voice](https://www.swfte.com/products/voice).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.VoiceCall;

import java.util.Map;

public class VoiceCallsExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // LIST
        client.voiceCalls().list();
        client.voiceCalls().list(0, 100);

        // IN-PROGRESS — currently active calls
        Map<String, Object> active = client.voiceCalls().inProgress();
        System.out.println("Active calls: " + active.get("count"));

        // GET single call
        String callSid = "CAabcdef1234567890";    // replace with your own
        VoiceCall call = client.voiceCalls().get(callSid);
        System.out.println("Direction: " + call.getDirection());
        System.out.println("From: " + call.getFromNumber() + " -> " + call.getToNumber());
        System.out.println("Duration: " + call.getDurationSeconds() + "s");

        // TRANSCRIPT and RECORDING
        Map<String, Object> transcript = client.voiceCalls().transcript(callSid);
        Map<String, Object> recording  = client.voiceCalls().recording(callSid);
        System.out.println("Recording URL: " + recording.get("url"));

        // AUDIT envelope (consent, retention, jurisdiction)
        client.voiceCalls().audit(callSid);

        // ALL CALLS for a chatflow
        client.voiceCalls().forChatFlow("chatflow-12345"); // replace with your own
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
