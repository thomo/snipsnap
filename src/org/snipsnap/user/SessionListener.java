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

package org.snipsnap.user;

import snipsnap.api.app.Application;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Gets called when a session gets a timeout and removes the
 * user of the timedout session from the current user list
 *
 * @author stephan
 * 
 */

public class SessionListener implements HttpSessionListener {
  public void sessionCreated(HttpSessionEvent event) {
    event.getSession().setMaxInactiveInterval(60*60);
  }

  public void sessionDestroyed(HttpSessionEvent event) {
    HttpSession session = event.getSession();
    Application.forceGet();
    Application.removeCurrentUser(session);
  }
}
