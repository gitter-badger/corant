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
package org.corant.shared.conversion.converter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import org.corant.shared.conversion.ConverterHints;

/**
 * corant-shared
 *
 * @author bingo 下午5:35:33
 *
 */
public class NumberZonedDateTimeConverter extends AbstractConverter<Number, ZonedDateTime> {

  public NumberZonedDateTimeConverter() {
    super();
  }

  /**
   * @param throwException
   */
  public NumberZonedDateTimeConverter(boolean throwException) {
    super(throwException);
  }

  /**
   * @param defaultValue
   */
  public NumberZonedDateTimeConverter(ZonedDateTime defaultValue) {
    super(defaultValue);
  }

  /**
   * @param defaultValue
   * @param throwException
   */
  public NumberZonedDateTimeConverter(ZonedDateTime defaultValue, boolean throwException) {
    super(defaultValue, throwException);
  }

  @Override
  public boolean isPossibleDistortion() {
    return true;
  }

  @Override
  protected ZonedDateTime convert(Number value, Map<String, ?> hints) throws Exception {
    ZoneId zoneId = ZoneId.systemDefault();
    Object hintZoneId = ConverterHints.getHint(hints, ConverterHints.CVT_ZONE_ID_KEY);
    if (hintZoneId instanceof ZoneId) {
      zoneId = (ZoneId) hintZoneId;
    } else if (hintZoneId instanceof String) {
      zoneId = ZoneId.of(hintZoneId.toString());
    }
    return ZonedDateTime.ofInstant(Instant.ofEpochMilli(value.longValue()), zoneId);
  }
}
