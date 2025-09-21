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

import com.core.v1.gcp.service.CoreInboundMessageService;
import com.core.v1.gcp.utils.CoreMessageConverter;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * {@code CoreInboundMessageHandler} handles incoming Google Cloud Pub/Sub messages.
 *
 * <p>It is responsible for:
 *
 * <ul>
 *   <li>Acknowledging (ack'ing) the Pub/Sub message upon successful processing.
 *   <li>Negatively acknowledging (knacking) the Pub/Sub message if an error occurs during
 *       processing.
 * </ul>
 *
 * <p>This class adheres to Spring's {@link MessageHandler} interface for message processing.
 *
 * @see MessageHandler
 * @see CoreMessageConverter
 */
@Slf4j
@Component
@RequiredArgsConstructor
public final class CoreInboundMessageHandler implements MessageHandler {

  /** Dependency for converting message payloads. */
  private final CoreMessageConverter coreMessageConverter;

  /** Dependency for handle incoming message payloads. */
  private final CoreInboundMessageService coreInboundMessageService;

  @Override
  public void handleMessage(final Message<?> message) throws MessagingException {
    Mono.just(message)
        .publishOn(Schedulers.boundedElastic())
        .flatMap(this::processMessage)
        .subscribe();
  }

  /**
   * Processes the incoming {@link Message} from Google Cloud Pub/Sub in a reactive manner. It
   * extracts the original Pub/Sub message, deserializes its payload, processes the message using
   * {@link CoreInboundMessageService}, and then acknowledges or negatively acknowledges the message
   * based on the processing outcome.
   *
   * @param message The incoming Spring {@link Message} containing the Pub/Sub payload and headers.
   * @return A {@link Mono<Void>} that completes when the message has been processed and
   *     acknowledged/unacknowledged.
   */
  private Mono<Void> processMessage(final Message<?> message) {
    return Mono.justOrEmpty(
            message
                .getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class))
        .switchIfEmpty(
            Mono.fromRunnable(
                () ->
                    log.warn(
                        "Cannot process message because it is not an AcknowledgeablePubsubMessage: {}",
                        message)))
        .flatMap(
            originalMessage -> {
              final String messageId = originalMessage.getPubsubMessage().getMessageId();
              return Mono.fromCallable(
                      () -> {
                        final String payload =
                            new String((byte[]) message.getPayload(), StandardCharsets.UTF_8);
                        log.info("Received coupon status update message with ID: {}", messageId);
                        log.atDebug().log("Payload for message ID [{}]: {}", messageId, payload);
                        return coreInboundMessageService.handleIncomingMessages(payload);
                      })
                  .flatMap(monoResult -> monoResult)
                  .doOnSuccess(
                      processedSuccessfully -> {
                        if (processedSuccessfully) {
                          log.info(
                              "Successfully processed message with ID: {}. Acknowledging.",
                              messageId);
                          originalMessage.ack();
                        } else {
                          originalMessage.nack();
                        }
                      })
                  .doOnError(
                      e -> {
                        log.atError()
                            .log(
                                "Failed to process message with ID: {}. Message will be nack'd.",
                                messageId,
                                e);
                        originalMessage.nack();
                      })
                  .then();
            });
  }
}
