import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.ChatRequest;
import com.swfte.sdk.models.Message;

import java.util.List;

/**
 * Can the Java SDK reach UnifiedGatewayControllerV2 on its shipped default?
 *
 * Same bar as the Python and Node checks: a 4xx/5xx from business logic counts
 * as REACHED — the request arrived at the controller. Only a 404, a connection
 * failure, or a missing SDK method mean it cannot get there.
 *
 * Built deliberately without a base-URL override, because the default is the
 * thing that was broken.
 */
public class GatewayConformance {

    static int reached = 0;
    static int unreachable = 0;

    static void probe(String endpoint, Runnable call) {
        String verdict;
        String detail;
        try {
            call.run();
            verdict = "REACHED";
            detail = "200";
        } catch (Exception e) {
            String text = String.valueOf(e.getMessage());
            if (text.contains("404") || text.contains("Not Found")) {
                verdict = "NOT_FOUND";
                detail = text;
            } else if (text.matches("(?s).*\\b(400|402|403|429|500|503)\\b.*")) {
                verdict = "REACHED";
                detail = text;
            } else {
                verdict = "UNREACHABLE";
                detail = e.getClass().getSimpleName() + ": " + text;
            }
        }
        if ("REACHED".equals(verdict)) reached++; else unreachable++;
        if (detail != null && detail.length() > 70) detail = detail.substring(0, 70);
        System.out.printf("  %-14s %-34s %s%n", verdict, endpoint, detail);
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("SWFTE_API_KEY");
        String workspace = System.getenv().getOrDefault("SWFTE_WORKSPACE_ID", "316");
        String model = System.getenv().getOrDefault("SWFTE_MODEL", "openai:gpt-4o-mini");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("FATAL: SWFTE_API_KEY required");
            System.exit(2);
        }

        // No .baseUrl(...) — the shipped default is under test.
        SwfteClient client = SwfteClient.builder()
                .apiKey(apiKey)
                .workspaceId(workspace)
                .build();

        System.out.println("baseUrl (shipped default): built from SwfteClient.Builder\n");

        probe("POST /chat/completions", () -> client.chat().completions().create(
                ChatRequest.builder()
                        .model(model)
                        .messages(List.of(Message.user("hi")))
                        .build()));

        probe("GET /models", () -> client.models().list());

        System.out.printf("%nreached: %d  ·  cannot reach: %d%n", reached, unreachable);
        System.exit(unreachable > 0 ? 1 : 0);
    }
}
