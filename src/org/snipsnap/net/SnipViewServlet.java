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
import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipSpace;
import org.snipsnap.snip.label.TypeLabel;
import org.snipsnap.user.AuthenticationService;
import org.snipsnap.user.Roles;
import snipsnap.api.user.User;
import org.snipsnap.util.URLEncoderDecoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Load a snip to view.
 *
 * @author Matthias L. Jugel
 * 
 */
public class SnipViewServlet extends HttpServlet {
  private final static Roles authRoles = new Roles(Roles.AUTHENTICATED);

  protected void doHead(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    doGet(request, response);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
          throws IOException, ServletException {

    Configuration config = Application.get().getConfiguration();
    snipsnap.api.user.User user = snipsnap.api.app.Application.get().getUser();
    AuthenticationService service = (AuthenticationService) snipsnap.api.container.Components.getComponent(AuthenticationService.class);

    if (service.isAuthenticated(user)) {
      user.lastAccess();
    }

    // handle the snip name
    String name = request.getPathInfo();
    if (null == name || "/".equals(name)) {
      name = config.getStartSnip();
    } else {
      name = name.substring(1);
    }
    String encodedSpace = config.getEncodedSpace();
    if (encodedSpace != null && encodedSpace.length() > 0) {
      name = name.replace(encodedSpace.charAt(0), ' ');
    }
//    System.out.println("name='"+name+"'");

    // load snip and set attributes for request
    SnipSpace space = (snipsnap.api.snip.SnipSpace) Components.getComponent(snipsnap.api.snip.SnipSpace.class);
    snipsnap.api.snip.Snip snip = space.load(name);

    String subname = null;
    if (null == snip) {
      // handle attachments
      int slashIndex = name.lastIndexOf('/');
      if (slashIndex != -1) {
        subname = name.substring(slashIndex + 1);
        name = name.substring(0, slashIndex);
        Logger.log(Logger.DEBUG, name + ": attachment: " + subname);
      }
      snip = space.load(name);
    }

    request.setAttribute("snip", snip);
//    request.setAttribute("URI", request.getRequestURL().toString());

    if (subname != null && subname.length() > 0) {
      try {
        request.setAttribute(FileDownloadServlet.FILENAME, subname);
        RequestDispatcher dispatcher =
                getServletContext().getNamedDispatcher("org.snipsnap.net.FileDownloadServlet");
        dispatcher.forward(request, response);
        return;
      } catch (ServletException e) {
        // jump to the not found page
        name = name + "/" + subname;
        snip = null;
      }
    }

    // stop special processing for HEAD requests
    if ("HEAD".equals(request.getMethod())) {
      response.setStatus(HttpServletResponse.SC_OK);
      return;
    }

    // Snip does not exist
    if (null == snip) {
      if (config.allow(Configuration.APP_PERM_CREATESNIP)) {
        response.sendRedirect(config.getUrl("/exec/edit?name=" + URLEncoderDecoder.encode(name, config.getEncoding())));
      } else {
        if ("snipsnap-notfound".equals(name)) {
          response.sendError(HttpServletResponse.SC_NOT_FOUND,
                             "Internal Error: could not find snipsnap-notfound page."
                             + "This may indicate that either the installation has failed or the Database is corrupted.");
          return;
        }
        response.sendRedirect(config.getUrl("/space/snipsnap-notfound?name=" + URLEncoderDecoder.encode(name, config.getEncoding())));
      }
      return;
    }

    String viewHandler = null;
    String type = null;
    Collection mimeTypes = snip.getLabels().getLabels("TypeLabel");
    if (!mimeTypes.isEmpty()) {
      Iterator handlerIt = mimeTypes.iterator();
      while (handlerIt.hasNext()) {
        TypeLabel typeLabel = (TypeLabel) handlerIt.next();
        viewHandler = typeLabel.getViewHandler();
        // search for default handler if non found
        if (null == viewHandler) {
          viewHandler = TypeLabel.getViewHandler(typeLabel.getTypeValue());
        }

        if (null != viewHandler) {
          type = typeLabel.getTypeValue();
          request.setAttribute("view_handler", viewHandler);
          request.setAttribute("mime_type", type);
          break;
        }
      }
    }

    Application app = Application.get();
    Map params = app.getParameters();
    params.put("viewed", snip);
    params.put("RSS", params.get("RSS") + "?snip=" + snip.getNameEncoded());

    snip.handle(request);
    RequestDispatcher dispatcher = request.getRequestDispatcher("/exec/snip.jsp");
    dispatcher.forward(request, response);
  }
}
