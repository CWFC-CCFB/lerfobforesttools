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
import repicea.util.ObjectUtility;

public class FrenchHDRelationship2018ClimateGenerator {

	static class ClimatePoint {
		final double xCoord;
		final double yCoord;
		final double meanSeasonalTemp;
		final double meanSeasonalPrec;
		
		ClimatePoint(double xCoord, double yCoord, double meanSeasonalTemp, double meanSeasonalPrec) {
			this.xCoord = xCoord;
			this.yCoord = yCoord;
			this.meanSeasonalTemp = meanSeasonalTemp;
			this.meanSeasonalPrec = meanSeasonalPrec; 
		}
		
		double getDistanceFromTheseCoordinates(double x, double y) {
			double xDiff = x - xCoord;
			double yDiff = y - yCoord;
			return Math.sqrt(xDiff * xDiff + yDiff * yDiff);
		}
	}
	
	private final Map<Integer, Map<Integer, List<ClimatePoint>>> zoneMap;
	
	FrenchHDRelationship2018ClimateGenerator() {
		zoneMap = new HashMap<Integer, Map<Integer, List<ClimatePoint>>>();
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
			double meanSeasonalTemp = Double.parseDouble(record[3].toString());
			double meanSeasonalPrec = Double.parseDouble(record[4].toString());
			ClimatePoint point = new ClimatePoint(xCoord, yCoord, meanSeasonalTemp, meanSeasonalPrec);
			registerClimatePoint(point);
		}
		reader.close();
	}

	private void registerClimatePoint(ClimatePoint point) {
		int xKey = Math.round((float) point.xCoord);
		int yKey = Math.round((float) point.yCoord); 
		
		if (!zoneMap.containsKey(xKey)) {
			zoneMap.put(xKey, new HashMap<Integer, List<ClimatePoint>>());
		}
		
		Map<Integer, List<ClimatePoint>> innerMap = zoneMap.get(xKey);
		if (!innerMap.containsKey(yKey)) {
			innerMap.put(yKey, new ArrayList<ClimatePoint>());
		}
		
		innerMap.get(yKey).add(point);
	}
	
	ClimatePoint getNearestClimatePoint(double x, double y) {
		ClimatePoint nearestPoint = null;
		int xKey = Math.round((float) x);
		int yKey = Math.round((float) y); 
		
		if (zoneMap.containsKey(xKey)) {
			Map<Integer, List<ClimatePoint>> innerMap = zoneMap.get(xKey);
			if (innerMap.containsKey(yKey)) {
				List<ClimatePoint> possibleClimatePoints = innerMap.get(yKey);
				double minDistance = Double.MAX_VALUE;
				double distanceToThisPoint;
				for (ClimatePoint cp : possibleClimatePoints) {
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
	
//	public static void main(String[] args) {
//		FrenchHDRelationship2018ClimateGenerator gen = new FrenchHDRelationship2018ClimateGenerator();
//		ClimatePoint cp = gen.getNearestClimatePoint(0, 48);
//		int u = 0;
//	}
	
}
