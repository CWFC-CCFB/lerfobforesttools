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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.distributions.EmpiricalDistribution;
import repicea.stats.estimates.Estimate;
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

	static class PriceModifier {
		final int fromYear;
		final int toYear;
		final double relativeChange;
		final double slope;
		
		PriceModifier(int fromYear, int toYear, double relativeChange) {
			if (toYear <= fromYear) {
				throw new InvalidParameterException("The toYear parameter must be greater than the fromYear parameter!");
			}
			this.fromYear = fromYear;
			this.toYear = toYear;
			this.relativeChange = relativeChange;
			slope = relativeChange / (toYear - fromYear);
		}
		
		double getRelativeChangePercentPlusOne(int toThisYear) {
			if (toThisYear >= toYear) {
				return 1d + relativeChange;
			} else if (toThisYear <= fromYear) {
				return 1d;
			} else {
				return 1d + (toThisYear - fromYear) * slope;
			}
		}
	}
	
	final Map<Integer, Year> yearDateMap;
	final FrenchNFIThinnerStandingPriceProvider caller;
	final Map<Integer, Double> observedPriceMap;
	private List<Integer> knownYears;
	private PriceModifier modifier;

	FrenchNFIThinnerStandingPriceProviderSubModel(boolean isVariabilityEnabled, FrenchNFIThinnerStandingPriceProvider caller) {
		super(false, isVariabilityEnabled, false); // although it was a residual error, it is handled through the random effects for convenience
		this.caller = caller;
		yearDateMap = new HashMap<Integer, Year>();
		observedPriceMap = new HashMap<Integer, Double>();
	}

	void setPriceModifier(PriceModifier modifier) {
		this.modifier = modifier;
	}
	
	@Override
	protected void init() {
		EmpiricalDistribution empDist = new EmpiricalDistribution();
		Matrix obs;
		for (Integer year : observedPriceMap.keySet()) {
			if (year >= caller.minimumYearDate && year <= caller.maximumYearDate) {
				double price = observedPriceMap.get(year);
				obs = new Matrix(1,1);
				obs.m_afData[0][0] = price;
				empDist.addRealization(obs);
			}
		}
		setParameterEstimates(new ModelParameterEstimates(empDist.getMean(), new Matrix(1,1)));
		setDefaultRandomEffects(HierarchicalLevel.YEAR, new GaussianEstimate(new Matrix(1,1), empDist.getVariance()));
	}
	
	double getStandingPriceForThisYear(int yearDate, int monteCarloID) {
		if (observedPriceMap.containsKey(yearDate)) {
			return observedPriceMap.get(yearDate);
		} else {
			double price;
			if (isRandomEffectsVariabilityEnabled) {
				if (!yearDateMap.containsKey(yearDate)) {
					yearDateMap.put(yearDate, new Year(yearDate));
				}
				Year year = yearDateMap.get(yearDate);
				year.monteCarloId = monteCarloID;
				price = getRandomEffectsForThisSubject(year).m_afData[0][0] + getModifier(yearDate);
			} else {
				price = getParameterEstimates().getMean().m_afData[0][0] + getModifier(yearDate);
			}
			return price;
		}
	}

	private double getModifier(int yearDate) {
		double innerModifier;
		if (modifier == null) {
			innerModifier = 1d;
		} else {
			innerModifier = modifier.getRelativeChangePercentPlusOne(yearDate);
		}
		return getParameterEstimates().getMean().m_afData[0][0] * (innerModifier - 1d);
	}
	
	private List<Integer> getKnownYears() {
		if (knownYears == null) {
			knownYears = new ArrayList<Integer>();
			for (Integer year : observedPriceMap.keySet()) {
				if (year >= caller.minimumYearDate && year <= caller.maximumYearDate) {
					knownYears.add(year);
				}
			}
		}
		return knownYears;
	}

	/*
	 * Instead of simulating a Gaussian random effect, the method now draws a random price within the observed prices (non-Javadoc)
	 * @see repicea.simulation.REpiceaPredictor#simulateDeviatesForRandomEffectsOfThisSubject(repicea.simulation.MonteCarloSimulationCompliantObject, repicea.stats.estimates.Estimate)
	 */
	@Override
	protected Matrix simulateDeviatesForRandomEffectsOfThisSubject(MonteCarloSimulationCompliantObject subject, Estimate<?> randomEffectsEstimate) {
		int index = (int) Math.floor(random.nextDouble() * getKnownYears().size());
		int randomYear = getKnownYears().get(index);
		Matrix randomDeviates = new Matrix(1,1);
		randomDeviates.m_afData[0][0] = observedPriceMap.get(randomYear);
		setDeviatesForRandomEffectsOfThisSubject(subject, randomDeviates);
		return randomDeviates.getDeepClone();
	}

	double[] getStandingPrices(int startingYear, int endingYear, int monteCarloID, double eurosToAdd) {
		if (endingYear <= startingYear) {
			throw new InvalidParameterException("The ending year must be greater than the starting year!");
		}
		int length = endingYear - startingYear;
		double[] priceArray = new double[length];
		for (int yearIndex = 0; yearIndex < priceArray.length; yearIndex++) {
			int yearDate = startingYear + yearIndex + 1;
			priceArray[yearIndex] = getStandingPriceForThisYear(yearDate, monteCarloID) + eurosToAdd;
		}
		return priceArray;
	}

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.SensitivityAnalysisParameter#getParameterEstimates()
	 */
	@Override
	protected ModelParameterEstimates getParameterEstimates() {
		return super.getParameterEstimates();
	}

}
