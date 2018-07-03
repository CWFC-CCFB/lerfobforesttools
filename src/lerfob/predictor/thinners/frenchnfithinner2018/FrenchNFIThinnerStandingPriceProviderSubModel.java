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
package lerfob.predictor.thinners.frenchnfithinner2018;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.distributions.EmpiricalDistribution;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
class FrenchNFIThinnerStandingPriceProviderSubModel extends REpiceaPredictor {

	class Year implements MonteCarloSimulationCompliantObject {

		int monteCarloId;
		final String subjectId;
		
		Year(int yearDate) {
			this.subjectId = ((Integer) yearDate).toString();
		}
		
		@Override
		public String getSubjectId() {
			return subjectId;
		}

		@Override
		public HierarchicalLevel getHierarchicalLevel() {
			return HierarchicalLevel.YEAR;
		}

		@Override
		public int getMonteCarloRealizationId() {
			return monteCarloId;
		}
		
	}
	
	
	final Map<Integer, Year> yearDateMap;
	final Map<Integer, Map<Integer, Double>> realizedPriceMap;		// realization : year : error value 
	final FrenchNFIThinnerStandingPriceProvider caller;
	final Map<Integer, Double> observedPriceMap;

	FrenchNFIThinnerStandingPriceProviderSubModel(boolean isVariabilityEnabled, FrenchNFIThinnerStandingPriceProvider caller) {
		super(false, isVariabilityEnabled, false); // although it was a residual error, it is handled through the random effects for convenience
		this.caller = caller;
		yearDateMap = new HashMap<Integer, Year>();
		observedPriceMap = new HashMap<Integer, Double>();
		realizedPriceMap = new HashMap<Integer, Map<Integer, Double>>(); 
	}

	@Override
	protected void init() {
		EmpiricalDistribution empDist = new EmpiricalDistribution();
		Matrix obs;
		for (Double price : observedPriceMap.values()) {
			obs = new Matrix(1,1);
			obs.m_afData[0][0] = price;
			empDist.addRealization(obs);
			setParameterEstimates(new GaussianEstimate(empDist.getMean(), new Matrix(1,1)));
			setDefaultRandomEffects(HierarchicalLevel.YEAR, new GaussianEstimate(new Matrix(1,1), empDist.getVariance()));
		}
	}
	
	double getStandingPriceForThisYear(int yearDate, int monteCarloID) {
		if (observedPriceMap.containsKey(yearDate)) {
			return observedPriceMap.get(yearDate);
		} else {
			double price = this.getParameterEstimates().getMean().m_afData[0][0];
			if (!yearDateMap.containsKey(yearDate)) {
				yearDateMap.put(yearDate, new Year(yearDate));
			}
			Year year = yearDateMap.get(yearDate);
			year.monteCarloId = monteCarloID;
			price += getRandomEffectsForThisSubject(year).m_afData[0][0];
			return price;
		}
		
	}
	
	double[] getStandingPrices(int startingYear, int endingYear, int monteCarloID) {
		if (endingYear <= startingYear) {
			throw new InvalidParameterException("The ending year must be greater than the starting year!");
		}
		int length = endingYear - startingYear;
		double[] priceArray = new double[length];
		for (int yearIndex = 0; yearIndex < priceArray.length; yearIndex++) {
			int yearDate = startingYear + yearIndex + 1;
			priceArray[yearIndex] = getStandingPriceForThisYear(yearDate, monteCarloID);
		}
		return priceArray;
	}


}
