/* Copyright (c) 2023 LibJ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.libj.test;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An {@link ExecutorService} that propagates exceptions thrown in executed or submitted tasks to the parent thread, in order to
 * facilitate enforcement of test assertions regarding exceptions thrown in multi-threaded executions.
 */
public class TestExecutorService implements ExecutorService {
  protected static final ConcurrentHashMap<Thread,Throwable> exception = new ConcurrentHashMap<>();

  static {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(final Thread t, final Throwable e) {
        exception.put(t, e);
      }
    });
  }

  protected ExecutorService target;
  private final ArrayList<Thread> threads = new ArrayList<>();

  /**
   * Creates a new {@link TestExecutorService} with the provided target {@link ExecutorService}.
   * @param target The target {@link ExecutorService}.
   * @throws NullPointerException If {@code target} is null.
   */
  public TestExecutorService(final ExecutorService target) {
    this.target = Objects.requireNonNull(target);
  }

  @Override
  public void execute(final Runnable command) {
    target.execute(() -> {
      threads.add(Thread.currentThread());
      command.run();
    });
  }

  @Override
  public void shutdown() {
    target.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return target.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return target.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return target.isTerminated();
  }

  @Override
  public <T>Future<T> submit(final Callable<T> task) {
    return target.submit(() -> {
      threads.add(Thread.currentThread());
      return task.call();
    });
  }

  @Override
  public <T>Future<T> submit(final Runnable task, final T result) {
    return target.submit(() -> {
      threads.add(Thread.currentThread());
      task.run();
    }, result);
  }

  @Override
  public Future<?> submit(final Runnable task) {
    return target.submit(() -> {
      threads.add(Thread.currentThread());
      task.run();
    });
  }

  @Override
  public <T>List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
    final ArrayList<Callable<T>> callables = new ArrayList<>(tasks);
    for (int i = 0, i$ = callables.size(); i < i$; ++i) { // [RA]
      final Callable<T> task = callables.get(i);
      callables.set(i, new Callable<T>() {
        @Override
        public T call() throws Exception {
          threads.add(Thread.currentThread());
          return task.call();
        }
      });
    }

    return target.invokeAll(callables);
  }

  @Override
  public <T>List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
    final ArrayList<Callable<T>> callables = new ArrayList<>(tasks);
    for (int i = 0, i$ = callables.size(); i < i$; ++i) { // [RA]
      final Callable<T> task = callables.get(i);
      callables.set(i, new Callable<T>() {
        @Override
        public T call() throws Exception {
          threads.add(Thread.currentThread());
          return task.call();
        }
      });
    }

    return target.invokeAll(callables, timeout, unit);
  }

  @Override
  public <T>T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
    final ArrayList<Callable<T>> callables = new ArrayList<>(tasks);
    for (int i = 0, i$ = callables.size(); i < i$; ++i) { // [RA]
      final Callable<T> task = callables.get(i);
      callables.set(i, new Callable<T>() {
        @Override
        public T call() throws Exception {
          threads.add(Thread.currentThread());
          return task.call();
        }
      });
    }

    return target.invokeAny(callables);
  }

  @Override
  public <T>T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    final ArrayList<Callable<T>> callables = new ArrayList<>(tasks);
    for (int i = 0, i$ = callables.size(); i < i$; ++i) { // [RA]
      final Callable<T> task = callables.get(i);
      callables.set(i, new Callable<T>() {
        @Override
        public T call() throws Exception {
          threads.add(Thread.currentThread());
          return task.call();
        }
      });
    }

    return target.invokeAny(callables, timeout, unit);
  }

  @Override
  public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
    final boolean result = target.awaitTermination(timeout, unit);
    for (int i = 0, i$ = threads.size(); i < i$; ++i) { // [RA]
      final Thread thread = threads.get(i);
      if (thread != null) {
        final Throwable t = exception.get(thread);
        if (t != null) {
          if (t instanceof Error)
            throw (Error)t;

          if (t instanceof RuntimeException)
            throw (RuntimeException)t;

          if (t instanceof InterruptedException)
            throw (InterruptedException)t;

          final InterruptedException e = new InterruptedException();
          e.initCause(t);
          throw e;
        }
      }
    }

    return result;
  }
}