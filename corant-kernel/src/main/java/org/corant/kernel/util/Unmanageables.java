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
package org.corant.kernel.util;

import static org.corant.shared.util.Assertions.shouldBeFalse;
import static org.corant.shared.util.ObjectUtils.forceCast;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import org.corant.Corant;
import org.jboss.weld.bean.builtin.BeanManagerProxy;
import org.jboss.weld.injection.InterceptionFactoryImpl;
import org.jboss.weld.manager.api.WeldInjectionTarget;
import org.jboss.weld.manager.api.WeldManager;

/**
 * corant-kernel
 *
 * @author bingo 下午5:54:12
 *
 */
public class Unmanageables {

  public static <T> UnmanageableInstance<T> accpet(T obj) {
    return UnmanageableInstance.of(obj).produce().inject().postConstruct();
  }

  public static <T> UnmanageableInstance<T> create(Class<T> clazz) {
    return UnmanageableInstance.of(clazz).produce().inject().postConstruct();
  }

  public static class UnmanageableInstance<T> implements AutoCloseable {

    private T instance;
    private final CreationalContext<T> creationalContext;
    private final WeldInjectionTarget<T> injectionTarget;
    private final AnnotatedType<T> annotatedType;
    private final T orginalInstance;
    private final WeldManager bm;
    private boolean disposed = false;

    public UnmanageableInstance(Class<T> clazz) {
      bm = Corant.me().getBeanManager();
      creationalContext = bm.createCreationalContext(null);
      annotatedType = bm.createAnnotatedType(clazz);
      injectionTarget = bm.getInjectionTargetFactory(annotatedType).createInjectionTarget(null);
      orginalInstance = null;
    }

    public UnmanageableInstance(T object) {
      shouldBeFalse(Manageables.isManagedBean(object));
      bm = Corant.me().getBeanManager();
      creationalContext = bm.createCreationalContext(null);
      annotatedType = bm.createAnnotatedType(forceCast(object.getClass()));
      injectionTarget =
          bm.getInjectionTargetFactory(annotatedType).createNonProducibleInjectionTarget();
      orginalInstance = object;
    }

    public static <T> UnmanageableInstance<T> of(Class<T> clazz) {
      return new UnmanageableInstance<>(clazz);
    }

    public static <T> UnmanageableInstance<T> of(T object) {
      return new UnmanageableInstance<>(object);
    }

    @Override
    public void close() throws Exception {
      preDestroy();
      dispose();
    }

    /**
     * Dispose of the instance, doing any necessary cleanup
     *
     * @throws IllegalStateException if dispose() is called before produce() is called
     * @throws IllegalStateException if dispose() is called on an instance that has already been
     *         disposed
     * @return self
     */
    public UnmanageableInstance<T> dispose() {
      if (instance == null) {
        throw new IllegalStateException("Trying to call dispose() before produce() was called");
      }
      if (disposed) {
        throw new IllegalStateException("Trying to call dispose() on already disposed instance");
      }
      disposed = true;
      injectionTarget.dispose(instance);
      creationalContext.release();
      return this;
    }

    /**
     * Get the instance
     *
     * @return the instance
     */
    public T get() {
      return instance;
    }

    /**
     * Inject the instance
     *
     * @throws IllegalStateException if inject() is called before produce() is called
     * @throws IllegalStateException if inject() is called on an instance that has already been
     *         disposed
     * @return self
     */
    public UnmanageableInstance<T> inject() {
      if (instance == null) {
        throw new IllegalStateException("Trying to call inject() before produce() was called");
      }
      if (disposed) {
        throw new IllegalStateException("Trying to call inject() on already disposed instance");
      }
      injectionTarget.inject(instance, creationalContext);
      return this;
    }

    /**
     * Call the @PostConstruct callback
     *
     * @throws IllegalStateException if postConstruct() is called before produce() is called
     * @throws IllegalStateException if postConstruct() is called on an instance that has already
     *         been disposed
     * @return self
     */
    public UnmanageableInstance<T> postConstruct() {
      if (instance == null) {
        throw new IllegalStateException(
            "Trying to call postConstruct() before produce() was called");
      }
      if (disposed) {
        throw new IllegalStateException(
            "Trying to call postConstruct() on already disposed instance");
      }
      injectionTarget.postConstruct(instance);
      return this;
    }

    /**
     * Call the @PreDestroy callback
     *
     * @throws IllegalStateException if preDestroy() is called before produce() is called
     * @throws IllegalStateException if preDestroy() is called on an instance that has already been
     *         disposed
     * @return self
     */
    public UnmanageableInstance<T> preDestroy() {
      if (instance == null) {
        throw new IllegalStateException("Trying to call preDestroy() before produce() was called");
      }
      if (disposed) {
        throw new IllegalStateException("Trying to call preDestroy() on already disposed instance");
      }
      injectionTarget.preDestroy(instance);
      return this;
    }

    /**
     * Create the instance
     *
     * @throws IllegalStateException if produce() is called on an already produced instance
     * @throws IllegalStateException if produce() is called on an instance that has already been
     *         disposed
     * @return self
     */
    public UnmanageableInstance<T> produce() {
      if (instance != null) {
        throw new IllegalStateException("Trying to call produce() on already constructed instance");
      }
      if (disposed) {
        throw new IllegalStateException("Trying to call produce() on an already disposed instance");
      }
      instance =
          InterceptionFactoryImpl.of(BeanManagerProxy.unwrap(bm), creationalContext, annotatedType)
              .createInterceptedInstance(
                  orginalInstance == null ? injectionTarget.produce(creationalContext)
                      : orginalInstance);
      return this;
    }

  }
}
