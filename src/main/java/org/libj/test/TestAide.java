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
import java.util.List;

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

  private TestAide() {
  }
}