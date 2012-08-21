/*
 * org.openmicroscopy.shoola.env.data.events.ViewInPluginEvent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.events;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.DataObject;

/** 
 * View the image in the specified plug-in.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ViewInPluginEvent
	extends RequestEvent
{

	/** Identifies the <code>ImageJ</code> plugin.*/
	public static final int IMAGE_J = 1;
	
	/** The selected plugin.*/
	private int plugin;
	
	/** The object to view.*/
	private DataObject object;
	
	/** The security context.*/
	private SecurityContext ctx;
	
	/**
	 * The id of the object.
	 */
	private long objectID;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param objectID The object's id to view.
	 * @param plugin The selected plugin.
	 */
	public ViewInPluginEvent(SecurityContext ctx, long objectID, int plugin)
	{
		this.plugin = plugin;
		this.objectID = objectID;
		this.ctx = ctx;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param ctx The security context.
	 * @param object The object to view.
	 * @param plugin The selected plugin.
	 */
	public ViewInPluginEvent(SecurityContext ctx, DataObject object, int plugin)
	{
		this.plugin = plugin;
		this.object = object;
		this.ctx = ctx;
	}
	
	/**
	 * Returns the security context.
	 * 
	 * @return See above.
	 */
	public SecurityContext getSecurityContext() { return ctx; }
	
	/** 
	 * Returns the selected plug-in.
	 * 
	 * @return See above.
	 */
	public int getPlugin() { return plugin; }
	
	/**
	 * Returns the data object.
	 * 
	 * @return See above.
	 */
	public DataObject getObject() { return object; }
	
	
	/**
	 * Returns the data object.
	 * 
	 * @return See above.
	 */
	public long getObjectID()
	{ 
		if (object != null) return object.getId();
		return objectID; 
	}
}