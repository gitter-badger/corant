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
package org.corant.suites.query.sqlquery;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import org.corant.kernel.service.ConversionService;
import org.corant.suites.query.QueryRuntimeException;
import org.corant.suites.query.dynamic.template.DynamicQueryTplMmResolver;
import org.corant.suites.query.dynamic.template.FreemarkerDynamicQueryTpl;
import org.corant.suites.query.mapping.Query;
import freemarker.template.TemplateException;

/**
 * asosat-query
 *
 * @author bingo 下午7:46:22
 *
 */
public class DefaultSqlNamedQueryTpl
    extends FreemarkerDynamicQueryTpl<DefaultSqlNamedQuerier, Object[]> {

  public DefaultSqlNamedQueryTpl(Query query, ConversionService conversionService) {
    super(query, conversionService);
  }

  /**
   * Generate SQL script with placeholder, and converted the parameter to appropriate type.
   */
  @Override
  public DefaultSqlNamedQuerier doProcess(Map<String, Object> param,
      DynamicQueryTplMmResolver<Object[]> tmm) {
    try (StringWriter sw = new StringWriter()) {
      getTemplate().process(param, sw);
      return new DefaultSqlNamedQuerier(sw.toString(), tmm.getParameters(), getResultClass(),
          getFetchQueries(), getHints());
    } catch (TemplateException | IOException | NullPointerException e) {
      throw new QueryRuntimeException(e, "Freemarker process stringTemplate occurred and error");
    }
  }

  @Override
  protected DynamicQueryTplMmResolver<Object[]> getTemplateMethodModel(Map<String, Object> param) {
    return new DefaultSqlNamedQueryTplMmResolver().injectTo(param);
  }

}
