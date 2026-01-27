package com.swfte.sdk.unit;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.exceptions.SwfteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SwfteClient initialization and configuration.
 */
class SwfteClientTest {

    private static final String TEST_API_KEY = "sk-swfte-test-key-12345";
    private static final String TEST_BASE_URL = "https://api.test.swfte.com/v1/gateway";
    private static final String TEST_WORKSPACE_ID = "ws-test-12345";

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize client with API key")
        void shouldInitializeWithApiKey() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .build();

            assertEquals(TEST_API_KEY, client.getApiKey());
            assertEquals("https://api.swfte.com/v1/gateway", client.getBaseUrl());
            assertEquals(60000, client.getTimeout());
            assertEquals(3, client.getMaxRetries());
        }

        @Test
        @DisplayName("Should throw exception when API key is missing")
        void shouldThrowWhenApiKeyMissing() {
            // Clear environment variable if set
            assertThrows(SwfteException.class, () -> {
                SwfteClient.builder().build();
            });
        }

        @Test
        @DisplayName("Should accept custom base URL")
        void shouldAcceptCustomBaseUrl() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .baseUrl(TEST_BASE_URL)
                    .build();

            assertEquals(TEST_BASE_URL, client.getBaseUrl());
        }

        @Test
        @DisplayName("Should strip trailing slash from base URL")
        void shouldStripTrailingSlashFromBaseUrl() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .baseUrl("https://api.test.swfte.com/v1/gateway/")
                    .build();

            assertFalse(client.getBaseUrl().endsWith("/"));
        }

        @Test
        @DisplayName("Should accept custom timeout")
        void shouldAcceptCustomTimeout() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .timeout(120000)
                    .build();

            assertEquals(120000, client.getTimeout());
        }

        @Test
        @DisplayName("Should accept custom max retries")
        void shouldAcceptCustomMaxRetries() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .maxRetries(5)
                    .build();

            assertEquals(5, client.getMaxRetries());
        }

        @Test
        @DisplayName("Should accept workspace ID")
        void shouldAcceptWorkspaceId() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .workspaceId(TEST_WORKSPACE_ID)
                    .build();

            assertEquals(TEST_WORKSPACE_ID, client.getWorkspaceId());
        }

        @Test
        @DisplayName("Should use default values for optional parameters")
        void shouldUseDefaultValues() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .build();

            assertEquals("https://api.swfte.com/v1/gateway", client.getBaseUrl());
            assertEquals(60000, client.getTimeout());
            assertEquals(3, client.getMaxRetries());
            assertNull(client.getWorkspaceId());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Builder should be fluent")
        void builderShouldBeFluent() {
            SwfteClient client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .baseUrl(TEST_BASE_URL)
                    .timeout(30000)
                    .maxRetries(2)
                    .workspaceId(TEST_WORKSPACE_ID)
                    .build();

            assertEquals(TEST_API_KEY, client.getApiKey());
            assertEquals(TEST_BASE_URL, client.getBaseUrl());
            assertEquals(30000, client.getTimeout());
            assertEquals(2, client.getMaxRetries());
            assertEquals(TEST_WORKSPACE_ID, client.getWorkspaceId());
        }

        @Test
        @DisplayName("Builder should create immutable client")
        void builderShouldCreateImmutableClient() {
            SwfteClient.Builder builder = SwfteClient.builder()
                    .apiKey(TEST_API_KEY);

            SwfteClient client1 = builder.build();
            SwfteClient client2 = builder.timeout(90000).build();

            // Original client should not be affected
            assertEquals(60000, client1.getTimeout());
            assertEquals(90000, client2.getTimeout());
        }
    }

    @Nested
    @DisplayName("Resource Accessors Tests")
    class ResourceAccessorsTests {

        private SwfteClient client;

        @BeforeEach
        void setUp() {
            client = SwfteClient.builder()
                    .apiKey(TEST_API_KEY)
                    .build();
        }

        @Test
        @DisplayName("Should have chat accessor")
        void shouldHaveChatAccessor() {
            assertNotNull(client.chat());
        }

        @Test
        @DisplayName("Should have agents accessor")
        void shouldHaveAgentsAccessor() {
            assertNotNull(client.agents());
        }

        @Test
        @DisplayName("Should have workflows accessor")
        void shouldHaveWorkflowsAccessor() {
            assertNotNull(client.workflows());
        }

        @Test
        @DisplayName("Should have deployments accessor")
        void shouldHaveDeploymentsAccessor() {
            assertNotNull(client.deployments());
        }

        @Test
        @DisplayName("Should have models accessor")
        void shouldHaveModelsAccessor() {
            assertNotNull(client.models());
        }

        @Test
        @DisplayName("Should have images accessor")
        void shouldHaveImagesAccessor() {
            assertNotNull(client.images());
        }

        @Test
        @DisplayName("Should have embeddings accessor")
        void shouldHaveEmbeddingsAccessor() {
            assertNotNull(client.embeddings());
        }

        @Test
        @DisplayName("Should have audio accessor")
        void shouldHaveAudioAccessor() {
            assertNotNull(client.audio());
        }

        @Test
        @DisplayName("Should have secrets accessor")
        void shouldHaveSecretsAccessor() {
            assertNotNull(client.secrets());
        }

        @Test
        @DisplayName("Should have conversations accessor")
        void shouldHaveConversationsAccessor() {
            assertNotNull(client.conversations());
        }

        @Test
        @DisplayName("Resource accessors should return same instance")
        void resourceAccessorsShouldReturnSameInstance() {
            var chat1 = client.chat();
            var chat2 = client.chat();
            assertSame(chat1, chat2);

            var agents1 = client.agents();
            var agents2 = client.agents();
            assertSame(agents1, agents2);
        }
    }

    @Nested
    @DisplayName("Configuration Validation Tests")
    class ConfigurationValidationTests {

        @Test
        @DisplayName("Should reject empty API key")
        void shouldRejectEmptyApiKey() {
            assertThrows(SwfteException.class, () -> {
                SwfteClient.builder()
                        .apiKey("")
                        .build();
            });
        }

        @Test
        @DisplayName("Should accept any non-empty API key format")
        void shouldAcceptAnyNonEmptyApiKey() {
            // The SDK should accept any non-empty string for flexibility
            SwfteClient client = SwfteClient.builder()
                    .apiKey("any-key-format")
                    .build();

            assertEquals("any-key-format", client.getApiKey());
        }
    }
}
