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
/*
 * Macro that replaces external links
 *
 * @author stephan
 * @team sonicteam
 * @version $Id$
 */

package org.snipsnap.snip.filter.macro;

import org.snipsnap.snip.Snip;
import org.snipsnap.snip.SnipLink;

import java.io.IOException;
import java.io.Writer;

public class FieldMacro extends Macro {
  public String getName() {
    return "field";
  }

  /**
   * {field:id|value|target|button}
   */
  public void execute(Writer writer, String[] params, String content, Snip snip)
      throws IllegalArgumentException, IOException {

    if (params != null && params.length > 0) {
      writer.write("<form id=\"form\" action=\"");
      if(params.length >= 3) {
        SnipLink.appendUrl(writer, params[2]);
      } else {
        SnipLink.appendUrl(writer, snip.getName());
      }
      writer.write("\" method=\"get\">");
      writer.write("<input size=\"18\" name=\"");
      writer.write(params[0]);
      writer.write("\"");
      if (params.length >= 2) {
        writer.write(" value=\"");
        writer.write(params[1]);
        writer.write("\"");
      }
      writer.write("/>");
      if(params.length >= 4) {
        writer.write(" <input type=\"submit\" name=\"submit\" value=\"");
        writer.write(params[3]);
        writer.write("\"/>");
      }
      writer.write("</form>");
      return;
    } else {
      throw new IllegalArgumentException("Number of arguments does not match");
    }
  }
}