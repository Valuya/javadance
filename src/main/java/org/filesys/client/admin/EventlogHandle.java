/*
 * Copyright (C) 2006-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.filesys.client.admin;

import org.filesys.smb.dcerpc.PolicyHandle;

/**
 * Eventlog Handle Class
 * 
 * <p>
 * Contains the handle to a remote eventlog.
 * 
 * @author gkspencer
 */
public class EventlogHandle extends PolicyHandle {

	/**
	 * Class constructor
	 * 
	 * @param logName String
	 */
	public EventlogHandle(String logName) {
		super();
		setName(logName);
	}

	/**
	 * Class constructor
	 * 
	 * @param logName String
	 * @param buf byte[]
	 * @param off int
	 */
	public EventlogHandle(String logName, byte[] buf, int off) {
		super(logName, buf, off);
	}
}
