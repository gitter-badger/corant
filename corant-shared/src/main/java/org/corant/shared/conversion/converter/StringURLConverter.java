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

import static org.corant.shared.util.Empties.isEmpty;
import java.net.URL;
import java.util.Map;

/**
 * corant-shared
 *
 * @author bingo 下午5:40:35
 *
 */
public class StringURLConverter extends AbstractConverter<String, URL> {

  public StringURLConverter() {
    super();
  }

  /**
   * @param throwException
   */
  public StringURLConverter(boolean throwException) {
    super(throwException);
  }

  /**
   * @param defaultValue
   */
  public StringURLConverter(URL defaultValue) {
    super(defaultValue);
  }

  /**
   * @param defaultValue
   * @param throwException
   */
  public StringURLConverter(URL defaultValue, boolean throwException) {
    super(defaultValue, throwException);
  }

  @Override
  protected URL convert(String value, Map<String, ?> hints) throws Exception {
    if (isEmpty(value)) {
      return getDefaultValue();
    }
    return new URL(value);
  }

}
