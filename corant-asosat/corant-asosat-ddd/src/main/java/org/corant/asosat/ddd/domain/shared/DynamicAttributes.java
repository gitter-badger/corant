/*
 * Copyright (c) 2013-2018, Bingo.Chen (finesoft@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.asosat.ddd.domain.shared;

import static org.corant.shared.util.MapUtils.asMap;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.shared.util.MapUtils.WrappedMap;

/**
 * corant-asosat-ddd
 *
 * @author bingo 下午1:36:42
 *
 */
public interface DynamicAttributes {

  public enum AttributeType {
    ENUM, STRING, BOOLEAN, NUMBERIC, TEMPORAL, LOCALE, CURRENCY, TIME_ZONE, REFERENCE;
  }

  @Converter
  public static class AttributeTypeJpaConverter
      implements AttributeConverter<AttributeType, String> {
    @Override
    public String convertToDatabaseColumn(AttributeType attribute) {
      return attribute.name();
    }

    @Override
    public AttributeType convertToEntityAttribute(String dbData) {
      return AttributeType.valueOf(dbData);
    }
  }

  public static class DynamicAttributeMap implements WrappedMap<String, Object>, Serializable {

    private static final long serialVersionUID = 6020146368094520321L;

    private final Map<String, Object> map = new LinkedHashMap<>();

    public DynamicAttributeMap() {}

    public DynamicAttributeMap(DynamicAttributeMap other) {
      if (other != null) {
        putAll(other);
      }
    }

    public DynamicAttributeMap(Map<String, Object> map) {
      if (map != null) {
        putAll(map);
      }
    }

    public static DynamicAttributeMap of(Object... objects) {
      return new DynamicAttributeMap(asMap(objects));
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      DynamicAttributeMap other = (DynamicAttributeMap) obj;
      if (map == null) {
        if (other.map != null) {
          return false;
        }
      } else if (!map.equals(other.map)) {
        return false;
      }
      return true;
    }

    @Override
    public DynamicAttributeMap getSubset(String key) {
      Object obj = unwrap().get(key);
      if (obj == null) {
        return null;
      } else if (obj instanceof DynamicAttributeMap) {
        return (DynamicAttributeMap) obj;
      } else {
        throw new CorantRuntimeException("Can't get subset from key %s", key);
      }
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (map == null ? 0 : map.hashCode());
      return result;
    }

    public DynamicAttributeMap unmodifiable() {
      return new DynamicAttributeMap() {

        private static final long serialVersionUID = 4947260095982487700L;

        final Map<String, Object> unmodifiable = Collections.unmodifiableMap(map);

        @Override
        public Map<String, Object> unwrap() {
          return unmodifiable;
        }
      };
    }

    @Override
    public Map<String, Object> unwrap() {
      return map;
    }

  }

  public interface DynamicNamedAttribute {

    String getName();

    AttributeType getType();

    <T> T getValue();

  }
}
