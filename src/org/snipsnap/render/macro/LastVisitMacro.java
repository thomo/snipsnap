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

package org.snipsnap.render.macro;

import org.radeox.macro.Macro;
import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.i18n.ResourceManager;
import org.snipsnap.snip.Modified;
import snipsnap.api.user.User;
import org.snipsnap.user.UserManager;
import org.snipsnap.user.UserManagerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.ResourceBundle;

/*
 * Macro that show last logout time from user
 *
 * @author stephan
 * 
 */

public class LastVisitMacro extends BaseMacro {
  public String getName() {
    return "last-visit";
  }

  public String getDescription() {
    return ResourceManager.getString("i18n.messages", "macro.lastlogin.description");
  }

  public String[] getParamDescription() {
    return ResourceManager.getString("i18n.messages", "macro.lastlogin.params").split(";");
  }

  public void execute(Writer writer, MacroParameter params)
      throws IllegalArgumentException, IOException {

    if (params.getLength() == 1) {
      User user = UserManagerFactory.getInstance().load(params.get("0"));
      writer.write("<b>");
      writer.write(ResourceManager.getString("i18n.messages", "macro.lastvisit.lastvisit"));
      writer.write("</b> ");
      writer.write(Modified.getNiceTime(user.getLastLogout()));
    } else {
      throw new IllegalArgumentException("Number of arguments does not match");
    }
  }
}
