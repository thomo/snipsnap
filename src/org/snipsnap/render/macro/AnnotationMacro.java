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
 * 
 */

package org.snipsnap.render.macro;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.i18n.ResourceManager;

import java.io.IOException;
import java.io.Writer;

public class AnnotationMacro extends BaseMacro {
  public String getName() {
    return "note";
  }

  public String getDescription() {
    return ResourceManager.getString("i18n.messages", "macro.annotation.description");
  }

  public String[] getParamDescription() {
    return ResourceManager.getString("i18n.messages", "macro.annotation.params").split(";");
  }

  public void execute(Writer writer, MacroParameter params)
      throws IllegalArgumentException, IOException {
    if (params.getLength() == 1) {
      writer.write("<footnote>");
      writer.write(params.get("0"));
      writer.write("</footnote>");
    } else {
      throw new IllegalArgumentException("footnote needs exactly one argument");
    }
    return;
  }
}
