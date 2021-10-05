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
package lerfob.predictor.mathilde.climate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class MathildeClimatePlotImpl implements MathildeClimatePlot {

	final String name;
	final double meanAnnualTempAbove6C;
	final int dateYr;
	final int growthStepLengthYr;
	final int nbDroughtsInUpcomingGrowthStep;
	final double xCoord;
	final double yCoord;
	final double pred;
	final double varPred;
	final double scaledResid;
	int realization;
	
	MathildeClimatePlotImpl(String name, 
			double xCoord, 
			double yCoord,
			int dateYr, 
			int growthStepLengthYr,
			int nbDroughtsInUpcomingGrowthStep,
			double meanAnnualTempAbove6C, 
			double pred, 
			double varPred,
			double scaledResid) {
		this.name = name;
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		this.dateYr = dateYr;
		this.growthStepLengthYr = growthStepLengthYr;
		this.nbDroughtsInUpcomingGrowthStep = nbDroughtsInUpcomingGrowthStep;
		this.meanAnnualTempAbove6C = meanAnnualTempAbove6C;
		this.pred = pred;
		this.varPred = varPred;
		this.scaledResid = scaledResid;
	}
	
	@Override
	public String getSubjectId() {
		return name;
	}

	@Override
	public int getDateYr() {
		return dateYr;
	}


	double getPrediction() {return pred;}

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

	@Override
	public double getGrowthStepLengthYr() {return growthStepLengthYr;}

	@Override
	public int getNumberOfDroughtsDuringUpcomingGrowthStep() {return nbDroughtsInUpcomingGrowthStep;}

	double getPredictionVariance() {
		return varPred;
	}
	
	double getScaledResidual() {
		return scaledResid;
	}


}
