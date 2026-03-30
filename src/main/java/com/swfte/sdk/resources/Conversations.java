package com.swfte.sdk.resources;

import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.HttpClient;
import com.swfte.sdk.models.Conversation;
import com.swfte.sdk.models.ConversationListResponse;
import com.swfte.sdk.models.ConversationMessage;
import com.swfte.sdk.models.MessagePage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conversations API resource for managing conversation history.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * SwfteClient client = SwfteClient.builder()
 *     .apiKey("sk-swfte-...")
 *     .workspaceId("my-workspace")
 *     .build();
 *
 * // Create a conversation
 * Conversation conversation = client.conversations().create(
 *     Conversation.builder()
 *         .title("Support Chat")
 *         .agentId("agent-123")
 *         .model("gpt-4")
 *         .systemPrompt("You are a helpful assistant.")
 *         .build()
 * );
 *
 * // Add messages
 * client.conversations().addMessage(
 *     conversation.getId(),
 *     ConversationMessage.builder()
 *         .role("user")
 *         .content("Hello!")
 *         .build()
 * );
 *
 * // Get conversation with messages
 * Conversation full = client.conversations().get(conversation.getId());
 * }</pre>
 */
public class Conversations {

    private final HttpClient httpClient;
    private final SwfteClient client;

    public Conversations(SwfteClient client) {
        this.client = client;
        this.httpClient = new HttpClient(client);
    }

    /**
     * Get the base URL for conversation endpoints.
     */
    private String getBaseUrl() {
        return "/v1/conversations";
    }

    /**
     * Create a new conversation.
     *
     * @param conversation the conversation to create
     * @return the created conversation
     */
    public Conversation create(Conversation conversation) {
        Map<String, Object> payload = new HashMap<>();

        if (conversation.getTitle() != null) {
            payload.put("title", conversation.getTitle());
        }
        if (conversation.getAgentId() != null) {
            payload.put("agentId", conversation.getAgentId());
        }
        if (conversation.getModel() != null) {
            payload.put("model", conversation.getModel());
        }
        if (conversation.getSystemPrompt() != null) {
            payload.put("systemPrompt", conversation.getSystemPrompt());
        }
        if (conversation.getMetadata() != null) {
            payload.put("metadata", conversation.getMetadata());
        }
        if (client.getWorkspaceId() != null) {
            payload.put("workspaceId", client.getWorkspaceId());
        }

        return httpClient.postWithCustomBase(getBaseUrl(), payload, Conversation.class);
    }

    /**
     * Get a conversation by ID.
     *
     * @param conversationId the conversation ID
     * @return the conversation with messages
     */
    public Conversation get(String conversationId) {
        return httpClient.getWithCustomBase(getBaseUrl() + "/" + conversationId, Conversation.class);
    }

    /**
     * List all conversations.
     *
     * @return list of conversations
     */
    public List<Conversation> list() {
        return list(0, 20);
    }

    /**
     * List conversations with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return list of conversations
     */
    public List<Conversation> list(int page, int size) {
        String url = getBaseUrl() + "?page=" + page + "&size=" + size;
        ConversationListResponse response = httpClient.getWithCustomBase(url, ConversationListResponse.class);
        return response.getConversations();
    }

    /**
     * List conversations for a specific agent.
     *
     * @param agentId the agent ID
     * @return list of conversations for that agent
     */
    public List<Conversation> listByAgent(String agentId) {
        return listByAgent(agentId, 0, 20);
    }

    /**
     * List conversations for a specific agent with pagination.
     *
     * @param agentId the agent ID
     * @param page the page number
     * @param size the page size
     * @return list of conversations for that agent
     */
    public List<Conversation> listByAgent(String agentId, int page, int size) {
        String url = getBaseUrl() + "?agentId=" + agentId + "&page=" + page + "&size=" + size;
        ConversationListResponse response = httpClient.getWithCustomBase(url, ConversationListResponse.class);
        return response.getConversations();
    }

