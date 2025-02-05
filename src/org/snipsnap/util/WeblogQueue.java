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
package org.snipsnap.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Queue implementation for Weblogs.
 * @author Stephan J. Schmidt
 * 
 */
public class WeblogQueue {
  private LinkedList queue;
  private int size;

  public WeblogQueue(int size) {
    this.size = size;
    queue = new LinkedList();
  }

  public Weblog add(Weblog weblog) {
    if (queue.contains(weblog)) {
      queue.remove(weblog);
    }

    if (queue.size() == size) {
      queue.removeLast();
    }
    queue.addFirst(weblog);
    return weblog;
  }

  public void remove(Weblog weblog) {
    queue.remove(weblog);
  }

  public List get() {
    return (List) queue;
  }

  public List get(int count) {
    count = Math.min(count, queue.size());
    return (List) queue.subList(0, count);
  }

}
