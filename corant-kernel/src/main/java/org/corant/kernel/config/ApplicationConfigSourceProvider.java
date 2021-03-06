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

import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.StringUtils.defaultBlank;
import static org.corant.shared.util.StringUtils.defaultString;
import static org.corant.shared.util.StringUtils.isBlank;
import static org.corant.shared.util.StringUtils.matchGlob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import org.corant.shared.exception.CorantRuntimeException;
import org.corant.shared.normal.Names.ConfigNames;
import org.corant.shared.normal.Priorities.ConfigPriorities;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

/**
 * corant-kernel
 *
 * @author bingo 下午6:34:41
 *
 */
public class ApplicationConfigSourceProvider implements ConfigSourceProvider {

  static Logger logger = Logger.getLogger(ApplicationConfigSourceProvider.class.getName());
  static String appBaseName = "application";
  static String[] appExtName = {".yaml", ".yml", ".properties", ".json", ".xml"};
  static String metaInf = "META-INF/";
  static String sysPro = System.getProperty(ConfigNames.CFG_LOCATION_KEY);
  static String sysEnv = System.getenv(ConfigNames.CFG_LOCATION_KEY);
  static String fileDir = defaultString(defaultBlank(sysPro, sysEnv));
  static String cfgUrlExPattern = System.getProperty(ConfigNames.CFG_LOCATION_EXCLUDE_PATTERN);

  static String[] classPaths =
      Arrays.stream(appExtName).map(e -> metaInf + appBaseName + e).toArray(String[]::new);

  static String[] filePaths = isBlank(fileDir) ? new String[0]
      : Arrays.stream(appExtName).map(e -> fileDir + File.separator + appBaseName + e)
          .toArray(String[]::new);
  static Predicate<URL> filter = (u) -> {
    if (isBlank(cfgUrlExPattern)) {
      return true;
    } else {
      return !matchGlob(u.toExternalForm(), true, cfgUrlExPattern);
    }
  };

  @Override
  public Iterable<ConfigSource> getConfigSources(ClassLoader classLoader) {
    List<ConfigSource> list = new ArrayList<>();
    try {
      list.addAll(ConfigSourceLoader.load(ConfigPriorities.APPLICATION_ORDINAL, filter, filePaths));
      if (isEmpty(filePaths)) {
        list.addAll(ConfigSourceLoader.load(classLoader, ConfigPriorities.APPLICATION_ORDINAL,
            filter, classPaths));
      }
      list.forEach(cs -> logger.info(
          () -> String.format("Loaded config source[%s] %s.", cs.getOrdinal(), cs.getName())));
    } catch (IOException e) {
      throw new CorantRuntimeException(e);
    }
    return list;
  }

}
