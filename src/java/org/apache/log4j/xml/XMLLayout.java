/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// Contributors:   Mathias Bogaert
package org.apache.log4j.xml;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.Transform;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Set;


/**
 * The output of the XMLLayout consists of a series of log4j:event
 * elements as defined in the <a
 * href="doc-files/log4j.dtd">log4j.dtd</a>. It does not output a
 * complete well-formed XML file. The output is designed to be
 * included as an <em>external entity</em> in a separate file to form
 * a correct XML file.
 *
 * <p>For example, if <code>abc</code> is the name of the file where
 * the XMLLayout ouput goes, then a well-formed XML file would be:
 *
  <pre>
   &lt;?xml version="1.0" ?&gt;

  &lt;!DOCTYPE log4j:eventSet SYSTEM "log4j.dtd" [&lt;!ENTITY data SYSTEM "abc"&gt;]&gt;

  &lt;log4j:eventSet version="1.2" xmlns:log4j="http://jakarta.apache.org/log4j/"&gt;
         &nbsp;&nbsp;&data;
  &lt;/log4j:eventSet&gt;
  </pre>

 * <p>This approach enforces the independence of the XMLLayout and the
 * appender where it is embedded.
 *
 * <p>The <code>version</code> attribute helps components to correctly
 * intrepret output generated by XMLLayout. The value of this
 * attribute should be "1.1" for output generated by log4j versions
 * prior to log4j 1.2 (final release) and "1.2" for relase 1.2 and
 * later.
 *
 * @author Ceki  G&uuml;lc&uuml;
 * @since 0.9.0
 * */
public class XMLLayout extends Layout {

  private boolean locationInfo = false;

  /**
   * The <b>LocationInfo</b> option takes a boolean value. By default,
   * it is set to false which means there will be no location
   * information output by this layout. If the the option is set to
   * true, then the file name and line number of the statement at the
   * origin of the log statement will be output.
   *
   * <p>If you are embedding this layout within an {@link
   * org.apache.log4j.net.SMTPAppender} then make sure to set the
   * <b>LocationInfo</b> option of that appender as well.
   * */
  public void setLocationInfo(boolean flag) {
    locationInfo = flag;
  }

  /**
     Returns the current value of the <b>LocationInfo</b> option.
   */
  public boolean getLocationInfo() {
    return locationInfo;
  }

  /** No options to activate. */
  public void activateOptions() {
  }

  /**
   * Formats a {@link LoggingEvent} in conformance with the log4j.dtd.
   * */
  public void format(Writer output, LoggingEvent event)
    throws IOException {
    // We yield to the \r\n heresy.
    output.write("<log4j:event logger=\"");
    output.write(event.getLoggerName());
    output.write("\" timestamp=\"");
    output.write(Long.toString(event.getTimeStamp()));
    output.write("\" sequenceNumber=\"");
    output.write(Long.toString(event.getSequenceNumber()));
    output.write("\" level=\"");
    output.write(event.getLevel().toString());
    output.write("\" thread=\"");
    output.write(event.getThreadName());
    output.write("\">\r\n");

    output.write("<log4j:message><![CDATA[");

    // Append the rendered message. Also make sure to escape any
    // existing CDATA sections.
    Transform.appendEscapingCDATA(output, event.getRenderedMessage());
    output.write("]]></log4j:message>\r\n");

    String ndc = event.getNDC();

    if (ndc != null) {
      output.write("<log4j:NDC><![CDATA[");
      output.write(ndc);
      output.write("]]></log4j:NDC>\r\n");
    }

//    Set mdcKeySet = event.getMDCKeySet();
//
//    if ((mdcKeySet != null) && (mdcKeySet.size() > 0)) {
//      /**
//      * Normally a sort isn't required, but for Test Case purposes
//      * we need to guarantee a particular order.
//      *
//      * Besides which, from a human readable point of view, the sorting
//      * of the keys is kinda nice..
//      */
//      List sortedList = new ArrayList(mdcKeySet);
//      Collections.sort(sortedList);
//
//      output.write("<log4j:MDC>\r\n");
//
//      Iterator iter = sortedList.iterator();
//
//      while (iter.hasNext()) {
//        String propName = iter.next().toString();
//       	output.write("    <log4j:data name=\"" + propName);
//
//        String propValue = event.getMDC(propName).toString();
//        output.write("\" value=\"" + propValue);
//        output.write("\"/>\r\n");
//      }
//
//      output.write("</log4j:MDC>\r\n");
//    }

    String[] s = event.getThrowableStrRep();

    if (s != null) {
      output.write("<log4j:throwable><![CDATA[");

      for (int i = 0; i < s.length; i++) {
        output.write(s[i]);
        output.write("\r\n");
      }

      output.write("]]></log4j:throwable>\r\n");
    }

    if (locationInfo) {
      LocationInfo locationInfo = event.getLocationInformation();
      output.write("<log4j:locationInfo class=\"");
      output.write(locationInfo.getClassName());
      output.write("\" method=\"");
      Transform.escapeTags(locationInfo.getMethodName(), output);
      output.write("\" file=\"");
      output.write(locationInfo.getFileName());
      output.write("\" line=\"");
      output.write(locationInfo.getLineNumber());
      output.write("\"/>\r\n");
    }

    Set propertySet = event.getPropertyKeySet();

    if ((propertySet != null) && (propertySet.size() > 0)) {
      output.write("<log4j:properties>\r\n");

      Iterator propIter = propertySet.iterator();

      while (propIter.hasNext()) {
        String propName = propIter.next().toString();
        output.write("    <log4j:data name=\"" + propName);

        String propValue = event.getProperty(propName).toString();
        output.write("\" value=\"" + propValue);
        output.write("\"/>\r\n");
      }

      output.write("</log4j:properties>\r\n");
    }

    output.write("</log4j:event>\r\n\r\n");
  }

  /**
     The XMLLayout prints and does not ignore exceptions. Hence the
     return value <code>false</code>.
  */
  public boolean ignoresThrowable() {
    return false;
  }
}
