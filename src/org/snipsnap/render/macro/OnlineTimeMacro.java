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
import snipsnap.api.app.Application;
import org.snipsnap.snip.Modified;
import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipSpaceFactory;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

/*
 * Macro that displays how long the SnipSnap is online (installed)
 *
 * @author stephan
 * @team sonicteam
 * 
 */

public class OnlineTimeMacro extends BaseMacro {
  public String getName() {
    return "online-time";
  }

  public String getDescription() {
    return ResourceManager.getString("i18n.messages", "macro.onlinetime.description");
  }

  public void execute(Writer writer, MacroParameter params)
      throws IllegalArgumentException, IOException {

    snipsnap.api.snip.Snip snip = SnipSpaceFactory.getInstance().load(snipsnap.api.app.Application.get().getConfiguration().getStartSnip());
    MessageFormat mf = new MessageFormat(ResourceManager.getString("i18n.messages", "macro.onlinetime.age"),
                                         ResourceManager.getLocale("i18n.messages"));
    writer.write(mf.format(new Object[]{Modified.getNiceTime(snip.getModified().getcTime())}));
  }
}
