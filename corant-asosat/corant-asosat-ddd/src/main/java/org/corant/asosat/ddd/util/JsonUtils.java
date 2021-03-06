/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.asosat.ddd.util;

import static org.corant.shared.util.MapUtils.asMap;
import static org.corant.shared.util.ObjectUtils.defaultObject;
import static org.corant.shared.util.StringUtils.isNotBlank;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.corant.Corant;
import org.corant.kernel.exception.GeneralRuntimeException;
import org.corant.suites.bundle.EnumerationBundle;
import org.corant.suites.bundle.GlobalMessageCodes;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;
import com.fasterxml.jackson.databind.ser.std.SqlDateSerializer;

/**
 * corant-asosat-ddd
 *
 * @author bingo 下午2:40:27
 *
 */
public class JsonUtils {
  final static Long BROWSER_SAFE_LONG = 9007199254740991L;
  final static BigInteger BROWSER_SAFE_BIGINTEGER = BigInteger.valueOf(BROWSER_SAFE_LONG);
  final static ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.registerModule(new SimpleModule().addSerializer(new SqlDateSerializer()));
    objectMapper.getSerializerProvider().setNullKeySerializer(NullSerializer.instance);
    objectMapper.enable(Feature.ALLOW_COMMENTS);
    objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
    objectMapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
    objectMapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
    objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
  }

  private JsonUtils() {}

  public static void clearEnumSerials() {
    EnumJsonSerializer.CACHES.clear();
  }

  /**
   * @return The ObjectMapper clone that use in this application
   */
  public static ObjectMapper copyMapper() {
    return objectMapper.copy();
  }

  /**
   * @return The ObjectMapper clone that use in this application for java script application
   */
  public static ObjectMapper copyMapperForJs() {
    ObjectMapper copy = copyMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(new BigIntegerJsonSerializer());
    module.addSerializer(new LongJsonSerializer());
    module.addSerializer(new EnumJsonSerializer());
    copy.registerModule(module);
    return copy;
  }

  /**
   * Convert input json string to Map
   *
   * @param cmd The json string
   * @return Map
   */
  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> fromJsonStr(String cmd) {
    return fromJsonStr(cmd, Map.class);
  }

  /**
   * Convert input string to Parameterized type object.
   *
   * @param cmd
   * @param parametrized
   * @param parameterClasses
   * @return Object
   */
  @SafeVarargs
  public static <C, E> C fromJsonStr(String cmd, Class<C> parametrized,
      Class<E>... parameterClasses) {
    if (!isNotBlank(cmd)) {
      return null;
    }
    try {
      return objectMapper.readValue(cmd,
          objectMapper.getTypeFactory().constructParametricType(parametrized, parameterClasses));
    } catch (IOException e) {
      throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, cmd);
    }
  }

  /**
   * Convert input string to Map.
   *
   * @param cmd
   * @param keyCls
   * @param valueCls
   * @return Map
   */
  public static <K, V> Map<K, V> fromJsonStr(String cmd, Class<K> keyCls, Class<V> valueCls) {
    if (!isNotBlank(cmd)) {
      return null;
    }
    try {
      return objectMapper.readValue(cmd,
          objectMapper.getTypeFactory().constructParametricType(Map.class, keyCls, valueCls));
    } catch (IOException e) {
      throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, cmd);
    }
  }

  /**
   * Convert input string to type object.
   *
   * @param cmd
   * @param clazz
   * @return
   */
  public static <T> T fromJsonStr(String cmd, Class<T> clazz) {
    if (isNotBlank(cmd)) {
      try {
        return objectMapper.readValue(cmd, clazz);
      } catch (IOException e) {
        e.printStackTrace();
        throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, cmd,
            clazz.getClass().getName());
      }
    }
    return null;
  }

  public static ObjectMapper referenceMapper() {
    return objectMapper;
  }

  /**
   * Convert object to json string
   *
   * @param obj
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonGenerationException
   */
  public static String toJsonStr(Object obj) {
    return toJsonStr(obj, false);
  }

  /**
   * Convert object to json string
   *
   * @param obj
   * @param pretty
   * @return toJsonStr
   */
  public static String toJsonStr(Object obj, boolean pretty) {
    if (obj != null) {
      try {
        if (pretty) {
          return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } else {
          return objectMapper.writeValueAsString(obj);
        }
      } catch (JsonProcessingException e) {
        throw new GeneralRuntimeException(e.getCause(), GlobalMessageCodes.ERR_OBJ_SEL, obj);
      }
    }
    return null;
  }

  static final class BigIntegerJsonSerializer extends JsonSerializer<BigInteger> {
    @Override
    public Class<BigInteger> handledType() {
      return BigInteger.class;
    }

    @Override
    public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
      if (value.compareTo(BROWSER_SAFE_BIGINTEGER) > 0) {
        gen.writeString(value.toString());
      } else {
        gen.writeNumber(value);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  static final class EnumJsonSerializer extends JsonSerializer<Enum> {

    static final Map<Enum, Map<String, Object>> CACHES = new ConcurrentHashMap<>();

    static volatile EnumerationBundle bundle;

    @Override
    public Class<Enum> handledType() {
      return Enum.class;
    }

    @Override
    public void serialize(Enum value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(resolveEnumLiteral(value));
    }

    EnumerationBundle resolveBundle() {
      if (bundle == null) {
        synchronized (this) {
          if (bundle == null && Corant.instance().select(EnumerationBundle.class).isResolvable()) {
            bundle = Corant.instance().select(EnumerationBundle.class).get();
          } else {
            bundle = new EnumJsonSerializerBundle();
          }
        }
      }
      return bundle;
    }

    Map<String, Object> resolveEnumLiteral(Enum value) {
      return CACHES.computeIfAbsent(value, (v) -> {
        String literal = resolveBundle().getEnumItemLiteral(value, Locale.getDefault());
        return asMap("name", value.name(), "literal", defaultObject(literal, value.name()), "class",
            value.getDeclaringClass().getName(), "ordinal", value.ordinal());
      });
    }
  }

  @SuppressWarnings("rawtypes")
  static final class EnumJsonSerializerBundle implements EnumerationBundle {
    @Override
    public String getEnumItemLiteral(Enum enumVal, Locale locale) {
      return enumVal.name();
    }
  }

  static final class LongJsonSerializer extends JsonSerializer<Long> {
    @Override
    public Class<Long> handledType() {
      return Long.class;
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
      if (value.compareTo(BROWSER_SAFE_LONG) > 0) {
        gen.writeString(value.toString());
      } else {
        gen.writeNumber(value);
      }
    }
  }
}
