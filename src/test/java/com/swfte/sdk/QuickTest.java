package com.swfte.sdk;

import com.swfte.sdk.models.*;

import java.util.List;

/**
 * Quick test of SDK against local service on port 1111
 */
public class QuickTest {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("SDK Quick Test - Port 1111");
        System.out.println("=".repeat(60));

        // Initialize client pointing to local service
        SwfteClient client = SwfteClient.builder()
            .apiKey("test-api-key")
            .baseUrl("http://localhost:1111/v2/gateway")
            .workspaceId("test-workspace")
            .build();

        int passed = 0;
        int failed = 0;

        // Test 1: Chat completion
        try {
            System.out.println("\n[Test 1] Chat Completion (OpenAI)");
            ChatRequest request = ChatRequest.builder()
                .model("gpt-4o-mini")
                .addMessage("user", "Say 'SDK works!' in 2 words")
                .maxTokens(10)
                .temperature(0.0)
                .build();

            ChatResponse response = client.chat().completions().create(request);
            String content = response.getChoices().get(0).getMessage().getContent();
            System.out.println("Response: " + content);
            System.out.println("Model: " + response.getModel());
            System.out.println("Tokens: " + response.getUsage().getTotalTokens());
            System.out.println("PASSED");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // Test 2: Embeddings
        try {
            System.out.println("\n[Test 2] Embeddings");
            EmbeddingRequest request = EmbeddingRequest.builder()
                .model("text-embedding-3-small")
                .input("Hello from SDK")
                .build();

            EmbeddingResponse response = client.embeddings().create(request);
            int dims = response.getData().get(0).getEmbedding().size();
            System.out.println("Embedding dimensions: " + dims);
            System.out.println("PASSED");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // Test 3: Models list
        try {
            System.out.println("\n[Test 3] List Models");
            List<Model> models = client.models().list();
            System.out.println("Total models: " + models.size());
            if (!models.isEmpty()) {
                System.out.println("First model: " + models.get(0).getId());
            }
            System.out.println("PASSED");
            passed++;
        } catch (Exception e) {
            System.out.println("FAILED: " + e.getMessage());
            e.printStackTrace();
            failed++;
        }

        // Summary
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        System.out.println("=".repeat(60));

        System.exit(failed > 0 ? 1 : 0);
    }
}