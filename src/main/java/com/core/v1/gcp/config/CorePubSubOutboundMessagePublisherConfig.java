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

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.spring.core.GcpProjectIdProvider;
import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import com.google.cloud.spring.pubsub.support.CachingPublisherFactory;
import com.google.cloud.spring.pubsub.support.DefaultPublisherFactory;
import com.google.cloud.spring.pubsub.support.PublisherFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up Google Cloud Pub/Sub outbound message publishers. This class
 * defines beans for project ID providers, credential providers, publisher factories, and Pub/Sub
 * publisher templates, specifically for outbound messages. It leverages Spring Cloud GCP's Pub/Sub
 * integration to facilitate message publishing.
 */
@Slf4j
@Configuration
public class CorePubSubOutboundMessagePublisherConfig {

  /**
   * Creates a {@link GcpProjectIdProvider} bean that supplies the Google Cloud Project ID for
   * Pub/Sub operations. The project ID is retrieved from the Spring environment property {@code
   * spring.cloud.gcp.projectId}.
   *
   * @param projectId The Google Cloud Project ID, injected from application properties.
   * @return A {@link GcpProjectIdProvider} instance providing the configured project ID.
   */
  @Bean("usPubSubPublisherProjectIdProvider")
  public GcpProjectIdProvider usPubSubPublisherProjectIdProvider(
      @Value("${spring.cloud.gcp.projectId}") final String projectId) {
    return () -> projectId;
  }

  /**
   * Creates a {@link CredentialsProvider} bean that supplies Google Cloud credentials for Pub/Sub
   * operations. This method uses the application default credentials, which are typically sourced
   * from the environment (e.g., GOOGLE_APPLICATION_CREDENTIALS environment variable or GCE service
   * account).
   *
   * @return A {@link CredentialsProvider} instance configured to use application default
   *     credentials.
   */
  @Bean("usPubSubPublisherCredentialsProvider")
  public CredentialsProvider usPubSubPublisherCredentialsProvider() {
    return GoogleCredentials::getApplicationDefault;
  }

  /**
   * Creates a {@link DefaultPublisherFactory} bean responsible for creating {@link
   * com.google.cloud.pubsub.v1.Publisher} instances. This factory is configured with the provided
   * project ID and credentials.
   *
   * @param projectIdProvider The {@link GcpProjectIdProvider} for obtaining the Google Cloud
   *     Project ID.
   * @param credentialsProvider The {@link CredentialsProvider} for obtaining Google Cloud
   *     credentials.
   * @return A {@link DefaultPublisherFactory} instance configured for Pub/Sub publishing.
   */
  @Bean("usPubSubPublisherFactory")
  public DefaultPublisherFactory usPubSubPublisherFactory(
      @Qualifier("usPubSubPublisherProjectIdProvider") final GcpProjectIdProvider projectIdProvider,
      @Qualifier("usPubSubPublisherCredentialsProvider")
          final CredentialsProvider credentialsProvider) {

    final DefaultPublisherFactory defaultPublisherFactory =
        new DefaultPublisherFactory(projectIdProvider);
    defaultPublisherFactory.setCredentialsProvider(credentialsProvider);
    return defaultPublisherFactory;
  }

  /**
   * Creates a {@link PubSubPublisherTemplate} bean for publishing messages to Google Cloud Pub/Sub.
   * This template uses the {@code usPubSubPublisherFactory} to create underlying publishers. It
   * provides a convenient way to interact with Pub/Sub topics.
   *
   * @param publisherFactory The {@link PublisherFactory} used to create Pub/Sub publishers. This is
   *     typically {@code usPubSubPublisherFactory}.
   * @return A {@link PubSubPublisherTemplate} instance for publishing messages.
   */
  @Bean("usPubSubPublisherTemplate")
  public PubSubPublisherTemplate usPubSubPublisherTemplate(
      @Qualifier("usPubSubPublisherFactory") final PublisherFactory publisherFactory) {

    log.info("usPubSubPublisherTemplate is initialized");
    return new PubSubPublisherTemplate(publisherFactory);
  }

  /**
   * Creates a {@link CachingPublisherFactory} bean. This factory wraps a {@link
   * DefaultPublisherFactory} and provides caching capabilities for {@link
   * com.google.cloud.pubsub.v1.Publisher} instances, improving performance by reusing publishers
   * for the same topic.
   *
   * @param publisherFactory The underlying {@link PublisherFactory} to be cached. This is typically
   *     {@code usPubSubPublisherFactory}.
   * @return A {@link CachingPublisherFactory} instance.
   */
  @Bean("usCachingPubSubPublisherTemplate")
  public CachingPublisherFactory usCachingPubSubPublisherTemplate(
      @Qualifier("usPubSubPublisherFactory") final PublisherFactory publisherFactory) {
    return new CachingPublisherFactory(publisherFactory);
  }

  /**
   * Creates the primary {@link PubSubPublisherTemplate} bean that applications will typically
   * inject and use for publishing messages. This template is configured to use the {@code
   * usCachingPubSubPublisherTemplate}, which provides caching for Pub/Sub publishers.
   *
   * @param publisherFactory The {@link PublisherFactory} to be used by this template. This is
   *     typically {@code usCachingPubSubPublisherTemplate}.
   * @return A {@link PubSubPublisherTemplate} instance for publishing messages with caching.
   */
  @Bean("pubSubPublisherTemplate")
  public PubSubPublisherTemplate pubSubPublisherTemplate(
      @Qualifier("usCachingPubSubPublisherTemplate") final PublisherFactory publisherFactory) {
    return new PubSubPublisherTemplate(publisherFactory);
  }
}
