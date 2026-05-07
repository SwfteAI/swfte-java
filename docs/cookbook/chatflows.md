# Cookbook: ChatFlows

The `ChatFlows` resource manages conversational forms — onboarding, lead-qualification, support, surveys — with field extraction, branching, validation, and multi-channel delivery (web, WhatsApp, Telegram, voice). Endpoints live under `/v2/chatflows`. See [swfte.com/products/chatflows](https://www.swfte.com/products/chatflows).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.ChatFlow;
import com.swfte.sdk.models.ChatFlowSession;
import com.swfte.sdk.models.ChatFlowVersion;

import java.util.Map;

public class ChatFlowsExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // CREATE
        ChatFlow flow = client.chatflows().create(
            ChatFlow.builder()
                .name("Lead Capture")
                .description("Qualify inbound leads from the website")
                .greeting("Hi! I just need three quick details to get you a quote.")
                .build()
        );

        // GET / LIST
        client.chatflows().get(flow.getId());
        client.chatflows().list(0, 20);

        // UPDATE (PUT)
        client.chatflows().update(flow.getId(),
            ChatFlow.builder()
                .name("Lead Capture (EU)")
                .description("Now collects EU consent")
                .build()
        );

        // VALIDATE + DEPLOY
        client.chatflows().validate(flow.getId());
        client.chatflows().deploy(flow.getId());

        // SESSIONS
        ChatFlowSession session = client.chatflows().startSession(flow.getId(),
            Map.of("channel", "web", "userId", "user-12345"));
        client.chatflows().listSessions(flow.getId());
        client.chatflows().stats(flow.getId());
        client.chatflows().getSession(session.getSessionId());

        // BUILDER discovery
        client.chatflows().builder().fieldTypes();
        client.chatflows().builder().actionTypes();
        client.chatflows().builder().pressStrategies();
        client.chatflows().builder().templates();

        // From template
        ChatFlow fromTpl = client.chatflows().builder().fromTemplate(
            "lead-capture-v3",                                  // replace with your own
            Map.of("name", "EU Lead Capture")
        );

        // Test + export
        client.chatflows().builder().test(fromTpl.getId(),
            Map.of("answers", Map.of("email", "test@example.com")));
        client.chatflows().builder().export(fromTpl.getId());

        // VERSIONS
        ChatFlowVersion v = client.chatflows().versions(flow.getId()).create(
            ChatFlowVersion.builder().version("2").notes("EU consent").build()
        );
        client.chatflows().versions(flow.getId()).list();
        client.chatflows().versions(flow.getId()).promote(v.getVersion());

        // PUBLISHING
        client.chatflows().publishing().publish(flow.getId(), Map.of("visibility", "PUBLIC"));
        client.chatflows().publishing().getPublished(flow.getId());

        // UNDEPLOY + DELETE
        client.chatflows().undeploy(flow.getId());
        client.chatflows().delete(flow.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
