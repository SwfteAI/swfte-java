# Cookbook: Documents

The `Documents` resource manages individual documents inside a dataset — create, list, segment, retry processing, pause/resume. Endpoints live under `/api/v2/datasets/{id}/documents`.

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.Document;

import java.util.List;
import java.util.Map;

public class DocumentsExample {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        String datasetId = "dataset-12345";       // replace with your own

        // CREATE — text or URL or file-id
        Map<String, Object> created = client.documents().create(datasetId, List.of(
            Document.builder()
                .name("Pricing FAQ")
                .dataSourceType("TEXT")
                .content("Our pricing is per-token plus per-second compute.")
                .build(),
            Document.builder()
                .name("API Reference")
                .dataSourceType("URL")
                .url("https://www.swfte.com/developers")
                .build()
        ));

        // LIST + processing status
        client.documents().list(datasetId, 0, 50);
        client.documents().processingStatus(datasetId);

        // GET / UPDATE individual
        String docId = "doc-12345";               // replace with your own
        client.documents().get(datasetId, docId);
        client.documents().update(datasetId, docId,
            Document.builder().enabled(true).build()
        );

        // SEGMENTS — chunked content with vectors
        client.documents().segments(datasetId, docId);

        // PAUSE / RESUME / RETRY
        client.documents().pause(datasetId, docId);
        client.documents().resume(datasetId, docId);
        client.documents().retry(datasetId, docId);

        // BATCH
        client.documents().batchUpdate(datasetId, Map.of(
            "documentIds", List.of(docId),
            "enabled", false
        ));
        client.documents().batchStatus(datasetId, "batch-12345"); // replace with your own

        // DELETE
        client.documents().delete(datasetId, docId);
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
