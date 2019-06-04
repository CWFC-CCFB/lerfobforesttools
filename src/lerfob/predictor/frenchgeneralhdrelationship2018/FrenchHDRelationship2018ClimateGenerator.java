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
package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.io.javacsv.CSVReader;
import repicea.simulation.climate.REpiceaClimateGenerator;
import repicea.simulation.climate.REpiceaClimateVariableMap;
import repicea.simulation.climate.REpiceaClimateVariableMap.UpdatableClimateVariableMap;
import repicea.simulation.covariateproviders.standlevel.GeographicalCoordinatesProvider;
import repicea.util.ObjectUtility;

/**
 * The FrenchHDRelationship2018ClimateGenerator class implements a model of climate that provides
 * the seasonal mean temperature and precipitation (e.g. from March to September). The climate variable
 * are calculated over the 1961-1990 period.
 * @author Mathieu Fortin - December 2017
 */
public class FrenchHDRelationship2018ClimateGenerator implements REpiceaClimateGenerator<GeographicalCoordinatesProvider> {

	@SuppressWarnings("serial")
	static class FrenchHDClimateVariableMap extends REpiceaClimateVariableMap implements UpdatableClimateVariableMap {
		
		final double xCoord;
		final double yCoord;
		final String ser;
		
		FrenchHDClimateVariableMap(double xCoord, double yCoord, double meanSeasonalTemp, double meanSeasonalPrec, String ser) {
			this.xCoord = xCoord;
			this.yCoord = yCoord;
			put(ClimateVariable.MeanSeasonalTempC, meanSeasonalTemp);
			put(ClimateVariable.MeanSeasonalPrecMm, meanSeasonalPrec);
			this.ser = ser;
		}
		
		double getDistanceFromTheseCoordinates(double x, double y) {
			double xDiff = x - xCoord;
			double yDiff = y - yCoord;
			return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		}
		
		String getSer() {return ser;}

		@Override
		public REpiceaClimateVariableMap getUpdatedClimateVariableMap(Map<ClimateVariable, Double> annualChanges, int dateYr) {
			int since2015 = dateYr - 2015;
			if (annualChanges == null || annualChanges.isEmpty()) {
				return this;
			} if (since2015 <= 0) {
				return this;
			} else {
				REpiceaClimateVariableMap updatedMap = new REpiceaClimateVariableMap();
				for (ClimateVariable var : keySet()) {
					if (annualChanges.containsKey(var)) {
						double annualChange = annualChanges.get(var);
						double newValue = get(var) + annualChange * since2015;
						updatedMap.put(var, newValue);
					} else {
						updatedMap.put(var, get(var)); // here we just copy the value since there is no change
					}
				}
				return updatedMap;
			}
		}
	}
	
	private final Map<Integer, Map<Integer, List<FrenchHDClimateVariableMap>>> zoneMap;
	
	FrenchHDRelationship2018ClimateGenerator() {
		zoneMap = new HashMap<Integer, Map<Integer, List<FrenchHDClimateVariableMap>>>();
		try {
			init();
		} catch (IOException e) {
			System.out.println("Unable to load the climate variable in the French HD relationships (version 2018)");			
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		CSVReader reader = new CSVReader(ObjectUtility.getRelativePackagePath(getClass()) + "dataForClimateGeneratorHDRelationships.csv");
		Object[] record;

		while ((record = reader.nextRecord()) != null) {
			double xCoord = Double.parseDouble(record[1].toString());
			double yCoord = Double.parseDouble(record[2].toString());
			String ser = record[3].toString().trim();
			double meanSeasonalTemp = Double.parseDouble(record[4].toString());
			double meanSeasonalPrec = Double.parseDouble(record[5].toString());
			FrenchHDClimateVariableMap point = new FrenchHDClimateVariableMap(xCoord, yCoord, meanSeasonalTemp, meanSeasonalPrec, ser);
			registerClimatePoint(point);
		}
		reader.close();
	}

	private void registerClimatePoint(FrenchHDClimateVariableMap point) {
		int xKey = Math.round((float) point.xCoord);
		int yKey = Math.round((float) point.yCoord); 
		
		if (!zoneMap.containsKey(xKey)) {
			zoneMap.put(xKey, new HashMap<Integer, List<FrenchHDClimateVariableMap>>());
		}
		
		Map<Integer, List<FrenchHDClimateVariableMap>> innerMap = zoneMap.get(xKey);
		if (!innerMap.containsKey(yKey)) {
			innerMap.put(yKey, new ArrayList<FrenchHDClimateVariableMap>());
		}
		
		innerMap.get(yKey).add(point);
	}
	
	private FrenchHDClimateVariableMap getNearestClimatePoint(double x, double y) {
		FrenchHDClimateVariableMap nearestPoint = null;
		int xKey = Math.round((float) x);
		int yKey = Math.round((float) y); 
		
		if (zoneMap.containsKey(xKey)) {
			Map<Integer, List<FrenchHDClimateVariableMap>> innerMap = zoneMap.get(xKey);
			if (innerMap.containsKey(yKey)) {
				List<FrenchHDClimateVariableMap> possibleClimatePoints = innerMap.get(yKey);
				double minDistance = Double.MAX_VALUE;
				double distanceToThisPoint;
				for (FrenchHDClimateVariableMap cp : possibleClimatePoints) {
					distanceToThisPoint = cp.getDistanceFromTheseCoordinates(x, y);
					if (distanceToThisPoint < minDistance) {
						nearestPoint = cp;
						minDistance = distanceToThisPoint;
					}
				}
			}
		}
		return nearestPoint;
	}
	
	List<FrenchHDClimateVariableMap> getClimatePoints() {
		List<FrenchHDClimateVariableMap> points = new ArrayList<FrenchHDClimateVariableMap>();
		for (Map<Integer, List<FrenchHDClimateVariableMap>> oMap : zoneMap.values()) {
			for (List<FrenchHDClimateVariableMap> pp : oMap.values()) {
				points.addAll(pp);
			}
		}
		return points;
	}

	@Override
	public FrenchHDClimateVariableMap getClimateVariables(GeographicalCoordinatesProvider plot) {
		return getNearestClimatePoint(plot.getLongitudeDeg(), plot.getLatitudeDeg());
	}
	
	
//	public static void main(String[] args) {
//		FrenchHDRelationship2018ClimateGenerator gen = new FrenchHDRelationship2018ClimateGenerator();
//		ClimatePoint cp = gen.getNearestClimatePoint(0, 48);
//		int u = 0;
//	}
	
}
