# Cookbook: Marketplace

The `Marketplace` resource lets workspaces browse published modules, install them in one click, and uninstall when no longer needed. Endpoints live under `/v2/marketplace`. See [swfte.com/marketplace](https://www.swfte.com/marketplace).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Installation;
import com.swfte.sdk.models.Publication;

import java.util.Map;

public class MarketplaceExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // BROWSE
        Map<String, Object> publications = client.marketplace().browse();

        // PUBLICATION DETAIL
        String publicationId = "pub-12345";        // replace with your own
        Publication publication = client.marketplace().get(publicationId);
        System.out.println("Title: " + publication.getName());
        System.out.println("Installs: " + publication.getInstallCount());

        // INSTALL into the current workspace
        Installation installation = client.marketplace().install(
            publicationId,
            Map.of(
                "variables", Map.of("region", "EU"),
                "renameAgents", true
            )
        );
        System.out.println("Installed as: " + installation.getId());

        // LIST installations in the current workspace
        client.marketplace().installations();

        // UNINSTALL
        client.marketplace().uninstall(installation.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
