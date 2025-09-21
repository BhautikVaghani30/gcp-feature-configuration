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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service implementation for handling inbound messages. This class processes incoming messages,
 * typically from a messaging queue or stream, and logs their reception. It is designed to be a
 * reactive component, returning a {@link Mono} to indicate the completion of the message handling
 * process.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoreInboundMessageServiceImpl implements CoreInboundMessageService {

  /**
   * Handles incoming messages in a reactive functional way. This method logs the received payload
   * and returns a {@link Mono} that emits {@code true} upon successful processing.
   *
   * @param payload The message payload as a {@link String}.
   * @return A {@link Mono} emitting {@code true} if the message was handled successfully.
   */
  @Override
  public Mono<Boolean> handleIncomingMessages(final String payload) {
    return Mono.fromCallable(
        () -> {
          log.atDebug().log(
              "CoreInboundMessageServiceImpl : handleIncomingMessages :: messages received : {} ",
              payload);
          return true;
        });
  }
}
