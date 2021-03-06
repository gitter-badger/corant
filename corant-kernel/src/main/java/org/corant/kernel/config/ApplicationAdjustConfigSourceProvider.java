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
package org.corant.kernel.config;

import static org.corant.shared.util.MapUtils.toMap;
import static org.corant.shared.util.StringUtils.asDefaultString;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import org.corant.shared.normal.Names.ConfigNames;
import org.corant.shared.normal.Priorities.ConfigPriorities;
import org.corant.shared.util.ObjectUtils;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * corant-kernel
 *
 * @author bingo 上午10:49:14
 *
 */
public class ApplicationAdjustConfigSourceProvider extends ApplicationConfigSourceProvider {

  @Override
  public Iterable<ConfigSource> getConfigSources(ClassLoader classLoader) {
    final Properties props = new Properties();
    System.getProperties().forEach((k, v) -> {
      String key = asDefaultString(k);
      String value = asDefaultString(v);
      if (key.startsWith(ConfigNames.CFG_AD_PREFIX)) {
        key = key.substring(ConfigNames.CFG_AD_PREFIX.length());
        props.put(key, value);
      }
    });
    if (!props.isEmpty()) {
      List<ConfigSource> list = new ArrayList<>();
      List<String> keys =
          props.keySet().stream().map(ObjectUtils::asString).collect(Collectors.toList());
      list.add(new AdjustConfigSource(props));
      list.forEach(cs -> logger.info(() -> String.format("Loaded adjust config source[%s] %s.",
          cs.getOrdinal(), String.join(",", keys))));
      return list;
    }
    return Collections.emptyList();
  }

  /**
   * corant-kernel
   *
   * @author bingo 下午8:56:38
   *
   */
  private static final class AdjustConfigSource implements ConfigSource {
    private final Properties props;

    /**
     * @param props
     */
    private AdjustConfigSource(Properties props) {
      this.props = props;
    }

    @Override
    public String getName() {
      return ConfigNames.CFG_AD_PREFIX;
    }

    @Override
    public int getOrdinal() {
      return ConfigPriorities.APPLICATION_ADJUST_ORDINAL;
    }

    @Override
    public Map<String, String> getProperties() {
      return toMap(props);
    }

    @Override
    public String getValue(String propertyName) {
      return props.getProperty(propertyName);
    }
  }

}
