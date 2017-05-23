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

package org.lib4j.test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

public class SurefireTestLayout extends LayoutBase<ILoggingEvent> {
  private static final String TEST = "[1;35m[TEST][0;39m ";
  @Override
  public String doLayout(final ILoggingEvent event) {
    final String message = event.getFormattedMessage();
    return (System.getProperty("sun.java.command").contains("surefire") ? TEST + (message.contains("\n") ? message.replace("\n", "\n" + TEST) : message) : event.getFormattedMessage()) + "\n";
  }
}