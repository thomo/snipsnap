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

import org.apache.commons.codec.binary.Base64;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;
import org.radeox.util.logging.Logger;
import snipsnap.api.app.Application;
import snipsnap.api.config.Configuration;
import snipsnap.api.container.Components;
import org.snipsnap.jdbc.IntHolder;
import org.snipsnap.snip.storage.SnipSerializer;
import org.snipsnap.snip.storage.UserSerializer;
import snipsnap.api.user.User;
import org.snipsnap.user.UserManager;
import org.snipsnap.versioning.VersionManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import snipsnap.api.snip.*;
import snipsnap.api.snip.SnipSpace;
import snipsnap.api.snip.Snip;

/**
 * Helper class for importing serialized database backups.
 *
 * @author Matthias L. Jugel
 * 
 */
public class XMLSnipImport {
  public final static int IMPORT_USERS = 0x01;
  public final static int IMPORT_SNIPS = 0x02;
  public final static int OVERWRITE = 0x04;

  private static ThreadLocal instance = new ThreadLocal() {
    protected synchronized Object initialValue() {
      return new HashMap();
    }
  };

  public static IntHolder getStatus() {
    IntHolder current = (IntHolder) ((Map) instance.get()).get("current");
    if (null == current) {
      current = new IntHolder(0);
      ((Map) instance.get()).put("current", current);
    }
    return current;
  }

  private static long charErrCount = 0;

  /**
   * Load snips and users into the SnipSpace from an xml document out of a stream.
   *
   * @param in    the input stream to load from
   * @param flags whether or not to overwrite existing content
   */
  public static void load(InputStream in, final int flags) throws IOException {
    SAXReader saxReader = new SAXReader();
    try {
      saxReader.addHandler("/snipspace/user", new ElementHandler() {
        public void onStart(ElementPath elementPath) {
          // nothing to do here ...
        }

        public void onEnd(ElementPath elementPath) {
          Element userElement = elementPath.getCurrent();
          if ((flags & IMPORT_USERS) != 0) {
            try {
              XMLSnipImport.loadUser(elementPath.getCurrent(), flags);
            } catch (Exception e) {
              Logger.fatal("XMLSnipImport: error importing user: " + userElement.elementText("name"));
            }
            getStatus().inc();
          }
          // prune the element to save memory
          userElement.detach();
        }
      });

      saxReader.addHandler("/snipspace/snip", new ElementHandler() {
        public void onStart(ElementPath elementPath) {
          // nothing to do here ...
        }

        public void onEnd(ElementPath elementPath) {
          Element snipElement = elementPath.getCurrent();
          if ((flags & IMPORT_SNIPS) != 0) {
            try {
              XMLSnipImport.loadSnip(snipElement, flags);
            } catch (Exception e) {
              Logger.fatal("XMLSnipImport: error importing snip: " + snipElement.elementText("name"));
            }
            getStatus().inc();
          }
          // prune the element to save memory
          snipElement.detach();
        }
      });


      // add a reader wrapper to remove illegal characters from input stream
      // it looks like the database export (XMLWriter) allows these to get through
      InputStreamReader reader = new InputStreamReader(in, "UTF-8") {
        public int read(char[] chars) throws IOException {
          int n = super.read(chars);
          for (int i = 0; i < n; i++) {
            chars[i] = replaceIfIllegal(chars[i]);
          }
          return n;
        }

        public int read(char[] chars, int start, int length) throws IOException {
          int n = super.read(chars, start, length);
          for (int i = 0; i < n; i++) {
            chars[i] = replaceIfIllegal(chars[i]);
          }
          return n;
        }

        private char replaceIfIllegal(char c) {
          if (c < 0x20 && !(c == 0x09 || c == 0x0a || c == 0x0d)) {
            charErrCount++;
            return (char) 0x20;
          }
          return c;
        }
      };

      saxReader.read(reader);
      Logger.warn("XMLSnipImport: corrected " + charErrCount + " characters in input");
      Logger.log("XMLSnipImport: imported " + getStatus().getValue() + " data records");
    } catch (DocumentException e) {
      Logger.warn("XMLSnipImport: unable to parse document", e);
      throw new IOException("Error parsing document: " + e);
    }
  }

  /**
   * Load a user object from a serialized xml element
   *
   * @param userElement the xml user element
   * @param flags       flags indicating overwriting any existing users or not
   */
  public static void loadUser(Element userElement, int flags) {
    Map userMap = UserSerializer.getInstance().getElementMap(userElement);
    userMap.remove(UserSerializer.USER_APPLICATION);

    String login = (String) userMap.get(UserSerializer.USER_NAME);
    String passwd = (String) userMap.get(UserSerializer.USER_PASSWORD);
    String email = (String) userMap.get(UserSerializer.USER_EMAIL);

    UserManager userManager = (UserManager) Components.getComponent(UserManager.class);
    snipsnap.api.user.User user = null;
    if (userManager.exists(login)) {
      if ((flags & OVERWRITE) == 0) {
        Logger.log("ignoring to import user '" + login + "'");
        return;
      }
      Logger.log("loading existing user '" + login + "'");
      user = userManager.load(login);
    } else {
      Logger.log("creating user '" + login + "'");
      user = userManager.create(login, passwd, email);
    }

    user = UserSerializer.getInstance().deserialize(userMap, user);
    userManager.systemStore(user);
  }

