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

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpc;
import org.radeox.util.logging.Logger;

import java.util.Vector;

/**
 * Small tool to test XML-RPC
 *
 * @author Stephan J. Schmidt
 * 
 */

public class XmlRpcTest {
  public static void main(String[] args) {
    try {
      Vector params = new Vector();
      XmlRpcClient test = new XmlRpcClient("http://localhost:8668/RPC2");
      params.clear();
      // Name of the weblog
      //params.addElement(config.getName());
      // Url of the weblog
      Object result = test.execute("generator.version", params);
      Logger.debug("result="+result);
      result = test.execute("snipSnap.dumpXml", params);
      System.out.println(new String((byte[]) result, "UTF-8"));
    } catch (Exception e) {
      Logger.warn("XmlRpcTest: unable to call XML-RPC", e);
    }

  }
}
