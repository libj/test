/* Copyright (c) 2016 lib4j
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

import java.lang.annotation.Annotation;
import java.util.List;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

public class AccessibleRunner extends BlockJUnit4ClassRunner {
  public AccessibleRunner(final Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected TestClass createTestClass(final Class<?> testClass) {
    return new TestClass(testClass) {
      @Override
      public List<FrameworkField> getAnnotatedFields(final Class<? extends Annotation> annotationClass) {
        final List<FrameworkField> fields = super.getAnnotatedFields(annotationClass);
        for (final FrameworkField field : fields)
          field.getField().setAccessible(true);

        return fields;
      }

      @Override
      public List<FrameworkMethod> getAnnotatedMethods() {
        final List<FrameworkMethod> methods = super.getAnnotatedMethods();
        for (final FrameworkMethod method : methods)
          method.getMethod().setAccessible(true);

        return methods;
      }
    };
  }

  @Override
  protected void validateFields(final List<Throwable> errors) {
  }

  // This overrides the super method and removes the isPublic() check on @Before and @After methods
  @Override
  protected void validateInstanceMethods(List<Throwable> errors) {
    validateTestMethods(errors);

    if (computeTestMethods().size() == 0) {
      errors.add(new Exception("No runnable methods"));
    }
  }
}