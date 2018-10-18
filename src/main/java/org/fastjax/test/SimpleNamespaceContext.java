/* Copyright (c) 2014 FastJAX
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

package org.fastjax.test;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

/**
 * Immutable class implementation of {@link NamespaceContext}.
 */
public class SimpleNamespaceContext implements NamespaceContext, Serializable {
  private static final long serialVersionUID = 1865343491264296309L;

  private final Map<String,String> prefixToNamespaceURI;
  private final Map<String,String> namespaceUriToPrefix = new HashMap<>();

  /**
   * Create a new {@code SimpleNamespaceContext} with the provided
   * {@code prefixToNamespaceURI} of prefix-to-namespaceURI mappings.
   *
   * @param prefixToNamespaceURI The map of prefix-to-namespaceURI mappings.
   */
  public SimpleNamespaceContext(final Map<String,String> prefixToNamespaceURI) {
    this.prefixToNamespaceURI = Collections.unmodifiableMap(prefixToNamespaceURI);
    for (final Map.Entry<String,String> entry : prefixToNamespaceURI.entrySet())
      namespaceUriToPrefix.put(entry.getKey(), entry.getValue());
  }

  @Override
  public String getNamespaceURI(final String prefix) {
    return prefixToNamespaceURI.get(prefix);
  }

  @Override
  public String getPrefix(final String uri) {
    return namespaceUriToPrefix.get(uri);
  }

  @Override
  public Iterator<String> getPrefixes(final String uri) {
    return prefixToNamespaceURI.keySet().iterator();
  }
}