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
package org.corant.suites.elastic;

import static org.corant.shared.util.ObjectUtils.shouldNotNull;
import java.util.function.BiConsumer;
import org.corant.Corant;
import org.corant.kernel.logging.LoggerFactory;
import org.corant.suites.elastic.metadata.ElasticIndexing;
import org.corant.suites.elastic.metadata.resolver.AbstractElasticIndexingResolver;

/**
 * corant-suites-elastic
 *
 * @author bingo 上午10:53:48
 *
 */
public class ElasticSchemaUtils {

  public static void stdout(String clusterName, BiConsumer<String, ElasticIndexing> out) {
    prepare();
    Corant corant = new Corant(ElasticSchemaUtils.class, "-disable_boost_line");
    corant.start();
    ElasticExtension extension = Corant.cdi().select(ElasticExtension.class).get();
    TempElasticIndexingResolver indexingResolver =
        Corant.wrapUnmanageableBean(new TempElasticIndexingResolver()).get();
    indexingResolver.config = shouldNotNull(extension.getConfig(clusterName));
    indexingResolver.initialize();
    indexingResolver.getIndexings().forEach(out::accept);
  }

  static void prepare() {
    LoggerFactory.disableLogger();
    System.setProperty("corant.temp.webserver.auto-start", "false");
  }

  public static class TempElasticIndexingResolver extends AbstractElasticIndexingResolver {

    ElasticConfig config;

    public TempElasticIndexingResolver() {}

    @Override
    protected ElasticConfig getConfig() {
      return config;
    }

    @Override
    protected void initialize() {
      super.initialize();
    }

    @Override
    protected void onPostConstruct() {}

  }
}