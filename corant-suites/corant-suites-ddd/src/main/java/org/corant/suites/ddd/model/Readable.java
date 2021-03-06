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
package org.corant.suites.ddd.model;

import java.util.Locale;
import java.util.function.Function;

/**
 * @author bingo 下午12:05:42
 *
 */
public interface Readable<T, R> {

  default String toHumanReader(Locale locale) {
    return toString();
  }

  @SuppressWarnings("unchecked")
  default R toJsonReader(Function<T, R> provider) {
    return provider.apply((T) this);
  }

}
