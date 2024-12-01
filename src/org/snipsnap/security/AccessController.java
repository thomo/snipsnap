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

package org.snipsnap.security;

import gabriel.Permission;
import gabriel.Subject;
import gabriel.components.AccessManager;
import gabriel.components.AccessManagerImpl;
import gabriel.components.context.AccessContext;
import gabriel.components.io.FileAclStore;
import gabriel.components.parser.AclParser;
import snipsnap.api.user.User;
import snipsnap.api.snip.Snip;

/**
 * Check for access to resources and operations
 *
 * @author Stephan J. Schmidt
 * 
 */

public interface AccessController {
  public static Permission EDIT_SNIP = new Permission("EDIT_SNIP");
  public static Permission ADD_ATTACHMENT = new Permission("ADD_ATTACHMENT");

  boolean checkPermission(snipsnap.api.user.User user, Permission permission, AccessContext context);
  boolean checkPermission(snipsnap.api.user.User user, Permission permission, snipsnap.api.snip.Snip snip);
}
