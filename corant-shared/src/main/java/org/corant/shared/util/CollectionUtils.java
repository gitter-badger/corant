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
package org.corant.shared.util;

import static org.corant.shared.util.Assertions.shouldNotNull;
import static org.corant.shared.util.Empties.isEmpty;
import static org.corant.shared.util.ObjectUtils.forceCast;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import org.corant.shared.util.CollectionUtils.ListJoins.JoinType;

/**
 *
 * @author bingo 上午12:31:10
 *
 */
public class CollectionUtils {

  private CollectionUtils() {
    super();
  }

  @SafeVarargs
  public static <T> List<T> asImmutableList(final T... objects) {
    if (objects == null || objects.length == 0) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(asList(objects));
  }

  @SafeVarargs
  public static <T> Set<T> asImmutableSet(final T... objects) {
    if (objects == null || objects.length == 0) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(asSet(objects));
  }

  public static <T> List<T> asList(final Enumeration<T> enumeration) {
    List<T> list = new ArrayList<>();
    if (enumeration != null) {
      while (enumeration.hasMoreElements()) {
        list.add(enumeration.nextElement());
      }
    }
    return list;
  }

  public static <T> List<T> asList(final Iterable<T> iterable) {
    if (iterable instanceof List) {
      return forceCast(iterable);
    } else {
      List<T> list = new ArrayList<>();
      if (iterable != null) {
        iterable.forEach(list::add);
      }
      return list;
    }
  }

  public static <T> List<T> asList(final Iterator<T> iterator) {
    List<T> list = new ArrayList<>();
    if (iterator != null) {
      while (iterator.hasNext()) {
        list.add(iterator.next());
      }
    }
    return list;
  }

  @SafeVarargs
  public static <T> List<T> asList(final T... objects) {
    ArrayList<T> list = new ArrayList<>(objects.length);
    for (T obj : objects) {
      list.add(obj);
    }
    return list;
  }

  @SafeVarargs
  public static <T> Set<T> asSet(final T... objects) {
    Set<T> set = new HashSet<>(objects.length);
    for (T obj : objects) {
      set.add(obj);
    }
    return set;
  }

  public static Object get(final Object object, final int index) {
    final int i = index;
    if (i < 0) {
      throw new IndexOutOfBoundsException("Index cannot be negative: " + i);
    }
    if (object == null) {
      throw new IllegalArgumentException("Unsupported object type: null");
    }
    if (object instanceof Iterable<?>) {
      return get(object, i);
    } else if (object instanceof Object[]) {
      return ((Object[]) object)[i];
    } else if (object instanceof Iterator<?>) {
      return IterableUtils.get((Iterator<?>) object, index);
    } else if (object instanceof Enumeration<?>) {
      return IterableUtils.get((Enumeration<?>) object, index);
    } else {
      try {
        return Array.get(object, i);
      } catch (final IllegalArgumentException ex) {
        throw new IllegalArgumentException(
            "Unsupported object type: " + object.getClass().getName());
      }
    }
  }

  public static int getSize(final Object object) {
    if (object == null) {
      return 0;
    } else if (object instanceof Collection<?>) {
      return ((Collection<?>) object).size();
    } else if (object instanceof Object[]) {
      return ((Object[]) object).length;
    } else if (object instanceof Iterable<?>) {
      return IterableUtils.getSize((Iterable<?>) object);
    } else if (object instanceof Iterator<?>) {
      return IterableUtils.getSize((Iterator<?>) object);
    } else if (object instanceof Enumeration<?>) {
      return IterableUtils.getSize((Enumeration<?>) object);
    } else {
      try {
        return Array.getLength(object);
      } catch (final IllegalArgumentException ex) {
        throw new IllegalArgumentException(
            "Unsupported object type: " + object.getClass().getName());
      }
    }
  }

  public static <F, J, T> List<T> mergeList(final List<F> from, final List<J> join,
      final BiFunction<F, J, T> combination, final BiPredicate<F, J> condition, JoinType type) {
    return new ListJoins<F, J, T>().select(combination).from(from).join(type, join).on(condition)
        .execute();
  }

  public static <T> List<List<T>> partition(final Collection<T> collection, int size) {
    List<List<T>> result = new ArrayList<>();
    if (collection != null) {
      final AtomicInteger counter = new AtomicInteger(0);
      collection.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size))
          .values().forEach(result::add);
    }
    return result;
  }

  public static class ListJoins<F, J, T> {

    private List<F> from;

    private List<J> join;
    private BiPredicate<F, J> on;
    private BiFunction<F, J, T> select;
    private JoinType type = JoinType.LEFT;

    public static <F, J, T> ListJoins<F, J, T> start() {
      return new ListJoins<>();
    }

    public List<T> execute() {
      List<T> result = new ArrayList<>();
      if (type == JoinType.LEFT && isEmpty(join)) {
        from.stream().map(x -> select.apply(x, null)).forEachOrdered(result::add);
      } else if (type == JoinType.LEFT && !isEmpty(join)) {
        from.stream().forEachOrdered(f -> {
          List<J> ms = join.stream().filter(j -> on.test(f, j)).collect(Collectors.toList());
          if (isEmpty(ms)) {
            result.add(select.apply(f, null));
          } else {
            ms.stream().map(m -> select.apply(f, m)).forEach(result::add);
          }
        });
      } else if (type == JoinType.INNER && !isEmpty(join)) {
        from.stream().forEachOrdered(f -> this.join.stream().filter(j -> on.test(f, j))
            .forEachOrdered(j -> result.add(select.apply(f, j))));
      } else if (type == JoinType.CARTESIAN && !isEmpty(join)) {
        from.stream()
            .forEachOrdered(f -> join.stream().forEachOrdered(j -> result.add(select.apply(f, j))));
      }
      return result;
    }

    public ListJoins<F, J, T> from(List<F> from) {
      this.from = shouldNotNull(from);
      return this;
    }

    public ListJoins<F, J, T> on(BiPredicate<F, J> on) {
      this.on = on == null ? (f, j) -> false : on;
      return this;
    }

    public ListJoins<F, J, T> select(BiFunction<F, J, T> select) {
      this.select = select;
      return this;
    }

    ListJoins<F, J, T> join(JoinType joinType, List<J> joined) {
      this.type = joinType == null ? JoinType.LEFT : joinType;
      this.join = joined;
      return this;
    }

    enum JoinType {
      LEFT, INNER, CARTESIAN;
    }
  }
}
