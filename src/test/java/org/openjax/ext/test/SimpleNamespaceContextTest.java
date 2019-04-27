/* Copyright (c) 2014 OpenJAX
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

package org.openjax.ext.test;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

@SuppressWarnings("unused")
public class SimpleNamespaceContextTest {
  @Test
  public void test() {
    try {
      new SimpleNamespaceContext(null);
      fail("Expected NullPointerException");
    }
    catch (final NullPointerException e) {
    }

    try {
      final SimpleNamespaceContext context = new SimpleNamespaceContext(Collections.singletonMap("foo", "bar"));
      context.getPrefix(null);
      fail("Expected IllegalArgumentException");
    }
    catch (final IllegalArgumentException e) {
    }

    try {
      final SimpleNamespaceContext context = new SimpleNamespaceContext(Collections.singletonMap("foo", "bar"));
      context.getPrefixes(null);
      fail("Expected IllegalArgumentException");
    }
    catch (final IllegalArgumentException e) {
    }

    final Map<String,String> prefixToNamespace = new HashMap<>();
    prefixToNamespace.put("foo1", "bar");
    prefixToNamespace.put("foo2", "bar");

    final SimpleNamespaceContext context = new SimpleNamespaceContext(prefixToNamespace);
    assertEquals("xml", context.getPrefix("http://www.w3.org/XML/1998/namespace"));
    assertEquals("xmlns", context.getPrefix("http://www.w3.org/2000/xmlns/"));
    assertTrue(context.getPrefix("bar").startsWith("foo"));

    assertEquals("xml", context.getPrefixes("http://www.w3.org/XML/1998/namespace").next());
    assertEquals("xmlns", context.getPrefixes("http://www.w3.org/2000/xmlns/").next());
    final Iterator<String> prefixes = context.getPrefixes("bar");
    assertTrue(prefixes.next().startsWith("foo"));
    assertTrue(prefixes.next().startsWith("foo"));
    assertFalse(prefixes.hasNext());
    try {
      prefixes.remove();
      fail("Expected UnsupportedOperationException");
    }
    catch (final UnsupportedOperationException e) {
    }
  }
}