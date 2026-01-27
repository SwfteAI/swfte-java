package com.swfte.sdk.models;

import java.util.List;

/**
 * Response for workflow list operations.
 */
public class WorkflowListResponse {
    
    private List<Workflow> content;
    private List<Workflow> workflows;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    public WorkflowListResponse() {}
    
    public List<Workflow> getContent() {
        return content;
    }
    
    public void setContent(List<Workflow> content) {
        this.content = content;
    }
    
    public List<Workflow> getWorkflows() {
        return workflows;
    }
    
    public void setWorkflows(List<Workflow> workflows) {
        this.workflows = workflows;
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
     * Get the list of workflows (from either content or workflows field).
     */
    public List<Workflow> getWorkflowList() {
        if (content != null && !content.isEmpty()) {
            return content;
        }
        return workflows;
    }
}







