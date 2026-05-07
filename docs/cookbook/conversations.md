# Cookbook: Conversations

The `Conversations` resource initiates and inspects voice/messaging conversations driven by an agent or chatflow — recording, transcript, scheduled retries. Endpoints live under `/v2/conversations`.

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Conversation;
import com.swfte.sdk.models.ConversationMessage;
import com.swfte.sdk.models.MessagePage;

public class ConversationsExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // INITIATE
        Conversation conversation = client.conversations().create(
            Conversation.builder().title("Support Chat").build()
        );

        // ADD a message
        client.conversations().addMessage(conversation.getId(),
            ConversationMessage.builder().role("user").content("Hello!").build()
        );

        // FETCH messages
        MessagePage page = client.conversations().getMessages(conversation.getId());
        page.getMessages().forEach(m -> System.out.println(m.getRole() + ": " + m.getContent()));

        // For voice or scheduled conversations, the V2 controller exposes:
        //   /v2/conversations/initiate
        //   /v2/conversations/{id}
        //   /v2/conversations/{id}/transcript
        //   /v2/conversations/{id}/recording
        //   /v2/conversations/{id}/terminate
        //   /v2/conversations/{id}/cancel-retries
        //   /v2/conversations/scheduled-retries
        //
        // These are reachable via SwfteClient's HTTP client; full integration
        // ships in the next minor release.
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
