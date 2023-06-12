/* Copyright (c) 2018 LibJ
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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.ComparisonFailure;
import org.openjax.xml.dom.DOMStyle;
import org.openjax.xml.dom.DOMs;
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

/**
 * A builder implementation of the test assertion pattern, designed to facilitate testing of XML values. This implementation is
 * based on <a href="https://www.xmlunit.org/">XMLUnit</a> and <a href="https://www.junit.org/">JUnit</a>.
 */
public final class AssertXml {
  private static final String diffPackageName = Comparison.class.getPackage().getName();
  private static final Pattern ATTR_NAME_PATTERN = Pattern.compile("/([^@])");
  private static final Pattern ATTR_MATCH_PATTERN = Pattern.compile("^.*/@[:a-z]+$");

  /**
   * Create a new {@link AssertXml} comparison instance between the provided {@code control} and {@code test} elements.
   *
   * @param control The control element.
   * @param test The test element.
   * @return A new {@link AssertXml} comparison instance.
   */
  public static AssertXml compare(final Element control, final Element test) {
    final HashMap<String,String> prefixToNamespaceURI = new HashMap<>();
    prefixToNamespaceURI.put("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
    final NamedNodeMap attributes = control.getAttributes();
    for (int i = 0, i$ = attributes.getLength(); i < i$; ++i) { // [RA]
      final Attr attribute = (Attr)attributes.item(i);
      if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attribute.getNamespaceURI()) && "xmlns".equals(attribute.getPrefix()))
        prefixToNamespaceURI.put(attribute.getLocalName(), attribute.getNodeValue());
    }

