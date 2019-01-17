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

import static org.corant.shared.util.CollectionUtils.isEmpty;
import static org.corant.shared.util.ObjectUtils.asString;
import static org.corant.shared.util.ObjectUtils.shouldNotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.corant.suites.elastic.Elastic6Constants;
import org.corant.suites.elastic.metadata.ElasticMapping;
import org.corant.suites.elastic.model.ElasticDocument;
import org.corant.suites.elastic.model.ElasticVersionedDocument;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;

/**
 * corant-suites-elastic
 *
 * @author bingo 下午4:10:36
 *
 */
public interface ElasticDocumentService {

  default int bulkIndex(List<ElasticDocument> docList, boolean flush) {
    return bulkIndex(docList, flush, this::resolveMapping);
  }

  int bulkIndex(List<ElasticDocument> docList, boolean flush,
      Function<Class<? extends ElasticDocument>, ElasticMapping<?>> mapper);

  default boolean delete(String indexName, String id) {
    return delete(indexName, id, false);
  }

  default boolean delete(String indexName, String id, boolean flush) {
    return deleteByQuery(indexName,
        QueryBuilders.idsQuery().types(Elastic6Constants.TYP_NME).addIds(id), flush) > 0;
  }

  long deleteByQuery(String indexName, QueryBuilder qb, boolean flush);

  default <T> T get(Class<T> cls, QueryBuilder qb) {
    ElasticMapping<T> mapping = shouldNotNull(resolveMapping(cls));
    return get(cls, mapping.getIndex().getName(), qb);
  }

  default <T> T get(Class<T> cls, String id) {
    return get(cls, QueryBuilders.idsQuery().types(Elastic6Constants.TYP_NME).addIds(id));
  }

  default <T> T get(Class<T> cls, String indexName, QueryBuilder qb) {
    List<T> list = select(cls, qb, null, 1);
    if (!isEmpty(list)) {
      return list.get(0);
    } else {
      return null;
    }
  }

  default boolean index(ElasticDocument document) {
    shouldNotNull(document);
    return index(document, document.getEsParentId(), false);
  }

  @SuppressWarnings({"rawtypes"})
  default boolean index(ElasticDocument document, String parentId, boolean flush) {
    ElasticMapping mapping = shouldNotNull(resolveMapping(document.getClass()));
    if (document instanceof ElasticVersionedDocument) {
      ElasticVersionedDocument verDoc = ElasticVersionedDocument.class.cast(document);
      return index(mapping.getIndex().getName(), asString(document.getEsId()), parentId,
          mapping.toMap(verDoc), flush, verDoc.getVn(), mapping.getVersionType());
    } else {
      return index(mapping.getIndex().getName(), asString(document.getEsId()), parentId,
          mapping.toMap(document), flush, 0, null);
    }
  }

  boolean index(String indexName, String id, String parentId, Map<?, ?> obj, boolean flush,
      long version, VersionType versionType);

  <T> ElasticMapping<T> resolveMapping(Class<T> docCls);

  default <T> List<T> select(Class<T> cls, QueryBuilder qb) {
    return select(cls, qb, null, Elastic6Constants.DFLT_SELECT_SIZE);
  }

  default <T> List<T> select(Class<T> cls, QueryBuilder qb, SortBuilder<?> sb, int size) {
    ElasticMapping<T> mapping = resolveMapping(cls);
    List<Map<String, Object>> rawResults = select(mapping.getIndex().getName(), qb, sb, size);
    if (!isEmpty(rawResults)) {
      return rawResults.stream().map(mapping::fromMap).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  List<Map<String, Object>> select(String indexName, QueryBuilder qb, SortBuilder<?> sb, int size,
      String... pops);

}