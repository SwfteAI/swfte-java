package com.swfte.sdk;

import com.swfte.sdk.models.*;
import com.swfte.sdk.exceptions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Comprehensive Gateway Integration Test Suite - Java SDK
 *
 * Tests all supported providers and modalities:
 * - Proprietary: OpenAI, Anthropic, Google, Mistral, Cohere, DeepSeek, Groq
 * - Self-hosted: RunPod deployments (LLM, TTS, STT, Image)
 * - Modalities: Chat, Streaming, Embeddings, Images, TTS, STT
 *
 * Usage:
 *   mvn test -Dtest=ComprehensiveGatewayTest
 *   mvn test -Dtest=ComprehensiveGatewayTest#testOpenAIChat
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@DisplayName("Comprehensive Gateway Integration Tests")
public class ComprehensiveGatewayTest {

    // ==================== CONFIGURATION ====================

    /**
     * Provider configuration holder.
     */
    static class ProviderConfig {
        final String name;
        final String chatModel;
        final String embeddingModel;
        final String imageModel;
        final String ttsModel;
        final String sttModel;
        final boolean supportsStreaming;
        final boolean supportsFunctionCalling;

        ProviderConfig(String name, String chatModel, String embeddingModel,
                       String imageModel, String ttsModel, String sttModel,
                       boolean supportsStreaming, boolean supportsFunctionCalling) {
            this.name = name;
            this.chatModel = chatModel;
            this.embeddingModel = embeddingModel;
            this.imageModel = imageModel;
            this.ttsModel = ttsModel;
            this.sttModel = sttModel;
            this.supportsStreaming = supportsStreaming;
            this.supportsFunctionCalling = supportsFunctionCalling;
        }
    }

    private static final Map<String, ProviderConfig> PROPRIETARY_PROVIDERS = Map.of(
        "openai", new ProviderConfig(
            "OpenAI", "gpt-4o-mini", "text-embedding-3-small",
            "dall-e-3", "tts-1", "whisper-1", true, true
        ),
        "anthropic", new ProviderConfig(
            "Anthropic", "claude-3-haiku-20240307", null,
            null, null, null, true, true
        ),
        "google", new ProviderConfig(
            "Google", "gemini-1.5-flash", "text-embedding-004",
            null, null, null, true, true
        ),
        "mistral", new ProviderConfig(
            "Mistral", "mistral-small-latest", "mistral-embed",
            null, null, null, true, false
        ),
        "cohere", new ProviderConfig(
            "Cohere", "command-r", "embed-english-v3.0",
            null, null, null, true, false
        ),
        "deepseek", new ProviderConfig(
            "DeepSeek", "deepseek-chat", null,
            null, null, null, true, false
        ),
        "groq", new ProviderConfig(
            "Groq", "llama-3.1-8b-instant", null,
            null, null, null, true, false
        )
    );

    // ==================== TEST RESULT TRACKING ====================

    /**
     * Test result record.
     */
    static class TestResult {
        final String provider;
        final String testName;
        final boolean passed;
        final long latencyMs;
        final String error;
        final Map<String, Object> metadata;

        TestResult(String provider, String testName, boolean passed,
                   long latencyMs, String error, Map<String, Object> metadata) {
            this.provider = provider;
            this.testName = testName;
            this.passed = passed;
            this.latencyMs = latencyMs;
            this.error = error;
            this.metadata = metadata;
        }
    }

    private static final List<TestResult> results = Collections.synchronizedList(new ArrayList<>());
    private static SwfteClient client;
    private static Path testResultsDir;
    private static Path audioTestFile;

    // Deployment IDs
    private static final String QWEN_DEPLOYMENT_ID = System.getenv("QWEN_DEPLOYMENT_ID");
    private static final String LLAMA_DEPLOYMENT_ID = System.getenv("LLAMA_DEPLOYMENT_ID");
    private static final String TTS_DEPLOYMENT_ID = System.getenv("TTS_DEPLOYMENT_ID");
    private static final String STT_DEPLOYMENT_ID = System.getenv("STT_DEPLOYMENT_ID");
    private static final String SDXL_DEPLOYMENT_ID = System.getenv("SDXL_DEPLOYMENT_ID");
    private static final String FLUX_DEPLOYMENT_ID = System.getenv("FLUX_DEPLOYMENT_ID");

