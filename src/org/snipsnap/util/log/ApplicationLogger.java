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

package org.snipsnap.util.log;

import org.radeox.util.logging.LogHandler;
import snipsnap.api.app.Application;

/**
 * Concrete Logger that logs to Application
 *
 * @author stephan
 * 
 */

public abstract class ApplicationLogger implements LogHandler {
  public void log(String output) {
    snipsnap.api.app.Application.get().log(output);
  }

  public void log(String output, Throwable e) {
    snipsnap.api.app.Application.get().log(output+": "+e.getMessage());
  }
}
