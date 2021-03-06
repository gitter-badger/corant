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
package org.corant.suites.jpa.shared;

import static org.corant.shared.util.Assertions.shouldBeFalse;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.StringUtils.defaultString;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;
import org.corant.kernel.util.CdiUtils;
import org.corant.shared.normal.Names.PersistenceNames;
import org.corant.suites.jpa.shared.inject.EntityManagerBean;
import org.corant.suites.jpa.shared.inject.EntityManagerFactoryBean;
import org.corant.suites.jpa.shared.inject.ExtendedPersistenceContextType;
import org.corant.suites.jpa.shared.inject.PersistenceContextInjectionPoint;
import org.corant.suites.jpa.shared.inject.TransactionPersistenceContextType;
import org.corant.suites.jpa.shared.metadata.PersistenceContextMetaData;
import org.corant.suites.jpa.shared.metadata.PersistenceUnitInfoMetaData;
import org.corant.suites.jpa.shared.metadata.PersistenceUnitMetaData;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * corant-suites-jpa-shared
 *
 * @author bingo 上午11:32:56
 *
 */
public class JpaExtension implements Extension {

  static final Set<PersistenceUnitMetaData> PUMDS =
      Collections.newSetFromMap(new ConcurrentHashMap<PersistenceUnitMetaData, Boolean>());

  static final Set<PersistenceContextMetaData> PCMDS =
      Collections.newSetFromMap(new ConcurrentHashMap<PersistenceContextMetaData, Boolean>());

  protected final Map<String, PersistenceUnitInfoMetaData> persistenceUnitInfoMetaDatas =
      new HashMap<>();

  protected Logger logger = Logger.getLogger(getClass().getName());

  public PersistenceUnitInfoMetaData getPersistenceUnitInfoMetaData(String name) {
    return persistenceUnitInfoMetaDatas.get(name);
  }

  public Map<String, PersistenceUnitInfoMetaData> getPersistenceUnitInfoMetaDatas() {
    return Collections.unmodifiableMap(persistenceUnitInfoMetaDatas);
  }

  protected String resolveUnitName(String name, String unitName) {
    String usePuName = defaultString(unitName, PersistenceNames.PU_DFLT_NME);
    usePuName = isEmpty(name) ? usePuName : usePuName + "." + name;
    return usePuName;
  }

  void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery abd, final BeanManager beanManager) {
    PUMDS.forEach(pumd -> {
      abd.addBean(new EntityManagerFactoryBean(beanManager, pumd));
    });
    PCMDS.forEach(pcmd -> {
      abd.addBean(new EntityManagerBean(beanManager, pcmd));
    });
  }

  void onBeforeBeanDiscovery(@Observes final BeforeBeanDiscovery event) {
    JpaConfig.from(ConfigProvider.getConfig()).getMetaDatas()
        .forEach(persistenceUnitInfoMetaDatas::put);
    logger.info(() -> String.format("Find jpa persistence unit [%s]",
        String.join(", ", persistenceUnitInfoMetaDatas.keySet())));
  }

  void onProcessInjectionPoint(@Observes ProcessInjectionPoint<?, ?> pip, BeanManager beanManager) {
    final InjectionPoint ip = pip.getInjectionPoint();
    PersistenceUnit pu = CdiUtils.getAnnotated(ip).getAnnotation(PersistenceUnit.class);
    if (pu != null) {
      PUMDS.add(PersistenceUnitMetaData.of(pu));
    }
    PersistenceContext pc = CdiUtils.getAnnotated(ip).getAnnotation(PersistenceContext.class);
    if (pc != null) {
      PersistenceContextMetaData pcmd = PersistenceContextMetaData.of(pc);
      if (pcmd.getType() != PersistenceContextType.TRANSACTION) {
        shouldBeFalse(ip.getBean().getScope().equals(ApplicationScoped.class));
        pip.setInjectionPoint(new PersistenceContextInjectionPoint(ip,
            ExtendedPersistenceContextType.INST, Any.Literal.INSTANCE));
      } else {
        pip.setInjectionPoint(new PersistenceContextInjectionPoint(ip,
            TransactionPersistenceContextType.INST, Any.Literal.INSTANCE));
      }
      PUMDS.add(pcmd.getUnit());
      PCMDS.add(pcmd);
    }
  }

}
