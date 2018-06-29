/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde.climate;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import lerfob.simulation.covariateproviders.standlevel.FrenchDepartmentProvider.FrenchDepartment;
import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public final class GeographicalCoordinatesGenerator {

	private final static Random RandomGenerator = new Random();

	private static GeographicalCoordinatesGenerator Singleton;

	public static class PlotCoordinates implements Comparable<PlotCoordinates> {
		public final double longitude;
		public final double latitude;
		private final double random;
		
		private PlotCoordinates(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
			random = RandomGenerator.nextDouble();
		}

		@Override
		public int compareTo(PlotCoordinates arg0) {
			if (random < arg0.random) {
				return -1;
			} else if (random == arg0.random) {
				return 0;
			} else {
				return 1;
			}
		}
	
		public String toString() {
			return "Latitude = " + latitude + "; Longitude = " + longitude;
		}
	}
	
	private final Map<FrenchDepartment, List<PlotCoordinates>> coordinateMap;
	private final Map<FrenchDepartment, PlotCoordinates> departmentMean;
		
	private GeographicalCoordinatesGenerator() {
		coordinateMap = new HashMap<FrenchDepartment, List<PlotCoordinates>>();
		departmentMean = new HashMap<FrenchDepartment, PlotCoordinates>();
		CSVReader reader = null;
		try {
			String filename = ObjectUtility.getRelativePackagePath(getClass()) + "plotCoordinates.csv"; 
			reader = new CSVReader(filename);
			Object[] record;
			
			while ((record = reader.nextRecord()) != null) {
				String departmentCode = record[6].toString();
				FrenchDepartment department = FrenchDepartment.getDepartment(departmentCode); 
				double latitude = Double.parseDouble(record[2].toString());
				double longitude = Double.parseDouble(record[1].toString());
				if (!coordinateMap.containsKey(department)) {
					coordinateMap.put(department, new ArrayList<PlotCoordinates>());
				}
				coordinateMap.get(department).add(new PlotCoordinates(latitude, longitude));
			}
			
			for (FrenchDepartment department : coordinateMap.keySet()) {
				List<PlotCoordinates> plotCoordinateColl = coordinateMap.get(department);
				Collections.sort(plotCoordinateColl);
				double latitude = 0;
				double longitude = 0;
				int n = 0;
				for (PlotCoordinates coord : plotCoordinateColl) {
					latitude += coord.latitude;
					longitude += coord.longitude;
					n++;
				}
				departmentMean.put(department, new PlotCoordinates(latitude/n, longitude/n));
			}
			
		} catch (IOException e) {
			System.out.println("Unable to read plot coordinates in LambertCoordinatesGenerator class");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	
	/**
	 * This method returns a number of plot coordinates for imputation.
	 * @param n the number of coordinates needed
	 * @param department a FrenchDepartment representing the department
	 * @return a List of PlotCoordinates instances
	 */
	public List<PlotCoordinates> getRandomCoordinates(int n, FrenchDepartment department) {
		if (!coordinateMap.containsKey(department)) {
			throw new InvalidParameterException("This department is not recognized in Mathilde!");
		} else {
			List<PlotCoordinates> innerColl = coordinateMap.get(department);
			if (n > innerColl.size()) {
				throw new InvalidParameterException("There is not enough plot coordinates!");
			} else {
				List<PlotCoordinates> outputList = new ArrayList<PlotCoordinates>();
				for (int i = 0; i < n; i++) {
					outputList.add(innerColl.get(i));
				}
				return outputList;
			}
		}
	}
	
	/**
	 * This method returns the mean coordinates for a given department.
	 * @param department a FrenchDepartment enum  that represents the department
	 * @return a PlotCoordinates instance
	 */
	public PlotCoordinates getMeanCoordinatesForThisDepartment(FrenchDepartment department) {
		if (!departmentMean.containsKey(department)) {
			throw new InvalidParameterException("This department is not recognized in Mathilde!");
		} else {
			return departmentMean.get(department);
		}
	}


	public static GeographicalCoordinatesGenerator getInstance() {
		if (Singleton == null) {
			Singleton = new GeographicalCoordinatesGenerator();
		} 
		return Singleton;
	}
	
//	public static void main(String[] args) {
//		PlotCoordinates coord = LambertCoordinatesGenerator.getInstance().getMeanCoordinatesForThisDepartment("08");
//		System.out.println(coord);
//		List<PlotCoordinates> coords = LambertCoordinatesGenerator.getInstance().getRandomCoordinates(3, "08");
//		int u = 0;
//	}
	
}
