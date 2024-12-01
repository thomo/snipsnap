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
import gabriel.Principal;
import org.picocontainer.PicoContainer;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.util.logging.Logger;
import snipsnap.api.app.Application;
import snipsnap.api.container.Components;
import snipsnap.api.render.context.SnipRenderContext;
import org.snipsnap.snip.attachment.Attachments;
import snipsnap.api.label.Labels;
import org.snipsnap.snip.label.RenderEngineLabel;
import org.snipsnap.snip.name.NameFormatter;
import org.snipsnap.snip.name.PathRemoveFormatter;
import org.snipsnap.snip.name.WeblogNameFormatter;
import org.snipsnap.user.Permissions;
import snipsnap.api.user.User;

import javax.servlet.http.HttpServletRequest;
// import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.List;

import snipsnap.api.snip.*;
import snipsnap.api.snip.SnipSpace;
import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipLink;
import snipsnap.api.snip.SnipSpaceFactory;

/**
 * Central class for snips.
 * <p/>
 * TODO: cUser, mUser, cTime, ... -> modified to composite object
 *
 * @author Stephan J. Schmidt
 * 
 */
public class SnipImpl implements Snip, ProxyAware {
  private Snip proxy;
  private Content content;

  private NameFormatter nameFormatter;
  private String applicationOid;

  //@TODO think about that
  public Snip parent;
  private List children;
  private Snip comment;
  private Comments comments;
  private String name;
  private String oUser;

  // @TODO: Composite Object
  private Permissions permissions;
  private Access access;
  private snipsnap.api.label.Labels labels;
  private Attachments attachments;
  private Modified modified;
  private int version = 1;

  // @TODO: Remove
  private String commentedName;
  private String parentName;

  public void setProxy(Proxy proxy) {
    this.proxy = (Snip) proxy;
  }

  private void init() {
    if (null == children) {
      children = SnipSpaceFactory.getInstance().getChildren(proxy);
    }
  }

  public SnipImpl(String name, String content) {
    this.name = name;
    this.modified = new Modified();
    this.access = new Access();
    this.content = new Content();
    this.content.setText(content);
  }

  public void handle(HttpServletRequest request) {
    access.handle(name, request);
    SnipSpaceFactory.getInstance().delayedStore(proxy);
  }

