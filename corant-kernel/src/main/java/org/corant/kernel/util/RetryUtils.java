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

import static org.corant.shared.util.Empties.isEmpty;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.interceptor.InvocationContext;
import org.corant.kernel.exception.GeneralRuntimeException;

/**
 * @author bingo 下午5:17:23
 *
 */
public class RetryUtils {

  protected static final Logger LOGGER = Logger.getLogger(RetryUtils.class.toString());

  static final String RTY_LOG =
      "An exception occurred during execution, enter the retry phase, the retry times is %s, interval is %s.";

  static final String RTY_ERR_LOG = "An exception occurred during supplier.";

  private RetryUtils() {
    super();
  }

  @SuppressWarnings("unchecked")
  public static <T> Retrier<T> retrier(InvocationContext ctx) {
    return (Retrier<T>) new InvocationRetrier(ctx);
  }

  public static <T> Retrier<T> retrier(Supplier<T> execution) {
    return new SupplierRetrier<>(execution);
  }

  public static Object retry(InvocationContext ctx, int times, Duration interval,
      Function<Exception, RuntimeException> transfer,
      @SuppressWarnings("unchecked") Class<? extends Exception>... ignoreExceptions) {
    return new InvocationRetrier(ctx).transfer(transfer).on(ignoreExceptions).times(times)
        .interval(interval).execute();
  }

  @SafeVarargs
  public static <T> T retry(Supplier<T> execution, int times, Duration interval,
      Function<Exception, RuntimeException> transfer,
      Class<? extends Exception>... ignoreExceptions) {
    return new SupplierRetrier<>(execution).transfer(transfer).on(ignoreExceptions).times(times)
        .interval(interval).execute();
  }

  public static class InvocationRetrier extends Retrier<Object> {

    private final InvocationContext ctx;

    public InvocationRetrier(InvocationContext ctx) {
      super();
      this.ctx = ctx;
    }

    @Override
    Object doExecute() throws Exception {
      return ctx.proceed();
    }
  }

  public abstract static class Retrier<T> {

    private int times = 8;
    private Duration interval = Duration.ofMillis(1000L);
    private Function<Exception, RuntimeException> transfer =
        e -> new GeneralRuntimeException(e, "");// FIXME message code
    private Set<Class<? extends Exception>> on = new LinkedHashSet<>();

    public T execute() {
      if (this.on.isEmpty()) {
        this.on.add(Exception.class);
      }
      int retryCounter = 1;
      Exception executeException = null;
      do {
        try {
          return this.doExecute();
        } catch (Exception e) {
          executeException = e;
          if (this.on.stream().noneMatch(ec -> ec.isAssignableFrom(e.getClass()))) {
            throw this.transfer.apply(e);
          }
          final int logretryCounter = retryCounter;
          LOGGER.warning(() -> String.format(RTY_LOG, logretryCounter, this.interval));
          try {
            Thread.sleep(this.interval.toMillis());
          } catch (InterruptedException te) {
            te.addSuppressed(e);
            Thread.currentThread().interrupt();
            throw this.transfer.apply(te);
          }
        } finally {
          retryCounter++;
        }
      } while (retryCounter <= this.times);
      throw this.transfer.apply(executeException);
    }

    public T executeOrElse(T s) {
      T r = null;
      try {
        r = this.execute();
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, e, () -> "Retry execute error");
        r = s;
      }
      return r;
    }

    public Retrier<T> interval(Duration interval) {
      this.interval = interval == null || interval.toMillis() < 0 ? Duration.ofMillis(0) : interval;
      return this;
    }

    @SafeVarargs
    public final Retrier<T> on(Class<? extends Exception>... ignoreExceptions) {
      this.on.addAll(Arrays.asList(ignoreExceptions));
      return this;
    }

    public final Retrier<T> on(Collection<Class<? extends Exception>> ignoreExceptions) {
      if (!isEmpty(ignoreExceptions)) {
        this.on.addAll(ignoreExceptions);
      }
      return this;
    }

    public Retrier<T> times(int times) {
      this.times = times < 0 ? 0 : times;
      return this;
    }

    public Retrier<T> transfer(Function<Exception, RuntimeException> transfer) {
      if (transfer != null) {
        this.transfer = transfer;
      }
      return this;
    }

    abstract T doExecute() throws Exception;
  }

  public static class SupplierRetrier<T> extends Retrier<T> {

    private final Supplier<T> supplier;

    public SupplierRetrier(Supplier<T> supplier) {
      super();
      this.supplier = supplier;
    }

    @Override
    T doExecute() throws Exception {
      return this.supplier.get();
    }

  }
}
