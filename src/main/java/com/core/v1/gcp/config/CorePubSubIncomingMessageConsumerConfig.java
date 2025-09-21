/*
 * Copyright 2025 com.core
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.core.v1.gcp.config;

import com.core.v1.gcp.messaging.CoreInboundMessageHandler;
import com.core.v1.gcp.service.CoreInboundMessageService;
import com.core.v1.gcp.utils.CoreMessageConverter;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.InstantiatingExecutorProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.core.PubSubConfiguration;
import com.google.cloud.spring.pubsub.core.subscriber.PubSubSubscriberTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import com.google.cloud.spring.pubsub.support.DefaultSubscriberFactory;
import com.google.cloud.spring.pubsub.support.SubscriberFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.lang.NonNull;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * Configuration class for setting up Google Cloud Pub/Sub incoming message consumer. This class
 * defines beans for Pub/Sub project ID, credentials, executor, subscriber factory, subscriber
 * template, inbound channel adapter, and message handler. It uses Spring Integration to connect
 * Pub/Sub subscriptions to message processing logic.
 *
 * <p>The configuration ensures that incoming messages from a specified Google Cloud Pub/Sub
 * subscription are received, processed by a {@link CoreInboundMessageHandler}, and acknowledged
 * manually.
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class CorePubSubIncomingMessageConsumerConfig {

  /**
   * The {@link CoreMessageConverter} dependency injected via constructor. This converter is
   * essential for transforming incoming Pub/Sub messages into a format suitable for the
   * application's business logic within the {@link CoreInboundMessageHandler}.
   */
  private final @NonNull CoreMessageConverter coreMessageConverter;

  private final @NonNull CoreInboundMessageService coreInboundMessageService;

  /**
   * Constructor for {@code CorePubSubIncomingMessageConsumerConfig}.
   *
   * <p>Injects the {@link CoreMessageConverter} which is used by the message handler to convert
   * incoming Pub/Sub messages.
   *
   * <p>Creates a {@link GcpProjectIdProvider} bean from a property value.
   *
   * @param projectId the GCP project ID, injected from application properties.
   * @return a {@link GcpProjectIdProvider} that returns the configured project ID.
   */
  @Bean("csuPubSubProjectIdProvider")
  public GcpProjectIdProvider csuPubSubProjectIdProvider(
      /**
       * The Google Cloud Project ID, typically loaded from {@code application.properties} using the
       * key {@code spring.cloud.gcp.projectId}. This ID is crucial for identifying the GCP project
       * where Pub/Sub resources reside.
       */
      @Value("${spring.cloud.gcp.projectId}") final String projectId) {
    return () -> projectId;
  }

  /**
   * Creates a {@link CredentialsProvider} bean that provides application default credentials.
   *
   * @return a {@link CredentialsProvider} for Google Cloud authentication.
   */
  @Bean("csuPubSubCredentialsProvider")
  public CredentialsProvider csuPubSubCredentialsProvider() {
    return GoogleCredentials::getApplicationDefault;
  }

  /**
   * Creates an {@link ExecutorProvider} for the Pub/Sub subscriber. Configures a single-threaded
   * executor to ensure sequential processing of messages, which can be important for maintaining
   * message order or simplifying concurrency management.
   *
   * @return an {@link ExecutorProvider} with a single-threaded executor.
   */
  @Bean
  public ExecutorProvider pubSubExecutorProvider() {
    return InstantiatingExecutorProvider.newBuilder().setExecutorThreadCount(1).build();
  }

  /**
   * Creates a {@link SubscriberFactory} for building Pub/Sub subscribers.
   *
   * @param pubSubExecutorProvider the executor provider for the subscriber threads.
   * @param gcpProjectIdProvider the GCP project ID provider.
   * @param credentialsProvider the provider for GCP credentials.
   * @return a fully configured {@link DefaultSubscriberFactory} instance.
   */
  @Bean("csuPubSubSubscriberFactory")
  public SubscriberFactory csuPubSubSubscriberFactory(
      @Qualifier("pubSubExecutorProvider") final ExecutorProvider pubSubExecutorProvider,
      @Qualifier("csuPubSubProjectIdProvider") final GcpProjectIdProvider gcpProjectIdProvider,
      @Qualifier("csuPubSubCredentialsProvider") final CredentialsProvider credentialsProvider) {

    // The PubSubConfiguration object is used by the factory for additional settings.
    // The initialize() method is deprecated and effectively a no-op as the project ID
    // is now primarily derived from the GcpProjectIdProvider.
    final PubSubConfiguration pubSubConfiguration = new PubSubConfiguration();
    pubSubConfiguration.initialize(
        gcpProjectIdProvider.getProjectId()); // Deprecated, but kept for compatibility
    final DefaultSubscriberFactory defaultSubscriberFactory =
        new DefaultSubscriberFactory(gcpProjectIdProvider, pubSubConfiguration);
    defaultSubscriberFactory.setExecutorProvider(pubSubExecutorProvider);
    defaultSubscriberFactory.setCredentialsProvider(credentialsProvider); // NOSONAR
    defaultSubscriberFactory.setParallelPullCount(1);

    return defaultSubscriberFactory;
  }

  /**
   * Creates a {@link PubSubSubscriberTemplate} for programmatic subscription interactions.
   *
   * @param subscriberFactory the {@link SubscriberFactory} used to create subscriber clients.
   * @return a configured {@link PubSubSubscriberTemplate}.
   */
  @Bean("csuPubSubSubscriberTemplate")
  public PubSubSubscriberTemplate csuPubSubSubscriberTemplate(
      @Qualifier("csuPubSubSubscriberFactory") final SubscriberFactory subscriberFactory) {
    return new PubSubSubscriberTemplate(subscriberFactory);
  }

  /**
   * Creates the input {@link MessageChannel} for incoming Pub/Sub messages.
   *
   * @return a new {@link DirectChannel} instance that acts as the entry point for Pub/Sub messages
   *     into the Spring Integration flow.
   */
  @Bean("csuPubSubInputChannel")
  public MessageChannel csuPubSubInputChannel() {
    return new DirectChannel();
  }

  /**
   * Creates the {@link PubSubInboundChannelAdapter} to connect a Pub/Sub subscription to a {@link
   * MessageChannel}.
   *
   * @param messageChannel the {@link MessageChannel} to which messages received from Pub/Sub will
   *     be sent. This is typically {@code csuPubSubInputChannel}.
   * @param pubSubSubscriberTemplate the {@link PubSubSubscriberTemplate} used by the adapter to
   *     interact with the Pub/Sub subscription.
   * @param subscriptionId the ID of the Google Cloud Pub/Sub subscription from which messages will
   *     be pulled. This value is injected from application properties.
   * @return a fully configured {@link PubSubInboundChannelAdapter} instance.
   */
  @Bean("csuPubSubInboundChannelAdapter")
  public PubSubInboundChannelAdapter csuPubSubInboundChannelAdapter(
      @Qualifier("csuPubSubInputChannel") final MessageChannel messageChannel,
      @Qualifier("csuPubSubSubscriberTemplate")
          final PubSubSubscriberTemplate pubSubSubscriberTemplate,
      @Value("${spring.cloud.gcp.pubsub.subscriptionId}") final String subscriptionId) {

    final PubSubInboundChannelAdapter adapter =
        new PubSubInboundChannelAdapter(pubSubSubscriberTemplate, subscriptionId);
    adapter.setOutputChannel(messageChannel);
    adapter.setAckMode(AckMode.MANUAL);

    return adapter;
  }

  /**
   * Creates the {@link MessageHandler} bean that processes messages from the input channel. This
   * handler is activated by messages on the {@code csuPubSubInputChannel}.
   *
   * @return a {@link CoreInboundMessageHandler} instance, responsible for processing the incoming
   *     Pub/Sub messages. It uses the {@code coreMessageConverter} to transform message payloads.
   */
  @Bean
  @ServiceActivator(inputChannel = "csuPubSubInputChannel")
  public MessageHandler csuPubSubStatusUpdateMessageHandler() {
    return new CoreInboundMessageHandler(coreMessageConverter, coreInboundMessageService);
  }
}
