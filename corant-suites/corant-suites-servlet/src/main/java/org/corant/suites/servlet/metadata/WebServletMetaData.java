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
package org.corant.suites.servlet.metadata;

import static org.corant.shared.util.Preconditions.requireNotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Servlet;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.annotation.WebServlet;

/**
 * corant-suites-servlet
 *
 * @author bingo 上午10:10:29
 *
 */
public class WebServletMetaData {

  private String name;
  private String[] value = new String[0];
  private String[] urlPatterns = new String[0];
  private int loadOnStartup = -1;
  private WebInitParamMetaData[] initParams;
  private boolean asyncSupported;
  private String smallIcon;
  private String largeIcon;
  private String description;
  private String displayName;
  private Class<? extends Servlet> clazz;
  private ServletSecurityMetaData security;



  /**
   * @param name
   * @param value
   * @param urlPatterns
   * @param loadOnStartup
   * @param initParams
   * @param asyncSupported
   * @param smallIcon
   * @param largeIcon
   * @param description
   * @param displayName
   * @param clazz
   */
  public WebServletMetaData(String name, Collection<String> value, Collection<String> urlPatterns,
      int loadOnStartup, Collection<WebInitParamMetaData> initParams, boolean asyncSupported,
      String smallIcon, String largeIcon, String description, String displayName,
      Class<? extends Servlet> clazz) {
    super();
    this.name = name;
    if (value != null) {
      this.value = value.toArray(new String[0]);
    }
    if (urlPatterns != null) {
      this.urlPatterns = urlPatterns.toArray(new String[0]);
    }
    this.loadOnStartup = loadOnStartup;
    if (initParams != null) {
      this.initParams = initParams.toArray(new WebInitParamMetaData[0]);
    }
    this.asyncSupported = asyncSupported;
    this.smallIcon = smallIcon;
    this.largeIcon = largeIcon;
    this.description = description;
    this.displayName = displayName;
    this.clazz = clazz;
  }

  public WebServletMetaData(WebServlet anno, Class<? extends Servlet> clazz) {
    if (anno != null) {
      name = anno.name();
      value = anno.value();
      urlPatterns = anno.urlPatterns();
      loadOnStartup = anno.loadOnStartup();
      initParams = WebInitParamMetaData.of(anno.initParams());
      asyncSupported = anno.asyncSupported();
      smallIcon = anno.smallIcon();
      largeIcon = anno.largeIcon();
      description = anno.description();
      displayName = anno.displayName();
      this.clazz = clazz;
    }
  }

  public WebServletMetaData(WebServlet anno, ServletSecurity secAnno,
      Class<? extends Servlet> clazz) {
    this(anno, clazz);
    if (secAnno != null) {
      setSecurity(new ServletSecurityMetaData(secAnno, clazz));
    }
  }

  protected WebServletMetaData() {}


  /**
   *
   * @return the clazz
   */
  public Class<? extends Servlet> getClazz() {
    return clazz;
  }

  /**
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   *
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   *
   * @return the initParams
   */
  public WebInitParamMetaData[] getInitParams() {
    return initParams;
  }

  public Map<String, String> getInitParamsAsMap() {
    Map<String, String> map = new HashMap<>(getInitParams().length);
    for (WebInitParamMetaData ipm : getInitParams()) {
      map.put(ipm.getName(), ipm.getValue());
    }
    return map;
  }

  /**
   *
   * @return the largeIcon
   */
  public String getLargeIcon() {
    return largeIcon;
  }

  /**
   *
   * @return the loadOnStartup
   */
  public int getLoadOnStartup() {
    return loadOnStartup;
  }

  /**
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   *
   * @return the security
   */
  public ServletSecurityMetaData getSecurity() {
    return security;
  }

  /**
   *
   * @return the smallIcon
   */
  public String getSmallIcon() {
    return smallIcon;
  }

  /**
   *
   * @return the urlPatterns
   */
  public String[] getUrlPatterns() {
    return urlPatterns;
  }

  /**
   *
   * @return the value
   */
  public String[] getValue() {
    return value;
  }

  /**
   *
   * @return the asyncSupported
   */
  public boolean isAsyncSupported() {
    return asyncSupported;
  }

  /**
   *
   * @param asyncSupported the asyncSupported to set
   */
  protected void setAsyncSupported(boolean asyncSupported) {
    this.asyncSupported = asyncSupported;
  }

  /**
   *
   * @param clazz the clazz to set
   */
  protected void setClazz(Class<? extends Servlet> clazz) {
    this.clazz = requireNotNull(clazz, "");// FIXME MSG
  }

  /**
   *
   * @param description the description to set
   */
  protected void setDescription(String description) {
    this.description = description;
  }

  /**
   *
   * @param displayName the displayName to set
   */
  protected void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   *
   * @param initParams the initParams to set
   */
  protected void setInitParams(WebInitParamMetaData[] initParams) {
    this.initParams = initParams;
  }

  /**
   *
   * @param largeIcon the largeIcon to set
   */
  protected void setLargeIcon(String largeIcon) {
    this.largeIcon = largeIcon;
  }

  /**
   *
   * @param loadOnStartup the loadOnStartup to set
   */
  protected void setLoadOnStartup(int loadOnStartup) {
    this.loadOnStartup = loadOnStartup;
  }

  /**
   *
   * @param name the name to set
   */
  protected void setName(String name) {
    this.name = name;
  }

  /**
   *
   * @param security the security to set
   */
  protected void setSecurity(ServletSecurityMetaData security) {
    this.security = security;
  }

  /**
   *
   * @param smallIcon the smallIcon to set
   */
  protected void setSmallIcon(String smallIcon) {
    this.smallIcon = smallIcon;
  }

  /**
   *
   * @param urlPatterns the urlPatterns to set
   */
  protected void setUrlPatterns(String[] urlPatterns) {
    this.urlPatterns = urlPatterns;
  }

  /**
   *
   * @param value the value to set
   */
  protected void setValue(String[] value) {
    this.value = value;
  }


}