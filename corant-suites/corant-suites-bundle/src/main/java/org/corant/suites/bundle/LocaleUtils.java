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
package org.corant.suites.bundle;

import java.util.Locale;

/**
 * corant-suites-bundle
 *
 * @author bingo 下午10:26:42
 *
 */
public class LocaleUtils {

  /**
   * IETF RFC 1766 tag separator
   */
  public final static char IETF_SEPARATOR = '-';
  public final static String EMPTY_STRING = "";

  public static Locale langToLocale(String lang) {
    return langToLocale(lang, IETF_SEPARATOR);
  }

  public static Locale langToLocale(String lang, char separator) {
    if (lang == null || lang.equals(EMPTY_STRING)) { // not specified => getDefault
      return Locale.getDefault();
    }
    String language = EMPTY_STRING;
    String country = EMPTY_STRING;
    String variant = EMPTY_STRING;

    int i1 = lang.indexOf(separator);
    if (i1 < 0) {
      language = lang;
    } else {
      language = lang.substring(0, i1);
      ++i1;
      int i2 = lang.indexOf(separator, i1);
      if (i2 < 0) {
        country = lang.substring(i1);
      } else {
        country = lang.substring(i1, i2);
        variant = lang.substring(i2 + 1);
      }
    }

    if (language.length() == 2) {
      language = language.toLowerCase(Locale.ENGLISH);
    } else {
      language = EMPTY_STRING;
    }

    if (country.length() == 2) {
      country = country.toUpperCase(Locale.ENGLISH);
    } else {
      country = EMPTY_STRING;
    }

    if (variant.length() > 0 && (language.length() == 2 || country.length() == 2)) {
      variant = variant.toUpperCase(Locale.ENGLISH);
    } else {
      variant = EMPTY_STRING;
    }

    return new Locale(language, country, variant);
  }
}
