/* Copyright (c) 2018 lib4j
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
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.lib4j.xml.dom.DOMStyle;
import org.lib4j.xml.dom.DOMs;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonListener;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.DOMDifferenceEngine;
import org.xmlunit.diff.DifferenceEngine;

public class AssertXml {
  private XPath newXPath() {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(new SimpleNamespaceContext(prefixToNamespaceURI));
    return xPath;
  }

  public static AssertXml compare(final Element controlElement, final Element testElement) {
    final Map<String,String> prefixToNamespaceURI = new HashMap<>();
    prefixToNamespaceURI.put("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    final NamedNodeMap attributes = controlElement.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
       final Attr attribute = (Attr)attributes.item(i);
       if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI()) && "xmlns".equals(attribute.getPrefix()))
         prefixToNamespaceURI.put(attribute.getLocalName(), attribute.getNodeValue());
    }

    return new AssertXml(prefixToNamespaceURI, controlElement, testElement);
  }

  private final Map<String,String> prefixToNamespaceURI;
  private final Element controlElement;
  private final Element testElement;

  private AssertXml(final Map<String,String> prefixToNamespaceURI, final Element controlElement, final Element testElement) {
    if (!controlElement.getPrefix().equals(testElement.getPrefix()))
      throw new IllegalArgumentException("Prefixes of control and test elements must be the same: " + controlElement.getPrefix() + " != " + testElement.getPrefix());

    this.prefixToNamespaceURI = prefixToNamespaceURI;
    this.controlElement = controlElement;
    this.testElement = testElement;
  }

  public void addAttribute(final Element element, final String xpath, final String name, final String value) throws XPathExpressionException {
    final XPathExpression expression = newXPath().compile(xpath);
    final NodeList nodes = (NodeList)expression.evaluate(element, XPathConstants.NODESET);
    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);
      if (!(node instanceof Element))
        throw new UnsupportedOperationException("Only support addition of attributes to elements");

      final Element target = (Element)node;
      final int colon = name.indexOf(':');
      final String namespaceURI = colon == -1 ? node.getNamespaceURI() : node.getOwnerDocument().lookupNamespaceURI(name.substring(0, colon));
      target.setAttributeNS(namespaceURI, name, value);
    }
  }

  public void remove(final Element element, final String ... xpaths) throws XPathExpressionException {
    for (final String xpath : xpaths) {
      final XPathExpression expression = newXPath().compile(xpath);
      final NodeList nodes = (NodeList)expression.evaluate(element, XPathConstants.NODESET);
      for (int i = 0; i < nodes.getLength(); ++i) {
        final Node node = nodes.item(i);
        if (node instanceof Attr) {
          final Attr attribute = (Attr)node;
          attribute.getOwnerElement().removeAttributeNode(attribute);
        }
        else {
          node.getParentNode().removeChild(node);
        }
      }
    }
  }

  public void replace(final Element element, final String xpath, final String name, final String value) throws XPathExpressionException {
    final XPathExpression expression = newXPath().compile(xpath);
    final NodeList nodes = (NodeList)expression.evaluate(element, XPathConstants.NODESET);
    for (int i = 0; i < nodes.getLength(); ++i) {
      final Node node = nodes.item(i);
      if (node instanceof Attr) {
        final Attr attribute = (Attr)node;
        if (name == null) {
          attribute.setValue(value);
        }
        else {
          final int colon = name.indexOf(':');
          final String namespaceURI = colon == -1 ? attribute.getNamespaceURI() : attribute.getOwnerDocument().lookupNamespaceURI(name.substring(0, colon));
          final Element owner = attribute.getOwnerElement();
          owner.removeAttributeNode(attribute);
          owner.setAttributeNS(namespaceURI, name, value);
        }
      }
      else {
        throw new UnsupportedOperationException("Only support replacement of attribute values");
      }
    }
  }

  public void replace(final Element element, final String xpath, final String value) throws XPathExpressionException {
    replace(element, xpath, null, value);
  }

  public void assertEqual() {
    final String prefix = controlElement.getPrefix();
    final String controlXml = DOMs.domToString(controlElement, DOMStyle.INDENT, DOMStyle.INDENT_ATTRS);
    final String testXml = DOMs.domToString(testElement, DOMStyle.INDENT, DOMStyle.INDENT_ATTRS);

    final Source controlSource = Input.fromString(controlXml).build();
    final Source testSource = Input.fromString(testXml).build();

    final DifferenceEngine diffEngine = new DOMDifferenceEngine();
    diffEngine.addDifferenceListener(new ComparisonListener() {
      @Override
      public void comparisonPerformed(final Comparison comparison, final ComparisonResult result) {
        final String controlXPath = comparison.getControlDetails().getXPath() == null ? null : comparison.getControlDetails().getXPath().replaceAll("/([^@])", "/" + prefix + ":$1");
        if (controlXPath == null || controlXPath.matches("^.*\\/@[:a-z]+$") || controlXPath.contains("text()"))
          return;

        try {
          Assert.assertEquals(controlXml, testXml);
        }
        catch (final ComparisonFailure e) {
          final StackTraceElement[] stackTrace = e.getStackTrace();
          int i;
          for (i = 3; i < stackTrace.length; i++)
            if (!stackTrace[i].getClassName().startsWith("org.xmlunit.diff"))
              break;

          final StackTraceElement[] filtered = new StackTraceElement[stackTrace.length - ++i];
          System.arraycopy(stackTrace, i, filtered, 0, stackTrace.length - i);
          e.setStackTrace(filtered);
          throw e;
        }

        Assert.fail(comparison.toString());
      }
    });

    diffEngine.compare(controlSource, testSource);
  }
}