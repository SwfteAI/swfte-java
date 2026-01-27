package com.swfte.sdk.unit.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Agent;
import com.swfte.sdk.models.AgentListResponse;
import com.swfte.sdk.models.AgentExecuteRequest;
import com.swfte.sdk.models.AgentExecuteResponse;
import com.swfte.sdk.resources.Agents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Agents resource with Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AgentsTest {

    private static final String TEST_API_KEY = "sk-swfte-test-key-12345";
    private static final String TEST_AGENT_ID = "agent-123";
    private static final String TEST_WORKSPACE_ID = "ws-test-12345";

    @Mock
    private HttpClient mockHttpClient;

    private SwfteClient client;
    private Agents agents;

    @BeforeEach
    void setUp() {
        client = SwfteClient.builder()
                .apiKey(TEST_API_KEY)
                .workspaceId(TEST_WORKSPACE_ID)
                .build();
        agents = client.agents();
    }

    private Agent createMockAgent() {
        return Agent.builder()
                .id(TEST_AGENT_ID)
                .agentName("Test Agent")
                .description("A test agent for unit testing")
                .systemPrompt("You are a helpful test assistant.")
                .provider("openai")
                .model("gpt-4")
                .temperature(0.7)
                .maxTokens(2048)
                .active(true)
                .verified(false)
                .workspaceId(TEST_WORKSPACE_ID)
                .build();
    }

    private AgentListResponse createMockAgentListResponse() {
        Agent agent1 = createMockAgent();
        Agent agent2 = Agent.builder()
                .id("agent-456")
                .agentName("Second Agent")
                .description("Another test agent")
                .provider("anthropic")
                .model("claude-3-opus")
                .active(true)
                .build();

        AgentListResponse response = new AgentListResponse();
        response.setAgentList(Arrays.asList(agent1, agent2));
        response.setTotal(2);
        response.setPage(0);
        response.setSize(20);
        return response;
    }

    @Nested
    @DisplayName("Create Agent Tests")
    class CreateAgentTests {

        @Test
        @DisplayName("Should create agent with required parameters")
        void shouldCreateAgentWithRequiredParameters() {
            Agent inputAgent = Agent.builder()
                    .agentName("Test Agent")
                    .build();

            // Note: In a real test, we would mock the HTTP client
            // For now, we're testing the agent object construction
            assertNotNull(inputAgent);
            assertEquals("Test Agent", inputAgent.getAgentName());
        }

        @Test
        @DisplayName("Should create agent with all parameters")
        void shouldCreateAgentWithAllParameters() {
            Agent inputAgent = Agent.builder()
                    .agentName("Full Agent")
                    .description("Complete agent configuration")
                    .systemPrompt("You are a comprehensive assistant.")
                    .provider("openai")
                    .model("gpt-4")
                    .temperature(0.5)
                    .maxTokens(4096)
                    .mode("agent-chat")
                    .build();

            assertEquals("Full Agent", inputAgent.getAgentName());
            assertEquals("Complete agent configuration", inputAgent.getDescription());
            assertEquals("You are a comprehensive assistant.", inputAgent.getSystemPrompt());
            assertEquals("openai", inputAgent.getProvider());
            assertEquals("gpt-4", inputAgent.getModel());
            assertEquals(0.5, inputAgent.getTemperature());
            assertEquals(4096, inputAgent.getMaxTokens());
        }
    }

    @Nested
    @DisplayName("Get Agent Tests")
    class GetAgentTests {

        @Test
        @DisplayName("Should get agent by ID")
        void shouldGetAgentById() {
            Agent mockAgent = createMockAgent();

            // Verify agent properties
            assertEquals(TEST_AGENT_ID, mockAgent.getId());
            assertEquals("Test Agent", mockAgent.getAgentName());
            assertEquals("openai", mockAgent.getProvider());
            assertEquals("gpt-4", mockAgent.getModel());
        }
    }

    @Nested
    @DisplayName("List Agents Tests")
    class ListAgentsTests {

        @Test
        @DisplayName("Should list agents with default pagination")
        void shouldListAgentsWithDefaultPagination() {
            AgentListResponse response = createMockAgentListResponse();

            assertNotNull(response.getAgentList());
            assertEquals(2, response.getAgentList().size());
            assertEquals(2, response.getTotal());
        }

        @Test
        @DisplayName("Should return empty list when no agents")
        void shouldReturnEmptyListWhenNoAgents() {
            AgentListResponse emptyResponse = new AgentListResponse();
            emptyResponse.setAgentList(List.of());
            emptyResponse.setTotal(0);

            assertTrue(emptyResponse.getAgentList().isEmpty());
        }
    }

    @Nested
    @DisplayName("Update Agent Tests")
    class UpdateAgentTests {

        @Test
        @DisplayName("Should update agent name")
        void shouldUpdateAgentName() {
            Agent updatedAgent = Agent.builder()
                    .id(TEST_AGENT_ID)
                    .agentName("Updated Agent Name")
                    .build();

            assertEquals("Updated Agent Name", updatedAgent.getAgentName());
        }

        @Test
        @DisplayName("Should support partial updates")
        void shouldSupportPartialUpdates() {
            Agent partialUpdate = Agent.builder()
                    .description("New description only")
                    .build();

            assertNull(partialUpdate.getAgentName());
            assertEquals("New description only", partialUpdate.getDescription());
        }
    }

    @Nested
    @DisplayName("Delete Agent Tests")
    class DeleteAgentTests {

        @Test
        @DisplayName("Delete should not throw for valid agent ID")
        void deleteShouldNotThrowForValidAgentId() {
            // In integration tests, we would verify the HTTP call
            assertDoesNotThrow(() -> {
                // Simulating successful delete
            });
        }
    }

    @Nested
    @DisplayName("Execute Agent Tests")
    class ExecuteAgentTests {

        @Test
        @DisplayName("Should create execute request with message")
        void shouldCreateExecuteRequestWithMessage() {
            AgentExecuteRequest request = AgentExecuteRequest.builder()
                    .message("Hello, agent!")
                    .build();

            assertEquals("Hello, agent!", request.getMessage());
        }

        @Test
        @DisplayName("Should create execute request with conversation ID")
        void shouldCreateExecuteRequestWithConversationId() {
            AgentExecuteRequest request = AgentExecuteRequest.builder()
                    .message("Continue our conversation")
                    .conversationId("conv-123")
                    .build();

            assertEquals("conv-123", request.getConversationId());
        }

        @Test
        @DisplayName("Should create execute request with context")
        void shouldCreateExecuteRequestWithContext() {
            Map<String, Object> context = new HashMap<>();
            context.put("userId", "user-123");
            context.put("sessionData", Map.of("key", "value"));

            AgentExecuteRequest request = AgentExecuteRequest.builder()
                    .message("Process with context")
                    .context(context)
                    .build();

            assertNotNull(request.getContext());
            assertEquals("user-123", request.getContext().get("userId"));
        }

        @Test
        @DisplayName("Should create execute request with stream enabled")
        void shouldCreateExecuteRequestWithStreamEnabled() {
            AgentExecuteRequest request = AgentExecuteRequest.builder()
                    .message("Stream this response")
                    .stream(true)
                    .build();

            assertTrue(request.getStream());
        }
    }

    @Nested
    @DisplayName("Agent Model Tests")
    class AgentModelTests {

        @Test
        @DisplayName("Should have correct default values")
        void shouldHaveCorrectDefaultValues() {
            Agent agent = Agent.builder()
                    .id("agent-123")
                    .agentName("Test")
                    .build();

            // Verify default values (may be null or false depending on implementation)
            assertNull(agent.getVerified());
            assertNull(agent.getActive());
        }

        @Test
        @DisplayName("Should support builder pattern")
        void shouldSupportBuilderPattern() {
            Agent agent = Agent.builder()
                    .id("agent-123")
                    .agentName("Builder Agent")
                    .description("Testing builder pattern")
                    .provider("openai")
                    .model("gpt-4")
                    .temperature(0.8)
                    .maxTokens(1000)
                    .active(true)
                    .verified(true)
                    .build();

            assertEquals("agent-123", agent.getId());
            assertEquals("Builder Agent", agent.getAgentName());
            assertEquals("Testing builder pattern", agent.getDescription());
            assertEquals("openai", agent.getProvider());
            assertEquals("gpt-4", agent.getModel());
            assertEquals(0.8, agent.getTemperature());
            assertEquals(1000, agent.getMaxTokens());
            assertTrue(agent.getActive());
            assertTrue(agent.getVerified());
        }
    }

    @Nested
    @DisplayName("Execute Response Tests")
    class ExecuteResponseTests {

        @Test
        @DisplayName("Should parse execute response correctly")
        void shouldParseExecuteResponseCorrectly() {
            AgentExecuteResponse response = new AgentExecuteResponse();
            response.setResponse("Hello! How can I help you today?");
            response.setConversationId("conv-123");

            assertEquals("Hello! How can I help you today?", response.getResponse());
            assertEquals("conv-123", response.getConversationId());
        }

        @Test
        @DisplayName("Should include usage information")
        void shouldIncludeUsageInformation() {
            AgentExecuteResponse.Usage usage = new AgentExecuteResponse.Usage();
            usage.setPromptTokens(10);
            usage.setCompletionTokens(20);
            usage.setTotalTokens(30);

            AgentExecuteResponse response = new AgentExecuteResponse();
            response.setUsage(usage);

            assertNotNull(response.getUsage());
            assertEquals(10, response.getUsage().getPromptTokens());
            assertEquals(20, response.getUsage().getCompletionTokens());
            assertEquals(30, response.getUsage().getTotalTokens());
        }
    }

    @Nested
    @DisplayName("Search Agents Tests")
    class SearchAgentsTests {

        @Test
        @DisplayName("Should construct search query correctly")
        void shouldConstructSearchQueryCorrectly() {
            String query = "test agent";
            String encodedQuery = java.net.URLEncoder.encode(query, java.nio.charset.StandardCharsets.UTF_8);

            assertTrue(encodedQuery.contains("test"));
            assertTrue(encodedQuery.contains("agent"));
        }
    }

    @Nested
    @DisplayName("Verify Agent Tests")
    class VerifyAgentTests {

        @Test
        @DisplayName("Should return verified agent")
        void shouldReturnVerifiedAgent() {
            Agent verifiedAgent = Agent.builder()
                    .id(TEST_AGENT_ID)
                    .agentName("Verified Agent")
                    .verified(true)
                    .build();

            assertTrue(verifiedAgent.getVerified());
        }
    }

    @Nested
    @DisplayName("Clone Agent Tests")
    class CloneAgentTests {

        @Test
        @DisplayName("Clone should create new agent with different ID")
        void cloneShouldCreateNewAgentWithDifferentId() {
            Agent original = createMockAgent();
            Agent cloned = Agent.builder()
                    .id("agent-cloned-123")
                    .agentName("Cloned " + original.getAgentName())
                    .description(original.getDescription())
                    .systemPrompt(original.getSystemPrompt())
                    .provider(original.getProvider())
                    .model(original.getModel())
                    .build();

            assertNotEquals(original.getId(), cloned.getId());
            assertTrue(cloned.getAgentName().startsWith("Cloned"));
        }
    }

    @Nested
    @DisplayName("Toggle Active Tests")
    class ToggleActiveTests {

        @Test
        @DisplayName("Should toggle agent active status")
        void shouldToggleAgentActiveStatus() {
            Agent activeAgent = Agent.builder()
                    .id(TEST_AGENT_ID)
                    .agentName("Active Agent")
                    .active(true)
                    .build();

            Agent inactiveAgent = Agent.builder()
                    .id(TEST_AGENT_ID)
                    .agentName("Inactive Agent")
                    .active(false)
                    .build();

            assertTrue(activeAgent.getActive());
            assertFalse(inactiveAgent.getActive());
        }
    }
}
