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
package org.corant.kernel.service;

import java.util.Collection;
import org.corant.shared.conversion.Converter;
import org.corant.shared.conversion.ConverterType;

/**
 *
 * @author bingo 上午12:16:42
 *
 */
public interface ConversionService {

  <C extends Collection<T>, T> C convert(final Object value, final Class<C> collectionClazz,
      final Class<T> clazz, Object... hints);

  <T> T convert(final Object value, final Class<T> clazz, Object... hints);

  void deregister(ConverterType<?, ?> converterType);

  <S, T> Converter<S, T> getConverter(final Class<S> sourceType, final Class<T> targetType);

  <T> void register(Converter<?, ?> converter);

}
