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

import org.radeox.util.logging.Logger;
import snipsnap.api.app.Application;
import snipsnap.api.config.Configuration;
import snipsnap.api.container.Components;
import org.snipsnap.container.SessionService;
import org.snipsnap.net.filter.MultipartWrapper;
import org.snipsnap.user.AuthenticationService;
import snipsnap.api.user.User;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet to login a user by checking user name and password.
 *
 * @author Matthias L. Jugel
 * 
 */
public class LoginServlet extends HttpServlet {
  private final static String ERR_PASSWORD = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    snipsnap.api.config.Configuration config = Application.get().getConfiguration();

    // If this is not a multipart/form-data request continue
    String type = request.getHeader("Content-Type");
    if (type != null && type.startsWith("multipart/form-data")) {
      try {
        request = new MultipartWrapper(request, config.getEncoding() != null ? config.getEncoding() : "UTF-8");
      } catch (IllegalArgumentException e) {
        Logger.warn("FileUploadServlet: multipart/form-data wrapper:" + e.getMessage());
      }
    }

    String login = request.getParameter("login");
    String password = request.getParameter("password");
    String referer = sanitize(request.getParameter("referer"));

    if (request.getParameter("cancel") == null) {
      snipsnap.api.user.User user = ((AuthenticationService) Components.getComponent(AuthenticationService.class)).authenticate(login, password);
      if (Application.getCurrentUsers().contains(user)) {
        Application.getCurrentUsers().remove(user);
      }

      HttpSession session = request.getSession();
      if (null == user) {
        request.setAttribute("tmpLogin", login);
        request.setAttribute("referer", referer);
        request.setAttribute("error", ERR_PASSWORD);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/exec/login.jsp");
        dispatcher.forward(request, response);
        return;
      }

      session.removeAttribute("referer");
      Application.get().setUser(user, session);

      SessionService service = (SessionService) Components.getComponent(SessionService.class);
      service.setUser(request, response, user);
    }

    response.sendRedirect(referer);
  }

  private String sanitize(String parameter) {
    if(parameter != null) {
      return parameter.split("[\r\n]")[0];
    }
    return parameter;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    String referer = request.getHeader("REFERER");
    if (referer == null || referer.length() == 0) {
      snipsnap.api.config.Configuration config = Application.get().getConfiguration();
      referer = config.getSnipUrl(config.getStartSnip());
    }

    if ("true".equals(request.getParameter("logoff"))) {
      HttpSession session = request.getSession();
      SessionService service = (SessionService) Components.getComponent(SessionService.class);
      service.removeCookie(request, response);
      // maybe not necessary
      // Application.removeCurrentUser(session);
      Application.get().setUser(null);
      session.invalidate();
    } else if ("true".equals(request.getParameter("timeout"))) {
      HttpSession session = request.getSession();
      Application.removeCurrentUser(session);
      session.invalidate();
    }

    response.sendRedirect(referer);
  }
}
