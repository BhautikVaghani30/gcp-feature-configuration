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

package com.core.v1.gcp.service;

import com.core.v1.gcp.messaging.CoreOutboundMessageHandler;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Service class for processing and publishing core messages. This class acts as an intermediary
 * between the application logic and the GCP Pub/Sub outbound messaging configuration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoreOutboundMessageService {

  /** Dependency on {@link CoreOutboundMessageHandler} for publishing messages to GCP Pub/Sub. */
  @Lazy private final CoreOutboundMessageHandler coreOutboundMessageHandler;

  /**
   * Processes a given payload by publishing it using the configured outbound message service.
   *
   * @param payload The message payload to be published.
   * @return A {@link CompletableFuture} that will complete with the message ID once the message is
   *     successfully published.
   */
  public CompletableFuture<String> processMessage(String payload) {
    return coreOutboundMessageHandler.publishMessage("payload");
  }
}
