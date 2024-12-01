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


import org.picocontainer.PicoContainer;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.context.RenderContext;
import snipsnap.api.app.Application;
import snipsnap.api.container.Components;
import snipsnap.api.render.context.SnipRenderContext;

import java.util.Collection;

import snipsnap.api.snip.*;
import snipsnap.api.snip.SnipSpace;
import snipsnap.api.snip.Snip;

/**
 * SnipFormatter supplies some methods for handling Snip Content.
 *
 * @author stephan
 * 
 */
public class SnipFormatter
{
	public static String toXML(Snip snip, String content)
	{
    //@FIXME: This duplicates SnipImpl.toXML()
    RenderEngine engine = (RenderEngine) snipsnap.api.container.Components.getComponent(snipsnap.api.container.Components.DEFAULT_ENGINE);
    RenderContext context = new snipsnap.api.render.context.SnipRenderContext(snip, (snipsnap.api.snip.SnipSpace) Components.getComponent(SnipSpace.class));
    context.setParameters(Application.get().getParameters());
    return engine.render(content, context);
  }
}
