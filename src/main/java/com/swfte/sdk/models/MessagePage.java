package com.swfte.sdk.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response for paginated message retrieval.
 */
public class MessagePage {

    private List<ConversationMessage> messages;

    @JsonProperty("hasMore")
    private boolean hasMore;

    @JsonProperty("nextToken")
    private String nextToken;

    @JsonProperty("totalCount")
    private Integer totalCount;

    public List<ConversationMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ConversationMessage> messages) {
        this.messages = messages;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getNextToken() {
        return nextToken;
    }

    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }
}
