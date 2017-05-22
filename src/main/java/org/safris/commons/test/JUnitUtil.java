/* Copyright (c) 2017 lib4j
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

package org.safris.commons.test;

public final class JUnitUtil {
  public static String[] getExpectedActual(final AssertionError error) {
    final String message = error.getMessage();
    int start = message.indexOf('<');
    int end = message.indexOf('>', start + 1);
    final String expected = message.substring(start + 1, end);
    start = message.indexOf('<', end + 1);
    end = message.indexOf('>', start + 1);
    final String actual = message.substring(start + 1, end);
    return new String[] {expected, actual};
  }

  private JUnitUtil() {
  }
}