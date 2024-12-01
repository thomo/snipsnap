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

package org.snipsnap.snip;

import dynaop.Proxy;
import dynaop.ProxyAware;
import snipsnap.api.app.Application;
import org.snipsnap.date.Month;
import org.snipsnap.semanticweb.rss.Rssify;
import org.snipsnap.user.Permissions;
import org.snipsnap.user.Roles;
import org.snipsnap.xmlrpc.WeblogsPing;

import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import snipsnap.api.snip.*;
import snipsnap.api.snip.SnipSpace;
import snipsnap.api.snip.Snip;

/**
 * BlogImpl for Blog.
 *
 * @author stephan
 * 
 */

public class BlogImpl implements Blog, ProxyAware {
  private Blog proxy;
  private String startName;
  private String name;
  private Snip blog;
  private snipsnap.api.snip.SnipSpace space;

  public BlogImpl(SnipSpace space, String blogName) {
    this.space = space;
    // May not be initialized, so set it to something sane
    this.startName = Application.get().getConfiguration().getStartSnip();
    if (blogName == null || "".equals(blogName)) {
      blogName = startName;
    }
    this.name = blogName;
    this.blog = space.load(name);
  }

  public void setProxy(Proxy proxy) {
    this.proxy = (Blog) proxy;
  }

  public String getName() {
    return this.name;
  }

  public Snip post(String content, String title) {
    return post(BlogKit.getContent(title, content));
  }

  public Snip post(String content) {
    Date date = new Date(new java.util.Date().getTime());
    return post(content, date);
  }

  public Snip post(String content, Date date) {
    String snipName = name + "/" + SnipUtil.toName(date);
    Snip snip = null;

    // Should several posts per day be one snip or
    // several snips?
    int max = findMaxPost(snipName);
    if (true) { // Application.get().getConfiguration().allow(Configuration.APP_PERM_MULTIPLEPOSTS)) {
      // if (space.exists(snipName)) {
      // }
      // how many children do exist?
      // get the highest count
      // e.g. start/2003-05-06
      snip = snip = space.create(snipName + "/" + (max + 1), content);
    } else {
      // there was a post with a least /1 then add to that post
      if (max != 0) {
        snipName = snipName + "/" + max;
      }
      if (space.exists(snipName)) {
        snip = space.load(snipName);
        snip.setContent(content + "\n\n" + snip.getContent());
      } else {
        snip = space.create(snipName, content);
      }
    }

    //snip.setParent(weblog);
    snip.addPermission(Permissions.EDIT_SNIP, Roles.OWNER);
    space.systemStore(snip);

    // Ping weblogs.com that we changed our site
    WeblogsPing.ping(blog);
    return snip;
  }

  private int findMaxPost(String snipName) {
    Snip[] existing = space.match(snipName + "/");
    int max = 0;
    //System.out.println("found="+existing.length+" name="+snipName);
    for (int i = 0; i < existing.length; i++) {
      Snip post = existing[i];
      String name = post.getName();
      int index = name.lastIndexOf('/');
      //System.out.println("name="+name);
      if (index != -1) {
        try {
          //System.out.println("parsing="+name.substring(index+1));
          max = Math.max(Integer.parseInt(name.substring(index + 1)), max);
          //System.out.println("max="+max);
        } catch (NumberFormatException e) {
          //
        }
      }
    }
    return max;
  }

  public List getFlatPosts() {
    return Rssify.rssify(getPosts(10));
  }

  public List getPosts(int count) {
    Calendar endC = new GregorianCalendar();
    endC.setTime(new java.util.Date());
    endC.add(Calendar.DAY_OF_MONTH, 1);
    Calendar startC = new GregorianCalendar();
    startC.setTimeInMillis(blog.getCTime().getTime());

    List posts = space.getByDate(blog.getName(), Month.toKey(startC), Month.toKey(endC));
    return posts.subList(0, Math.min(posts.size(), count));
  }

  public Snip getSnip() {
    return blog;
  }
}
