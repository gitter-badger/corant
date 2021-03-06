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
package org.corant.suites.ddd.message;

import java.io.Serializable;
import java.time.Instant;

/**
 * @author bingo 上午10:41:55
 */
public interface Message extends Serializable {

  static int compare(Message m1, Message m2) {
    int occuredTimeCmpr = compareOccurredTime(m1, m2);
    return occuredTimeCmpr == 0 ? compareSequenceNumber(m1, m2) : occuredTimeCmpr;
  }

  static int compareOccurredTime(Message m1, Message m2) {
    return m1.occurredTime().compareTo(m2.occurredTime());
  }

  static int compareSequenceNumber(Message m1, Message m2) {
    return Long.compare(m1.sequenceNumber(), m2.sequenceNumber());
  }

  MessageMetadata getMetadata();

  Object getPayload();

  default Instant occurredTime() {
    return getMetadata() == null ? null : getMetadata().getOccurredTime();
  }

  default String queueName() {
    return getMetadata() == null ? null : getMetadata().getQueue().toString();
  }

  default long sequenceNumber() {
    return getMetadata() == null ? -1L : getMetadata().getSequenceNumber();
  }

  default Object sourceObject() {
    return getMetadata() == null ? null : getMetadata().getSource();
  }

  default String trackingToken() {
    return getMetadata() == null ? null : getMetadata().getTrackingToken();
  }

  public interface ExchangedMessage extends Message {

    MessageIdentifier getOriginalMessage();

  }


  public static interface MessageHandling extends Serializable {

    Instant getHandledTime();

    Object getHandler();

    Object getMessageId();

    Object getQueue();

    boolean isSuccess();
  }

  public static interface MessageIdentifier {

    Serializable getId();

    Object getQueue();

    Serializable getType();

  }

  public static interface MessageMetadata extends Serializable {

    Object getAttributes();

    Instant getOccurredTime();

    Object getQueue();

    long getSequenceNumber();

    Object getSource();

    default String getTrackingToken() {
      return null;
    }

    void resetSequenceNumber(long sequenceNumber);
  }

  public interface MessageQueues {

    static final String DFLT = "default";

  }

}