    return new AssertXml(new SimpleNamespaceContext(prefixToNamespaceURI), control, test);
  }

  private final NamespaceContext namespaceContext;
  private final Element control;
  private final Element test;

  private AssertXml(final NamespaceContext namespaceContext, final Element control, final Element test) {
    if (!control.getPrefix().equals(test.getPrefix()))
      throw new IllegalArgumentException("Prefixes of control and test elements must be the same: \"" + control.getPrefix() + "\" != \"" + test.getPrefix() + "\"");

    this.namespaceContext = namespaceContext;
    this.control = control;
    this.test = test;
  }

  private XPath newXPath() {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(namespaceContext);
    return xPath;
  }

  private AssertXml addAttr(final Element element, final String xpath, final String name, final String value) throws XPathExpressionException {
    final XPathExpression expression = newXPath().compile(xpath);
    final NodeList nodes = (NodeList)expression.evaluate(element, XPathConstants.NODESET);
    for (int i = 0, i$ = nodes.getLength(); i < i$; ++i) { // [RA]
      final Node node = nodes.item(i);
      if (!(node instanceof Element))
        throw new UnsupportedOperationException("Only support addition of attributes to elements");

      final Element target = (Element)node;
      final int colon = name.indexOf(':');
      final String namespaceURI = colon == -1 ? node.getNamespaceURI() : node.getOwnerDocument().lookupNamespaceURI(name.substring(0, colon));
      target.setAttributeNS(namespaceURI, name, value);
    }

    return this;
  }

  /**
   * Add an attribute to a target node of the {@code control} element in this {@link AssertXml} instance.
   *
   * @param xpath The XPath of the target node.
   * @param name The name of the attribute.
   * @param value The value of the attribute.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If the {@code xpath} expression cannot be compiled or evaluated.
   */
  public AssertXml addAttrToControl(final String xpath, final String name, final String value) throws XPathExpressionException {
    return addAttr(control, xpath, name, value);
  }

  /**
   * Add an attribute to a target node of the {@code test} element in this {@link AssertXml} instance.
   *
   * @param xpath The XPath of the target node.
   * @param name The name of the attribute.
   * @param value The value of the attribute.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If the {@code xpath} expression cannot be compiled or evaluated.
   */
  public AssertXml addAttrToTest(final String xpath, final String name, final String value) throws XPathExpressionException {
    return addAttr(test, xpath, name, value);
  }

  private AssertXml remove(final Element element, final String ... xpaths) throws XPathExpressionException {
    for (final String xpath : xpaths) { // [A]
      final XPathExpression expression = newXPath().compile(xpath);
      final NodeList nodes = (NodeList)expression.evaluate(element, XPathConstants.NODESET);
      for (int i = 0, i$ = nodes.getLength(); i < i$; ++i) { // [RA]
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

    return this;
  }

  /**
   * Remove nodes identified by the provided {@code xpaths} from the {@code control} element in this {@link AssertXml} instance.
   *
   * @param xpaths The XPaths of the target nodes to remove.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If an XPath expression cannot be compiled or evaluated.
   */
  public AssertXml removeFromControl(final String ... xpaths) throws XPathExpressionException {
    return remove(control, xpaths);
  }

  /**
   * Remove nodes identified by the provided {@code xpaths} from the {@code test} element in this {@link AssertXml} instance.
   *
   * @param xpaths The XPaths of the target nodes to remove.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If an XPath expression cannot be compiled or evaluated.
   */
  public AssertXml removeFromTest(final String ... xpaths) throws XPathExpressionException {
    return remove(test, xpaths);
  }

  private AssertXml replaceAttr(final Element element, final String xpath, final String name, final String value) throws XPathExpressionException {
    final XPathExpression expression = newXPath().compile(xpath);
    final NodeList nodes = (NodeList)expression.evaluate(element, XPathConstants.NODESET);
    for (int i = 0, i$ = nodes.getLength(); i < i$; ++i) { // [RA]
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

    return this;
  }

  /**
   * Replace an attribute at {@code xpath} in the {@code control} element with the replacement {@code name} and {@code value}.
   *
   * @param xpath The XPath of the target attribute.
   * @param name The name of the replacement attribute, or {@code null} to retain the current name.
   * @param value The value of the replacement attribute.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If the {@code xpath} expression cannot be compiled or evaluated.
   */
  public AssertXml replaceAttrInControl(final String xpath, final String name, final String value) throws XPathExpressionException {
    return replaceAttr(control, xpath, name, value);
  }

  /**
   * Replace an attribute at {@code xpath} in the {@code test} element with the replacement {@code name} and {@code value}.
   *
   * @param xpath The XPath of the target attribute.
   * @param name The name of the replacement attribute, or {@code null} to retain the current name.
   * @param value The value of the replacement attribute.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If the {@code xpath} expression cannot be compiled or evaluated.
   */
  public AssertXml replaceAttrInTest(final String xpath, final String name, final String value) throws XPathExpressionException {
    return replaceAttr(test, xpath, name, value);
  }

  /**
   * Replace an attribute at {@code xpath} in the {@code control} element with the replacement {@code value}.
   *
   * @param xpath The XPath of the target attribute.
   * @param value The value of the replacement attribute.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If the {@code xpath} expression cannot be compiled or evaluated.
   */
  public AssertXml replaceAttrInControl(final String xpath, final String value) throws XPathExpressionException {
    return replaceAttr(control, xpath, null, value);
  }

  /**
   * Replace an attribute at {@code xpath} in the {@code test} element with the replacement {@code value}.
   *
   * @param xpath The XPath of the target attribute.
   * @param value The value of the replacement attribute.
   * @return This {@link AssertXml} instance.
   * @throws XPathExpressionException If the {@code xpath} expression cannot be compiled or evaluated.
   */
  public AssertXml replaceAttrInTest(final String xpath, final String value) throws XPathExpressionException {
    return replaceAttr(test, xpath, null, value);
  }

  /**
   * Assert equality of the {@code control} and {@code test} elements in this {@link AssertXml} instance. If they are not, a
   * {@link ComparisonFailure}, without a message, and without its stack trace filtered, is thrown.
   * <p>
   * This method is equivalent to calling {@code assertEqual(false)}.
   *
   * @throws ComparisonFailure If the {@code control} and {@code test} elements in this {@link AssertXml} instance are not equal.
   */
  public void assertEqual() {
    assertEqual(null, false);
  }

  /**
   * Assert equality of the {@code control} and {@code test} elements in this {@link AssertXml} instance. If they are not, a
   * {@link ComparisonFailure} is thrown with the given message, and without its stack trace filtered.
   * <p>
   * This method is equivalent to calling {@code assertEqual(message, false)}.
   *
   * @param message The identifying message for the {@link ComparisonFailure} (null is okay).
   * @throws ComparisonFailure If the {@code control} and {@code test} elements in this {@link AssertXml} instance are not equal.
   */
  public void assertEqual(final String message) {
    assertEqual(message, false);
  }

  /**
   * Assert equality of the {@code control} and {@code test} elements in this {@link AssertXml} instance. If they are not, a
   * {@link ComparisonFailure} without a message is thrown.
   *
   * @param filterStacktrace If {@code true}, a {@link ComparisonFailure} will have its "test framework internal" stack trace
   *          elements removed, making the top stack trace element the test entrypoint.
   * @throws ComparisonFailure If the {@code control} and {@code test} elements in this {@link AssertXml} instance are not equal.
   */
  public void assertEqual(final boolean filterStacktrace) {
    assertEqual(null, filterStacktrace);
  }

  /**
   * Assert equality of the {@code control} and {@code test} elements in this {@link AssertXml} instance. If they are not, a
   * {@link ComparisonFailure} is thrown with the given message.
   *
   * @param message The identifying message for the {@link ComparisonFailure} (null is okay).
   * @param filterStacktrace If {@code true}, a {@link ComparisonFailure} will have its "test framework internal" stack trace
   *          elements removed, making the top stack trace element the test entrypoint.
   * @throws ComparisonFailure If the {@code control} and {@code test} elements in this {@link AssertXml} instance are not equal.
   */
  public void assertEqual(final String message, final boolean filterStacktrace) {
    final String prefix = control.getPrefix();
    final String controlXml = DOMs.domToString(control, DOMStyle.INDENT, DOMStyle.INDENT_ATTRS);
    final String testXml = DOMs.domToString(test, DOMStyle.INDENT, DOMStyle.INDENT_ATTRS);

    final Source controlSource = Input.fromString(controlXml).build();
    final Source testSource = Input.fromString(testXml).build();

    final DifferenceEngine diffEngine = new DOMDifferenceEngine();
    diffEngine.addDifferenceListener(new ComparisonListener() {
      @Override
      public void comparisonPerformed(final Comparison comparison, final ComparisonResult result) {
        final String xPath = comparison.getControlDetails().getXPath();
        if (xPath == null)
          return;

        final String controlXPath = ATTR_NAME_PATTERN.matcher(xPath).replaceAll("/" + prefix + ":$1");
        if (ATTR_MATCH_PATTERN.matcher(controlXPath).matches() || controlXPath.contains("text()"))
          return;

        try {
          assertEquals(message, controlXml, testXml);
        }
        catch (final ComparisonFailure e) {
          if (filterStacktrace) {
            final StackTraceElement[] stackTrace = e.getStackTrace();
            int i = 3;
            while (i < stackTrace.length)
              if (!stackTrace[i++].getClassName().startsWith(diffPackageName))
                break;

            final int length = stackTrace.length - ++i;
            final StackTraceElement[] filtered = new StackTraceElement[length];
            System.arraycopy(stackTrace, i, filtered, 0, length);
            e.setStackTrace(filtered);
          }

          throw e;
        }

        fail(comparison.toString());
      }
    });

    diffEngine.compare(controlSource, testSource);
  }
}