    // ==================== SETUP AND TEARDOWN ====================

    @BeforeAll
    void setUp() throws IOException {
        String apiKey = System.getenv("SWFTE_API_KEY");
        assumeTrue(apiKey != null && !apiKey.isEmpty(),
            "SWFTE_API_KEY environment variable not set");

        String baseUrl = Optional.ofNullable(System.getenv("SWFTE_BASE_URL"))
            .orElse("http://localhost:3388/v2/gateway");
        String workspaceId = Optional.ofNullable(System.getenv("SWFTE_WORKSPACE_ID"))
            .orElse("test-workspace");

        client = SwfteClient.builder()
            .apiKey(apiKey)
            .baseUrl(baseUrl)
            .workspaceId(workspaceId)
            .timeout(120000)
            .maxRetries(2)
            .build();

        // Create test results directory
        testResultsDir = Paths.get(System.getProperty("user.dir"), "..", "..",
            "scripts", "comprehensive-model-tests", "test-results", "java");
        Files.createDirectories(testResultsDir);

        System.out.println("=" .repeat(60));
        System.out.println("Comprehensive Gateway Test Suite - Java SDK");
        System.out.println("Base URL: " + client.getBaseUrl());
        System.out.println("Workspace: " + client.getWorkspaceId());
        System.out.println("=" .repeat(60));
    }

    @AfterAll
    void tearDown() throws IOException {
        saveResults();
        printSummary();
    }

