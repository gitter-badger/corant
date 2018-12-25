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
package org.corant.shared.conversion.converter.factory;

import static org.corant.shared.util.StringUtils.splitNotBlank;
import java.util.Map;
import org.corant.shared.conversion.ConversionException;
import org.corant.shared.conversion.Converter;
import org.corant.shared.conversion.ConverterFactory;

/**
 * corant-shared
 *
 * @author bingo 上午10:15:06
 *
 */
public class ObjectEnumConverterFactory implements ConverterFactory<Object, Enum<?>> {

  @Override
  public Converter<Object, Enum<?>> create(Class<Enum<?>> targetClass, Enum<?> defaultValue,
      boolean useNullValueIfErr, boolean useDefaultValueIfErr) {
    return (t, h) -> {
      try {
        return convert(t, targetClass, h);
      } catch (Exception e) {
        if (useNullValueIfErr) {
          return null;
        } else if (useDefaultValueIfErr) {
          return defaultValue;
        }
        throw new ConversionException(e);
      }
    };
  }

  @Override
  public boolean isSupportTargetClass(Class<?> targetClass) {
    return Enum.class.isAssignableFrom(targetClass);
  }

  protected <T extends Enum<?>> T convert(Object value, Class<T> targetClass, Map<String, ?> hints)
      throws Exception {
    if (value instanceof Enum<?> && value.getClass().isAssignableFrom(targetClass)) {
      return targetClass.cast(value);
    } else if (value instanceof Integer) {
      return targetClass.getEnumConstants()[Integer.class.cast(value)];
    } else {
      String[] values = splitNotBlank(value.toString(), ".", true);
      String name = values[values.length - 1];
      for (T t : targetClass.getEnumConstants()) {
        if (t.name().equalsIgnoreCase(name)) {
          return t;
        }
      }
      throw new ConversionException("Can not convert %s -> %s", value.getClass(), targetClass);
    }

  }


}