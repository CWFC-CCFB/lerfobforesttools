/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin AgroParisTech/INRA UMR LERFoB
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
package lerfob.carbonbalancetool.detailedlci;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import repicea.io.GFileFilter;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class DetailedLCIManager extends HashMap<String, DetailedLCI> {

	private static final DetailedLCIManager Instance = new DetailedLCIManager();
	
	private DetailedLCIManager() {
		URL url = ClassLoader.getSystemResource(ObjectUtility.getRelativePackagePath(getClass()));
		List<String> filenames = new ArrayList<String>();
		File[] files = new File(url.getFile()).listFiles();
		if (files == null) {
			File directory = new File(ObjectUtility.getPackagePath(getClass()));
			files = directory.listFiles();
		}
		for (File file : files) {
			if (GFileFilter.CSV.accept(file)) {
				filenames.add(file.getAbsolutePath());
			}
		}
		DetailedLCI lci;
		try {
			for (String filename : filenames) {
//				System.out.println("Reading file : " + filename);
				lci = new DetailedLCI(filename); 
				put(filename, lci);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static DetailedLCIManager getInstance() {
		return Instance;
	}
	
	public static void main(String[] args) {
		getInstance();
	}
	
	
}
