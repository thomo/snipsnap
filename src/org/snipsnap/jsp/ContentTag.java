/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://snipsnap.org/ for updates and contact.
 *
 * --LICENSE NOTICE--
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * --LICENSE NOTICE--
 */
package org.snipsnap.jsp;

import org.apache.taglibs.standard.lang.support.ExpressionEvaluatorManager;
import org.radeox.filter.Filter;
import org.radeox.filter.HtmlRemoveFilter;
import org.radeox.util.logging.Logger;
import snipsnap.api.snip.Snip;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.io.StringWriter;


/*
 * Tag that displays the content of a snip. Can remove HTML from the content
 * and generate an extract.
 *
 * @author Stephan J. Schmidt
 * 
 */

public class ContentTag extends TagSupport {
  snipsnap.api.snip.Snip snip = null;
  boolean extract = false;
  boolean removeHtml = false;
  boolean encodeHtml = false;

  public int doStartTag() throws JspException {
    if (null != snip) {
      String content = snip.getXMLContent();
      if (removeHtml) {
        Filter filter = new HtmlRemoveFilter();
        content = filter.filter(content, null);
        if (extract) {
          if (content.length() > 40) {
            content = content.substring(0, 40);
            int ampIndex = content.lastIndexOf("&");
            int colonIndex = content.lastIndexOf(";");
            // did we cut a entity like &x1212; ?
            if (ampIndex > colonIndex) {
              content = content.substring(0, ampIndex - 1);
            }
            content = content + " ...";
          }
        }
      }
      if (encodeHtml) {
        StringWriter stringWriter = new StringWriter();
        try {
          OutputFormat outputFormat = new OutputFormat();
          outputFormat.setNewlines(true);
          XMLWriter xmlWriter = new XMLWriter(stringWriter, outputFormat);
          xmlWriter.write(content);
          xmlWriter.flush();
        } catch (IOException e) {
          Logger.warn("ContentTag: unable to write encoded content: "+e);
        }
        content = stringWriter.toString();
      }
      try {
        JspWriter out = pageContext.getOut();
        out.print(content);
      } catch (IOException e) {
        Logger.warn("doStartTag in ContentTag", e);
      }
    }
    return super.doStartTag();
  }

  public void setEncode(boolean encode) {
    this.encodeHtml = encode;
  }

  public void setExtract(boolean extract) {
    this.extract = extract;
  }

  public void setRemoveHtml(boolean removeHtml) {
    this.removeHtml = removeHtml;
  }

  public void setSnip(String snip) {
    try {
      this.snip = (Snip) ExpressionEvaluatorManager.evaluate("snip", snip, Snip.class, this, pageContext);
    } catch (JspException e) {
      Logger.warn("unable to evaluate expression", e);
    }
  }
}
