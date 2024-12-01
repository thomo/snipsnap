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
package org.snipsnap.admin;

import org.apache.xmlrpc.XmlRpcException;
import org.snipsnap.config.ServerConfiguration;
import org.snipsnap.net.filter.EncRequestWrapper;
import org.snipsnap.server.AdminXmlRpcClient;
import snipsnap.api.config.Configuration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;

public class AdminInitFilter implements Filter {

  private final static String DEFAULT_ENCODING = "UTF-8";

  protected final static String ATT_AUTHENTICATED = "authenticated";
  protected final static String ATT_SERVERCONFIG = "serverconfig";
  protected final static String ATT_CONFIG = "config";
  protected final static String ATT_STEP = "step";
  protected final static String ATT_ERRORS = "errors";
  protected final static String ATT_APPS = "applications";

  protected final static String PARAM_INSTALL = "install";
  protected final static String PARAM_EXPERT = "expert";

  protected Preferences serverConf = null;
  protected AdminXmlRpcClient adminClient;

  public void init(FilterConfig config) throws ServletException {
    serverConf = (Preferences) config.getServletContext().getAttribute("server.config");
    try {
      String url = serverConf.get(ServerConfiguration.ADMIN_URL, null);
      String user = serverConf.get(ServerConfiguration.ADMIN_USER, null);
      String pass = serverConf.get(ServerConfiguration.ADMIN_PASS, null);
      adminClient = new AdminXmlRpcClient(url, user, pass);
    } catch (Exception e) {
      System.out.println("!! Unable to create XML-RPC client, check system preferences: " + serverConf);
      throw new ServletException(e);
    }
  }

  public void destroy() {
    //config = null;
  }

  public void doFilter(ServletRequest req, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    // make sure it's an http servlet request
    HttpServletRequest request = (HttpServletRequest) req;

    // make sure the request has a correct character encoding
    // the enc-wrapper ensures some methods return correct strings too
    try {
      request.setCharacterEncoding(DEFAULT_ENCODING);
      request = new EncRequestWrapper(request, request.getCharacterEncoding());
    } catch (UnsupportedEncodingException e) {
      System.err.println("AdminInitFilter: unsupported encoding: " + e);
    }

    // get or create session and application object
    HttpSession session = request.getSession();
    Properties config = null;
    Map errors = new HashMap();

    String path = request.getServletPath();
    if (null == path || "".equals(path)) {
      System.out.println("Redirecting '" + path + "' -> " + request.getContextPath() + "/");
      ((HttpServletResponse) response).sendRedirect(request.getContextPath() + "/");
      return;
    }

    // except css files and images everything is protected
    if (!(path.startsWith("/images") || path.endsWith(".css"))) {
      RequestDispatcher dispatcher = request.getRequestDispatcher("main.jsp");
      String step = null;

      // check authentication and verify session
      if (!"true".equals(session.getAttribute(ATT_AUTHENTICATED))) {
        String serverPass = (String) serverConf.get(ServerConfiguration.ADMIN_PASS, "");
        String installPass = path;
        if ("".equals(installPass) || "/".equals(installPass)) {
          installPass = "/" + request.getParameter("password");
        }

        if ("".equals(installPass) || !serverPass.equals(installPass.substring(1))) {
          step = "login";
        } else {
          session.setAttribute(ATT_AUTHENTICATED, "true");
        }
      }

      Map applications = null;
      if (null == step) {
        config = (Properties) session.getAttribute(ATT_CONFIG);
        if (null == config) {
          config = new Properties();
          config.load(AdminInitFilter.class.getResourceAsStream("/org/snipsnap/config/globals.conf"));
        }

        String host = request.getParameter(Configuration.APP_HOST);
        String port = request.getParameter(Configuration.APP_PORT);
        String contextPath = request.getParameter(Configuration.APP_PATH);

        if (null != host && !"".equals(host)) {
          config.setProperty(Configuration.APP_HOST, host);
        }
        if (null != port && !"".equals(port)) {
          config.setProperty(snipsnap.api.config.Configuration.APP_PORT, port);
        }
        if (null != contextPath && !"".equals(contextPath)) {
          config.setProperty(Configuration.APP_PATH, contextPath);
        }

        try {
          applications = adminClient.getApplications();
          request.setAttribute(ATT_APPS, new Integer(applications.size()));
        } catch (XmlRpcException e) {
          System.err.println("AdminInitFilter: error retrieving existing applications: " + e);
          e.printStackTrace();
        } catch (IOException e) {
          System.err.println("AdminInitFilter: unable to contact server: " + e);
          e.printStackTrace();
        }

        if (null != request.getParameter(PARAM_EXPERT) ||
                (null == request.getParameter(PARAM_INSTALL) && (applications != null && applications.size() > 0))) {
          step = "install";
        } else {
          URL url;
          try {
            url = install(config.getProperty(Configuration.APP_HOST),
                          config.getProperty(Configuration.APP_PORT),
                          config.getProperty(Configuration.APP_PATH));
            if (url != null) {
              ((HttpServletResponse) response).sendRedirect(url.toString());
              session.removeAttribute(ATT_CONFIG);
              return;
            }
          } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
            System.out.println(e.getCause());
            errors.put(e.getMessage(), "unknown");
          }
          step = "install";
        }
      }

      session.setAttribute(ATT_CONFIG, config);
      request.setAttribute(ATT_ERRORS, errors);
      request.setAttribute(ATT_STEP, step);
      dispatcher.forward(request, response);
      return;
    }

    // apply the chain
    chain.doFilter(request, response);
  }

  protected URL install(String host, String port, String path) throws Exception {
    //System.out.println("install("+host+", "+port+", "+path+")");
    try {
      return adminClient.install(host + "_" + port + "_" + path.replace('/', '_'), host, port, path);
    } catch (XmlRpcException e) {
      throw e;
    }
  }
}
