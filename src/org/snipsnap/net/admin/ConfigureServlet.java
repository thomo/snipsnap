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
package org.snipsnap.net.admin;

import org.radeox.util.logging.Logger;
import org.snipsnap.app.ApplicationManager;
import org.snipsnap.config.ConfigurationManager;
import org.snipsnap.config.ConfigurationProxy;
import org.snipsnap.config.InitializeDatabase;
import org.snipsnap.net.filter.MultipartWrapper;
import snipsnap.api.config.Configuration;
import snipsnap.api.container.Components;
import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipSpace;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * @author Matthias L. Jugel
 * 
 */
public class ConfigureServlet extends HttpServlet {

  protected final static String ATT_PREFIX = "prefix";
  protected final static String ATT_KEY = "key";
  protected final static String ATT_APPLICATION = "app";
  protected final static String ATT_CONFIG = "newconfig";
  protected final static String ATT_GLOBAL_CONFIG = "global";
  protected final static String ATT_VALIDATOR = "validator";
  protected final static String ATT_ADVANCED = "advanced";
  protected final static String ATT_DEFAULTS = "defaults";
  protected final static String ATT_USAGE = "usage";
  protected final static String ATT_FINISH = "finish";
  protected final static String ATT_STEPS = "steps";
  protected final static String ATT_STEP = "step";
  protected final static String ATT_ERRORS = "errors";
  protected final static String ATT_USER = "configuser";

  protected final static String STEP_LOGIN = "login";

  protected final static String STEP_APPLICATION = "application";
  protected final static String STEP_THEME = "theme";
  protected final static String STEP_ADMINISTRATOR = "administrator";
  protected final static String STEP_LOCALIZATION = "localization";
  protected final static String STEP_PERMISSIONS = "permissions";
  protected final static String STEP_MAIL = "mail";
  protected final static String STEP_MOBLOG = "moblog";
  protected final static String STEP_PROXY = "proxy";
  protected final static String STEP_DATABASE = "database";
  protected final static String STEP_EXPERT = "expert";
  protected final static String STEP_FINISH = "finish";

  protected final static String STEP_IMPORT = "import";
  protected final static String STEP_EXPORT = "export";
  protected final static String STEP_USERS = "users";
  protected final static String STEP_SEARCH = "search";
  protected final static String STEP_MAINTENANCE = "maintenance";

  private final static List LOGIN_STEPS = Arrays.asList(new String[]{
          STEP_LOGIN
  });

  private final static List BASIC_STEPS = Arrays.asList(new String[]{
          STEP_ADMINISTRATOR,
          STEP_APPLICATION,
          STEP_FINISH,
  });

  private final static List EXPERT_STEPS = Arrays.asList(new String[]{
          STEP_THEME,
          STEP_LOCALIZATION,
          STEP_PERMISSIONS,
          STEP_MAIL,
          STEP_MOBLOG,
          STEP_PROXY,
          STEP_EXPERT,
  });

  private final static List CONFIG_STEPS = Arrays.asList(new String[]{
          STEP_USERS,
          STEP_IMPORT,
          STEP_EXPORT,
          STEP_SEARCH,
          STEP_MAINTENANCE
  });

  private final static List HANDLERS = Arrays.asList(new String[]{
          "org.snipsnap.net.admin.Maintenance",
          "org.snipsnap.net.admin.DatabaseExport",
          "org.snipsnap.net.admin.DatabaseImport",
          "org.snipsnap.net.admin.ManageSearchEngine",
          "org.snipsnap.net.admin.ManageUsers",
          "org.snipsnap.net.admin.SetupAdministrator",
          "org.snipsnap.net.admin.SetupApplication",
          "org.snipsnap.net.admin.SetupDatabase",
          "org.snipsnap.net.admin.SetupExpert",
          "org.snipsnap.net.admin.SetupLocalization",
          "org.snipsnap.net.admin.SetupMail",
          "org.snipsnap.net.admin.SetupMoblog",
          "org.snipsnap.net.admin.SetupPermissions",
          "org.snipsnap.net.admin.SetupProxy",
          "org.snipsnap.net.admin.SetupTheme"
  });

  Map handlers = new HashMap();

