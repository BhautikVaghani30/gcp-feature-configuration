# GCP Feature Configuration Service

This service is designed to configure and manage Google Cloud features, with a primary focus on message handling with
Google Cloud Pub/Sub.

## Features

* **Google Cloud Pub/Sub Integration:**
    * Consumes messages from a Pub/Sub subscription.
    * Publishes messages to a Pub/Sub topic.
* **Reactive Processing:** Built with Spring WebFlux for a non-blocking, reactive architecture.
* **Spring Integration:** Uses Spring Integration for message routing and handling.
* **Configuration Management:** Centralized configuration for GCP services.

## Technologies Used

* **Java 21**
* **Spring Boot 3.3.1**
* **Spring Cloud**
* **Spring Integration**
* **Google Cloud Pub/Sub**
* **Project Reactor**
* **Gradle**
* **Lombok**
* **MapStruct**

## Getting Started

### Prerequisites

* Java 21 or later
* Gradle 8.x
* Google Cloud SDK installed and authenticated
* A Google Cloud project with the Pub/Sub API enabled

### Configuration

1. **`application.properties`:**

   Update the `src/main/resources/application.properties` file with your GCP project details:

   ```properties
   # Standard GCP properties
   spring.cloud.gcp.pubsub.subscriptionId=<YOUR_SUBSCRIPTION_ID>
   spring.cloud.gcp.pubsub.topicId=<YOUR_TOPIC_ID>

   # Your Google Cloud Project ID
   spring.cloud.gcp.projectId=<YOUR_PROJECT_ID>
   ```

2. **Authentication:**

   Ensure your local environment is authenticated with Google Cloud. You can do this by running:

   ```bash
   gcloud auth application-default login
   ```

### Building the Project

To build the project, run the following command from the root directory:

```bash
./gradlew build
```

### Running the Application

You can run the application using the following Gradle command:

```bash
./gradlew bootRun
```

## Project Structure

* `src/main/java/com/core/v1/gcp`:
    * `config`: Spring configuration classes for Pub/Sub integration.
    * `messaging`: Message handler implementations.
    * `service`: Business logic and services.
    * `utils`: Utility classes, such as message converters.
* `src/main/resources`:
    * `application.properties`: Application configuration.

## Pub/Sub Messaging

* **Inbound:** Messages are consumed from the subscription specified by `spring.cloud.gcp.pubsub.subscriptionId`. The
  `CoreInboundMessageHandler` processes these messages.
* **Outbound:** Messages can be published to the topic specified by `spring.cloud.gcp.pubsub.topicId` using the
  `CoreOutboundMessageService`.