  public static void loadSnip(Element snipElement, int flags) {
    Map snipMap = SnipSerializer.getInstance().getElementMap(snipElement);
    snipMap.remove(SnipSerializer.SNIP_APPLICATION);

    String name = (String) snipMap.get(SnipSerializer.SNIP_NAME);
    String content = (String) snipMap.get(SnipSerializer.SNIP_CONTENT);

    SnipSpace space = (snipsnap.api.snip.SnipSpace) snipsnap.api.container.Components.getComponent(SnipSpace.class);
    Snip snip = null;
    if (space.exists(name)) {
      Logger.log("loading existing snip '" + name + "'");
      snip = space.load(name);
      if ((flags & OVERWRITE) == 0) {
        snip.setContent(snip.getContent() + content);
        snipMap.remove(SnipSerializer.SNIP_CONTENT);
      }
    } else {
      Logger.log("creating snip '" + name + "'");
      snip = space.create(name, content);
    }

    UserManager um = (UserManager) snipsnap.api.container.Components.getComponent(UserManager.class);
    snipsnap.api.user.User importUser = snipsnap.api.app.Application.get().getUser();

    // check existing users
    if (!um.exists((String) snipMap.get(SnipSerializer.SNIP_CUSER))) {
      snipMap.put(SnipSerializer.SNIP_CUSER, importUser.getLogin());
    }
    if (!um.exists((String) snipMap.get(SnipSerializer.SNIP_MUSER))) {
      snipMap.put(SnipSerializer.SNIP_MUSER, importUser.getLogin());
    }
    if (!um.exists((String) snipMap.get(SnipSerializer.SNIP_OUSER))) {
      snipMap.put(SnipSerializer.SNIP_OUSER, importUser.getLogin());
    }

    // first restore attached files, then remove the data element
    restoreAttachments(snipElement);
    snip = SnipSerializer.getInstance().deserialize(snipMap, snip);
    restoreVersions(snipElement, snip, (flags & OVERWRITE) != 0);
    snip.getBackLinks().getSize();
    // ensure that the configuration snip is stored normally
    // so the configuration is updated
    if (snipsnap.api.config.Configuration.SNIPSNAP_CONFIG.equals(snip.getName())) {
      space.store(snip);
    } else {
      space.systemStore(snip);
    }
  }

  private static void restoreAttachments(Element snipEl) {
    Configuration config = snipsnap.api.app.Application.get().getConfiguration();
    File attRoot = config.getFilePath();
    Element attachmentsEl = snipEl.element("attachments");
    if (null != attachmentsEl) {
      Iterator attIt = attachmentsEl.elements("attachment").iterator();
      while (attIt.hasNext()) {
        Element att = (Element) attIt.next();
        if (att.element("data") != null) {
          File attFile = new File(attRoot, att.elementText("location"));
          try {
            // make sure the directory hierarchy exists
            attFile.getParentFile().mkdirs();
            byte buffer[] = Base64.decodeBase64(att.elementText("data").getBytes("UTF-8"));
            BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(attFile));
            os.write(buffer);
            os.flush();
            os.close();
          } catch (Exception e) {
            Logger.fatal("unable to store attachment: " + e);
            e.printStackTrace();
          }
          att.element("data").detach();
        }
      }
    }
  }

  private static void restoreVersions(Element snipEl, Snip snip, boolean overwrite) {
    VersionManager versionManager = (VersionManager) Components.getComponent(VersionManager.class);
    List currentVersions = versionManager.getHistory(snip);
    if (currentVersions.size() > 0 && overwrite) {
      // TODO missing in version manager
      // versionManager.removeHistory();
    }

    currentVersions = versionManager.getHistory(snip);
    int versionNo = currentVersions.size();

    SnipSerializer serializer = SnipSerializer.getInstance();
    Element versionsEl = snipEl.element("versions");
    if (versionsEl != null) {
      Iterator versionsElIt = versionsEl.elementIterator("snip");
      while (versionsElIt.hasNext()) {
        Element versionSnipEl = (Element) versionsElIt.next();
        Snip versionSnip = serializer.deserialize(versionSnipEl, SnipFactory.createSnip("", ""));
        if (versionNo > 0) {
          versionSnip.setVersion(versionNo++);
        }
        versionManager.storeVersion(versionSnip);
      }
    }
  }
}
