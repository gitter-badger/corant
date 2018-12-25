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
package org.corant.suites.webserver.shared;

/**
 * corant-suites-webserver-shared
 *
 * @author bingo 下午3:30:43
 *
 */
public interface WebServerHandlers {

  @FunctionalInterface
  public static interface PostStartedHandler {
    void onPostStarted(WebServer webServer);
  }

  @FunctionalInterface
  public static interface PostStoppedHandler {
    void onPostStopped(WebServer webServer);
  }

  @FunctionalInterface
  public static interface PreStartHandler {
    boolean onPreStart(WebServer webServer);
  }

  @FunctionalInterface
  public static interface PreStopHandler {
    boolean onPreStop(WebServer webServer);
  }
}