    /**
     * Update a conversation.
     *
     * @param conversationId the conversation ID
     * @param updates the updates to apply
     * @return the updated conversation
     */
    public Conversation update(String conversationId, Conversation updates) {
        Map<String, Object> payload = new HashMap<>();

        if (updates.getTitle() != null) {
            payload.put("title", updates.getTitle());
        }
        if (updates.getSystemPrompt() != null) {
            payload.put("systemPrompt", updates.getSystemPrompt());
        }
        if (updates.getMetadata() != null) {
            payload.put("metadata", updates.getMetadata());
        }

        return httpClient.putWithCustomBase(getBaseUrl() + "/" + conversationId, payload, Conversation.class);
    }

    /**
     * Delete a conversation.
     *
     * @param conversationId the conversation ID
     */
    public void delete(String conversationId) {
        httpClient.deleteWithCustomBase(getBaseUrl() + "/" + conversationId);
    }

    // ==================== Message Operations ====================

    /**
     * Add a message to a conversation.
     *
     * @param conversationId the conversation ID
     * @param message the message to add
     * @return the created message
     */
    public ConversationMessage addMessage(String conversationId, ConversationMessage message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("role", message.getRole());
        payload.put("content", message.getContent());

        if (message.getName() != null) {
            payload.put("name", message.getName());
        }
        if (message.getToolCalls() != null) {
            payload.put("toolCalls", message.getToolCalls());
        }
        if (message.getToolCallId() != null) {
            payload.put("toolCallId", message.getToolCallId());
        }
        if (message.getMetadata() != null) {
            payload.put("metadata", message.getMetadata());
        }

        return httpClient.postWithCustomBase(
            getBaseUrl() + "/" + conversationId + "/messages",
            payload,
            ConversationMessage.class
        );
    }

    /**
     * Get messages from a conversation with cursor-based pagination.
     *
     * @param conversationId the conversation ID
     * @return page of messages
     */
    public MessagePage getMessages(String conversationId) {
        return getMessages(conversationId, 50, null, null);
    }

    /**
     * Get messages from a conversation with options.
     *
     * @param conversationId the conversation ID
     * @param limit maximum number of messages to return
     * @param before cursor - get messages before this message ID
     * @param after cursor - get messages after this message ID
     * @return page of messages
     */
    public MessagePage getMessages(String conversationId, int limit, String before, String after) {
        StringBuilder url = new StringBuilder(getBaseUrl() + "/" + conversationId + "/messages");
        url.append("?limit=").append(limit);

        if (before != null) {
            url.append("&before=").append(before);
        }
        if (after != null) {
            url.append("&after=").append(after);
        }

        return httpClient.getWithCustomBase(url.toString(), MessagePage.class);
    }

    /**
     * Get a specific message.
     *
     * @param conversationId the conversation ID
     * @param messageId the message ID
     * @return the message
     */
    public ConversationMessage getMessage(String conversationId, String messageId) {
        return httpClient.getWithCustomBase(
            getBaseUrl() + "/" + conversationId + "/messages/" + messageId,
            ConversationMessage.class
        );
    }

    /**
     * Delete a specific message.
     *
     * @param conversationId the conversation ID
     * @param messageId the message ID
     */
    public void deleteMessage(String conversationId, String messageId) {
        httpClient.deleteWithCustomBase(getBaseUrl() + "/" + conversationId + "/messages/" + messageId);
    }

    /**
     * Clear all messages from a conversation.
     *
     * @param conversationId the conversation ID
     */
    public void clearMessages(String conversationId) {
        httpClient.deleteWithCustomBase(getBaseUrl() + "/" + conversationId + "/messages");
    }

    /**
     * Search conversations by content.
     *
     * @param query search query
     * @return list of matching conversations
     */
    public List<Conversation> search(String query) {
        return search(query, 0, 20);
    }

    /**
     * Search conversations with pagination.
     *
     * @param query search query
     * @param page page number
     * @param size page size
     * @return list of matching conversations
     */
    public List<Conversation> search(String query, int page, int size) {
        String url = getBaseUrl() + "/search?q=" + encodeParam(query) + "&page=" + page + "&size=" + size;
        ConversationListResponse response = httpClient.getWithCustomBase(url, ConversationListResponse.class);
        return response.getConversations();
    }

    private String encodeParam(String param) {
        try {
            return java.net.URLEncoder.encode(param, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return param;
        }
    }
}
