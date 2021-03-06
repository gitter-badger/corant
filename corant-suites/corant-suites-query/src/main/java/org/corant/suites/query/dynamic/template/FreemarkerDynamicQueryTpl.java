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
package org.corant.suites.query.dynamic.template;

import static org.corant.shared.util.Empties.isEmpty;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.corant.kernel.service.ConversionService;
import org.corant.suites.query.QueryRuntimeException;
import org.corant.suites.query.mapping.FetchQuery;
import org.corant.suites.query.mapping.Query;
import org.corant.suites.query.mapping.QueryHint;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * asosat-query
 *
 * @author bingo 下午3:50:40
 *
 */
public abstract class FreemarkerDynamicQueryTpl<T, P> implements DynamicQueryTpl<T> {

  static final Configuration FM_CFG = new Configuration(Configuration.VERSION_2_3_28);

  protected final String queryName;
  protected final Template template;
  protected final Map<String, Class<?>> paramConvertSchema;
  protected final long cachedTimestemp;
  protected final Class<?> resultClass;
  protected final List<FetchQuery> fetchQueries;
  protected final ConversionService conversionService;
  protected final List<QueryHint> hints = new ArrayList<>();

  public FreemarkerDynamicQueryTpl(Query query, ConversionService conversionService) {
    if (query == null || conversionService == null) {
      throw new QueryRuntimeException(
          "Can not initialize freemarker query template from null query param!");
    }
    this.conversionService = conversionService;
    this.fetchQueries = Collections.unmodifiableList(query.getFetchQueries());
    this.queryName = query.getName();
    this.resultClass = query.getResultClass() == null ? Map.class : query.getResultClass();
    this.paramConvertSchema = Collections.unmodifiableMap(query.getParamMappings().entrySet()
        .stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getType())));
    if (!isEmpty(query.getHints())) {
      query.getHints().forEach(hints::add);
    }
    this.cachedTimestemp = Instant.now().toEpochMilli();

    try {
      this.template = new Template(this.queryName, query.getScript(), FM_CFG);
    } catch (IOException e) {
      throw new QueryRuntimeException(e);
    }
  }

  /**
   * It is not enabled yet
   */
  @Override
  public long getCachedTimestemp() {
    return this.cachedTimestemp;
  }

  /**
   * The fetch queries in this query, if there are not exist then return empty.
   */
  @Override
  public List<FetchQuery> getFetchQueries() {
    return this.fetchQueries;
  }

  /**
   * @return the hints
   */
  public List<QueryHint> getHints() {
    return hints;
  }

  /**
   * The parameter name and to converted type.
   */
  @Override
  public Map<String, Class<?>> getParamConvertSchema() {
    return this.paramConvertSchema;
  }

  /**
   * The query name (with version suffix)
   */
  @Override
  public String getQueryName() {
    return this.queryName;
  }

  /**
   * The query result type, may be Map or Pojo.
   */
  @Override
  public Class<?> getResultClass() {
    return this.resultClass;
  }

  /**
   * The dynamic query script template to use
   */
  @Override
  public Template getTemplate() {
    return this.template;
  }

  /**
   * Use parameters to process query script.
   */
  @Override
  public T process(Map<String, Object> param) {
    Map<String, Object> useParam = convertParameter(param);// convert parameter
    DynamicQueryTplMmResolver<P> qtmm = getTemplateMethodModel(useParam); // inject method object
    this.preProcess(useParam);
    T result = this.doProcess(useParam, qtmm);
    this.postProcess(result, qtmm);
    return result;
  }

  /**
   * Convert parameter to use.
   *
   * @param param
   * @return convertParameter
   */
  protected Map<String, Object> convertParameter(Map<String, Object> param) {
    Map<String, Object> convertedParam = new HashMap<>();
    if (param != null) {
      convertedParam.putAll(param);
      getParamConvertSchema().forEach((pn, pc) -> {
        if (convertedParam.containsKey(pn)) {
          convertedParam.put(pn, conversionService.convert(param.get(pn), pc));
        }
      });
    }
    return convertedParam;
  }

  /**
   * Process delegation
   *
   * @param param
   * @param tmm
   * @return doProcess
   */
  protected abstract T doProcess(Map<String, Object> param, DynamicQueryTplMmResolver<P> tmm);

  protected abstract DynamicQueryTplMmResolver<P> getTemplateMethodModel(
      Map<String, Object> useParam);

  protected void postProcess(T result, DynamicQueryTplMmResolver<P> qtmm) {}

  protected void preProcess(Map<String, Object> param) {}
}
