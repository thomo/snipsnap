/*
 * This file is part of "SnipSnap Wiki/Weblog".
 *
 * Copyright (c) 2004 Fraunhofer Gesellschaft
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
package org.snipsnap.snip.name;


/**
 * Formatter that capitalizes the name of weblogs and comments of weblogse
 *
 * @author stephan
 * 
 */

public class WeblogNameFormatter implements NameFormatter {
  // Null Object Pattern
  private NameFormatter parent = new NoneFormatter();

  public void setParent(NameFormatter parent) {
    this.parent = parent;
  }

  public String format(String name) {
    String parentName = parent.format(name);
    if (parentName.matches(".*/[0-9]{4}-[0-9][0-9]-[0-9][0-9]/[0-9]+")) {
      int lastSlashIndex = parentName.lastIndexOf('/');
      String date = parentName.substring(parentName.lastIndexOf("/", lastSlashIndex - 1) + 1, lastSlashIndex);
      return date + " #" + parentName.substring(lastSlashIndex + 1);
    }
    return parentName;
  }
}
