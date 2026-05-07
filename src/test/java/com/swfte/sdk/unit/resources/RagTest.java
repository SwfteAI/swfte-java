package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.RagSearchRequest;
import com.swfte.sdk.models.RagSearchResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RagTest {

    private TestServer server;
    private SwfteClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new TestServer("{\"results\":[],\"strategy\":\"hybrid\"}");
        client = SwfteClient.builder()
            .apiKey("sk-swfte-test")
            .workspaceId("ws-test")
            .baseUrl(server.baseUrl())
            .maxRetries(1)
            .build();
    }

    @AfterEach
    void tearDown() { server.close(); }

    @Test
    void shouldExposeRagViaClient() {
        assertNotNull(client.rag());
    }

    @Test
    void searchPostsToSearchPath() {
        RagSearchResponse response = client.rag().search(
            RagSearchRequest.builder()
                .query("How do refunds work?")
                .datasetIds(List.of("dataset-12345"))
                .topK(8)
                .rerank(true)
                .build()
        );
        assertEquals("POST", server.last().method);
        assertEquals("/v2/rag/search", server.last().path);
        assertEquals("hybrid", response.getStrategy());
    }

    @Test
    void embeddingModelsGetsModelsPath() {
        client.rag().embeddingModels();
        assertEquals("GET", server.last().method);
        assertEquals("/v2/rag/models/embeddings", server.last().path);
    }

    @Test
    void buildVocabularyPostsToBuildPath() {
        client.rag().buildVocabulary(null);
        assertEquals("POST", server.last().method);
        assertEquals("/v2/rag/vocabulary/build", server.last().path);
    }
}
