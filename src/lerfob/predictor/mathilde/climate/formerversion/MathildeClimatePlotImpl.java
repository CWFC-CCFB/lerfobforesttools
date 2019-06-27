/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde.climate.formerversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lerfob.predictor.mathilde.climate.MathildeClimatePlot;

class MathildeClimatePlotImpl implements MathildeClimatePlot {

	final String name;
	final double meanAnnualTempAbove6C;
	final int dateYr;
	final double xCoord;
	final double yCoord;
	final double pred;
	int realization;
	
	MathildeClimatePlotImpl(String name, double xCoord, double yCoord, int dateYr, double meanAnnualTempAbove6C, double pred) {
		this.name = name;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.dateYr = dateYr;
		this.meanAnnualTempAbove6C = meanAnnualTempAbove6C;
		this.pred = pred;
	}
	
	@Override
	public String getSubjectId() {
		return name;
	}

	@Override
	public int getDateYr() {
		return dateYr;
	}


	protected double getPrediction() {return pred;}

	@Override
	public int getMonteCarloRealizationId() {return realization;}

	
	@Override
	public double getLatitudeDeg() {return yCoord;}

	@Override
	public double getLongitudeDeg() {return xCoord;}

	@Override
	public double getElevationM() {
		return 0;
	}

	@Override
	public List<MathildeClimatePlot> getAllMathildeClimatePlots() {
		Map<String, MathildeClimatePlot> standMap = new TreeMap<String, MathildeClimatePlot>(); 
		for (MathildeClimatePlot s : MathildeClimatePredictor.getReferenceStands()) {
			MathildeClimatePlotImpl stand = (MathildeClimatePlotImpl) s;
			if (!standMap.containsKey(stand.name)) {
				standMap.put(stand.name, stand);
			}
		}
		List<MathildeClimatePlot> stands = new ArrayList<MathildeClimatePlot>();
		stands.addAll(standMap.values());
		return stands;
	}

	/*
	 * Useless for this predictor (non-Javadoc)
	 * @see repicea.simulation.covariateproviders.standlevel.GrowthStepLengthYrProvider#getGrowthStepLengthYr()
	 */
	@Override
	public double getGrowthStepLengthYr() {
		return 0;
	}

	/*
	 * Useless for this predictor (non-Javadoc)
	 * @see repicea.simulation.covariateproviders.standlevel.GrowthStepLengthYrProvider#getGrowthStepLengthYr()
	 */
	@Override
	public int getNumberOfDroughtsDuringUpcomingGrowthStep() {
		return 0;
	}


}
