package lerfob.predictor.mathilde.climate;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
	
	private final Map<String, List<PlotCoordinates>> coordinateMap;
	private final Map<String, PlotCoordinates> departmentMean;
		
	private GeographicalCoordinatesGenerator() {
		coordinateMap = new HashMap<String, List<PlotCoordinates>>();
		departmentMean = new HashMap<String, PlotCoordinates>();
		try {
			String filename = ObjectUtility.getRelativePackagePath(getClass()) + "plotCoordinates.csv"; 
			CSVReader reader = new CSVReader(filename);
			Object[] record;
			
			while ((record = reader.nextRecord()) != null) {
				String department = record[6].toString();
				double latitude = Double.parseDouble(record[2].toString());
				double longitude = Double.parseDouble(record[1].toString());
				if (!coordinateMap.containsKey(department)) {
					coordinateMap.put(department, new ArrayList<PlotCoordinates>());
				}
				coordinateMap.get(department).add(new PlotCoordinates(latitude, longitude));
			}
			
			for (String department : coordinateMap.keySet()) {
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
		}
	}

	
	/**
	 * This method returns a number of plot coordinates for imputation.
	 * @param n the number of coordinates needed
	 * @param department a String representing the department e.g. "88"
	 * @return a List of PlotCoordinates instances
	 */
	public List<PlotCoordinates> getRandomCoordinates(int n, String department) {
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
	 * @param department a String that represents the department e.g. "88"
	 * @return a PlotCoordinates instance
	 */
	public PlotCoordinates getMeanCoordinatesForThisDepartment(String department) {
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
