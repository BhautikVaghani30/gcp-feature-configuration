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

package com.core.v1.gcp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * A utility component for converting JSON strings to Java objects.
 *
 * <p>This class uses a pre-configured, thread-safe {@link ObjectReader} for efficient and
 * consistent deserialization.
 */
@Component
@Slf4j
public class CoreMessageConverter {

  private final ObjectReader objectReader;

  /**
   * Constructs the MessageConverter and initializes a reusable {@link ObjectReader}.
   *
   * @param objectMapper The Spring-configured {@link ObjectMapper} to use for creating the reader.
   */
  public CoreMessageConverter(ObjectMapper objectMapper) {
    this.objectReader =
        objectMapper
            .reader()
            .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  /**
   * Converts a JSON payload string into an object of the specified type.
   *
   * @param payload The JSON string to convert.
   * @param type The class of the target object.
   * @param <T> The type of the target object.
   * @return The converted object.
   * @throws JsonProcessingException if the payload cannot be converted to the specified type.
   */
  public <T> T convert(String payload, Class<T> type) throws JsonProcessingException {
    return objectReader.forType(type).readValue(payload);
  }
}
