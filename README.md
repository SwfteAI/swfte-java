# Swfte Java SDK

[![Maven Central](https://img.shields.io/maven-central/v/com.swfte/swfte-sdk.svg)](https://search.maven.org/artifact/com.swfte/swfte-sdk)
[![Java Version](https://img.shields.io/badge/Java-11%2B-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/github/actions/workflow/status/swfte/agents-service/java-sdk.yml?branch=main)](https://github.com/swfte/agents-service/actions)
[![Coverage](https://img.shields.io/codecov/c/github/swfte/agents-service)](https://codecov.io/gh/swfte/agents-service)
[![Javadoc](https://javadoc.io/badge2/com.swfte/swfte-sdk/javadoc.svg)](https://javadoc.io/doc/com.swfte/swfte-sdk)

The official Java SDK for the Swfte AI Gateway - unified access to all AI providers through a single API.

## Table of Contents

- [Installation](#installation)
- [Quick Start](#quick-start)
- [Features](#features)
- [Supported Models](#supported-models)
- [Examples](#examples)
- [Configuration](#configuration)
- [Error Handling](#error-handling)
- [Requirements](#requirements)
- [Documentation](#documentation)
- [Contributing](#contributing)
- [License](#license)

## Installation

### Maven

```xml
<dependency>
    <groupId>com.swfte</groupId>
    <artifactId>swfte-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle (Groovy)

```groovy
implementation 'com.swfte:swfte-sdk:1.0.0'
```

### Gradle (Kotlin)

```kotlin
implementation("com.swfte:swfte-sdk:1.0.0")
```

## Quick Start

```java
import com.swfte.sdk.SwfteClient;
import com.swfte.sdk.models.ChatRequest;
import com.swfte.sdk.models.ChatResponse;
import com.swfte.sdk.models.Message;

import java.util.List;

public class Example {
    public static void main(String[] args) {
        // Initialize the client
        SwfteClient client = SwfteClient.builder()
            .apiKey("sk-swfte-...")
            .build();

        // Create a chat completion
        ChatResponse response = client.chat().completions().create(
            ChatRequest.builder()
                .model("openai:gpt-4")  // or "anthropic:claude-3-opus", "deployed:my-model"
                .addMessage(Message.system("You are a helpful assistant."))
                .addMessage(Message.user("Hello!"))
                .build()
        );

        System.out.println(response.getChoices().get(0).getMessage().getContent());
    }
}
```

## Features

| Feature | Description |
|---------|-------------|
| **Unified API** | Access OpenAI, Anthropic, Google Gemini, and self-hosted models through one API |
| **OpenAI Compatible** | Similar interface to OpenAI SDK |
| **Streaming Support** | Real-time streaming responses using Java Streams |
| **Builder Pattern** | Fluent API for building requests |
| **Java 11+** | Works with Java 11 and above |
| **Async Support** | CompletableFuture-based async operations |
| **Automatic Retries** | Built-in retry logic with exponential backoff |
| **Thread-Safe** | Safe to use from multiple threads |

## Supported Models

### External Providers

| Provider | Models | Capabilities |
|----------|--------|--------------|
| **OpenAI** | `openai:gpt-4`, `openai:gpt-4-turbo`, `openai:gpt-3.5-turbo`, `openai:dall-e-3`, `openai:whisper-1`, `openai:tts-1` | Chat, Images, Audio, Embeddings |
| **Anthropic** | `anthropic:claude-3-opus`, `anthropic:claude-3-sonnet`, `anthropic:claude-3-haiku` | Chat |
| **Google** | `google:gemini-pro`, `google:gemini-pro-vision` | Chat, Vision |

### Self-Hosted (via RunPod)

| Model | Use Case |
|-------|----------|
| `deployed:llama-3-8b` | Text generation |
| `deployed:sdxl` | Image generation |
| `deployed:whisper-large` | Audio transcription |

## Examples

### Streaming

```java
import com.swfte.sdk.models.ChatChunk;

ChatRequest request = ChatRequest.builder()
    .model("openai:gpt-4")
    .addMessage(Message.user("Tell me a story"))
    .build();

client.chat().completions().createStream(request)
    .forEach(chunk -> {
        if (chunk.getChoices().get(0).getDelta().getContent() != null) {
            System.out.print(chunk.getChoices().get(0).getDelta().getContent());
        }
    });
```

### Async Operations

```java
import java.util.concurrent.CompletableFuture;

CompletableFuture<ChatResponse> future = client.chat().completions().createAsync(
    ChatRequest.builder()
        .model("openai:gpt-4")
        .addMessage(Message.user("Hello!"))
        .build()
);

future.thenAccept(response -> {
    System.out.println(response.getChoices().get(0).getMessage().getContent());
});
```

### Image Generation

```java
import com.swfte.sdk.models.ImageRequest;
import com.swfte.sdk.models.ImageResponse;

ImageResponse response = client.images().generate(
    ImageRequest.builder()
        .model("openai:dall-e-3")
        .prompt("A sunset over mountains in watercolor style")
        .size("1024x1024")
        .build()
);

System.out.println(response.getData().get(0).getUrl());
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

System.out.println("Embedding dimension: " + response.getData().get(0).getEmbedding().size());
```

### Audio Transcription

```java
import java.nio.file.Files;
import java.nio.file.Paths;

byte[] audioData = Files.readAllBytes(Paths.get("audio.mp3"));
TranscriptionResponse result = client.audio().transcriptions().create(
    "openai:whisper-1",
    audioData
);

System.out.println(result.getText());
```

### Text-to-Speech

```java
import java.nio.file.Files;
import java.nio.file.Paths;

byte[] audio = client.audio().speech().create(
    "openai:tts-1",
    "Hello world!",
    "nova"
);

Files.write(Paths.get("output.mp3"), audio);
```

### List Models

```java
import com.swfte.sdk.models.Model;

List<Model> models = client.models().list();
for (Model model : models) {
    System.out.println(model.getId() + " - " + model.getOwnedBy());
}
```

## Configuration

```java
SwfteClient client = SwfteClient.builder()
    .apiKey("sk-swfte-...")                        // Required
    .baseUrl("https://api.swfte.com/v1/gateway")    // Optional: custom endpoint
    .timeout(60000)                                 // Optional: timeout in ms
    .maxRetries(3)                                  // Optional: retry attempts
    .workspaceId("ws-123")                          // Optional: workspace ID
    .build();
```

### Environment Variables

| Variable | Description |
|----------|-------------|
| `SWFTE_API_KEY` | Default API key |
| `SWFTE_WORKSPACE_ID` | Default workspace ID |
| `SWFTE_BASE_URL` | Custom API base URL |

## Error Handling

```java
import com.swfte.sdk.exceptions.AuthenticationException;
import com.swfte.sdk.exceptions.RateLimitException;
import com.swfte.sdk.exceptions.ApiException;
import com.swfte.sdk.exceptions.SwfteException;

try {
    ChatResponse response = client.chat().completions().create(
        ChatRequest.builder()
            .model("openai:gpt-4")
            .addMessage(Message.user("Hello!"))
            .build()
    );
} catch (AuthenticationException e) {
    System.err.println("Invalid API key");
} catch (RateLimitException e) {
    System.err.println("Rate limit exceeded");
} catch (ApiException e) {
    System.err.println("API error: " + e.getMessage() + " (status: " + e.getStatusCode() + ")");
} catch (SwfteException e) {
    System.err.println("Error: " + e.getMessage());
}
```

## Requirements

- Java 11 or higher
- Jackson 2.15+ (included as dependency)

## Documentation

- [API Reference](https://docs.swfte.com/java-sdk)
- [Javadoc](https://javadoc.io/doc/com.swfte/swfte-sdk)
- [Migration Guide](https://docs.swfte.com/java-sdk/migration)
- [Examples](https://github.com/swfte/agents-service/tree/main/sdks/java/examples)
- [Changelog](CHANGELOG.md)

## Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

MIT License - see [LICENSE](LICENSE) file for details.

---

Built with love by the [Swfte](https://swfte.com) team.
