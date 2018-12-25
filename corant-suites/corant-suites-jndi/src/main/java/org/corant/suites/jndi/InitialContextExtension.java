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
package org.corant.suites.jndi;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.NamingManager;
import org.corant.shared.normal.Names.JndiNames;

/**
 * corant-suites-jndi
 *
 * @author bingo 下午7:19:21
 *
 */
public class InitialContextExtension implements Extension {

  public static final String[] DFLT_SUB_CTX = new String[] {JndiNames.JNDI_ROOT_NME,
      JndiNames.JNDI_COMP_NME, JndiNames.JNDI_APPS_NME, JndiNames.JNDI_DATS_NME};
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private boolean useCorantContext = false;
  private InitialContext context;

  public InitialContext getContext() {
    return context;
  }

  public boolean isUseCorantContext() {
    return useCorantContext;
  }

  void beforeBeanDiscovery(@Observes @Priority(Integer.MAX_VALUE) final BeforeBeanDiscovery event) {
    try {
      if (!NamingManager.hasInitialContextFactoryBuilder()) {
        NamingManager.setInitialContextFactoryBuilder(e -> NamingContext::new);
        useCorantContext = true;
      }
    } catch (IllegalStateException | NamingException e) {
      logger.log(Level.WARNING, null, e);
    }


    try {
      context = new InitialContext();
      for (String subCtx : DFLT_SUB_CTX) {
        context.createSubcontext(subCtx);
      }
      if (useCorantContext) {
        logger.info(() -> String.format(
            "Initial namingcontext that build from corant, create subcontexts with %s.",
            String.join(" ", DFLT_SUB_CTX)));
      } else {
        logger.info(() -> String.format("Initial namingcontext, create subcontexts with %s.",
            String.join(" ", DFLT_SUB_CTX)));
      }
    } catch (NamingException e) {
      logger.log(Level.WARNING, null, e);
    }
  }

}