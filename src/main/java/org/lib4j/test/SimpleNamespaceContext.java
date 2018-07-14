/* Copyright (c) 2014 lib4j
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext implements NamespaceContext {
  private final HashMap<String,String> prefixToNamespaceURI = new HashMap<>();
  private final HashMap<String,String> namespaceUriToPrefix = new HashMap<>();

  public SimpleNamespaceContext(final Map<String,String> prefixToNamespaceURI) {
    this.prefixToNamespaceURI.putAll(prefixToNamespaceURI);
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
  public Iterator getPrefixes(final String uri) {
    return prefixToNamespaceURI.keySet().iterator();
  }
}