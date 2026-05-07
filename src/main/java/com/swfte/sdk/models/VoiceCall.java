package com.swfte.sdk.models;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a voice call handled by Swfte voice runtime (Twilio or WebRTC).
 */
public class VoiceCall {

    private String callSid;
    private String parentCallSid;
    private String chatFlowId;
    private String workspaceId;
    private String direction;
    private String status;
    private String fromNumber;
    private String toNumber;
    private Long durationSeconds;
    private String recordingUrl;
    private String transcriptUrl;
    private Map<String, Object> metadata;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public VoiceCall() {}

    public String getCallSid() { return callSid; }
    public void setCallSid(String callSid) { this.callSid = callSid; }
    public String getParentCallSid() { return parentCallSid; }
    public void setParentCallSid(String parentCallSid) { this.parentCallSid = parentCallSid; }
    public String getChatFlowId() { return chatFlowId; }
    public void setChatFlowId(String chatFlowId) { this.chatFlowId = chatFlowId; }
    public String getWorkspaceId() { return workspaceId; }
    public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFromNumber() { return fromNumber; }
    public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }
    public String getToNumber() { return toNumber; }
    public void setToNumber(String toNumber) { this.toNumber = toNumber; }
    public Long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Long durationSeconds) { this.durationSeconds = durationSeconds; }
    public String getRecordingUrl() { return recordingUrl; }
    public void setRecordingUrl(String recordingUrl) { this.recordingUrl = recordingUrl; }
    public String getTranscriptUrl() { return transcriptUrl; }
    public void setTranscriptUrl(String transcriptUrl) { this.transcriptUrl = transcriptUrl; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
}