  public int getVersion() {
    return this.version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public void setApplication(String applicationOid) {
    this.applicationOid = applicationOid;
  }

  public String getApplication() {
    return applicationOid;
  }

  public Access getAccess() {
    return access;
  }

  public Modified getModified() {
    return modified;
  }

  /**
   * Returns true, when the snip is a weblog.
   * Currently only test against 'start'.
   * Should be extendet to test a "weblog"-label
   *
   * @return true, if the snip is a weblog
   */
  public boolean isWeblog() {
    return getContent().indexOf("{weblog") != -1;
  }

  /**
   * Conveniance function for JSP
   *
   * @return true, if snip is not a weblog
   */
  public boolean isNotWeblog() {
    return !isWeblog();
  }

  public Principal getOwner() {
    return new Principal(getCUser());
  }

  public void setOwner(Principal principal) {
    // do nothing for now
  }

  public boolean isOwner(Principal principal) {
    return principal.equals(getOwner());
  }

  public void addPermission(String permission, String role) {
    permissions.add(permission, role);
  }

  public void setPermissions(Permissions permissions) {
    this.permissions = permissions;
  }

  public Permissions getPermissions() {
    return permissions;
  }

  public String getOUser() {
    return oUser;
  }

  public void setOUser(User oUser) {
    this.oUser = oUser.getLogin();
  }

  public void setOUser(String oUser) {
    this.oUser = oUser;
  }

  public Attachments getAttachments() {
    return attachments;
  }

  public void setAttachments(Attachments attachments) {
    this.attachments = attachments;
  }

  public snipsnap.api.label.Labels getLabels() {
    return labels;
  }

  public void setLabels(snipsnap.api.label.Labels labels) {
    this.labels = labels;
  }

  public Links getBackLinks() {
    return access.getBackLinks();
  }

  public Links getSnipLinks() {
    return access.getSnipLinks();
  }

  public void setBackLinks(Links backLinks) {
    access.setBackLinks(backLinks);
  }

  public void setSnipLinks(Links snipLinks) {
    access.setSnipLinks(snipLinks);
  }

  public int getViewCount() {
    return access.getViewCount();
  }

  public void setViewCount(int count) {
    access.setViewCount(count);
  }

  public int incViewCount() {
    return access.incViewCount();
  }

  public Timestamp getCTime() {
    return modified.getcTime();
  }

  public void setCTime(Timestamp cTime) {
    this.modified.setcTime(cTime);
  }

  public Timestamp getMTime() {
    return modified.getmTime();
  }

  public void setMTime(Timestamp mTime) {
    this.modified.setmTime(mTime);
  }

  public String getCUser() {
    return modified.getcUser();
  }

  public void setCUser(User cUser) {
    this.modified.setcUser(cUser.getLogin());
  }

  public void setCUser(String cUser) {
    this.modified.setcUser(cUser);
  }

  public String getMUser() {
    return modified.getmUser();
  }

  public void setMUser(snipsnap.api.user.User mUser) {
    this.modified.setmUser(mUser.getLogin());
  }

  public void setMUser(String mUser) {
    this.modified.setmUser(mUser);
  }

  public List getChildren() {
    init();
    return children;
  }

  public void setCommentedSnip(Snip comment) {
    this.comment = comment;
  }

  public Snip getCommentedSnip() {
    if (null != commentedName && !"".equals(commentedName) && null == comment) {
      comment = SnipSpaceFactory.getInstance().load(commentedName);
    }
    return comment;
  }

  public boolean isComment() {
    return !(null == getCommentedSnip());
  }

  public Comments getComments() {
    if (null == comments) {
      comments = new Comments(proxy);
    }
    return comments;
  }

  /**
   * Get a list of child snips, ordered by date with
   * the newest one first
   *
   * @return List of child snips
   */
  public List getChildrenDateOrder() {
    return SnipSpaceFactory.getInstance().getChildrenDateOrder(proxy, 10);
  }

  public List getChildrenModifiedOrder() {
    return SnipSpaceFactory.getInstance().getChildrenModifiedOrder(proxy, 10);
  }

  /**
   * Add a child snip. Sets the parent of
   * the child to this snip and <b>stores</b> the
   * child because of the new parent.
   *
   * @param snip Snip to add as child
   */
  public void addSnip(Snip snip) {
    init();
    if (!children.contains(snip)) {
      snip.setParent(proxy);
      children.add(snip);
      SnipSpaceFactory.getInstance().systemStore(snip);
    }
  }

  /**
   * Removes child snip from this nsip
   *
   * @param snip Child to remove
   */
  public void removeSnip(Snip snip) {
    init();
    if (children.contains(snip)) {
      children.remove(snip);
      // snip.setParent(null);
    }
  }

  public Snip getParent() {
    if (null != parentName && !"".equals(parentName) && null == parent) {
      parent = SnipSpaceFactory.getInstance().load(parentName);
    }
    return parent;
  }

  /**
   * Directly sets the parent snip, does
   * not add the snip to the parent.
   * This is needed for restoring from storage.
   * Better solution wanted
   *
   * @param parentSnip new parent snip of this snip
   */
  public void setDirectParent(Snip parentSnip) {
    this.parent = parentSnip;
  }

  public void setParentName(String name) {
    this.parentName = name;
  }

  public String getParentName() {
    return this.parent == null ? parentName : this.parent.getName();
  }

  public void setCommentedName(String name) {
    this.commentedName = name;
  }

  public String getCommentedName() {
    return this.parent == null ? commentedName : this.comment.getName();
  }

  // REMOVE!
  public void setParent(Snip parentSnip) {
    if (parentSnip == this.parent) {
      return;
    }

    if (null != this.parent) {
      this.parent.removeSnip(proxy);
    }
    this.parent = parentSnip;
    parentSnip.addSnip(proxy);
  }

  public String getName() {
    return name;
  }

  /**
   * Return a short version of the name.
   * Useful for vertical snip listings, where
   * the snips should not be to long.
   * End of snip name will be replaced with "..."
   *
   * @return Short name of snip
   */
  public String getShortName() {
    return SnipLink.cutLength(getName(), 20);
  }

  /**
   * Return an encoded version of the name,
   * especially spaces replaced with "+"
   *
   * @return encoded name of snip
   */
  public String getNameEncoded() {
    try {
      return SnipLink.encode(getName());
    } catch (Exception e) {
      return getName();
    }
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getContent() {
    return content.getText();
  }

  public void setContent(String content) {
    this.content.setText(content);
  }

  public String getLink() {
    return SnipLink.createLink(this.name);
  }

  public String getAttachmentString() {
    // THIS IS FILE DEPENDENT!
    // ABSTRACT TO ATTACHMENT STORAGE      \
    return attachments.getLinks(name);
  }

  public String toXML() {
    RenderEngineLabel reLabel =
            (RenderEngineLabel) getLabels().getLabel("RenderEngine");
    RenderEngine engine = null;
    if (reLabel != null) {
      try {
        engine = (RenderEngine) Components.getComponent(Class.forName(reLabel.getValue()));
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

    // make sure we get a render engine
    if (null == engine) {
      engine = (RenderEngine) Components.getComponent(Components.DEFAULT_ENGINE);
    }


    RenderContext context = new SnipRenderContext(proxy,
                                                  (SnipSpace) Components.getComponent(SnipSpace.class));
    context.setParameters(Application.get().getParameters());

    return engine.render(getContent(), context);
  }

  // ERRORS SHOULD NOT BE HANDLED IN SNIP
  public String getXMLContent() {
    String tmp = null;
    try {
      tmp = toXML();
    } catch (Exception e) {
      tmp = "<span class=\"error\">" + e + "</span>";
      e.printStackTrace();
      Logger.warn("SnipImpl: unable to get XMLContent", e);
    } catch (Error err) {
      err.printStackTrace();
      tmp = "<span class=\"error\">" + err + "</span>";
    }

    return tmp;
  }

  public Writer appendTo(Writer s) throws IOException {
    s.write(getXMLContent());
    return s;
  }

  public SnipPath getPath() {
    return new SnipPath(this);
  }

  public String getTitle() {
    if (null == nameFormatter) {
      nameFormatter = new PathRemoveFormatter();
      nameFormatter.setParent(new WeblogNameFormatter());
    }
    return nameFormatter.format(name);
  }

  public int hashCode() {
    return name.hashCode();
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Snip)) {
      return false;
    }

    return ((Snip) obj).getName().equals((this.name));
  }

  public String toString() {
    return getName();
  }

  public Snip copy(String newName) {
    snipsnap.api.snip.SnipSpace space = (SnipSpace) Components.getComponent(SnipSpace.class);
    Snip newSnip = space.create(newName, getContent());
    newSnip.setLabels(new Labels(newSnip, getLabels().toString()));
    newSnip.setPermissions(getPermissions());

    Attachments copy = getAttachments().copy(newSnip.getName());
    newSnip.setAttachments(copy);
    space.store(newSnip);
    return newSnip;
  }
}
