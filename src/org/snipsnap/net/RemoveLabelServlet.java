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

import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipLink;
import snipsnap.api.snip.SnipSpaceFactory;
import snipsnap.api.app.Application;
import snipsnap.api.config.Configuration;
import org.snipsnap.net.filter.MultipartWrapper;
import org.radeox.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Removing a label from a snip
 * @author Marco Mosconi
 * 
 */
public class RemoveLabelServlet extends HttpServlet {
  protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    throws ServletException, IOException {
    doGet(httpServletRequest, httpServletResponse);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    snipsnap.api.config.Configuration config = snipsnap.api.app.Application.get().getConfiguration();
    // If this is not a multipart/form-data request continue
    String type = request.getHeader("Content-Type");
    if (type != null && type.startsWith("multipart/form-data")) {
      try {
        request = new MultipartWrapper(request, config.getEncoding() != null ? config.getEncoding() : "UTF-8");
      } catch (IllegalArgumentException e) {
        Logger.warn("RemoveLabelServlet: multipart/form-data wrapper:" + e.getMessage());
      }
    }

    String snipName = request.getParameter("snipname");
    if (null == snipName) {
      response.sendRedirect(config.getUrl("/space/" + config.getStartSnip()));
      return;
    }
    // cancel pressed
    if (null != request.getParameter("cancel")) {
      response.sendRedirect(config.getUrl("/exec/labels?snipname=" + snipsnap.api.snip.SnipLink.encode(snipName)));
      return;
    }

    snipsnap.api.snip.Snip snip = SnipSpaceFactory.getInstance().load(snipName);
    String labelName = request.getParameter("labelname");
    String labelValue = request.getParameter("labelvalue");
    snip.getLabels().removeLabel(labelName, labelValue);

    RequestDispatcher dispatcher = request.getRequestDispatcher("/exec/labels?snipname=" + snipsnap.api.snip.SnipLink.encode(snipName));
    dispatcher.forward(request, response);
  }
}
