package com.swfte.sdk;

import com.swfte.sdk.models.*;
import com.swfte.sdk.exceptions.SwfteException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Comprehensive Gateway Integration Test - Java SDK
 * Tests all modalities: Chat, Embeddings, Images, TTS, STT
 * Tests all providers: OpenAI, Anthropic, RunPod
 *
 * Run with: java -cp target/classes:target/test-classes com.swfte.sdk.GatewayIntegrationTest
 */
public class GatewayIntegrationTest {

    private static SwfteClient client;

    // RunPod deployment IDs (set via environment variables)
    private static final String QWEN_DEPLOYMENT_ID = System.getenv("QWEN_DEPLOYMENT_ID");
    private static final String TTS_DEPLOYMENT_ID = System.getenv("TTS_DEPLOYMENT_ID");
    private static final String STT_DEPLOYMENT_ID = System.getenv("STT_DEPLOYMENT_ID");
    private static final String COMFYUI_DEPLOYMENT_ID = System.getenv("COMFYUI_DEPLOYMENT_ID");

    private static int passed = 0;
    private static int failed = 0;
    private static int skipped = 0;

    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Comprehensive Gateway Test - Java SDK");

        // Initialize client
        client = SwfteClient.builder()
            .apiKey("test-api-key")
            .baseUrl("http://localhost:3388/v2/gateway")
            .workspaceId("test-workspace")
            .build();

        System.out.println("Base URL: " + client.getBaseUrl());
        System.out.println("Workspace: " + client.getWorkspaceId());
        System.out.println("=".repeat(60));

        // Run all tests
        runTest("OpenAI Chat", GatewayIntegrationTest::testOpenAIChat);
        runTest("Anthropic Chat", GatewayIntegrationTest::testAnthropicChat);
        runTest("Embeddings", GatewayIntegrationTest::testEmbeddings);
        runTest("DALL-E Image", GatewayIntegrationTest::testImageGeneration);
        runTest("OpenAI TTS", GatewayIntegrationTest::testTTS);
        runTest("OpenAI STT", GatewayIntegrationTest::testSTT);
        runTest("Streaming Chat", GatewayIntegrationTest::testStreamingChat);
        runTest("Error Handling", GatewayIntegrationTest::testErrorHandling);

        // RunPod tests
        runTest("Qwen 2.5 Chat", GatewayIntegrationTest::testQwenChat);
        runTest("ComfyUI SDXL", GatewayIntegrationTest::testComfyUIImage);
        runTest("RunPod TTS", GatewayIntegrationTest::testRunPodTTS);
        runTest("RunPod STT", GatewayIntegrationTest::testRunPodSTT);

        // Print results
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Results: " + passed + " passed, " + failed + " failed, " + skipped + " skipped");
        System.out.println("=".repeat(60));

