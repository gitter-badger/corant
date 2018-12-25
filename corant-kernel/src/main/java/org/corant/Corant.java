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
package org.corant;

import static org.corant.shared.util.ObjectUtils.shouldBeTrue;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import org.corant.kernel.event.CorantLifecycleEvent.LifecycleEventEmitter;
import org.corant.kernel.event.PostContainerStartedEvent;
import org.corant.kernel.event.PreContainerStopEvent;
import org.corant.kernel.util.UnmanageableInstance;
import org.corant.kernel.util.Unmanageables;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.api.WeldManager;

/**
 * corant-kernel
 *
 * @author bingo 上午11:52:09
 *
 */
public class Corant {

  private static Corant INSTANCE;
  private Class<?> configClass;
  private ClassLoader classLoader = Corant.class.getClassLoader();
  private WeldContainer container;

  public Corant(Class<?> configClass) {
    this.configClass = configClass;
    if (this.configClass != null) {
      classLoader = configClass.getClassLoader();
    }
    INSTANCE = this;
  }

  public Corant(ClassLoader classLoader) {
    this.classLoader = classLoader;
    INSTANCE = this;
  }

  public static CDI<Object> cdi() {
    validateRunning();
    return INSTANCE.container;
  }

  public static void fireAsyncEvent(Object event, Annotation... qualifiers) {
    validateRunning();
    if (event != null) {
      if (qualifiers.length > 0) {
        INSTANCE.getBeanManager().getEvent().select(qualifiers).fireAsync(event);
      } else {
        INSTANCE.getBeanManager().getEvent().fireAsync(event);
      }
    }
  }

  public static void fireEvent(Object event, Annotation... qualifiers) {
    validateRunning();
    if (event != null) {
      if (qualifiers.length > 0) {
        INSTANCE.getBeanManager().getEvent().select(qualifiers).fire(event);
      } else {
        INSTANCE.getBeanManager().getEvent().fire(event);
      }
    }
  }

  public static Corant instance() {
    return INSTANCE;
  }

  public static <T> UnmanageableInstance<T> produceUnmanageableBean(Class<T> clazz) {
    return Unmanageables.create(clazz);
  }

  public static <T> UnmanageableInstance<T> wrapUnmanageableBean(T object) {
    return Unmanageables.accpet(object);
  }

  private static void validateRunning() {
    shouldBeTrue(INSTANCE != null && INSTANCE.isRuning(),
        "The corant instance is null or is not in running");
  }

  public Corant accept(Consumer<Corant> consumer) {
    if (consumer != null) {
      consumer.accept(this);
    }
    return this;
  }

  public <R> R apply(Function<Corant, R> function) {
    if (function != null) {
      return function.apply(this);
    }
    return null;
  }

  public synchronized WeldManager getBeanManager() {
    validateRunning();
    return (WeldManager) container.getBeanManager();
  }

  public synchronized boolean isRuning() {
    return container != null && container.isRunning();
  }

  public synchronized Corant start() {
    Weld weld = new Weld();
    weld.setClassLoader(classLoader);
    weld.addExtensions(new CorantExtension());
    if (configClass != null) {
      weld.addPackages(true, configClass);
    }
    Thread.currentThread().setContextClassLoader(classLoader);
    container = weld.addProperty(Weld.SHUTDOWN_HOOK_SYSTEM_PROPERTY, true).initialize();
    LifecycleEventEmitter emitter = container.select(LifecycleEventEmitter.class).get();
    emitter.fire(new PostContainerStartedEvent());
    return this;
  }

  public synchronized void stop() {
    if (isRuning()) {
      LifecycleEventEmitter emitter = container.select(LifecycleEventEmitter.class).get();
      emitter.fire(new PreContainerStopEvent());
      ConfigProviderResolver.instance().releaseConfig(ConfigProvider.getConfig());
      container.close();
    }
  }

  class CorantExtension implements Extension {
    void produce(@Observes AfterBeanDiscovery event) {
      event.addBean().addType(Corant.class).scope(ApplicationScoped.class)
          .addQualifier(Default.Literal.INSTANCE).addQualifier(Any.Literal.INSTANCE)
          .produceWith((obj) -> Corant.this);
    }
  }
}