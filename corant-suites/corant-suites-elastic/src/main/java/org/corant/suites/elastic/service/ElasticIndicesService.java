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
package org.corant.suites.elastic.service;

import static org.corant.shared.util.ObjectUtils.shouldNotNull;
import java.util.Map;
import org.corant.suites.elastic.metadata.ElasticMapping;

/**
 * corant-suites-elastic
 *
 * @author bingo 下午4:08:34
 *
 */
public interface ElasticIndicesService {

  default boolean create(ElasticMapping<?> mapping) {
    shouldNotNull(mapping);
    return create(mapping.getIndex().getName(), mapping.getIndex().getSetting().getSetting(),
        mapping.getSchema());
  }

  boolean create(String indexName, Map<String, Object> setting, Map<String, Object> schema);

  boolean delete(String indexName);

  boolean isExist(String... indexName);
}