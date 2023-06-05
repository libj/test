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

import static org.junit.Assert.*;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TestExecutorServiceTest {
  private static void sleep(final long millis) {
    if (millis < 0)
      return;

    try {
      Thread.sleep(millis);
    }
    catch (final InterruptedException e) {
      System.err.println(e.getMessage());
      System.err.flush();
      System.exit(-1);
    }
  }

  private static void testNoConcurrentModificationException(final boolean doFail) throws Throwable {
    final TestExecutorService executor = new TestExecutorService(Executors.newFixedThreadPool(4));
    executor.execute(() -> {
      for (int i = 0; i < 10; ++i) // [N]
        sleep(10);
    });

    sleep(10);

    executor.execute(() -> {
      for (int i = 0; i < 10; ++i) // [N]
        sleep(5);

      if (doFail)
        fail("Fail");
    });

    executor.shutdown();
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
  }

  @Test
  public void testNoConcurrentModificationExceptionHashMap() throws Throwable {
    try {
      testNoConcurrentModificationException(true);
      fail("Expected AssertionError");
    }
    catch (final AssertionError e) {
    }
  }

  @Test
  public void testNoConcurrentModificationExceptionConcurrentHashMap() throws Throwable {
    testNoConcurrentModificationException(false);
  }
}