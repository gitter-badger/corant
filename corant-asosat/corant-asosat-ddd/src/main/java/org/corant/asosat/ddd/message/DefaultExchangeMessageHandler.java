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
package org.corant.asosat.ddd.message;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.corant.Corant;
import org.corant.suites.ddd.annotation.qualifier.MQ.MQLiteral;
import org.corant.suites.ddd.annotation.stereotype.InfrastructureServices;
import org.corant.suites.ddd.message.Message;
import org.corant.suites.ddd.message.Message.ExchangedMessage;
import org.corant.suites.ddd.message.MessageService.MessageConvertor;
import org.corant.suites.ddd.message.MessageService.MessageStroage;

/**
 * @author bingo 上午11:38:09
 *
 */
@ApplicationScoped
@InfrastructureServices
public class DefaultExchangeMessageHandler implements ExchangedMessageHandler {

  @Inject
  protected MessageStroage stroage;

  @Inject
  protected MessageConvertor convertor;

  public DefaultExchangeMessageHandler() {}

  @Override
  @Transactional
  public void handle(@ObservesAsync ExchangedMessage message) {
    if (null == message || null == message.queueName()) {
      return;
    }
    MQLiteral qualifier = MQLiteral.of(message.queueName());
    Message persistMessage = convertor.from(message);
    if (persistMessage != null) {
      stroage.store(persistMessage);
      Corant.fireAsyncEvent(persistMessage, qualifier);
    } else {
      Corant.fireAsyncEvent(new DefaultTransientMessage(message), qualifier);
    }
  }

}