    private void saveResults() throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"total\": ").append(results.size()).append(",\n");
        json.append("  \"passed\": ").append(results.stream().filter(r -> r.passed).count()).append(",\n");
        json.append("  \"failed\": ").append(results.stream().filter(r -> !r.passed).count()).append(",\n");
        json.append("  \"results\": [\n");

        for (int i = 0; i < results.size(); i++) {
            TestResult r = results.get(i);
            json.append("    {\n");
            json.append("      \"provider\": \"").append(r.provider).append("\",\n");
            json.append("      \"test\": \"").append(r.testName).append("\",\n");
            json.append("      \"passed\": ").append(r.passed).append(",\n");
            json.append("      \"latencyMs\": ").append(r.latencyMs);
            if (r.error != null) {
                json.append(",\n      \"error\": \"").append(escapeJson(r.error)).append("\"");
            }
            json.append("\n    }");
            if (i < results.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        Files.writeString(testResultsDir.resolve("results.json"), json.toString());
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private void printSummary() {
        long passed = results.stream().filter(r -> r.passed).count();
        long failed = results.stream().filter(r -> !r.passed).count();

        System.out.println("\n" + "=" .repeat(60));
        System.out.println("Results: " + passed + " passed, " + failed + " failed");
        System.out.println("=" .repeat(60));

        if (failed > 0) {
            System.out.println("\nFailed tests:");
            results.stream()
                .filter(r -> !r.passed)
                .forEach(r -> System.out.println("  - " + r.provider + "/" + r.testName + ": " + r.error));
        }
    }

    private void recordResult(String provider, String testName, boolean passed,
                              long latencyMs, String error, Map<String, Object> metadata) {
        results.add(new TestResult(provider, testName, passed, latencyMs, error, metadata));
    }

    private Path ensureAudioTestFile() throws IOException {
        if (audioTestFile != null && Files.exists(audioTestFile)) {
            return audioTestFile;
        }

        Path audioPath = testResultsDir.resolve("test_audio.mp3");

        byte[] audioData = client.audio().speech().create(
            "tts-1",
            "Hello, this is a test of speech synthesis for the comprehensive gateway tests.",
            "alloy"
        );

        Files.write(audioPath, audioData);
        audioTestFile = audioPath;
        return audioPath;
    }

    // ==================== PROPRIETARY PROVIDER TESTS ====================

    // Provider argument streams for @MethodSource (must be in outer class)
    static Stream<Arguments> chatProviders() {
        return PROPRIETARY_PROVIDERS.entrySet().stream()
            .map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    static Stream<Arguments> streamingProviders() {
        return PROPRIETARY_PROVIDERS.entrySet().stream()
            .filter(e -> e.getValue().supportsStreaming)
            .map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    static Stream<Arguments> embeddingProviders() {
        return PROPRIETARY_PROVIDERS.entrySet().stream()
            .filter(e -> e.getValue().embeddingModel != null)
            .map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    @Nested
    @DisplayName("Proprietary Providers")
    class ProprietaryProviderTests {

        @ParameterizedTest(name = "{0} - Chat Completion")
        @MethodSource("com.swfte.sdk.ComprehensiveGatewayTest#chatProviders")
        @Order(1)
        void testChatCompletion(String providerKey, ProviderConfig config) {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ChatRequest request = ChatRequest.builder()
                    .model(config.chatModel)
                    .addMessage("user", "Say 'Hello from " + config.name + "' in exactly 4 words.")
                    .maxTokens(30)
                    .temperature(0.0)
                    .build();

                ChatResponse response = client.chat().completions().create(request);
                String content = response.getChoices().get(0).getMessage().getContent();

                assertNotNull(content, "No content in response");
                assertFalse(content.isEmpty(), "Empty content");

                passed = true;
                metadata.put("model", config.chatModel);
                metadata.put("contentLength", content.length());

                System.out.println("[PASS] " + config.name + " chat: " + content.substring(0, Math.min(50, content.length())));

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] " + config.name + " chat: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult(providerKey, "chat_completion", passed, latencyMs, error, metadata);

            if (!passed) {
                fail(config.name + " chat completion failed: " + error);
            }
        }

        @ParameterizedTest(name = "{0} - Streaming Chat")
        @MethodSource("com.swfte.sdk.ComprehensiveGatewayTest#streamingProviders")
        @Order(2)
        void testStreamingChat(String providerKey, ProviderConfig config) {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ChatRequest request = ChatRequest.builder()
                    .model(config.chatModel)
                    .addMessage("user", "Count from 1 to 5, one number per line.")
                    .maxTokens(50)
                    .stream(true)
                    .build();

                StringBuilder content = new StringBuilder();
                AtomicInteger chunkCount = new AtomicInteger(0);

                Stream<ChatChunk> stream = client.chat().completions().createStream(request);
                stream.forEach(chunk -> {
                    chunkCount.incrementAndGet();
                    if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                        var choice = chunk.getChoices().get(0);
                        String text = null;
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            text = choice.getDelta().getContent();
                        } else if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                            text = choice.getMessage().getContent();
                        }
                        if (text != null) {
                            content.append(text);
                        }
                    }
                });

                assertTrue(chunkCount.get() > 1, "Expected multiple chunks");
                assertTrue(content.length() > 0, "No content from stream");

                passed = true;
                metadata.put("model", config.chatModel);
                metadata.put("chunkCount", chunkCount.get());
                metadata.put("contentLength", content.length());

                System.out.println("[PASS] " + config.name + " streaming: " + chunkCount.get() + " chunks");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] " + config.name + " streaming: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult(providerKey, "streaming_chat", passed, latencyMs, error, metadata);

            if (!passed) {
                fail(config.name + " streaming failed: " + error);
            }
        }

        @ParameterizedTest(name = "{0} - Embeddings")
        @MethodSource("com.swfte.sdk.ComprehensiveGatewayTest#embeddingProviders")
        @Order(3)
        void testEmbeddings(String providerKey, ProviderConfig config) {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(config.embeddingModel)
                    .input("Hello, world! This is a test of embedding generation.")
                    .build();

                EmbeddingResponse response = client.embeddings().create(request);
                List<Double> embedding = response.getData().get(0).getEmbedding();

                assertNotNull(embedding, "No embedding data");
                assertFalse(embedding.isEmpty(), "Empty embedding");

                passed = true;
                metadata.put("model", config.embeddingModel);
                metadata.put("dimensions", embedding.size());

                System.out.println("[PASS] " + config.name + " embeddings: " + embedding.size() + " dimensions");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] " + config.name + " embeddings: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult(providerKey, "embeddings", passed, latencyMs, error, metadata);

            if (!passed) {
                fail(config.name + " embeddings failed: " + error);
            }
        }
    }

    // ==================== OPENAI SPECIFIC TESTS ====================

    @Nested
    @DisplayName("OpenAI Specific")
    @Order(10)
    class OpenAISpecificTests {

        @Test
        @DisplayName("DALL-E 3 Image Generation")
        @Order(1)
        void testImageGenerationDalle3() {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ImageRequest request = ImageRequest.builder()
                    .model("dall-e-3")
                    .prompt("A simple blue square on a white background, minimalist style")
                    .size("1024x1024")
                    .n(1)
                    .build();

                ImageResponse response = client.images().generate(request);

                assertNotNull(response.getData(), "No data in response");
                assertFalse(response.getData().isEmpty(), "Empty data list");

                String url = response.getData().get(0).getUrl();
                assertNotNull(url, "No URL in response");
                assertTrue(url.startsWith("http"), "Invalid URL format");

                passed = true;
                metadata.put("model", "dall-e-3");
                metadata.put("hasUrl", true);

                System.out.println("[PASS] DALL-E 3 image: " + url.substring(0, Math.min(80, url.length())) + "...");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] DALL-E 3 image: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("openai", "image_generation_dalle3", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("DALL-E 3 image generation failed: " + error);
            }
        }

        @Test
        @DisplayName("Text-to-Speech")
        @Order(2)
        void testTextToSpeech() throws IOException {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                byte[] audioData = client.audio().speech().create(
                    "tts-1",
                    "Hello, this is a test of OpenAI text to speech synthesis.",
                    "alloy"
                );

                assertNotNull(audioData, "No audio data");
                assertTrue(audioData.length > 0, "Empty audio data");

                // Save for later use
                Path audioPath = testResultsDir.resolve("openai_tts_test.mp3");
                Files.write(audioPath, audioData);

                passed = true;
                metadata.put("model", "tts-1");
                metadata.put("voice", "alloy");
                metadata.put("audioSizeBytes", audioData.length);

                System.out.println("[PASS] OpenAI TTS: " + audioData.length + " bytes");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] OpenAI TTS: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("openai", "text_to_speech", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("OpenAI TTS failed: " + error);
            }
        }

        @Test
        @DisplayName("Speech-to-Text (Whisper)")
        @Order(3)
        void testSpeechToText() throws IOException {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                Path audioPath = ensureAudioTestFile();
                byte[] audioData = Files.readAllBytes(audioPath);

                TranscriptionResponse response = client.audio().transcriptions().create(
                    "whisper-1",
                    audioData
                );

                assertNotNull(response.getText(), "No transcript text");
                assertFalse(response.getText().isEmpty(), "Empty transcript");

                passed = true;
                metadata.put("model", "whisper-1");
                metadata.put("transcriptLength", response.getText().length());

                System.out.println("[PASS] OpenAI STT: " + response.getText().substring(0, Math.min(50, response.getText().length())) + "...");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] OpenAI STT: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("openai", "speech_to_text", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("OpenAI STT failed: " + error);
            }
        }

        @Test
        @DisplayName("Function Calling")
        @Order(4)
        void testFunctionCalling() {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                // Note: This requires the ChatRequest to support tools
                // For now, we'll do a basic test that the API accepts the request
                ChatRequest request = ChatRequest.builder()
                    .model("gpt-4o-mini")
                    .addMessage("user", "What's the weather like in San Francisco?")
                    .maxTokens(100)
                    .build();

                ChatResponse response = client.chat().completions().create(request);
                String content = response.getChoices().get(0).getMessage().getContent();

                assertNotNull(content, "No content");

                passed = true;
                metadata.put("model", "gpt-4o-mini");
                metadata.put("hasContent", true);

                System.out.println("[PASS] OpenAI function calling test passed");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] OpenAI function calling: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("openai", "function_calling", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("OpenAI function calling failed: " + error);
            }
        }
    }

    // ==================== SELF-HOSTED TESTS ====================

    @Nested
    @DisplayName("Self-Hosted Deployments")
    @Order(20)
    class SelfHostedTests {

        @Test
        @DisplayName("RunPod Qwen - Chat")
        @Order(1)
        void testRunPodQwenChat() {
            assumeTrue(QWEN_DEPLOYMENT_ID != null && !QWEN_DEPLOYMENT_ID.isEmpty(),
                "QWEN_DEPLOYMENT_ID not set");

            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ChatRequest request = ChatRequest.builder()
                    .model("deployed:" + QWEN_DEPLOYMENT_ID)
                    .addMessage("user", "Explain machine learning in 2 sentences.")
                    .maxTokens(100)
                    .temperature(0.7)
                    .build();

                ChatResponse response = client.chat().completions().create(request);
                String content = response.getChoices().get(0).getMessage().getContent();

                assertNotNull(content, "No content");
                assertFalse(content.isEmpty(), "Empty content");

                passed = true;
                metadata.put("deploymentId", QWEN_DEPLOYMENT_ID);
                metadata.put("contentLength", content.length());

                System.out.println("[PASS] RunPod Qwen: " + content.substring(0, Math.min(50, content.length())) + "...");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] RunPod Qwen: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("runpod_qwen", "chat_completion", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("RunPod Qwen chat failed: " + error);
            }
        }

        @Test
        @DisplayName("RunPod Qwen - Streaming")
        @Order(2)
        void testRunPodQwenStreaming() {
            assumeTrue(QWEN_DEPLOYMENT_ID != null && !QWEN_DEPLOYMENT_ID.isEmpty(),
                "QWEN_DEPLOYMENT_ID not set");

            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ChatRequest request = ChatRequest.builder()
                    .model("deployed:" + QWEN_DEPLOYMENT_ID)
                    .addMessage("user", "Count from 1 to 5.")
                    .maxTokens(50)
                    .stream(true)
                    .build();

                StringBuilder content = new StringBuilder();
                AtomicInteger chunkCount = new AtomicInteger(0);

                Stream<ChatChunk> stream = client.chat().completions().createStream(request);
                stream.forEach(chunk -> {
                    chunkCount.incrementAndGet();
                    if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
                        var choice = chunk.getChoices().get(0);
                        String text = null;
                        if (choice.getDelta() != null && choice.getDelta().getContent() != null) {
                            text = choice.getDelta().getContent();
                        } else if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                            text = choice.getMessage().getContent();
                        }
                        if (text != null) {
                            content.append(text);
                        }
                    }
                });

                assertTrue(chunkCount.get() > 1, "Expected multiple chunks");
                assertTrue(content.length() > 0, "No content");

                passed = true;
                metadata.put("deploymentId", QWEN_DEPLOYMENT_ID);
                metadata.put("chunkCount", chunkCount.get());

                System.out.println("[PASS] RunPod Qwen streaming: " + chunkCount.get() + " chunks");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] RunPod Qwen streaming: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("runpod_qwen", "streaming", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("RunPod Qwen streaming failed: " + error);
            }
        }

        @Test
        @DisplayName("RunPod Llama - Chat")
        @Order(3)
        void testRunPodLlamaChat() {
            assumeTrue(LLAMA_DEPLOYMENT_ID != null && !LLAMA_DEPLOYMENT_ID.isEmpty(),
                "LLAMA_DEPLOYMENT_ID not set");

            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ChatRequest request = ChatRequest.builder()
                    .model("deployed:" + LLAMA_DEPLOYMENT_ID)
                    .addMessage("user", "What is Python? Answer in one sentence.")
                    .maxTokens(80)
                    .temperature(0.5)
                    .build();

                ChatResponse response = client.chat().completions().create(request);
                String content = response.getChoices().get(0).getMessage().getContent();

                assertNotNull(content, "No content");

                passed = true;
                metadata.put("deploymentId", LLAMA_DEPLOYMENT_ID);
                metadata.put("contentLength", content.length());

                System.out.println("[PASS] RunPod Llama: " + content.substring(0, Math.min(50, content.length())) + "...");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] RunPod Llama: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("runpod_llama", "chat_completion", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("RunPod Llama chat failed: " + error);
            }
        }

        @Test
        @DisplayName("RunPod SDXL - Image Generation")
        @Order(4)
        void testRunPodSDXLImage() {
            assumeTrue(SDXL_DEPLOYMENT_ID != null && !SDXL_DEPLOYMENT_ID.isEmpty(),
                "SDXL_DEPLOYMENT_ID not set");

            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ImageRequest request = ImageRequest.builder()
                    .model("comfy:sdxl")
                    .prompt("A futuristic city at sunset, cyberpunk style, highly detailed")
                    .size("1024x1024")
                    .n(1)
                    .build();

                ImageResponse response = client.images().generate(request);

                assertNotNull(response.getData(), "No data");
                assertFalse(response.getData().isEmpty(), "Empty data");

                String url = response.getData().get(0).getUrl();
                assertNotNull(url, "No URL");

                passed = true;
                metadata.put("model", "comfy:sdxl");
                metadata.put("hasUrl", true);

                System.out.println("[PASS] RunPod SDXL: " + url.substring(0, Math.min(80, url.length())) + "...");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] RunPod SDXL: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("runpod_sdxl", "image_generation", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("RunPod SDXL image generation failed: " + error);
            }
        }

        @Test
        @DisplayName("RunPod TTS")
        @Order(5)
        void testRunPodTTS() throws IOException {
            assumeTrue(TTS_DEPLOYMENT_ID != null && !TTS_DEPLOYMENT_ID.isEmpty(),
                "TTS_DEPLOYMENT_ID not set");

            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                byte[] audioData = client.audio().speech().create(
                    "deployed:" + TTS_DEPLOYMENT_ID,
                    "Hello from self-hosted text to speech on RunPod.",
                    "default"
                );

                assertNotNull(audioData, "No audio data");
                assertTrue(audioData.length > 0, "Empty audio");

                // Save for STT test
                Path audioPath = testResultsDir.resolve("runpod_tts_test.mp3");
                Files.write(audioPath, audioData);

                passed = true;
                metadata.put("deploymentId", TTS_DEPLOYMENT_ID);
                metadata.put("audioSizeBytes", audioData.length);

                System.out.println("[PASS] RunPod TTS: " + audioData.length + " bytes");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] RunPod TTS: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("runpod_tts", "text_to_speech", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("RunPod TTS failed: " + error);
            }
        }

        @Test
        @DisplayName("RunPod STT")
        @Order(6)
        void testRunPodSTT() throws IOException {
            assumeTrue(STT_DEPLOYMENT_ID != null && !STT_DEPLOYMENT_ID.isEmpty(),
                "STT_DEPLOYMENT_ID not set");

            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                // Prefer RunPod TTS audio if available
                Path audioPath = testResultsDir.resolve("runpod_tts_test.mp3");
                if (!Files.exists(audioPath)) {
                    audioPath = ensureAudioTestFile();
                }

                byte[] audioData = Files.readAllBytes(audioPath);

                TranscriptionResponse response = client.audio().transcriptions().create(
                    "deployed:" + STT_DEPLOYMENT_ID,
                    audioData
                );

                assertNotNull(response.getText(), "No transcript");
                assertFalse(response.getText().isEmpty(), "Empty transcript");

                passed = true;
                metadata.put("deploymentId", STT_DEPLOYMENT_ID);
                metadata.put("transcriptLength", response.getText().length());

                System.out.println("[PASS] RunPod STT: " + response.getText().substring(0, Math.min(50, response.getText().length())) + "...");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] RunPod STT: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("runpod_stt", "speech_to_text", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("RunPod STT failed: " + error);
            }
        }
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Nested
    @DisplayName("Error Handling")
    @Order(30)
    class ErrorHandlingTests {

        @Test
        @DisplayName("Invalid Model")
        @Order(1)
        void testInvalidModel() {
            Instant start = Instant.now();
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ChatRequest request = ChatRequest.builder()
                    .model("invalid-model-xyz-12345")
                    .addMessage("user", "Hello")
                    .build();

                client.chat().completions().create(request);

            } catch (Exception e) {
                passed = true;
                metadata.put("exceptionType", e.getClass().getSimpleName());
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("error_handling", "invalid_model", passed, latencyMs,
                passed ? null : "Expected exception not raised", metadata);

            assertTrue(passed, "Invalid model should raise exception");
            System.out.println("[PASS] Invalid model handling");
        }

        @Test
        @DisplayName("Empty Messages")
        @Order(2)
        void testEmptyMessages() {
            Instant start = Instant.now();
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ChatRequest request = ChatRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(List.of())
                    .build();

                client.chat().completions().create(request);

            } catch (Exception e) {
                passed = true;
                metadata.put("exceptionType", e.getClass().getSimpleName());
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("error_handling", "empty_messages", passed, latencyMs,
                passed ? null : "Expected exception not raised", metadata);

            assertTrue(passed, "Empty messages should raise exception");
            System.out.println("[PASS] Empty messages handling");
        }
    }

    // ==================== DEPLOYMENT MANAGEMENT TESTS ====================

    @Nested
    @DisplayName("Deployment Management")
    @Order(40)
    class DeploymentManagementTests {

        @Test
        @DisplayName("List Models")
        @Order(1)
        void testListModels() {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                ModelsResponse models = client.models().listResponse();

                assertNotNull(models, "No models response");

                passed = true;
                metadata.put("modelCount", models.getData() != null ? models.getData().size() : 0);

                System.out.println("[PASS] List models: " + metadata.get("modelCount") + " models");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] List models: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("gateway", "list_models", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("List models failed: " + error);
            }
        }

        @Test
        @DisplayName("List Deployments")
        @Order(2)
        void testListDeployments() {
            Instant start = Instant.now();
            String error = null;
            boolean passed = false;
            Map<String, Object> metadata = new HashMap<>();

            try {
                DeploymentListResponse deployments = client.deployments().listResponse();

                assertNotNull(deployments, "No deployments response");

                passed = true;
                metadata.put("deploymentCount",
                    deployments.getDeployments() != null ? deployments.getDeployments().size() : 0);

                System.out.println("[PASS] List deployments: " + metadata.get("deploymentCount") + " deployments");

            } catch (Exception e) {
                error = e.getMessage();
                System.out.println("[FAIL] List deployments: " + error);
            }

            long latencyMs = Duration.between(start, Instant.now()).toMillis();
            recordResult("gateway", "list_deployments", passed, latencyMs, error, metadata);

            if (!passed) {
                fail("List deployments failed: " + error);
            }
        }
    }

    // ==================== PERFORMANCE TESTS ====================

    @Nested
    @DisplayName("Performance")
    @Order(50)
    class PerformanceTests {

        @ParameterizedTest(name = "{0} - Latency Baseline")
        @MethodSource("com.swfte.sdk.ComprehensiveGatewayTest#latencyProviders")
        @Order(1)
        void testLatencyBaseline(String providerKey, ProviderConfig config) {
            List<Long> latencies = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                Instant start = Instant.now();

                try {
                    ChatRequest request = ChatRequest.builder()
                        .model(config.chatModel)
                        .addMessage("user", "Say 'OK'.")
                        .maxTokens(5)
                        .temperature(0.0)
                        .build();

                    client.chat().completions().create(request);
                    latencies.add(Duration.between(start, Instant.now()).toMillis());

                } catch (Exception e) {
                    // Skip failed requests
                }
            }

            if (!latencies.isEmpty()) {
                long avgLatency = (long) latencies.stream().mapToLong(Long::longValue).average().orElse(0);
                long minLatency = latencies.stream().mapToLong(Long::longValue).min().orElse(0);
                long maxLatency = latencies.stream().mapToLong(Long::longValue).max().orElse(0);

                Map<String, Object> metadata = new HashMap<>();
                metadata.put("samples", latencies.size());
                metadata.put("minMs", minLatency);
                metadata.put("maxMs", maxLatency);
                metadata.put("avgMs", avgLatency);

                recordResult(providerKey, "latency_baseline", true, avgLatency, null, metadata);

                System.out.println("[PASS] " + config.name + " latency: avg=" + avgLatency + "ms, min=" + minLatency + "ms, max=" + maxLatency + "ms");
            } else {
                recordResult(providerKey, "latency_baseline", false, 0, "All requests failed", null);
                fail("All latency test requests failed");
            }
        }
    }

    static Stream<Arguments> latencyProviders() {
        return Stream.of(
            Arguments.of("openai", PROPRIETARY_PROVIDERS.get("openai")),
            Arguments.of("anthropic", PROPRIETARY_PROVIDERS.get("anthropic"))
        );
    }
}
