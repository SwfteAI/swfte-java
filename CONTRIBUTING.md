# Contributing to Swfte Java SDK

Thank you for your interest in contributing to the Swfte Java SDK! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Testing](#testing)
- [Code Style](#code-style)
- [Pull Request Process](#pull-request-process)
- [Reporting Issues](#reporting-issues)

## Contributor License Agreement

Before your first pull request can be merged, you must sign the [Swfte CLA](https://cla.swfte.com). This ensures that Swfte, Inc. retains the ability to license the project under the current or future terms.

By submitting a contribution, you certify that:

- You have the right to submit it under the MIT License
- You agree to the terms of the Swfte CLA
- Your contribution does not contain third-party code incompatible with the MIT License

## Code of Conduct

Please read and follow our [Code of Conduct](CODE_OF_CONDUCT.md) to keep our community approachable and respectable.

## Getting Started

1. Fork the repository on GitHub
2. Clone your fork locally
3. Set up the development environment
4. Create a branch for your changes

## Development Setup

### Prerequisites

- Java 11 or higher (we recommend Java 17 or 21)
- Maven 3.8+

### Installation

```bash
# Clone your fork
git clone https://github.com/YOUR_USERNAME/swfte-java.git
cd swfte-java

# Build the project
mvn clean install

# Skip tests during initial build
mvn clean install -DskipTests
```

### Environment Variables

For running integration tests:

```bash
export SWFTE_API_KEY="your-api-key"
export SWFTE_BASE_URL="https://api.swfte.com"
```

## Making Changes

1. Create a new branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following our code style guidelines

3. Add or update tests as needed

4. Update JavaDoc if you're changing public APIs

5. Commit your changes with clear, descriptive messages

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=AgentsTest

# Run integration tests only
mvn test -Dgroups=integration
```

### Test Structure

```
src/test/java/com/swfte/sdk/
├── unit/
│   ├── SwfteClientTest.java
│   ├── resources/
│   │   ├── AgentsTest.java
│   │   └── ...
│   └── models/
│       └── ...
└── integration/
    └── ApiIntegrationTest.java
```

### Writing Tests

- Use JUnit 5 for test framework
- Use Mockito for mocking dependencies
- Use WireMock for mocking HTTP responses
- Each new feature should have corresponding tests
- Aim for at least 80% code coverage

Example test:

```java
@ExtendWith(MockitoExtension.class)
class AgentsTest {
    @Mock
    private SwfteClient client;

    @Test
    void shouldCreateAgent() {
        // Given
        Agent expected = Agent.builder()
            .id("agent-123")
            .agentName("Test Agent")
            .build();

        // When / Then
        assertNotNull(expected.getId());
    }
}
```

## Code Style

### Formatting

We follow Google Java Style Guide with some modifications:

```bash
# Check style (if Checkstyle is configured)
mvn checkstyle:check

# Format code (if Spotless is configured)
mvn spotless:apply
```

### Style Guidelines

- Use 4 spaces for indentation (no tabs)
- Maximum line length: 120 characters
- Use descriptive variable and method names
- Add JavaDoc for all public classes and methods
- Use `final` for method parameters and local variables where appropriate
- Prefer immutable objects

### JavaDoc Guidelines

```java
/**
 * Creates a new agent with the specified configuration.
 *
 * @param name the name of the agent (required)
 * @param config the agent configuration (optional)
 * @return the created agent
 * @throws ApiException if the API returns an error
 */
public Agent createAgent(String name, AgentConfig config) {
    // implementation
}
```

### Builder Pattern

Use Lombok `@Builder` for model classes:

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent {
    private String id;
    private String agentName;
    private String model;
}
```

## Pull Request Process

1. **Update your branch**:
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Run all checks locally**:
   ```bash
   mvn clean verify
   ```

3. **Push and create PR**

4. **PR Requirements**:
   - Clear description of changes
   - Link to related issues
   - All CI checks passing
   - Code review approval
   - No merge conflicts

5. **After merge**: Delete your branch

## Commit Message Guidelines

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add streaming support for chat completions
fix: handle connection timeout properly
docs: update README with Maven installation
test: add unit tests for Workflows resource
chore: upgrade Jackson to 2.16.0
refactor: extract HTTP client to separate class
```

## Building

```bash
# Build JAR
mvn clean package

# Build with sources and JavaDoc
mvn clean package -P release

# Generate site with documentation
mvn site
```

## Dependency Management

- Keep dependencies minimal
- Use provided scope for optional dependencies
- Update dependencies regularly for security

## Reporting Issues

### Bug Reports

Please include:

- Java version (output of `java -version`)
- SDK version
- Operating system
- Maven/Gradle version
- Steps to reproduce
- Expected vs actual behavior
- Error messages and stack traces

### Feature Requests

Please describe:

- The problem you're trying to solve
- Your proposed solution
- Any alternatives you've considered

## Questions?

- Open a [GitHub Discussion](https://github.com/swfte/swfte-java/discussions)
- Join our [Discord community](https://discord.gg/swfte)
- Email us at sdk@swfte.com

Thank you for contributing!
