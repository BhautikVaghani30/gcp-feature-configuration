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

package com.core.v1.gcp.messaging;

import com.google.cloud.spring.pubsub.core.publisher.PubSubPublisherTemplate;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for publishing outbound messages to a Google Cloud Pub/Sub topic. This class
 * provides a method to publish messages to a pre-configured Pub/Sub topic using a {@link
 * PubSubPublisherTemplate}.
 */
@Slf4j
@Configuration
public class CoreOutboundMessageHandler {

  private final PubSubPublisherTemplate template;
  private final String topic;

  public CoreOutboundMessageHandler(
      @Qualifier("pubSubPublisherTemplate") PubSubPublisherTemplate template,
      @Value("${spring.cloud.gcp.pubsub.topicId}") String topic) {
    this.template = template;
    this.topic = topic;
  }

  /**
   * This method is used to post message to PUBSUB topic.
   *
   * @param payload The message payload to be published.
   * @return CompletableFuture<String>
   */
  public CompletableFuture<String> publishMessage(String payload) {
    log.atDebug().log("Publishing ODS message to {} ", this.topic);
    return this.template.publish(this.topic, payload);
  }
}
