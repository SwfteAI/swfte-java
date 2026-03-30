# Swfte Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.swfte/swfte-sdk.svg)](https://search.maven.org/artifact/com.swfte/swfte-sdk)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Java 11+](https://img.shields.io/badge/java-11+-blue.svg)](https://www.oracle.com/java/technologies/downloads/)

The official Java client library for the [Swfte API](https://docs.swfte.com) -- a unified gateway to 200+ AI models from OpenAI, Anthropic, Google, and self-hosted infrastructure through a single interface.

## Documentation

Full API reference and guides are available at [docs.swfte.com](https://docs.swfte.com).

## Installation

### Maven

```xml
<dependency>
    <groupId>com.swfte</groupId>
    <artifactId>swfte-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.swfte:swfte-sdk:1.0.0'
```

## Quick Start

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.ChatRequest;
import com.swfte.sdk.models.ChatResponse;
import com.swfte.sdk.models.Message;

import java.util.List;

SwfteClient client = SwfteClient.builder()
    .apiKey("sk-swfte-...")
    .build();

ChatResponse response = client.chat().completions().create(
    ChatRequest.builder()
        .model("openai:gpt-4")
        .messages(List.of(new Message("user", "Hello, world!")))
        .build()
);

System.out.println(response.getChoices().get(0).getMessage().getContent());
```

## Usage

### Chat Completions

```java
ChatResponse response = client.chat().completions().create(
    ChatRequest.builder()
        .model("anthropic:claude-3-opus")
        .messages(List.of(
            new Message("system", "You are a helpful assistant."),
            new Message("user", "Explain quantum computing in one sentence.")
        ))
        .temperature(0.7)
        .maxTokens(256)
        .build()
);
```

### Streaming

```java
Stream<String> stream = client.chat().completions().createStream(
    ChatRequest.builder()
        .model("openai:gpt-4")
        .messages(List.of(new Message("user", "Write a short poem.")))
        .stream(true)
        .build()
);

stream.forEach(chunk -> System.out.print(chunk));
```

### Agents

```java
import com.swfte.sdk.models.Agent;

// Create an agent
Agent agent = client.agents().create(
    Agent.builder()
        .agentName("Research Assistant")
        .systemPrompt("You are a research assistant specializing in AI.")
        .provider("OPENAI")
        .model("gpt-4")
        .build()
);

// List agents
List<Agent> agents = client.agents().list();

// Update an agent (V2 PATCH)
Agent updated = client.agents().update(agent.getId(),
    Agent.builder().description("Updated description").build()
);

// Toggle active status
client.agents().toggleActive(agent.getId(), false);

// Associate a workflow
client.agents().associateWorkflow(agent.getId(), "workflow-id");

// Delete an agent
client.agents().delete(agent.getId());
```

### Workflows

```java
import com.swfte.sdk.models.Workflow;
import com.swfte.sdk.models.WorkflowNode;
import com.swfte.sdk.models.WorkflowEdge;
import com.swfte.sdk.models.WorkflowExecution;

// Create a workflow
Workflow workflow = client.workflows().create(
    Workflow.builder()
        .name("Content Pipeline")
        .nodes(List.of(
            WorkflowNode.builder().id("start").type("TRIGGER").build(),
            WorkflowNode.builder().id("llm").type("LLM").build(),
            WorkflowNode.builder().id("end").type("END").build()
        ))
        .edges(List.of(
            new WorkflowEdge("e1", "start", "llm"),
            new WorkflowEdge("e2", "llm", "end")
        ))
        .build()
);

// Execute a workflow
WorkflowExecution execution = client.workflows().execute(
    workflow.getId(), Map.of("input", "Hello")
);

// Wait for completion
WorkflowExecution result = client.workflows().waitForCompletion(execution.getId());
```

### GPU Model Deployments

```java
import com.swfte.sdk.models.Deployment;

// Deploy a model to GPU infrastructure
Deployment deployment = client.deployments().create(
    Deployment.builder()
        .modelName("meta-llama/Llama-3.2-8B-Instruct")
        .modelType("chat")
        .build()
);

// Wait for deployment to be ready
Deployment ready = client.deployments().waitForReady(deployment.getId());
System.out.println("Endpoint: " + ready.getEndpointUrl());

// Clean up
client.deployments().delete(deployment.getId());
```

### Images

```java
import com.swfte.sdk.models.ImageRequest;
import com.swfte.sdk.models.ImageResponse;

ImageResponse response = client.images().generate(
    ImageRequest.builder()
        .model("openai:dall-e-3")
        .prompt("A sunset over a mountain range, oil painting style")
        .size("1024x1024")
        .quality("hd")
        .build()
);
```

### Embeddings

```java
import com.swfte.sdk.models.EmbeddingRequest;
import com.swfte.sdk.models.EmbeddingResponse;

EmbeddingResponse response = client.embeddings().create(
    EmbeddingRequest.builder()
        .model("openai:text-embedding-3-small")
        .input("The quick brown fox jumps over the lazy dog")
        .build()
);
```

### Audio

```java
import com.swfte.sdk.models.TranscriptionResponse;

// Speech to text
byte[] audioFile = Files.readAllBytes(Path.of("recording.mp3"));
TranscriptionResponse transcript = client.audio().transcriptions()
    .create("openai:whisper-1", audioFile);

// Text to speech
byte[] audio = client.audio().speech()
    .create("openai:tts-1", "Hello, welcome to Swfte.", "alloy");
```

### Secrets

```java
import com.swfte.sdk.models.Secret;

// Store an API key securely
Secret secret = client.secrets().create(
    Secret.builder()
        .name("my-api-key")
        .secretType("API_KEY")
        .value("sk-...")
        .environment("production")
        .build()
);

// Validate a secret
boolean isValid = client.secrets().validate(secret.getId());
```

### Conversations

```java
import com.swfte.sdk.models.Conversation;
import com.swfte.sdk.models.ConversationMessage;

// Create a conversation
Conversation conversation = client.conversations().create(
    Conversation.builder().title("Support Chat").build()
);

// Add messages
client.conversations().addMessage(conversation.getId(),
    ConversationMessage.builder().role("user").content("Hello!").build()
);

// Retrieve message history
MessagePage messages = client.conversations().getMessages(conversation.getId());
```

## Configuration

```java
SwfteClient client = SwfteClient.builder()
    .apiKey("sk-swfte-...")                   // Required. Also reads SWFTE_API_KEY env var.
    .baseUrl("https://api.swfte.com/v2/gateway")  // Default
    .timeout(60000)                            // Request timeout in ms
    .maxRetries(3)                             // Retry count for failed requests
    .workspaceId("ws-...")                     // Workspace scoping. Also reads SWFTE_WORKSPACE_ID.
    .build();
```

| Parameter | Type | Default | Description |
|---|---|---|---|
| `apiKey` | `String` | `SWFTE_API_KEY` env | Your Swfte API key |
| `baseUrl` | `String` | `https://api.swfte.com/v2/gateway` | API base URL |
| `timeout` | `int` | `60000` | Request timeout (ms) |
| `maxRetries` | `int` | `3` | Max retry attempts |
| `workspaceId` | `String` | `SWFTE_WORKSPACE_ID` env | Workspace ID |

## Error Handling

```java
import com.swfte.sdk.exceptions.*;

try {
    ChatResponse response = client.chat().completions().create(request);
} catch (AuthenticationException e) {
    System.err.println("Invalid API key");
} catch (RateLimitException e) {
    System.err.println("Rate limit exceeded, retry later");
} catch (ApiException e) {
    System.err.println("API error " + e.getStatusCode() + ": " + e.getMessage());
} catch (SwfteException e) {
    System.err.println("SDK error: " + e.getMessage());
}
```

| Exception | Description |
|---|---|
| `SwfteException` | Base exception for all SDK errors |
| `AuthenticationException` | Invalid or missing API key (HTTP 401) |
| `RateLimitException` | Rate limit exceeded (HTTP 429) |
| `ApiException` | General API error with status code |

## Supported Providers

| Provider | Models | Qualifier Prefix |
|---|---|---|
| OpenAI | GPT-4, GPT-4o, o1, DALL-E, Whisper, TTS | `openai:` |
| Anthropic | Claude 3.5, Claude 3 Opus/Sonnet/Haiku | `anthropic:` |
| Google | Gemini 2.0, Gemini 1.5 Pro/Flash | `google:` |
| Self-hosted | Any model via RunPod/vLLM deployment | `runpod:` |

## Requirements

- Java 11 or later
- Jackson 2.15+ (included as dependency)

## Contributing

We welcome contributions. Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines and our [Code of Conduct](CODE_OF_CONDUCT.md).

All contributors must sign the [Swfte CLA](https://cla.swfte.com) before their first pull request can be merged.

## Security

To report a vulnerability, please see [SECURITY.md](SECURITY.md). Do not open a public issue for security concerns.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

Copyright (c) 2025 Swfte, Inc.
