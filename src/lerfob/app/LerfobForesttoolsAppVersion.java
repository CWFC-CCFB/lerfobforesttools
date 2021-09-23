/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2015 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.app;

import repicea.app.REpiceaAppVersion;

/**
 * This class retrieves information on the version and other features of the 
 * lerfob-forestools.jar application. 
 * @author Mathieu Fortin - August 2015
 */
public class LerfobForesttoolsAppVersion extends REpiceaAppVersion {

	private static LerfobForesttoolsAppVersion SINGLETON;
	
	private LerfobForesttoolsAppVersion() {
		super();
	}

	/**
	 * This method returns the singleton instance of REpiceaJARSVNAppVersion class which can be requested
	 * to return the revision number of this version.
	 * @return the singleton instance of the REpiceaJARSVNAppVersion class
	 */
	public static LerfobForesttoolsAppVersion getInstance() {
		if (SINGLETON == null) {
			SINGLETON = new LerfobForesttoolsAppVersion();
		}
		return SINGLETON;
	}
	

}