        System.exit(failed > 0 ? 1 : 0);
    }

    @FunctionalInterface
    interface TestFunction {
        Boolean run() throws Exception;
    }

    private static void runTest(String name, TestFunction test) {
        try {
            Boolean result = test.run();
            if (result == null) {
                skipped++;
            } else if (result) {
                passed++;
            } else {
                failed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED: " + name + ": " + e.getMessage());
            failed++;
        }
    }

    // ==================== PROPRIETARY PROVIDER TESTS ====================

    private static Boolean testOpenAIChat() {
        System.out.println("\n=== Test: OpenAI Chat ===");

        ChatRequest request = ChatRequest.builder()
            .model("gpt-4o-mini")
            .addMessage("user", "Say 'Hello from OpenAI' in exactly 3 words")
            .maxTokens(20)
            .temperature(0.0)
            .build();

        ChatResponse response = client.chat().completions().create(request);

        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println("Response: " + content);

        if (content == null || content.isEmpty()) {
            throw new AssertionError("No content in response");
        }

        System.out.println("PASSED: OpenAI Chat");
        return true;
    }

    private static Boolean testAnthropicChat() {
        System.out.println("\n=== Test: Anthropic Chat ===");

        ChatRequest request = ChatRequest.builder()
            .model("claude-3-haiku-20240307")
            .addMessage("user", "Say 'Hello from Claude' in exactly 3 words")
            .maxTokens(20)
            .temperature(0.0)
            .build();

        ChatResponse response = client.chat().completions().create(request);

        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println("Response: " + content);

        if (content == null || content.isEmpty()) {
            throw new AssertionError("No content in response");
        }

        System.out.println("PASSED: Anthropic Chat");
        return true;
    }

    private static Boolean testEmbeddings() {
        System.out.println("\n=== Test: Embeddings ===");

        EmbeddingRequest request = EmbeddingRequest.builder()
            .model("text-embedding-3-small")
            .input("Hello world")
            .build();

        EmbeddingResponse response = client.embeddings().create(request);

        int dims = response.getData().get(0).getEmbedding().size();
        System.out.println("Embedding dimensions: " + dims);

        if (dims != 1536) {
            throw new AssertionError("Expected 1536 dimensions, got " + dims);
        }

        System.out.println("PASSED: Embeddings");
        return true;
    }

    private static Boolean testImageGeneration() {
        System.out.println("\n=== Test: DALL-E Image Generation ===");

        ImageRequest request = ImageRequest.builder()
            .model("dall-e-3")
            .prompt("A simple blue square on white background")
            .size("1024x1024")
            .n(1)
            .build();

        ImageResponse response = client.images().generate(request);

        String url = response.getData().get(0).getUrl();
        System.out.println("Image URL: " + (url != null ? url.substring(0, Math.min(80, url.length())) + "..." : "null"));

        if (url == null || !url.startsWith("http")) {
            throw new AssertionError("Invalid URL");
        }

        System.out.println("PASSED: Image Generation");
        return true;
    }

    private static Boolean testTTS() throws IOException {
        System.out.println("\n=== Test: OpenAI Text-to-Speech ===");

        byte[] audioData = client.audio().speech().create(
            "tts-1",
            "Hello, this is a test of OpenAI text to speech.",
            "alloy"
        );

        System.out.println("Audio size: " + audioData.length + " bytes");

        if (audioData.length == 0) {
            throw new AssertionError("No audio data");
        }

        // Save to file for STT test
        try (FileOutputStream fos = new FileOutputStream("/tmp/test_speech_java.mp3")) {
            fos.write(audioData);
        }

        System.out.println("PASSED: OpenAI TTS (saved to /tmp/test_speech_java.mp3)");
        return true;
    }

    private static Boolean testSTT() throws IOException {
        System.out.println("\n=== Test: OpenAI Speech-to-Text ===");

        // Read audio from TTS test
        byte[] audioData = Files.readAllBytes(Path.of("/tmp/test_speech_java.mp3"));

        TranscriptionResponse response = client.audio().transcriptions().create(
            "whisper-1",
            audioData
        );

        String text = response.getText();
        System.out.println("Transcript: " + text);

        if (text == null || text.isEmpty()) {
            throw new AssertionError("No transcript");
        }

        System.out.println("PASSED: OpenAI STT");
        return true;
    }

    private static Boolean testStreamingChat() {
        System.out.println("\n=== Test: Streaming Chat ===");

        ChatRequest request = ChatRequest.builder()
            .model("gpt-4o-mini")
            .addMessage("user", "Count from 1 to 5, one number per line")
            .maxTokens(50)
            .stream(true)
            .build();

        StringBuilder content = new StringBuilder();
        System.out.print("Streaming: ");

        Stream<ChatChunk> stream = client.chat().completions().createStream(request);
        stream.forEach(chunk -> {
            if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                var choice = chunk.getChoices().get(0);
                // Handle both delta (streaming) and message (wrapped) response formats
                String text = null;
                if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                    text = choice.getDelta().getContent();
                } else if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                    text = choice.getMessage().getContent();
                }
                if (text != null) {
                    content.append(text);
                    System.out.print(text);
                }
            }
        });

        System.out.println();

        if (content.length() == 0) {
            throw new AssertionError("No streamed content");
        }

        System.out.println("PASSED: Streaming Chat");
        return true;
    }

    private static Boolean testErrorHandling() {
        System.out.println("\n=== Test: Error Handling ===");

        try {
            ChatRequest request = ChatRequest.builder()
                .model("invalid-model-xyz-12345")
                .addMessage("user", "Hello")
                .build();

            client.chat().completions().create(request);
            System.out.println("FAILED: Should have thrown");
            return false;
        } catch (SwfteException e) {
            System.out.println("Got expected SwfteException: " + e.getClass().getSimpleName());
            System.out.println("PASSED: Error Handling");
            return true;
        } catch (Exception e) {
            System.out.println("Got exception (acceptable): " + e.getClass().getSimpleName());
            System.out.println("PASSED: Error Handling");
            return true;
        }
    }

    // ==================== RUNPOD TESTS ====================

    private static Boolean testQwenChat() {
        if (QWEN_DEPLOYMENT_ID == null || QWEN_DEPLOYMENT_ID.isEmpty()) {
            System.out.println("\n=== Test: Qwen 2.5 Chat (RunPod) - SKIPPED (no deployment ID) ===");
            return null;
        }

        System.out.println("\n=== Test: Qwen 2.5 Chat (RunPod) ===");

        ChatRequest request = ChatRequest.builder()
            .model("deployed:" + QWEN_DEPLOYMENT_ID)
            .addMessage("user", "Explain machine learning in 2 sentences")
            .maxTokens(100)
            .temperature(0.7)
            .build();

        ChatResponse response = client.chat().completions().create(request);

        String content = response.getChoices().get(0).getMessage().getContent();
        System.out.println("Response: " + content);

        if (content == null) {
            throw new AssertionError("No content");
        }

        System.out.println("PASSED: Qwen 2.5 Chat");
        return true;
    }

    private static Boolean testComfyUIImage() {
        if (COMFYUI_DEPLOYMENT_ID == null || COMFYUI_DEPLOYMENT_ID.isEmpty()) {
            System.out.println("\n=== Test: ComfyUI SDXL Image (RunPod) - SKIPPED (no deployment ID) ===");
            return null;
        }

        System.out.println("\n=== Test: ComfyUI SDXL Image (RunPod) ===");

        ImageRequest request = ImageRequest.builder()
            .model("comfy:sdxl")
            .prompt("A futuristic city skyline at sunset, cyberpunk style")
            .size("1024x1024")
            .n(1)
            .build();

        ImageResponse response = client.images().generate(request);

        String url = response.getData().get(0).getUrl();
        System.out.println("Image URL: " + (url != null ? url.substring(0, Math.min(80, url.length())) + "..." : "null"));

        if (url == null) {
            throw new AssertionError("No URL");
        }

        System.out.println("PASSED: ComfyUI SDXL");
        return true;
    }

    private static Boolean testRunPodTTS() throws IOException {
        if (TTS_DEPLOYMENT_ID == null || TTS_DEPLOYMENT_ID.isEmpty()) {
            System.out.println("\n=== Test: RunPod TTS - SKIPPED (no deployment ID) ===");
            return null;
        }

        System.out.println("\n=== Test: RunPod TTS (XTTS-v2) ===");

        byte[] audioData = client.audio().speech().create(
            "deployed:" + TTS_DEPLOYMENT_ID,
            "Hello from self-hosted text to speech on RunPod.",
            "alloy"
        );

        System.out.println("Audio size: " + audioData.length + " bytes");

        try (FileOutputStream fos = new FileOutputStream("/tmp/test_java_runpod_speech.mp3")) {
            fos.write(audioData);
        }

        if (audioData.length == 0) {
            throw new AssertionError("No audio");
        }

        System.out.println("PASSED: RunPod TTS");
        return true;
    }

    private static Boolean testRunPodSTT() throws IOException {
        if (STT_DEPLOYMENT_ID == null || STT_DEPLOYMENT_ID.isEmpty()) {
            System.out.println("\n=== Test: RunPod STT - SKIPPED (no deployment ID) ===");
            return null;
        }

        System.out.println("\n=== Test: RunPod STT (Whisper) ===");

        // Use RunPod TTS audio if available, otherwise use OpenAI TTS audio
        Path audioPath = Path.of("/tmp/test_java_runpod_speech.mp3");
        if (!Files.exists(audioPath)) {
            audioPath = Path.of("/tmp/test_speech_java.mp3");
        }
        byte[] audioData = Files.readAllBytes(audioPath);

        TranscriptionResponse response = client.audio().transcriptions().create(
            "deployed:" + STT_DEPLOYMENT_ID,
            audioData
        );

        String text = response.getText();
        System.out.println("Transcript: " + text);

        if (text == null) {
            throw new AssertionError("No transcript");
        }

        System.out.println("PASSED: RunPod STT");
        return true;
    }
}
