/* Copyright (c) 2016 FastJAX
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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;

/**
 * A LogBack Layout intended to be used for the maven-surefire-plugin that
 * modifies the output of log messages by prepending a yellow-colored "[TEST] "
 * prefix to each log entry. The implementation of this layout takes
 * consideration of the possibility that the layout may be modifying output from
 * a test run by an IDE. In this case, the message is not altered (this has only
 * been tested in Eclipse).
 *
 * @see <a href="https://logback.qos.ch/manual/layouts.html">LogBack Layouts</a>
 * @see <a href="https://maven.apache.org/surefire/maven-surefire-plugin/">Maven
 *      Surefire Plugin</a>
 */
public class SurefireTestLayout extends LayoutBase<ILoggingEvent> {
  private static final String RESET = "\033[0;39m";
  private static final String TEST = " [\033[0;36mTEST" + RESET + "] ";

  private final boolean inSurefireTest = System.getProperty("sun.java.command").contains("surefire");

  @Override
  public String doLayout(final ILoggingEvent event) {
    final String message = event.getFormattedMessage();
    return (inSurefireTest ? TEST + (message.contains("\n") ? message.replace("\n", "\n" + TEST) : message) : event.getFormattedMessage()) + "\n";
  }
}