package com.swfte.sdk.models;

import java.util.List;

/**
 * Response for agent list operations.
 */
public class AgentListResponse {
    
    private List<Agent> content;
    private List<Agent> agents;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    public AgentListResponse() {}
    
    public List<Agent> getContent() {
        return content;
    }
    
    public void setContent(List<Agent> content) {
        this.content = content;
    }
    
    public List<Agent> getAgents() {
        return agents;
    }
    
    public void setAgents(List<Agent> agents) {
        this.agents = agents;
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
    
    /**
     * Get the list of agents (from either content or agents field).
     */
    public List<Agent> getAgentList() {
        if (content != null && !content.isEmpty()) {
            return content;
        }
        return agents;
    }

    // Compatibility methods for tests

    /**
     * Set the agent list (alias for setAgents).
     */
    public void setAgentList(List<Agent> agents) {
        this.agents = agents;
        this.content = agents;
    }

    /**
     * Get total (alias for getTotalElements).
     */
    public int getTotal() {
        return totalElements;
    }

    /**
     * Set total (alias for setTotalElements).
     */
    public void setTotal(int total) {
        this.totalElements = total;
    }

    /**
     * Get page (alias for getCurrentPage).
     */
    public int getPage() {
        return currentPage;
    }

    /**
     * Set page (alias for setCurrentPage).
     */
    public void setPage(int page) {
        this.currentPage = page;
    }

    /**
     * Get size (alias for getPageSize).
     */
    public int getSize() {
        return pageSize;
    }

    /**
     * Set size (alias for setPageSize).
     */
    public void setSize(int size) {
        this.pageSize = size;
    }
}







