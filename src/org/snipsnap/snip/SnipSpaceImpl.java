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

import org.apache.lucene.search.Hits;
import org.radeox.util.logging.Logger;
import org.snipsnap.app.Application;
import org.snipsnap.interceptor.Aspects;
import org.snipsnap.notification.Notification;
import org.snipsnap.snip.sFiletorage.QuerySnipStorage;
import org.snipsnap.snip.storage.*;
import org.snipsnap.user.Digest;
import org.snipsnap.util.Queue;
import org.snipsnap.util.mail.PostDaemon;

import java.sql.Timestamp;
import java.util.*;

/**
 * SnipSpace implementation handles all the operations with snips like
 * loading, storing, searching etc.
 *
 * TODO move indexing to Interceptor
 * TODO move ETag / changed handling to Interceptor
 * TODO move changed to Interceptor
 *
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public class SnipSpaceImpl implements SnipSpace {
  // List of snips that were changed
  private Queue changed;
  // List of snips that are scheduled for storage
  private List delayed;
  private SnipIndexer indexer;
  private Timer timer;
  private String eTag;
  private SnipStorage storage;

  private Map blogs;

  public SnipSpaceImpl() {
  }

  public void init() {
    changed = new Queue(100);
    storage = new JDBCSnipStorage();
    blogs = new HashMap();

    // Fully fill the cache with all Snips
    if ("full".equals(Application.get().getConfiguration().getCache())) {
      Logger.debug("Cache strategy is: keep full, using MemorySnipStorage and QuerySnipStorage");
      // If we keep all snips in memory we can use queries directly on the snip list
      // Wrap the real storage with the memory storage wrapper and an in-memory
      // query class
      storage = new QuerySnipStorage(new MemorySnipStorage(storage));
    } else if ("cache".equals(Application.get().getConfiguration().getCache())
        && storage instanceof CacheableStorage) {
      Logger.debug("Cache strategy is: cache, using CacheSnipStorage");
      // Otherwise at least wrap the persistence store
      // with a cache that does not need to load and create objects
      CacheableStorage old = (CacheableStorage) storage;
      storage = new CacheSnipStorage(storage);
      // We have to tell CacheStorage (JDBCSnipStorage) where to get
      // it's cache from for checking
      old.setCache(((CacheStorage) storage).getCache());
    }

    indexer = new SnipIndexer();
    changed.fill(storage.storageByRecent(50));
    // We do not store frequent changes right away but
    // collect them in "delayed"
    delayed = new ArrayList();

    setETag();
    timer = new Timer();
    timer.schedule(new TimerTask() {
      public void run() {
        synchronized (delayed) {
          ListIterator iterator = delayed.listIterator();
          while (iterator.hasNext()) {
            Snip snip = (Snip) iterator.next();
            systemStore(snip);
            iterator.remove();
          }
        }

      }
      // execute after 5 minutes and then
      // every 5 minutes
    }, 5 * 60 * 1000, 5 * 60 * 1000);

    // getting this will trigger Postdaemon
    PostDaemon.getInstance();
  }

  public String getETag() {
    return "\"" + eTag + "\"";
  }

  public Blog getBlog() {
    return getBlog(Application.get().getConfiguration().getStartSnip());
  }

  // Perhaps add getBlog(Wnip)
  public Blog getBlog(String name) {
    Blog blog;
    if (blogs.containsKey(name)) {
      blog = (Blog) blogs.get(name);
    } else {
      blog = (Blog) Aspects.newInstance(
          new BlogImpl((SnipSpace) Aspects.getThis(), name),
          Blog.class);
      blogs.put(name, blog);
    }
    return blog;
  }

  // A snip is changed by the user (created, stored)
  public void changed(Snip snip) {
    changed.add(snip);
    setETag();
  }

  public void setETag() {
    eTag = Digest.getDigest(new java.util.Date().toString());
  }

  public int getSnipCount() {
    return storage.storageCount();
  }

  public List getChanged() {
    return getChanged(15);
  }

  public List getChanged(int count) {
    return changed.get(count);
  }

  public List getAll() {
    return storage.storageAll();
  }

  public List getSince(Timestamp date) {
    return storage.storageByDateSince(date);
  }

  public List getByDate(String start, String end) {
    return storage.storageByDateInName(start, end);
  }

  /**
   * A list of Snips, ordered by "hotness", currently
   * viewcount.
   *
   * @param count number of snips in the result
   * @return List of snips, ordered by hotness
   */
  public List getHot(int count) {
    return storage.storageByHotness(count);
  }

  public List getComments(Snip snip) {
    return storage.storageByComments(snip);
  }

  public List getByUser(String login) {
    return storage.storageByUser(login);
  }

  public List getChildren(Snip snip) {
    return storage.storageByParent(snip);
  }

  public List getChildrenDateOrder(Snip snip, int count) {
    return storage.storageByParentNameOrder(snip, count);
  }

  public List getChildrenModifiedOrder(Snip snip, int count) {
    return storage.storageByParentModifiedOrder(snip, count);
  }

  public void reIndex() {
    List snips = getAll();
    Iterator iterator = snips.iterator();
    while (iterator.hasNext()) {
      Snip snip = (Snip) iterator.next();
      indexer.reIndex(snip);
    }
  }

  public Hits search(String queryString) {
    return indexer.search(queryString);
  }

  public String getContent(String title, String content) {
    return content = "1 " + title + " {anchor:" + title + "}\n" + content;
  }

  public boolean exists(String name) {
    if (null == load(name)) {
      return false;
    } else {
      return true;
    }
  }

  public Snip load(String name) {
    return storage.storageLoad(name);
  }

  public void store(Snip snip) {
    Application app = Application.get();
    long start = app.start();
    snip.setMUser(app.getUser());
    changed(snip);
    snip.setMTime(new Timestamp(new java.util.Date().getTime()));
    storage.storageStore(snip);
    indexer.reIndex(snip);
    app.stop(start, "store - " + snip.getName());
    return;
  }

  /**
   * Method with with wich the system can store snips.
   * This methode does not change the mTime, the mUser,
   * reindex the snip or add the snip to the modified list
   *
   * @param snip The snip to store
   */
  public void systemStore(Snip snip) {
    //Logger.debug("systemStore - "+snip.getName());
    Application app = Application.get();
    long start = app.start();
    storage.storageStore(snip);
    indexer.reIndex(snip);
    app.stop(start, "systemStore - " + snip.getName());
    return;
  }


  /**
   * Delays the storage of a snip for some time. Some information
   * in a snip are changeg every view. To not store a snip every
   * time it is viewed, delay the store and wait until some changes
   * are cummulated. Should only be used, when the loss of the
   * changes is tolerable.
   *
   * @param snip Snip to delay for storage
   */
  public void delayedStrore(Snip snip) {
    //Logger.debug("delayedStore - "+snip.getName());
    Logger.debug("delayedStore");
    synchronized (delayed) {
      if (!delayed.contains(snip)) {
        delayed.add(snip);
      }
    }
  }

  public Snip create(String name, String content) {
    name = name.trim();
    Snip snip = storage.storageCreate(name, content);
    changed(snip);
    indexer.index(snip);
    Application.get().notify(Notification.SNIP_CREATE, snip);
    return snip;
  }

  public void remove(Snip snip) {
    changed.remove(snip);
    storage.storageRemove(snip);
    indexer.removeIndex(snip);
    return;
  }
}