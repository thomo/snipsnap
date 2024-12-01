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
package org.snipsnap.net;

import org.radeox.util.logging.Logger;
import snipsnap.api.app.Application;
import snipsnap.api.config.Configuration;
import org.snipsnap.net.filter.MultipartWrapper;
import snipsnap.api.snip.Snip;
import snipsnap.api.snip.SnipSpace;
import snipsnap.api.snip.SnipSpaceFactory;
import org.snipsnap.snip.attachment.Attachment;
import org.snipsnap.snip.attachment.Attachments;
import org.snipsnap.snip.attachment.storage.AttachmentStorage;
import org.snipsnap.snip.storage.XMLFileSnipStorage;
import org.snipsnap.user.Permissions;
import org.snipsnap.user.Roles;
import org.snipsnap.user.Security;
import snipsnap.api.user.User;
import snipsnap.api.container.Components;
import org.snipsnap.security.AccessController;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;

import gabriel.components.context.OwnerAccessContext;
import gabriel.Permission;

/**
 * Servlet to store attachments to snips.
 * @author Matthias L. Jugel
 * 
 */
public class FileUploadServlet extends HttpServlet {

  private Roles roles = new Roles();

  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    roles.add("Editor");
    roles.add("Admin");
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    doPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    Configuration config = Application.get().getConfiguration();

    // If this is not a multipart/form-data request continue
    String type = request.getHeader("Content-Type");
    if (type != null && type.startsWith("multipart/form-data")) {
      try {
        request = new MultipartWrapper(request, config.getEncoding() != null ? config.getEncoding() : "UTF-8");
      } catch (IllegalArgumentException e) {
        Logger.warn("FileUploadServlet: multipart/form-data wrapper:" + e.getMessage());
      }
    }

    String snipName = request.getParameter("name");

    if (null == snipName) {
      response.sendRedirect(config.getUrl());
      return;
    }

    snipsnap.api.snip.SnipSpace space = SnipSpaceFactory.getInstance();
    Snip snip = space.load(snipName);

    if (request.getParameter("cancel") != null) {
      response.sendRedirect(config.getUrl("/space/" + snip.getNameEncoded()));
      return;
    }

    AttachmentStorage attachmentStorage = (AttachmentStorage) Components.getComponent(AttachmentStorage.class);
    AccessController controller = (AccessController) Components.getComponent(AccessController.class);

    User user = Application.get().getUser();
    if (controller.checkPermission(user, AccessController.ADD_ATTACHMENT, snip)) {
      if (request.getParameter("upload") != null) {
        try {
          uploadFile(request, snip);
        } catch (IOException e) {
          request.setAttribute("error", "I/O Error while uploading.");
          e.printStackTrace();
        }
      } else if (request.getParameter("delete") != null) {
        String files[] = request.getParameterValues("attfile");

        if (files != null && files.length > 0) {
          Attachments attachments = snip.getAttachments();
          for (int fileNo = 0; fileNo < files.length; fileNo++) {
            Attachment attachment = attachments.getAttachment(files[fileNo]);
            if (null != attachment) {
              attachmentStorage.delete(attachment);
              attachments.removeAttachment(attachment);
            }
          }
          // make sure to store the changed snip
          SnipSpaceFactory.getInstance().store(snip);
        } else {
          request.setAttribute("error", "Please select files to delete.");
        }
      }
    } else {
      request.setAttribute("error", "You don't have permission to upload or delete files.");
    }

    request.setAttribute("snip", snip);
    request.setAttribute("snip_name", snipName);
    RequestDispatcher dispatcher = request.getRequestDispatcher("/exec/upload.jsp");
    dispatcher.forward(request, response);
  }

  public String uploadFile(HttpServletRequest request, snipsnap.api.snip.Snip snip) throws IOException {
    AttachmentStorage attachmentStorage = (AttachmentStorage) Components.getComponent(AttachmentStorage.class);

    MultipartWrapper wrapper = (MultipartWrapper) request;
    String fileName = wrapper.getParameter("filename");
    String contentType = wrapper.getParameter("mimetype");
    if (null == contentType || contentType.length() == 0) {
      contentType = wrapper.getFileContentType("file");
    }
    // make sure the file name does not contain slashes or backslashes
    if (null == fileName || fileName.length() == 0) {
      fileName = getCanonicalFileName(wrapper.getFileName("file"));
    } else {
      fileName = getCanonicalFileName(fileName);
    }

    InputStream fileInputStream = wrapper.getFileInputStream("file");

    if (fileInputStream != null && fileName != null && fileName.length() > 0 && contentType != null) {

      // Logger.log(Logger.DEBUG, "Uploading '" + relativeFileLocation.getName() + "' to '" + file.getCanonicalPath() + "'");
      File relativeFileLocation = new File(snip.getName(), fileName);
      Attachment attachment = new Attachment(relativeFileLocation.getName(), contentType, 0,  new Date(), relativeFileLocation.getPath());

      OutputStream out = attachmentStorage.getOutputStream(attachment);
      int size = storeAttachment(out, fileInputStream);
      attachment.setSize(size);
      out.close();
      fileInputStream.close();

      snip.getAttachments().addAttachment(attachment);

      snipsnap.api.snip.SnipSpaceFactory.getInstance().store(snip);
      return fileName;
    }
    return null;
  }

  // make file name pure without the path
  public static String getCanonicalFileName(String fileName) throws IOException {
    int slashIndex = fileName.lastIndexOf('\\');
    if (slashIndex >= 0) {
      Logger.log(Logger.WARN, "Windows path detected, cutting off: " + fileName);
      fileName = fileName.substring(slashIndex + 1);
    }

    slashIndex = fileName.lastIndexOf('/');
    if (slashIndex != -1) {
      Logger.log(Logger.WARN, "UNIX path detected, cutting off: " + fileName);
      fileName = fileName.substring(slashIndex + 1);
    }

    if (fileName.equals(XMLFileSnipStorage.SNIP_XML)) {
      throw new IOException("illegal file name");
    }

    return fileName;
  }

  // store file from input stream into a file
  public int storeAttachment(OutputStream out, InputStream in) throws IOException {
    byte[] buf = new byte[4096];
    int length = 0, size = 0;
    while ((length = in.read(buf)) != -1) {
      out.write(buf, 0, length);
      size += length;
    }
    return size;
  }
}
