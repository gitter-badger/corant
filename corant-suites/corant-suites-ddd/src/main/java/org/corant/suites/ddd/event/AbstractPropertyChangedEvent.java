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
package org.corant.suites.ddd.event;

import java.time.Instant;
import org.corant.suites.ddd.annotation.stereotype.Events;
import org.corant.suites.ddd.model.Aggregate;

/**
 * corant-suites-ddd
 *
 * @author bingo 下午5:57:36
 *
 */
@Events
public abstract class AbstractPropertyChangedEvent<T extends Aggregate, PT> extends AbstractEvent {

  private static final long serialVersionUID = 6311499831097921960L;

  private final PT oldValue;

  private final PT newValue;

  public AbstractPropertyChangedEvent(T source, PT oldValue, PT newValue) {
    super(source);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  public PT getNewValue() {
    return this.newValue;
  }

  @Override
  public Instant getOccurredTime() {
    return super.getOccurredTime();
  }

  public PT getOldValue() {
    return this.oldValue;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getSource() {
    Object source = super.getSource();
    return source == null ? null : (T) source;
  }
}
