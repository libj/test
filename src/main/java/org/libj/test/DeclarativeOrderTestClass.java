/* Copyright (c) 2023 LibJ
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.libj.lang.Classes;

/**
 * A subclass of {@link TestClass} that sorts test method execution in the order of declaration of the test methods in the source of
 * the test class. The use of this class overrides the effect of the {@link org.junit.FixMethodOrder} annotation.
 *
 * @implNote Javassist must be present on the system classpath, and line number information must be available in the bytecode of the
 *           test class.
 */
public class DeclarativeOrderTestClass extends TestClass {
  public DeclarativeOrderTestClass(final Class<?> clazz) {
    super(clazz);
  }

  @Override
  protected void scanAnnotatedMembers(final Map<Class<? extends Annotation>,List<FrameworkMethod>> methodsForAnnotations, final Map<Class<? extends Annotation>,List<FrameworkField>> fieldsForAnnotations) {
    final LinkedHashMap<Class<? extends Annotation>,List<FrameworkMethod>> temp = new LinkedHashMap<>();
    super.scanAnnotatedMembers(temp, fieldsForAnnotations);

    for (final List<FrameworkMethod> frameworkMethods : temp.values()) { // [C]
      final Method[] methods = new Method[frameworkMethods.size()];
      for (int i = 0, i$ = frameworkMethods.size(); i < i$; ++i) // [RA]
        methods[i] = frameworkMethods.get(i).getMethod();

      try {
        Classes.sortDeclarativeOrder(methods, true);
      }
      catch (final ClassNotFoundException e) {
        throw new IllegalStateException("Javassist is not present on the system classpath", e);
      }

      for (final Method method : methods) // [A]
        addToAnnotationLists(new FrameworkMethod(method), methodsForAnnotations);
    }
  }
}