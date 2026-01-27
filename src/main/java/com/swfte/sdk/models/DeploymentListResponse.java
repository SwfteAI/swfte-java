package com.swfte.sdk.models;

import java.util.List;

/**
 * Response for deployment list operations.
 */
public class DeploymentListResponse {
    
    private List<Deployment> content;
    private List<Deployment> deployments;
    private int totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    
    public DeploymentListResponse() {}
    
    public List<Deployment> getContent() {
        return content;
    }
    
    public void setContent(List<Deployment> content) {
        this.content = content;
    }
    
    public List<Deployment> getDeployments() {
        return deployments;
    }
    
    public void setDeployments(List<Deployment> deployments) {
        this.deployments = deployments;
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
     * Get the list of deployments (from either content or deployments field).
     */
    public List<Deployment> getDeploymentList() {
        if (content != null && !content.isEmpty()) {
            return content;
        }
        return deployments;
    }
}







