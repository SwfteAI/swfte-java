# Cookbook: Files

The `Files` resource is the workspace's file store, used by datasets, voice playback, agent attachments, and chatflows. Endpoints live under `/api/v2/files`.

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.FileMetadata;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FilesExample {
    public static void main(String[] args) throws Exception {
        SwfteClient client = SwfteClient.builder()
            .apiKey(System.getenv("SWFTE_API_KEY"))
            .workspaceId("ws-12345")              // replace with your own
            .build();

        // CONFIG — discover allowed mime types and size limits
        client.files().config();

        // UPLOAD a single file
        byte[] bytes = Files.readAllBytes(Path.of("invoice.pdf"));
        FileMetadata uploaded = client.files().upload("invoice.pdf", "application/pdf", bytes);
        System.out.println("Uploaded as " + uploaded.getId());

        // LIST / GET
        client.files().list(0, 50);
        client.files().get(uploaded.getId());

        // DOWNLOAD / PREVIEW
        client.files().download(uploaded.getId());
        client.files().preview(uploaded.getId());

        // UPDATE USAGE — record where this file is referenced
        client.files().updateUsage(uploaded.getId(), Map.of(
            "datasetId", "dataset-12345",        // replace with your own
            "purpose", "knowledge"
        ));

        // CLEANUP orphans (admin)
        client.files().cleanup();

        // DELETE
        client.files().delete(uploaded.getId());
    }
}
```

Full reference at [swfte.com/developers](https://www.swfte.com/developers).
