/* Copyright (c) 2009 lib4j
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

package org.lib4j.test;

public final class TestRuntime {
  public static boolean isInIDE() {
    // See if the test is being invoked from IDEA
    if (System.getProperty("idea.launcher.port") != null)
      return true;

    // See if the test is being invoked from CodeGuide
    if (System.getProperty("java.compiler") != null || System.getProperty("java.library.path").contains("codeguide"))
      return true;

    // See if the test is being invoked from Eclipse
    if (System.getProperty("java.library.path").contains("eclipse"))
      return true;

    // See if we are using the RemoteTestRunner for Eclipse
    if (System.getProperty("sun.java.command").contains("org.eclipse.jdt.internal.junit.runner.RemoteTestRunner"))
      return true;

    return false;
  }

  public static boolean isInTest() {
    if (isInTest != null)
      return isInTest;

    for (final StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace())
      if ("junit.framework.TestCase".equals(stackTraceElement.getClassName()) || "org.junit.internal.runners.MethodRoadie".equals(stackTraceElement.getClassName()))
        return isInTest = true;

    return isInTest = false;
  }

  private static Boolean isInTest = null;

  private TestRuntime() {
  }
}