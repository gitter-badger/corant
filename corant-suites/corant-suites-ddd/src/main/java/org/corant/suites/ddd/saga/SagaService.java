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
package org.corant.suites.ddd.saga;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Stream;
import org.corant.suites.ddd.message.Message;
import org.corant.suites.ddd.model.Aggregate.AggregateIdentifier;

/**
 * corant-asosat-ddd
 *
 * @author bingo 下午3:48:30
 *
 */
public interface SagaService {
  Stream<SagaManager> getManagers(Annotation... annotations);

  void persist(Saga saga);

  void trigger(Message message);

  public static interface SagaManager {

    Saga begin(Message message);

    void end(Message message);

    Saga get(String queue, String trackingToken);

    List<Saga> select(AggregateIdentifier aggregateIdentifier);
  }
}
