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
package org.corant.asosat.exp.application;

import static org.corant.shared.util.ObjectUtils.isEquals;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import javax.transaction.Transactional;
import org.corant.asosat.exp.domain.TestDefaultGenericAggregate;
import org.corant.shared.exception.CorantRuntimeException;

/**
 * corant-asosat-exp
 *
 * @author bingo 上午11:44:27
 *
 */
@ApplicationScoped
@Transactional
public class TestApplicationService2 {

  @Inject
  @PersistenceContext(unitName = "dmmsPu")
  EntityManager em;

  @Inject
  @Named("dmmsRwDs")
  DataSource ds;

  public void testEntityManager(String param) {
    TestDefaultGenericAggregate obj = new TestDefaultGenericAggregate();
    obj.setName(param);
    em.persist(obj);
    em.flush();
    if (isEquals(param, "0")) {
      testEntityManager1();
    }
  }

  public void testEntityManager1() {
    TestDefaultGenericAggregate obj = new TestDefaultGenericAggregate();
    obj.setName("jimmy");
    em.persist(obj);
    throw new CorantRuntimeException("xxxxxxxxxxxxx");
  }
}