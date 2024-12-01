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
package org.snipsnap.config;

import snipsnap.api.app.Application;
import snipsnap.api.config.*;
import snipsnap.api.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

/**
 * Implementation of a configuration map which is used as storage for configuration parameters.
 * @see Configuration
 * 
 */
public class ConfigurationMap {

  private final static String GLOBALS_CONF = "/org/snipsnap/config/globals.conf";
  private final static String DEFAULTS_CONF = "/org/snipsnap/config/defaults.conf";
  private final static String TRANSPOSE_MAP = "/org/snipsnap/config/transpose.map";


  // local settings available for a specific instance
  private Properties properties = null;
  private Properties defaults = null;

  // internal transposition map for old property file versions
  private Properties transposeMap = null;

  // static content, global values
  private static File webInfDir = null;
  private static Properties globals = null;
  private static Properties globalDefaults = null;

  // initialize global defaults and globals itself
  static {
    globalDefaults = new Properties();
    try {
      globalDefaults.load(ConfigurationMap.class.getResourceAsStream(GLOBALS_CONF));
    } catch (IOException e) {
      System.err.println("Configuration: unable to load global defaults: " + e.getMessage());
    }
    globals = new Properties(globalDefaults);
  }

  public void setGlobal(String name, String value) {
    ConfigurationMap.globals.setProperty(name, value);
  }

  public String getGlobal(String name) {
    String value = replaceTokens(ConfigurationMap.globals.getProperty(name));
    return "".equals(value) ? null : value;
  }

  public String getGlobalDefault(String name) {
    String value = globalDefaults.getProperty(name);
    return ("".equals(value) ? null : value);
  }


  // get all properties
  public Properties getGlobals() {
    return globals;
  }

  // Globals handling
  public void loadGlobals(InputStream stream) throws IOException {
    ConfigurationMap.globals.load(stream);
  }

  public void storeGlobals(OutputStream stream) throws IOException {
    ConfigurationMap.globals.store(stream, "SnipSnap Globals Configuration $Revision$");
  }

  /**
   * Sets the WEB-INF directory, where information is stored.
   * @param dir
   */
  public void setWebInfDir(File dir) {
    ConfigurationMap.webInfDir = dir;
  }

  /**
   * Returns the configuration directory which is identical to the parent directory of the
   * configuration file or null.
   * @return the configuration directory
   */
  public File getWebInfDir() {
    return ConfigurationMap.webInfDir;
  }

  public String getFileStore() {
    return getFileStore((String)snipsnap.api.app.Application.get().getObject(snipsnap.api.app.Application.OID)).getPath();
  }

  public File getFileStore(String applicationOid) {
    return new File(getGlobal(Globals.APP_FILE_STORE), applicationOid);
  }

  public String getVersion() {
    String version = System.getProperty(ServerConfiguration.VERSION);
    if (null == version) {
      version = getGlobal(ServerConfiguration.VERSION);
    }
    return version;
  }

  /**
   * Initialize new configuration map
   * @param init
   */
  public ConfigurationMap(snipsnap.api.config.Configuration init) {
    initDefaults();
    webInfDir = init.getWebInfDir();
    initialize((Properties) init.getProperties().clone());
  }

  public ConfigurationMap() {
    initDefaults();
    // instantiate properties with defaults
    initialize((Properties) defaults.clone());
  }

  private void initDefaults() {
    defaults = new Properties();
    try {
      defaults.load(Configuration.class.getResourceAsStream(DEFAULTS_CONF));
    } catch (Exception e) {
      System.err.println("Configuration: unable to load defaults: " + e.getMessage());
    }
  }

  private void initialize(Properties initProperties) {
    properties = initProperties;

    try {
      transposeMap = new Properties();
      transposeMap.load(Configuration.class.getResourceAsStream(TRANSPOSE_MAP));
    } catch (Exception e) {
      System.err.println("Configuration: unable to load transposition map: " + e.getMessage());
    }
  }


