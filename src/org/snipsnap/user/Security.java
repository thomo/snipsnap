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

import org.snipsnap.snip.Ownable;
import snipsnap.api.snip.Snip;
import snipsnap.api.user.*;
import snipsnap.api.user.User;
import snipsnap.api.container.Components;

/**
 * Security manager for checking permission, roles etc.
 *
 * @author stephan
 * 
 */

public class Security {
  /**
   * Adds authenticated role to user roles
   *
   * @param user User to check
   * @return List of roles
   */
  private static Roles getRoles(User user) {
    Roles userRoles = new Roles(user.getRoles());
    AuthenticationService service = (AuthenticationService) snipsnap.api.container.Components.getComponent(AuthenticationService.class);

    if (service.isAuthenticated(user)) {
      userRoles.add(Roles.AUTHENTICATED);
    }
    return userRoles;
  }

  /**
   * Adds owner role to roles list
   *
   * @param user User to check
   * @param object Object with possible owner
   * @return List of roles for user and object
   */
  private static Roles getRoles(User user, snipsnap.api.snip.Snip object) {
    Roles roles = getRoles(user);
//    if (object instanceof Ownable) {
//      Ownable o = object;
//      if (o.isOwner(user)) {
//        roles.add(Roles.OWNER);
//      }
//    }
    return roles;
  }

  /**
   * Checks whether a object has the given permissiom, e.g.
   * if a snip has the permission "Edit" for the roles "Owner".
   * Returns false if there is no "Edit" permission.
   *
   * @param permission String with the permission to check, e.g. "Edit"
   * @param object Object to check permission agains
   * @param roles Roles object containing the roles
   * @return true if the object has the permission for the roles
   */
  public static boolean existsPermission(String permission, snipsnap.api.snip.Snip object, Roles roles) {
    Permissions permissions = object.getPermissions();
    return permissions.exists(permission, roles);
  }

  public static boolean hasRoles(User user, Roles roles) {
    Roles userRoles = getRoles(user);
    return userRoles.containsAny(roles);
  }

  public static boolean hasRoles(User user, Snip object, Roles roles) {
    Roles userRoles = getRoles(user, object);
    return userRoles.containsAny(roles);
  }

  /**
   * Check if the user has the permission on the object.
   * Returns true if there is e.g no "Edit" permission.
   *
   * @param permission the permission to check, e.g. "Edit"
   * @param user the user to check permissions for, e.g. "funzel"
   * @param object the object that should be manipulated
   * @return
   */
  public static boolean checkPermission(String permission, snipsnap.api.user.User user, snipsnap.api.snip.Snip object) {
    Permissions permissions = object.getPermissions();
    if (null == permissions || permissions.empty()) {
      return true;
    } else {
      return permissions.check(permission, getRoles(user, object));
    }
  }
}
