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
package org.corant.suites.datasource.hikari;

import static org.corant.shared.util.Assertions.shouldBeFalse;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.suites.datasource.shared.AbstractDataSourceExtension;
import org.corant.suites.datasource.shared.DataSourceConfig;
import org.eclipse.microprofile.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * corant-suites-datasource
 *
 * @author bingo 下午7:56:58
 *
 */
public class HikariCPDataSourceExtension extends AbstractDataSourceExtension {

  HikariDataSource doProduce(Instance<Object> instance, String name) throws NamingException {
    DataSourceConfig cfg = DataSourceConfig.of(instance.select(Config.class).get(), name);
    shouldBeFalse(cfg.isJta() || cfg.isXa());
    HikariConfig cfgs = new HikariConfig();
    cfgs.setJdbcUrl(cfg.getConnectionUrl());
    cfgs.setDriverClassName(cfg.getDriver().getName());
    if (cfg.getUsername() != null) {
      cfgs.setUsername(cfg.getUsername());
    }
    if (cfg.getPassword() != null) {
      cfgs.setPassword(cfg.getPassword());
    }
    cfgs.setMinimumIdle(cfg.getMinSize());
    cfgs.setMaximumPoolSize(cfg.getMaxSize());
    cfgs.setPoolName(cfg.getName());
    cfgs.setValidationTimeout(cfg.getValidationTimeout().toMillis());
    HikariDataSource datasource = new HikariDataSource(cfgs);
    registerDataSource(name, datasource);
    InitialContext jndi = instance.select(InitialContext.class).isResolvable()
        ? instance.select(InitialContext.class).get()
        : null;
    registerDataSource(name, datasource);
    registerJndi(jndi, name, datasource);
    return datasource;
  }

  void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery event) {
    if (event != null) {
      for (final String dataSourceName : getDataSourceNames()) {
        event.<DataSource>addBean().addQualifier(NamedLiteral.of(dataSourceName))
            .addTransitiveTypeClosure(HikariDataSource.class).beanClass(HikariDataSource.class)
            .scope(ApplicationScoped.class).produceWith(beans -> {
              try {
                return doProduce(beans, dataSourceName);
              } catch (NamingException e) {
                throw new CorantRuntimeException(e);
              }
            }).disposeWith((dataSource, beans) -> dataSource.close());
      }
    }
  }
}
