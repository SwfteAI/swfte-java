package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response for listing conversations.
 */
public class ConversationListResponse {

    private List<Conversation> conversations;

    @JsonProperty("totalElements")
    private int totalElements;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("currentPage")
    private int currentPage;

    @JsonProperty("pageSize")
    private int pageSize;

    public List<Conversation> getConversations() {
        return conversations;
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
