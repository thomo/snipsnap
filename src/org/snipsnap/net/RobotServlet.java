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

import snipsnap.api.snip.SnipSpace;
import snipsnap.api.snip.Snip;
import snipsnap.api.container.Components;
import snipsnap.api.config.Configuration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet to login recognise a robot. This servlet is mapped to robots.txt
 * and updates the list of robot addresses in usermanager.
 * @author Matthias L. Jugel
 * 
 */
public class RobotServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String name = request.getHeader("User-Agent");
    String host = request.getRemoteHost();

    SnipSpace space = (SnipSpace) snipsnap.api.container.Components.getComponent(SnipSpace.class);
    if(space.exists(Configuration.SNIPSNAP_CONFIG_ROBOTS_TXT)) {
      snipsnap.api.snip.Snip robotstxt = space.load(Configuration.SNIPSNAP_CONFIG_ROBOTS_TXT);
      PrintWriter writer = new PrintWriter(response.getOutputStream());
      writer.println(robotstxt.getContent());
      writer.flush();
      // TODO add denied robots from SNIPSNAP_CONFIG_ROBOTS
    } else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
  }
}
