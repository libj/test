/* Copyright (c) 2014 LibJ
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * Immutable class implementation of {@link NamespaceContext}.
 */
public class SimpleNamespaceContext implements NamespaceContext, Serializable {
  private static final long serialVersionUID = 1865343491264296309L;

  private static final List<String> xmlns = Arrays.asList("xmlns");
  private static final List<String> xml = Arrays.asList("xml");

  private final Map<String,String> prefixToNamespaceURI;
  private final Map<String,List<String>> namespaceUriToPrefix = new HashMap<>();

  /**
   * Create a new {@link SimpleNamespaceContext} with the provided
   * {@code prefixToNamespaceURI} of prefix-to-namespaceURI mappings.
   *
   * @param prefixToNamespaceURI The map of prefix-to-namespaceURI mappings.
   * @throws NullPointerException If {@code prefixToNamespaceURI} is null.
   */
  public SimpleNamespaceContext(final Map<String,String> prefixToNamespaceURI) {
    this.prefixToNamespaceURI = prefixToNamespaceURI;
    for (final Map.Entry<String,String> entry : prefixToNamespaceURI.entrySet()) {
      List<String> prefixes = namespaceUriToPrefix.get(entry.getValue());
      if (prefixes == null)
        namespaceUriToPrefix.put(entry.getValue(), prefixes = new ArrayList<>());

      prefixes.add(entry.getKey());
    }
  }

  @Override
  public String getNamespaceURI(final String prefix) {
    return prefixToNamespaceURI.get(prefix);
  }

  @Override
  public String getPrefix(final String uri) {
    if (uri == null)
      throw new IllegalArgumentException("uri == null");

    if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(uri))
      return "xmlns";

    if (XMLConstants.XML_NS_URI.equals(uri))
      return "xml";

    final List<String> prefixes = namespaceUriToPrefix.get(uri);
    return prefixes == null ? null : prefixes.get(0);
  }

  @Override
  public Iterator<String> getPrefixes(final String uri) {
    if (uri == null)
      throw new IllegalArgumentException("uri == null");

    if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(uri))
      return xmlns.iterator();

    if (XMLConstants.XML_NS_URI.equals(uri))
      return xml.iterator();

    final List<String> prefixes = namespaceUriToPrefix.get(uri);
    return prefixes == null ? null : new Iterator<String>() {
      private final Iterator<String> iterator = prefixes.iterator();

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public String next() {
        return iterator.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}