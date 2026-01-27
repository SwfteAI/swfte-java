---
name: Bug Report
about: Report a bug to help us improve the Swfte Java SDK
title: '[BUG] '
labels: bug
assignees: ''
---

## Description

A clear and concise description of what the bug is.

## Environment

- **SDK Version**: (e.g., 1.0.0)
- **Java Version**: (e.g., 17.0.9, 21.0.1)
- **Operating System**: (e.g., macOS 14.0, Ubuntu 22.04, Windows 11)
- **Build Tool**: (Maven, Gradle)
- **Build Tool Version**: (e.g., Maven 3.9.5, Gradle 8.5)

## Steps to Reproduce

1. Initialize client with '...'
2. Call method '...'
3. See error

## Expected Behavior

A clear and concise description of what you expected to happen.

## Actual Behavior

A clear and concise description of what actually happened.

## Code Sample

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.ChatRequest;
import com.swfte.sdk.models.ChatResponse;
import com.swfte.sdk.models.Message;

public class Example {
    public static void main(String[] args) {
        SwfteClient client = SwfteClient.builder()
            .apiKey("sk-swfte-...")
            .build();

        // Minimal code to reproduce the issue
        ChatResponse response = client.chat().completions().create(
            ChatRequest.builder()
                .model("openai:gpt-4")
                .addMessage(Message.user("Hello!"))
                .build()
        );
    }
}
```

## Stack Trace

```
Paste the full error message and stack trace here
```

## Additional Context

Add any other context about the problem here (screenshots, logs, related issues, etc.).

## Possible Solution

(Optional) If you have suggestions on how to fix the bug.
