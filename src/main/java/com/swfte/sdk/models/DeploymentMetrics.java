package com.swfte.sdk.models;

import java.util.Map;

/**
 * Metrics for a deployment.
 */
public class DeploymentMetrics {
    
    private String deploymentId;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double gpuUsage;
    private Long totalRequests;
    private Double avgLatency;
    private Double errorRate;
    private Long uptime;
    private Map<String, Object> additionalMetrics;
    
    public DeploymentMetrics() {}
    
    public String getDeploymentId() {
        return deploymentId;
    }
    
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
    
    public Double getCpuUsage() {
        return cpuUsage;
    }
    
    public void setCpuUsage(Double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    
    public Double getMemoryUsage() {
        return memoryUsage;
    }
    
    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
    
    public Double getGpuUsage() {
        return gpuUsage;
    }
    
    public void setGpuUsage(Double gpuUsage) {
        this.gpuUsage = gpuUsage;
    }
    
    public Long getTotalRequests() {
        return totalRequests;
    }
    
    public void setTotalRequests(Long totalRequests) {
        this.totalRequests = totalRequests;
    }
    
    public Double getAvgLatency() {
        return avgLatency;
    }
    
    public void setAvgLatency(Double avgLatency) {
        this.avgLatency = avgLatency;
    }
    
    public Double getErrorRate() {
        return errorRate;
    }
    
    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }
    
    public Long getUptime() {
        return uptime;
    }
    
    public void setUptime(Long uptime) {
        this.uptime = uptime;
    }
    
    public Map<String, Object> getAdditionalMetrics() {
        return additionalMetrics;
    }
    
    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }
}







