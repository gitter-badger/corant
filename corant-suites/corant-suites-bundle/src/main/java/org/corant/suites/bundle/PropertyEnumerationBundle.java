/*
 * Copyright (c) 2013-2018. BIN.CHEN
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.corant.suites.bundle;

import static org.corant.shared.util.ClassUtils.tryAsClass;
import static org.corant.shared.util.CollectionUtils.asSet;
import static org.corant.shared.util.StringUtils.split;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.inject.Inject;
import org.corant.shared.util.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * @author bingo 上午10:30:07
 *
 */
@ApplicationScoped
@SuppressWarnings("rawtypes")
public class PropertyEnumerationBundle implements EnumerationBundle {

  @Inject
  Logger logger;

  final Map<Locale, EnumLiteralsObject> holder = new ConcurrentHashMap<>();

  private volatile boolean init = false;

  @Inject
  @Any
  @ConfigProperty(name = "bundle.enum.source.path.regex",
      defaultValue = ".*enum([A-Za-z0-9_-]*)\\.properties$")
  String pathRegex;

  @Inject
  @Any
  @ConfigProperty(name = "bundle.packages", defaultValue = "META-INF/")
  String packages;

  @Inject
  @Any
  @ConfigProperty(name = "bundle.enum.source.load.way", defaultValue = "false")
  volatile boolean lazyLoad = false;

  public PropertyEnumerationBundle() {}

  @SuppressWarnings("unchecked")
  @Override
  public List<Class<Enum>> getAllEnumClass() {
    return new ArrayList(holder.values().stream().flatMap(e -> e.classLiteral.keySet().stream())
        .collect(Collectors.toSet()));
  }

  @Override
  public String getEnumClassLiteral(Class<?> enumClass, Locale locale) {
    return holder.get(locale) == null ? null : holder.get(locale).getLiteral(enumClass);
  }

  @SuppressWarnings("unchecked")
  @Override
  public String getEnumItemLiteral(Enum enumVal, Locale locale) {
    load();
    return this.getEnumItemLiterals((Class<Enum>) enumVal.getDeclaringClass(), locale).get(enumVal);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Enum> Map<T, String> getEnumItemLiterals(Class<T> enumClass, Locale locale) {
    load();
    Map<Enum, String> lLiterals =
        holder.get(locale) == null ? null : holder.get(locale).getLiterals(enumClass);
    return lLiterals == null ? null : new LinkedHashMap(lLiterals);
  }

  public synchronized void reload() {
    init = false;
    load();
  }

  @SuppressWarnings("unchecked")
  protected void load() {
    if (!init) {
      synchronized (this) {
        if (!init) {
          try {
            destroy();
            Set<String> pkgs = asSet(split(packages, ";"));
            final Pattern filter = Pattern.compile(pathRegex, Pattern.CASE_INSENSITIVE);
            pkgs.stream().filter(StringUtils::isNotBlank).forEach(pkg -> {
              PropertyResourceBundle
                  .getBundles(pkg, (r) -> filter.matcher(r.getResourceName()).find())
                  .forEach((s, res) -> {
                    logger.info(() -> String.format(
                        "Find enumeration resource, the path is %s, use pattern [%s]", s,
                        pathRegex));
                    Locale locale = res.getLocale();
                    EnumLiteralsObject obj =
                        holder.computeIfAbsent(locale, (k) -> new EnumLiteralsObject());
                    res.dump().forEach((k, v) -> {
                      int i = k.lastIndexOf(".");
                      String enumClsName = k.substring(0, i), enumItemKey = null;
                      Class enumCls = null;
                      try {
                        enumCls = Class.forName(enumClsName);
                        enumItemKey = k.substring(i + 1);
                      } catch (ClassNotFoundException e) {
                        enumCls = tryAsClass(k);
                        if (enumCls != null && Enum.class.isAssignableFrom(enumCls)) {
                          obj.putEnumClass(enumCls, v);
                        } else {
                          throw new RuntimeException("enum class " + s + " error");
                        }
                      }
                      if (enumItemKey != null) {
                        obj.putEnum(Enum.valueOf(enumCls, enumItemKey), v);
                      }
                    });
                  });
            });
            // TODO validate
          } finally {
            init = true;
          }
        }
      }
    }
  }

  @PreDestroy
  synchronized void destroy() {
    holder.forEach((k, v) -> {
      v.classLiteral.clear();
      v.enumLiterals.clear();
    });
    holder.clear();
  }

  @PostConstruct
  synchronized void init() {
    if (!lazyLoad) {
      load();
    }
  }

  static class EnumLiteralsObject {

    private final Map<Class<Enum>, String> classLiteral = new ConcurrentHashMap<>();
    private final Map<Class<Enum>, Map<Enum, String>> enumLiterals = new ConcurrentHashMap<>();

    public String getLiteral(Class clz) {
      return classLiteral.get(clz);
    }

    public Map<Enum, String> getLiterals(Class clz) {
      return enumLiterals.get(clz);
    }

    @SuppressWarnings("unchecked")
    public void putEnum(Enum e, String literal) {
      Class declaringClass = e.getDeclaringClass();
      if (!enumLiterals.containsKey(declaringClass)) {
        enumLiterals.put(declaringClass,
            new TreeMap<Enum, String>((Enum o1, Enum o2) -> o1.ordinal() - o2.ordinal()));
      }
      Map<Enum, String> map = enumLiterals.get(declaringClass);
      if (map.put(e, literal) != null) {
        throw new RuntimeException("enum " + e.getClass() + ", value " + e + " is duplicate");
      }
    }

    @SuppressWarnings("unchecked")
    public void putEnumClass(Class clz, String literal) {
      if (classLiteral.put(clz, literal) != null) {
        throw new RuntimeException("enum " + clz + ", is duplicate");
      }
    }
  }
}