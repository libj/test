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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Helpful utility functions for JUnit tests.
 */
public class JUnitUtil {
  private static final URL[] emptyResources = {};

  private static URL[] getResources(final String path, final Pattern pattern, final ClassLoader classLoader, final BufferedReader reader, final int depth) throws IOException {
    final String fileName = reader.readLine();
    if (fileName == null)
      return new URL[depth];

    final String name = path + fileName;
    if (pattern != null && !pattern.matcher(name).matches())
      return getResources(path, pattern, classLoader, reader, depth);

    final URL[] urls = getResources(path, pattern, classLoader, reader, depth + 1);
    urls[depth] = classLoader.getResource(name);
    return urls;
  }

  /**
   * Finds the resources prefixed by the given {@code path} in the specified {@link ClassLoader}.
   *
   * @param path The path prefix of the resources to find.
   * @param regex The regular expression pattern to match resource names.
   * @param classLoader The {@link ClassLoader} in which to find the resources.
   * @return The resources prefixed by the given {@code path} in the specified {@link ClassLoader}.
   * @throws IOException If an I/O error has occurred.
   * @throws NullPointerException If {@code path} is null.
   */
  public static URL[] getResources(final String path, final String regex, final ClassLoader classLoader) throws IOException {
    final InputStream in = classLoader.getResourceAsStream(path);
    if (in == null)
      return emptyResources;

    try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
      return getResources(path.isEmpty() || path.endsWith("/") ? path : path + "/", regex == null ? null : Pattern.compile(regex), classLoader, reader, 0);
    }
  }

  /**
   * Finds the resources prefixed by the given {@code path} in the specified {@link ClassLoader}.
   *
   * @param path The path prefix of the resources to find.
   * @param classLoader The {@link ClassLoader} in which to find the resources.
   * @return The resources prefixed by the given {@code path} in the specified {@link ClassLoader}.
   * @throws IOException If an I/O error has occurred.
   */
  public static URL[] getResources(final String path, final ClassLoader classLoader) throws IOException {
    return getResources(path, null, classLoader);
  }

  /**
   * Finds the resources prefixed by the given {@code path} in the system class loader.
   *
   * @param path The path prefix of the resources to find.
   * @param regex The regular expression pattern to match resource names.
   * @return The resources prefixed by the given {@code path} in the system class loader.
   * @throws IOException If an I/O error has occurred.
   */
  public static URL[] getResources(final String path, final String regex) throws IOException {
    return getResources(path, regex, ClassLoader.getSystemClassLoader());
  }

  /**
   * Finds the resources prefixed by the given {@code path} in the system class loader.
   *
   * @param path The path prefix of the resources to find.
   * @return The resources prefixed by the given {@code path} in the system class loader.
   * @throws IOException If an I/O error has occurred.
   */
  public static URL[] getResources(final String path) throws IOException {
    return getResources(path, null, ClassLoader.getSystemClassLoader());
  }

  /**
   * Sorts the provided array of file {@link URL}s by the their byte size, ascending.
   *
   * @param resources The file {@link URL}s to sort.
   * @return The provided array of file {@link URL}s, sorted by the their byte size, ascending.
   */
  public static URL[] sortBySize(final URL ... resources) {
    Arrays.sort(resources, (final URL o1, final URL o2) -> {
      try {
        return Long.compare(new File(o1.toURI()).length(), new File(o2.toURI()).length());
      }
      catch (final URISyntaxException e) {
        throw new RuntimeException(e);
      }
    });
    return resources;
  }

  private JUnitUtil() {
  }
}