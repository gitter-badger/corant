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

import static org.corant.shared.util.Assertions.shouldBeFalse;
import java.util.Map;
import freemarker.template.TemplateMethodModelEx;

/**
 * asosat-query
 *
 * @author bingo 下午5:40:20
 *
 */
public interface DynamicQueryTplMmResolver<CP> extends TemplateMethodModelEx {

  CP getParameters();

  QueryTemplateMethodModelType getType();

  default DynamicQueryTplMmResolver<CP> injectTo(Map<String, Object> parameters) {
    if (parameters != null) {
      String tmmName = getType().name();
      shouldBeFalse(parameters.containsKey(tmmName));
      parameters.put(getType().name(), this);
    }
    return this;
  }

  public enum QueryTemplateMethodModelType {
    SP, MP, EP
  }

}
