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
package org.corant.asosat.exp.data;

import static org.corant.shared.util.MapUtils.asMap;
import static org.corant.shared.util.ObjectUtils.asString;
import java.util.ArrayList;
import java.util.List;
import org.corant.suites.elastic.metadata.annotation.EsEmbeddable;
import org.corant.suites.elastic.metadata.annotation.EsKeyword;
import org.corant.suites.elastic.metadata.annotation.EsText;

/**
 * corant-asosat-exp
 *
 * @author bingo 上午10:03:22
 *
 */
@EsEmbeddable
public class TestElasticEmbeddable {

  @EsKeyword(ignore_above = 256)
  private String emKeyword;

  @EsText
  private String emText;

  /**
   * @param emKeyword
   * @param emText
   */
  public TestElasticEmbeddable(Object emKeyword, Object emText) {
    super();
    this.emKeyword = asString(emKeyword);
    this.emText = asString(emText);
  }

  public static List<TestElasticEmbeddable> of(String... args) {
    List<TestElasticEmbeddable> list = new ArrayList<>();
    asMap((Object[]) args).forEach((k, v) -> list.add(new TestElasticEmbeddable(k, v)));
    return list;
  }

  /**
   *
   * @return the emKeyword
   */
  public String getEmKeyword() {
    return emKeyword;
  }

  /**
   *
   * @return the emText
   */
  public String getEmText() {
    return emText;
  }

  /**
   *
   * @param emKeyword the emKeyword to set
   */
  public void setEmKeyword(String emKeyword) {
    this.emKeyword = emKeyword;
  }

  /**
   *
   * @param emText the emText to set
   */
  public void setEmText(String emText) {
    this.emText = emText;
  }

}