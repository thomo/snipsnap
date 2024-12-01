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

package org.snipsnap.versioning.cookbook;

import org.snipsnap.versioning.DifferenceService;

import java.util.List;

/**
 * Returns differences between two text strings
 * and implements the DifferenceService component inferface
 *
 * @author Stephan J. Schmidt
 * 
 */

public class CookbookDifferenceService implements DifferenceService {

  public CookbookDifferenceService() {
  }

  public List diff(String text1, String text2) {
    CookbookDiff diff = new CookbookDiff();
//    System.err.println("old="+text1);
//    System.err.println("new="+text2);
    return diff.diff(text1, text2);
  }
}