  /**
   * Stores the configuration in a properties to the stream given.
   * @param stream the output stream to write the configuration to
   * @throws IOException
   */
  public void store(OutputStream stream) throws IOException {
    properties.store(stream, "SnipSnap Configuration $Revision$");
  }

  /**
   * Load configuration from input stream. This method returns true if the load was
   * successful as is and false if a change has occurred and the configuration should
   * be stored back in its new form.
   * @param stream the input stream where properties are contained
   * @return true for as is loading and false for internally changed content
   * @throws IOException
   */
  public boolean load(InputStream stream) throws IOException {
    properties.load(stream);
    return !convertOldProperties();
  }

  private boolean convertOldProperties() {
    boolean hasChanged = false;
    Iterator propIt = transposeMap.keySet().iterator();
    while (propIt.hasNext()) {
      String oldProperty = (String) propIt.next();
      String newProperty = transposeMap.getProperty(oldProperty);
      String value = properties.getProperty(oldProperty);
      if (value != null) {
        if (newProperty != null) {
          if (newProperty.startsWith("@DEPRECATED")) {
            if (convertDeprecatedProperty(oldProperty, value)) {
              properties.remove(oldProperty);
              hasChanged = true;
            } else {
              System.out.println("INFO: Configuration option '" + oldProperty + "' is deprecated:");
              System.out.println("INFO: " + newProperty.substring("@DEPRECATED".length()));
              System.out.println("INFO: Please edit configuration file manually.");
            }
          }
          if (newProperty.startsWith("@DUPLICATE")) {
            newProperty = newProperty.substring("@DUPLICATE".length());
            String newValue = properties.getProperty(newProperty);
            //System.out.println("INFO: "+newProperty+"="+newValue);
            if (null == newValue || "".equals(newValue)) {
              System.out.println("INFO: duplicating value of '" + oldProperty + "' to '" + newProperty + "'");
              properties.setProperty(newProperty, value);
              hasChanged = true;
            }
          } else {
            System.out.println("INFO: converting '" + oldProperty + "' to '" + newProperty + "'");
            properties.remove(oldProperty);
            properties.setProperty(newProperty, value);
            hasChanged = true;
          }
        }
      }
    }
    return hasChanged;
  }

