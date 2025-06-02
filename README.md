# Notification Service

This is a notification service built with Spring Boot using hexagonal architecture (also known as ports and adapters).

## 🚀 Features

- Hexagonal architecture for better separation of concerns
- RabbitMQ integration for asynchronous messaging
- PostgreSQL database for persistence
- Redis for caching
- OpenTelemetry for observability
- Datadog integration for metrics
- Testcontainers for integration testing

## 🛠️ Technologies

- Java 21
- Spring Boot 3.2.3
- PostgreSQL
- Redis
- RabbitMQ
- Testcontainers

## 📋 Prerequisites

- Java 21 or higher
- Docker and Docker Compose
- Maven

## 🔧 Installation

1. Clone the repository:
```bash
git clone [REPOSITORY_URL]
cd notification-service
```

2. Start required services using Docker Compose:
```bash
docker-compose up -d
```

3. Build the project:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

## 🏗️ Project Structure

The project follows a hexagonal architecture with the following layers:

- `domain`: Contains business logic and domain entities
- `application`: Implements application use cases
- `infrastructure`: Contains adapters for databases, messaging, etc.
- `api`: Exposes REST endpoints and handles HTTP requests

## 🧪 Testing

The project includes unit and integration tests using Testcontainers:

```bash
./mvnw test
```
