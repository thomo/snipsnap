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

import org.radeox.util.logging.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Permissions holds a ACL list with permissions and
 * roles
 *
 * @author stephan
 * 
 */

public class Permissions {
  public final static String EDIT_SNIP = "Edit";
  public final static String ATTACH_TO_SNIP = "Attach";
  public final static String POST_TO_SNIP = "Post";

  private Map permissions;

  public Permissions() {
  }

  public Permissions(Map permissions) {
    this.permissions = permissions;
  }

  public Permissions(String permissions) {
    //@TODO late deserialization
    this.permissions = deserialize(permissions);
  }

  private void init() {
    if (null == permissions) {
      permissions = new HashMap();
    }
  }

  public String toString() {
    return serialize();
  }

  public boolean empty() {
    return null == permissions || permissions.isEmpty();
  }

  public void remove(String permission, String role) {
    init();
    if (permissions.containsKey(permission)) {
      Roles roles = (Roles) permissions.get(permission);
      roles.remove(role);
      // remove permission if there are no more roles
      if (roles.isEmpty()) {
        Logger.debug("Empty.");
        permissions.remove(permission);
      } else {
        Logger.debug("not Empty." + permissions.toString());
      }
    }
  }

  public void add(String permission) {
    init();
    if (!permissions.containsKey(permission)) {
      permissions.put(permission, new Roles());
    }
  }

  public void add(String permission, String role) {
    init();
    if (!permissions.containsKey(permission)) {
      permissions.put(permission, new Roles());
    }
    ((Roles) permissions.get(permission)).add(role);
    return;
  }

  public void add(String permission, Roles roles) {
    init();
    if (!permissions.containsKey(permission)) {
      permissions.put(permission, new Roles());
    }
    ((Roles) permissions.get(permission)).addAll(roles);
    return;
  }

  public boolean exists(String permission, Roles roles) {
    // if no permission is set, then return false
    if (null == permissions || !permissions.containsKey(permission)) { return false; }
    Roles permRoles = (Roles) permissions.get(permission);
    return permRoles.containsAny(roles);
  }

  public boolean check(String permission, Roles roles) {
    // Policy: If no permission is set, everything is allowed
    if (null == permissions || !permissions.containsKey(permission)) { return true; }
    Roles permRoles = (Roles) permissions.get(permission);
//    Logger.debug("--check");
//    Logger.debug("permission="+ permission);
//    Logger.debug("roles="+roles);
//    Logger.debug("object roles="+permRoles);
//    Logger.debug("result=" + permRoles.containsAny(roles));
    return permRoles.containsAny(roles);
  }

  public Map deserialize(String permissions) {
    Map perms = new HashMap();
    if (permissions == null || "".equals(permissions)) { return perms; }

    StringTokenizer tokenizer = new StringTokenizer(permissions, "|");
    while (tokenizer.hasMoreTokens()) {
      String permission = tokenizer.nextToken();
      Roles roles = getRoles(permission);
      permission = getPermission(permission);
      perms.put(permission, roles);
    }

    return perms;
  }

  private String serialize() {
    if (null == permissions || permissions.isEmpty()) { return ""; }

    StringBuffer permBuffer = new StringBuffer();
    Iterator iterator = permissions.keySet().iterator();
    while (iterator.hasNext()) {
      String permission = (String) iterator.next();
      permBuffer.append(permission);
      permBuffer.append(":");
      Roles roles = (Roles) permissions.get(permission);
      Iterator rolesIterator = roles.iterator();
      while (rolesIterator.hasNext()) {
        String role = (String) rolesIterator.next();
        permBuffer.append(role);
        if (rolesIterator.hasNext()) {
          permBuffer.append(",");
        }
      }
      if (iterator.hasNext()) {
        permBuffer.append("|");
      }
    }
    return permBuffer.toString();
  }

  private String after(String string, String delimiter) {
    return string.substring(string.indexOf(delimiter) + 1);
  }

  private String before(String string, String delimiter) {
    return string.substring(0, string.indexOf(delimiter));
  }

  private String getPermission(String rolesString) {
    return before(rolesString, ":");
  }

  private Roles getRoles(String rolesString) {
    Roles roles = new Roles();
    StringTokenizer tokenizer = new StringTokenizer(after(rolesString, ":"), ",");
    while (tokenizer.hasMoreTokens()) {
      String role = tokenizer.nextToken();
      roles.add(role);
    }
    return roles;
  }

}
