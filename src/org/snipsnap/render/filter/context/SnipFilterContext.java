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

package org.snipsnap.render.filter.context;

import org.radeox.macro.parameter.MacroParameter;
import org.radeox.filter.context.BaseFilterContext;
import org.snipsnap.render.macro.parameter.SnipMacroParameter;
import snipsnap.api.render.context.SnipRenderContext;
import snipsnap.api.snip.Snip;

/**
 * Special implementation of FilterContext to execute
 * filters in a SnipSnap enviroment. Especially stores
 * the snip in which it is called.
 *
 * @author Stephan J. Schmidt
 * 
 */

public class SnipFilterContext extends BaseFilterContext {
  private snipsnap.api.snip.Snip snip;

  public SnipFilterContext(snipsnap.api.snip.Snip snip) {
    this.snip = snip;
  }

  public snipsnap.api.snip.Snip getSnip() {
    return snip;
  }

  public SnipRenderContext getSnipRenderContext() {
    return (SnipRenderContext) this.context;
  }


  public MacroParameter getMacroParameter() {
    return new SnipMacroParameter(this.context);
  }

}
