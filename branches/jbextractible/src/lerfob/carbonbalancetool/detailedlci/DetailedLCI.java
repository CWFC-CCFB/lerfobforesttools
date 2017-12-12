/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DetailedLCI class contains all the components of the lifecycle inventory.
 * @author Mathieu Fortin - February 2014
 */
public class DetailedLCI {

	public static enum Unit {
		m3,
		ha,
		kg,
		Bq,
		MJ,
		m2a,
		m2,
		m3y;
	}

	@SuppressWarnings("unused")
	private static class LCIComponent {
		String element;
		String compartment;
		String subcompartment;
		Unit basicUnit;
		double amount;
		
		private LCIComponent(String element, 
				String compartment,
				String subcompartment,
				String unit,
				String total) {
			this.element = element;
			this.compartment = compartment;
			this.subcompartment = subcompartment;
			this.basicUnit = Unit.valueOf(unit);
			try {
				this.amount = Double.parseDouble(total);
			} catch (NumberFormatException e) {
				this.amount = 0d;
			}
		}
	}

	@SuppressWarnings("unused")
	private Unit basicUnit;
	@SuppressWarnings("unused")
	private double amount;
	private String filename;
	
	private List<LCIComponent> components;
	private Map<String, Map<String, List<LCIComponent>>> componentMap;
	
	/**
	 * Protected constructor to be called from within the package.
	 * @param filename the file to be read.
	 * @throws IOException
	 */
	protected DetailedLCI(String filename) throws IOException {
		components = new ArrayList<LCIComponent>();
		componentMap = new HashMap<String, Map<String, List<LCIComponent>>>();
		this.filename = filename;
		readFile();
	}
	
	private void readFile() throws IOException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
			boolean startReading = false;
			Map<String, List<LCIComponent>> subMap;
			LCIComponent component;
			String line;
			String[] record;
			
			while ((line = br.readLine()) != null) {
				if (!startReading) {
					if (line.startsWith("Product")) {
						record = line.split(";");
						String[] subRecord = record[1].split(" ");
						amount = Double.parseDouble(subRecord[0]);
						basicUnit = Unit.valueOf(subRecord[1].trim());
					}
					if (line.startsWith("N�")) {
						startReading = true;
					}
				} else {
					record = line.split(";");
					component = new LCIComponent(record[1], 
							record[2], 
							record[3], 
							record[4].replace(".", "_").replace("*", "_"), 
							record[5]);
					components.add(component);
					if (!componentMap.containsKey(component.compartment)) {
						componentMap.put(component.compartment, new HashMap<String, List<LCIComponent>>());
					}
					subMap = componentMap.get(component.compartment);
					if (!subMap.containsKey(component.element)) {
						subMap.put(component.element, new ArrayList<LCIComponent>());
					}
					subMap.get(component.element).add(component);
//					if (!unitString.contains(component.unit)) {
//						unitString.add(component.unit);
//					}
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (br != null) {
				br.close();
			}
		}
	}
	
	@Override
	public String toString() {
		String name = filename.replace("/", "\\");
		int index = filename.lastIndexOf(".");
		name = name.substring(name.lastIndexOf("\\")+1, index);
		return name; 
	}
	
	public static void main(String[] args) throws IOException {
		new DetailedLCI("testLCI.CSV");
		System.exit(0);
	}
	
	
}