  private boolean convertDeprecatedProperty(String oldProperty, String value) {
    if ("app.domain".equals(oldProperty) || "app.url".equals(oldProperty)) {
      if (value != null && value.length() > 0) {
        try {
          URL url = new URL(value);
          System.out.println("INFO: converting '" + oldProperty + "' to 'app.real.*'");
          properties.setProperty(Configuration.APP_REAL_HOST, url.getHost());
          if (url.getPort() >= 0 && url.getPort() != 80) {
            properties.setProperty(Configuration.APP_REAL_PORT, "" + url.getPort());
          }
          properties.setProperty(Configuration.APP_REAL_PATH, url.getPath());
        } catch (MalformedURLException e) {
          System.out.println("WARNING: unable to convert '" + oldProperty + "': malformed URL: '" + value + "'");
          return false;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Set configuration parameter.
   * @param name the configuration parameter name as in Configuration interface
   * @see Configuration
   * @param value the new value
   */
  public void set(String name, String value) {
    properties.setProperty(name, value);
  }

  /**
   * Get configuration parameter. This method ensures that a parameter that is empty
   * (null or null sized string) is returned as NULL.
   * @param name the configuration parameter name as in Configuration interface
   * @see Configuration
   * @return the value of the configuration parameter
   */
  public String get(String name) {
    String value = replaceTokens(properties.getProperty(name));
    return "".equals(value) ? null : value;
  }

  // TODO replace with generic replacement method
  private String replaceTokens(String value) {
    if (value != null) {
      int idx = value.indexOf("%WEBINF%");
      if (idx != -1) {
        StringBuffer replaced = new StringBuffer();
        if (idx > 0) {
          replaced.append(value.substring(0, idx));
        }
        replaced.append(getWebInfDir().getPath());
        int endIdx = idx + "%WEBINF%".length();
        if (endIdx < value.length()) {
          replaced.append(value.substring(endIdx));
        }
        return replaced.toString();
      }
    }

    return value;
  }

  public String get(String name, String defaultValue) {
    String value = get(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }

  public Properties getProperties() {
    return properties;
  }

  public String getDefault(String name) {
    String value = defaults.getProperty(name);
    return ("".equals(value) ? null : value);
  }

  public File getFilePath(String applicationOid) {
    return new File(getFileStore(applicationOid), "snips");
  }

  public File getFilePath() {
    return getFilePath((String) snipsnap.api.app.Application.get().getObject(Application.OID));
  }

  public File getIndexPath() {
    return new File(getFileStore(), "index");
  }

  public File getUserPath(String applicationOid) {
    return new File(getFileStore(applicationOid), "users");
  }

  /**
   * Create a real locale object from the strings in our configuration.
   * @return the locale configured
   */
  public Locale getLocale() {
    String language = get(Configuration.APP_LANGUAGE, "en");
    String country = get(Configuration.APP_COUNTRY, "US");
    return new Locale(language, country);
  }

  public String getPath() {
    if ("true".equals(get(Configuration.APP_REAL_AUTODETECT))) {
      String realPath = get(Configuration.APP_REAL_PATH);
      if (null != realPath) {
        return realPath;
      }
    }
    return getGlobal(Globals.APP_PATH);
  }

  /**
   * Return base url to Snipsnap instance
   */
  public String getUrl() {
    URL url = (URL)Application.get().getObject(snipsnap.api.app.Application.URL);
    if(null != url) {
      return url.toExternalForm();
    }

    String prot = get(Configuration.APP_REAL_PROTOCOL, "http");
    String host = get(Configuration.APP_REAL_HOST);
    String port = get(Configuration.APP_REAL_PORT);
    String path = get(Configuration.APP_REAL_PATH);

    if (null == host) {
      host = getGlobal(Globals.APP_HOST);
      port = getGlobal(Globals.APP_PORT);
      path = getGlobal(Globals.APP_PATH);
    }

    StringBuffer tmp = new StringBuffer();
    tmp.append(prot).append("://");
    try {
      tmp.append(host == null || host.length() == 0 /*|| "localhost".equals(host)*/ ? InetAddress.getLocalHost().getHostName() : host);
    } catch (UnknownHostException e) {
      tmp.append(System.getProperty("host", "localhost"));
    }
    if (port != null && !"80".equals(port)) {
      tmp.append(":");
      tmp.append(port);
    }
    tmp.append(path != null ? path : "");
    return tmp.toString();
  }

  /**
   * Returns an external URL to this instance of SnipSnap
   *
   * @param target Path to add to url, e.g. "/exec/"
   */
  public String getUrl(String target) {
    return getUrl() + target;
  }

  /**
   * Returns an external URL of a snip
   * @param snipName Name of the snip
   */
  public String getSnipUrl(String snipName) {
    return getUrl("/space/" + snipName);
  }

  // PERMISSIONS
  public boolean allow(String action) {
    return "allow".equals(get(action));
  }

  public boolean deny(String action) {
    return "deny".equals(get(action));
  }

  public boolean getAllowRegister() {
    return !deny(Configuration.APP_PERM_REGISTER);
  }

  public boolean isConfigured() {
    return "true".equals(properties.getProperty(Configuration.APP_CONFIGURED));
  }

  public boolean isInstalled() {
    return "true".equals(ConfigurationMap.globals.getProperty(Globals.APP_INSTALLED));
  }


  public String toString() {
    StringBuffer result = new StringBuffer();
    Iterator it = properties.keySet().iterator();
    result.append("{");
    while (it.hasNext()) {
      String key = (String) it.next();
      result.append(key).append("=" + properties.get(key)).append(",");
    }
    result.append("}");
    return result.toString();
  }
}