  public void init() throws ServletException {
    super.init();
    Iterator handlerIt = HANDLERS.iterator();
    while (handlerIt.hasNext()) {
      String className = (String) handlerIt.next();
      try {
        SetupHandler handler = (SetupHandler) Class.forName(className).newInstance();
        String handlerName = handler.getName();
        handlers.put(handlerName, handler);
        System.err.println("ConfigureServlet: added setup handler: " + handlerName);
      } catch (Exception e) {
        System.err.println("ConfigureServlet: unable to instantiate setup handler: " + e);
        e.printStackTrace();
      }
    }
  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {

    // If this is not a multipart/form-data request continue
    String type = request.getHeader("Content-Type");
    if (type != null && type.startsWith("multipart/form-data")) {
      try {
        request = new MultipartWrapper(request, "UTF-8");
      } catch (IllegalArgumentException e) {
        Logger.warn("ConfigureServlet: multipart/form-data wrapper:" + e.getMessage());
      }
    }

    HttpSession session = request.getSession();
    Configuration config = (Configuration) session.getAttribute(ATT_CONFIG);

    String prefix = request.getParameter(ATT_PREFIX);
    if (null == prefix) {
      prefix = (String) request.getAttribute(snipsnap.api.config.Configuration.APP_PREFIX);
    }
    if (prefix != null && config != null && !prefix.equals(config.getPrefix())) {
      session.removeAttribute(ATT_CONFIG);
      config = null;
    }

    ApplicationManager appManager;
    String appOid;
    if (config == null && ConfigurationProxy.getInstance().isInstalled()) {
      appManager = (ApplicationManager) Components.getComponent(ApplicationManager.class);
      appOid = appManager.getApplication(prefix != null && !"".equals(prefix) ? prefix : "/");
      Configuration existingConfig = ConfigurationManager.getInstance().getConfiguration(appOid);
      session.removeAttribute(ATT_USER);
      session.removeAttribute(ATT_STEP);
      session.removeAttribute(ATT_STEPS);
      if (existingConfig != null) {
        // make a copy and work on it as long as we are changing
        config = ConfigurationProxy.newInstance(existingConfig);
      }
    }

    if (null == config) {
      config = ConfigurationProxy.newInstance();
      if (prefix != null && !"".equals(prefix)) {
        if (!prefix.startsWith("/")) {
          prefix = "/" + prefix;
        }
        config.setPrefix(prefix);
      }
    }

    request.setAttribute(ATT_PREFIX, prefix);

    // TODO same as in InitFilter
    snipsnap.api.app.Application app = snipsnap.api.app.Application.get();
    String xForwardedHost = request.getHeader("X-Forwarded-Host");
    if (xForwardedHost != null) {
      String protocol = config.get(snipsnap.api.config.Configuration.APP_REAL_PROTOCOL, "http");
      String contextPath = config.get(Configuration.APP_REAL_PATH, "");

      int colonIndex = xForwardedHost.indexOf(':');
      String host = xForwardedHost;
      if (colonIndex != -1) {
        host = host.substring(0, colonIndex);
        int port = Integer.parseInt(xForwardedHost.substring(colonIndex + 1));
        app.storeObject(snipsnap.api.app.Application.URL, new URL(protocol, host, port, contextPath));
      } else {
        app.storeObject(snipsnap.api.app.Application.URL, new URL(protocol, host, contextPath));
      }
    } else {
      String protocol = new URL(request.getRequestURL().toString()).getProtocol();
      String host = request.getServerName();
      int port = request.getServerPort();
      String contextPath = request.getContextPath() + ("/".equals(prefix) ? "" : prefix);

      app.storeObject(snipsnap.api.app.Application.URL, new URL(protocol, host, port, contextPath));
    }

    snipsnap.api.app.Application.get().setConfiguration(config);
    session.setAttribute(ATT_CONFIG, config);

//    if (config.allow(Configuration.APP_PERM_REGISTER) &&
//      config.allow(Configuration.APP_PERM_WEBLOGSPING)) {
//      session.setAttribute(ATT_USAGE, "public");
//    } else if (!config.allow(Configuration.APP_PERM_REGISTER) &&
//      !config.allow(Configuration.APP_PERM_WEBLOGSPING)) {
//      session.setAttribute(ATT_USAGE, "closed");
//    } else if (!config.allow(Configuration.APP_PERM_WEBLOGSPING)) {
//      session.setAttribute(ATT_USAGE, "intranet");
//    } else {
//      session.setAttribute(ATT_USAGE, "custom");
//    }

    snipsnap.api.user.User user = snipsnap.api.app.Application.get().getUser();
    if (!config.isInstalled() || !config.isConfigured() || user.isAdmin()) {
      List steps = (List) session.getAttribute(ATT_STEPS);
      if (steps == null) {
        if (config.isConfigured() && null != user && user.isAdmin()) {
          steps = new ArrayList(BASIC_STEPS);
          steps.addAll(EXPERT_STEPS);
          steps.addAll(CONFIG_STEPS);
          steps.remove(STEP_FINISH);
          steps.remove(STEP_ADMINISTRATOR);
          session.setAttribute(ATT_ADVANCED, "true");
          session.setAttribute(ATT_DEFAULTS, "true");
          session.setAttribute(ATT_USER, user);
        } else {
          String installKey = (String) session.getAttribute(snipsnap.api.config.Configuration.APP_INSTALL_KEY);
          if (null == installKey) {
            installKey = request.getParameter("key");
            if (null == installKey || !config.getInstallKey().equals(installKey)) {
              request.setAttribute("steps", LOGIN_STEPS);
              request.setAttribute("step", "login");
              RequestDispatcher dispatcher = request.getRequestDispatcher("/admin/configure.jsp");
              dispatcher.forward(request, response);
              session.removeAttribute(ATT_CONFIG);
              return;
            }
            session.setAttribute(snipsnap.api.config.Configuration.APP_INSTALL_KEY, installKey);
          }

          steps = new ArrayList(BASIC_STEPS);
          if (!config.isInstalled()) {
            steps.add(0, STEP_DATABASE);
          }

          session.setAttribute(ATT_USAGE, "public");
        }
      }

      if (session.getAttribute(ATT_DEFAULTS) == null) {
        Locale locale = request.getLocale();
        if (null != locale.getCountry() && !"".equals(locale.getCountry())) {
          config.set(snipsnap.api.config.Configuration.APP_COUNTRY, locale.getCountry());
        }
        if (null != locale.getLanguage() && !"".equals(locale.getLanguage())) {
          config.set(snipsnap.api.config.Configuration.APP_LANGUAGE, locale.getLanguage());
        }
        // divide offset (ms) by an hour
        int offset = TimeZone.getDefault().getRawOffset() / (60 * 60 * 1000);
        String id = "GMT" + (offset >= 0 ? "+" : "") + offset;
        config.set(Configuration.APP_TIMEZONE, TimeZone.getTimeZone(id).getID());
        session.setAttribute(ATT_DEFAULTS, "true");
      }

      String step = request.getParameter("step");
      if (null == step || "".equals(step)) {
        step = request.getParameter("select");
        if (null == step || "".equals(step)) {
          step = (String) steps.get(0);
        }
      } else {
        Map errors = checkStep(step, request, response, config);
        if (null == errors) {
          return;
        } else if (errors.size() == 0) {
          if (request.getParameter("finish") != null) {
//            TODO: What was that for?
//            Map params = request.getParameterMap();
//            Iterator iterator = params.keySet().iterator();
//            Map paramMap = new HashMap();
//            while (iterator.hasNext()) {
//              String key = (String) iterator.next();
//              String[] values = (String[]) params.get(key);
//              paramMap.put(key, values[0]);
//            }

            try {
              InitializeDatabase.init(config, new OutputStreamWriter(System.out));
              response.sendRedirect(config.getUrl());
              return;
            } catch (Exception e) {
              // restore config just to be sure
              errors.put("fatal", e.getMessage());
              e.printStackTrace();
            }
          } else {
            if (request.getParameter("next") != null) {
              int idx = steps.indexOf(step);
              // if we see a "next" and this is the end it must be expert settings
              if (null != request.getParameter(ATT_ADVANCED)) {
                session.setAttribute(ATT_ADVANCED, "true");
                if (request.getParameter("advanced.all") != null) {
                  steps = addSteps(steps, EXPERT_STEPS);
                } else {
                  List list = new ArrayList();
                  Iterator it = request.getParameterMap().keySet().iterator();
                  while (it.hasNext()) {
                    String advStep = (String) it.next();
                    if (advStep.startsWith("advanced.step.")) {
                      advStep = advStep.substring("advanced.step.".length());
                      list.add(advStep);
                    }
                  }
                  addSteps(steps, list);
                }
                step = (String) steps.get(idx);
              } else {
                step = (String) steps.get(idx + 1);
              }
            } else if (request.getParameter("previous") != null) {
              int idx = steps.indexOf(step);
              step = (String) steps.get(idx - 1);
            } else if (request.getParameter("save") != null) {
              SnipSpace space = (SnipSpace) snipsnap.api.container.Components.getComponent(snipsnap.api.snip.SnipSpace.class);
              ByteArrayOutputStream configStream = new ByteArrayOutputStream();
              config.store(configStream);
              Snip configSnip = space.load(Configuration.SNIPSNAP_CONFIG);
              configSnip.setContent(configStream.toString("UTF-8"));
              space.store(configSnip);

              FileOutputStream globalsOs = new FileOutputStream(new File(config.getWebInfDir(), "application.conf"));
              config.storeGlobals(globalsOs);
              globalsOs.flush();
              globalsOs.close();
            }
          }
        } else {
          request.setAttribute(ATT_ERRORS, errors);
        }
      }

      if (!request.getContextPath().equals(config.getPath())) {
        config.setPath(request.getContextPath());
      }

      session.setAttribute(ATT_STEP, step);
      session.setAttribute(ATT_STEPS, steps);
      RequestDispatcher dispatcher = request.getRequestDispatcher("/admin/configure.jsp");
      dispatcher.forward(request, response);
      if (config.isConfigured()) {
        session.removeAttribute(ATT_CONFIG);
      }
      return;
    }
    session.removeAttribute(ATT_CONFIG);
    response.sendRedirect(config.getUrl());
  }


  private Map checkStep(String step,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Configuration config) {
    Map errors = new HashMap();

    if (handlers.containsKey(step)) {
      SetupHandler handler = (SetupHandler) handlers.get(step);
      return handler.setup(request, response, config, errors);
    } else {
      System.err.println("ConfigureServlet: unknown step: " + step);
    }

    return errors;
  }

  private List addSteps(List steps, List toAdd) {
    steps.remove(STEP_FINISH);
    Iterator it = toAdd.iterator();
    while (it.hasNext()) {
      String step = (String) it.next();
      if (!steps.contains(step)) {
        steps.add(step);
      }
    }
    steps.add(STEP_FINISH);
    return steps;
  }
}
