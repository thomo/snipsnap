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

import snipsnap.api.config.Configuration;
import org.snipsnap.config.ConfigurationProxy;
import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipSpaceFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Display information about the weblog for
 * really simple discovery,
 * http://archipelago.phrasewise.com/stories/storyReader$1330
 *
 * @author Stephan J. Schmidt
 * 
 */
public class RsdServlet extends HttpServlet {
  private Configuration config;
  private String startName;

  public void init(ServletConfig servletConfig) throws ServletException {
    config = ConfigurationProxy.getInstance();
 }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    Snip snip = SnipSpaceFactory.getInstance().getBlog().getSnip();

    request.setAttribute("snip", snip);
    request.setAttribute("space", SnipSpaceFactory.getInstance());
    request.setAttribute("config", config);

    request.setAttribute("url", config.getUrl("/space"));
    request.setAttribute("baseurl", config.getUrl());

    RequestDispatcher dispatcher = request.getRequestDispatcher("/rsd.jsp");
    dispatcher.forward(request, response);
  }
}