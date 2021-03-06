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
package org.corant.asosat.ddd.domain.model;

import javax.persistence.MappedSuperclass;
import org.corant.suites.ddd.model.Value;

/**
 * corant-asosat-ddd
 *
 * @author bingo 下午1:33:25
 *
 */
@MappedSuperclass
public abstract class AbstractValueObject implements Value {

  private static final long serialVersionUID = -4414975083746116630L;

  public AbstractValueObject() {}
}
