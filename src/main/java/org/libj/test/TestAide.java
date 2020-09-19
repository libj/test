/* Copyright (c) 2020 LibJ
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

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Helpful utility functions for test executions.
 */
public final class TestAide {
  private static boolean inSurefireTestInited;
  private static boolean inSurefireTest;

  /**
   * Returns {@code true} if the current runtime is executed in a Maven SureFire
   * plugin, otherwise {@code false}.
   *
   * @return {@code true} if the current runtime is executed in a Maven SureFire
   *         plugin, otherwise {@code false}.
   */
  public static boolean isInSurefireTest() {
    if (inSurefireTestInited)
      return inSurefireTest;

    inSurefireTest = System.getProperty("sun.java.command").contains("org.apache.maven.surefire") || System.getProperty("surefire.test.class.path") != null;
    inSurefireTestInited = true;
    return inSurefireTest;
  }

  private static boolean inCiTestInited;
  private static boolean inCiTest;

  /**
   * Returns {@code true} if the current runtime is executed in a Continuous
   * Integration Environment of Travis CI, Circle CI, or GitHub Actions.
   *
   * @return {@code true} if the current runtime is executed in a Continuous
   *         Integration Environment of Travis CI, Circle CI or GitHub Actions,
   *         otherwise {@code false}.
   */
  public static boolean isInCiTest() {
    if (inCiTestInited)
      return inCiTest;

    final String property = System.getenv("CI");
    inCiTest = property != null && !"false".equals(property);
    inCiTestInited = true;
    return inCiTestInited;
  }

  private static boolean inDebugInited;
  private static boolean inDebug;

  /**
   * Returns {@code true} if the current runtime is executed in debug mode,
   * otherwise {@code false}.
   *
   * @return {@code true} if the current runtime is executed in debug mode,
   *         otherwise {@code false}.
   */
  public static boolean isInDebug() {
    if (inDebugInited)
      return inDebug;

    inDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
    inDebugInited = true;
    return inDebug;
  }

  /**
   * Prints the runtime parameters for the current VM.
   *
   * @param ps The {@link PrintStream} to which the results are to be printed.
   * @throws NullPointerException If here are parameters to print and {@code ps}
   *           is null.
   */
  public static void printRuntimeParameters(final PrintStream ps) {
    final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
    final List<String> arguments = bean.getInputArguments();
    for (final String argument : arguments)
      ps.println(argument);
  }

  /**
   * Prints the provided {@link Throwable} and its backtrace to the specified
   * {@link PrintStream}.
   * <p>
   * This method differentiates itself from
   * {@link Throwable#printStackTrace(PrintStream)} by terminating the printout
   * of the backtrace at the first occurrence (if any) of a stack trace element
   * representing {@code "runReflectiveCall"} of a class in the
   * {@code "org.junit.runners"} package.
   *
   * @param out The {@code PrintStream} to use for output.
   * @param t The {@link Throwable} to print.
   */
  public static void printStackTrace(final PrintStream out, final Throwable t) {
    // Guard against malicious overrides of Throwable.equals by
    // using a Set with identity equality semantics.
    printStackTrace(out, t, Collections.newSetFromMap(new IdentityHashMap<>()));
  }

  private static void printStackTrace(final PrintStream out, final Throwable t, final Set<Throwable> visited) {
    synchronized (t) {
      if (visited.add(t)) {
        // Print our stack trace
        out.println(t.toString());
        final StackTraceElement[] stackTraceElements = t.getStackTrace();
        for (final StackTraceElement stackTraceElement : stackTraceElements) {
          if ("runReflectiveCall".equals(stackTraceElement.getMethodName()) && stackTraceElement.getClassName().startsWith("org.junit.runners."))
            break;

          out.println("\tat " + stackTraceElement);
        }

        // Print suppressed exceptions, if any
        for (final Throwable suppressed : t.getSuppressed()) {
          out.print("\nSuppressed: ");
          printStackTrace(out, suppressed, visited);
        }

        // Print cause, if any
        final Throwable cause = t.getCause();
        if (cause != null) {
          out.print("Caused by: ");
          printStackTrace(out, t, visited);
        }
      }
    }
  }

  private TestAide() {
  }
}