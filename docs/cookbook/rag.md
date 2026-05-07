# Cookbook: RAG (Advanced)

The `Rag` resource exposes the platform's hybrid retrieval surface — vector + lexical search, rerankers, embedding model catalogue, BM25 vocabulary build. Endpoints live under `/v2/rag`. See [swfte.com/products/rag](https://www.swfte.com/products/rag).

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.RagSearchRequest;
import com.swfte.sdk.models.RagSearchResponse;

import java.util.List;
import java.util.Map;

public class RagExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // Discover available models and strategies
        client.rag().embeddingModels();
        client.rag().rerankerModels();
        client.rag().strategies();
        client.rag().config();

        // BM25 vocabulary
        client.rag().buildVocabulary(Map.of("datasetIds", List.of("dataset-12345"))); // replace with your own
        client.rag().vocabularyStats();

        // HYBRID SEARCH
        RagSearchResponse response = client.rag().search(
            RagSearchRequest.builder()
                .query("How do refunds work for annual plans?")
                .datasetIds(List.of("dataset-12345"))
                .topK(8)
                .strategy("hybrid")
                .rerank(true)
                .rerankerModel("voyage:rerank-2")
                .scoreThreshold(0.4)
                .includeMetadata(true)
                .filters(Map.of("language", "en"))
                .build()
        );

        response.getResults().forEach(r ->
            System.out.printf("%.3f  %s%n", r.getScore(), r.getContent())
        );

        // RERANK an arbitrary list (e.g. results from your own retriever)
        client.rag().rerank(
            "How do refunds work?",
            List.of(
                Map.of("id", "doc-1", "content", "Refund policy: 30 days."),
                Map.of("id", "doc-2", "content", "Annual plans pro-rate refunds.")
            ),
            "voyage:rerank-2"
        );
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
