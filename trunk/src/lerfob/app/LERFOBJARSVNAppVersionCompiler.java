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

import repicea.app.AbstractAppVersionCompiler;
import repicea.util.ObjectUtility;

public class LERFOBJARSVNAppVersionCompiler extends AbstractAppVersionCompiler {

	private static final String APP_URL = "http://svn.code.sf.net/p/lerfobforesttools/code/trunk";
	private static String Version_Filename_Bin = ObjectUtility.getPackagePath(LERFOBJARSVNAppVersionCompiler.class) + LERFOBJARSVNAppVersion.ShortFilename;
	
	public LERFOBJARSVNAppVersionCompiler() {
		super();
	}
	
	
	public static void main(String[] args) {
		LERFOBJARSVNAppVersionCompiler compiler = new LERFOBJARSVNAppVersionCompiler();
		try {
			compiler.createRevisionFile(APP_URL, Version_Filename_Bin);
			System.out.println("Revision file in binaries successfully updated!");
		} catch (Exception e) {
			System.out.println("Error while updating revision file!");
		}

	}

}
