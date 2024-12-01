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
package org.snipsnap.net;

import snipsnap.api.app.Application;
import snipsnap.api.config.Configuration;
import snipsnap.api.container.Components;
import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipSpace;
import snipsnap.api.user.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to remove snips from the database.
 *
 * @author Matthias L. Jugel
 * 
 */
public class SnipRemoveServlet extends HttpServlet {
  public void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    Application app = Application.get();
    snipsnap.api.user.User user = app.getUser();
    Configuration config = app.getConfiguration();

    String name = request.getParameter("name");
    // TODO include check for current snip (see Access)
    if (user != null && user.isAdmin()) {
      snipsnap.api.snip.SnipSpace space = (SnipSpace) Components.getComponent(SnipSpace.class);
      Snip snip = space.load(name);

      // check for comment and remove from comment list
      if (snip.getCommentedSnip() != null) {
        snip.getCommentedSnip().getComments().getComments().remove(snip);
      }

      space.remove(snip);
      response.sendRedirect(config.getUrl("/space/" + config.getStartSnip()));
      return;
    }
    response.sendRedirect(config.getUrl("/space/" + name));
  }
}
