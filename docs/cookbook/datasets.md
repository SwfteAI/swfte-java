# Cookbook: Datasets

The `Datasets` resource manages RAG knowledge bases. Endpoints live under `/api/v2/datasets`. See [swfte.com/products/rag](https://www.swfte.com/products/rag).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Dataset;

public class DatasetsExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // CREATE
        Dataset dataset = client.datasets().create(
            Dataset.builder()
                .name("Product Docs")
                .description("Public product documentation, refreshed nightly")
                .embeddingModel("openai:text-embedding-3-small")
                .indexingStrategy("hybrid")
                .build()
        );
        System.out.println("Created dataset: " + dataset.getId());

        // GET / LIST
        client.datasets().get(dataset.getId());
        client.datasets().list(0, 50);

        // UPDATE
        client.datasets().update(dataset.getId(),
            Dataset.builder().description("Now nightly + on-publish").build()
        );

        // CHECK USAGE — which agents/chatflows reference this dataset
        client.datasets().useCheck(dataset.getId());

        // ENABLE PUBLIC API ACCESS
        client.datasets().toggleApiAccess(dataset.getId(), "ENABLED");

        // DELETE
        client.datasets().delete(dataset.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
