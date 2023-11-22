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
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.StatisticalUtility;
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

	static class Modifier {
		final int fromYear;
		final int toYear;
		final double relativeChange;
		double slope;
		final boolean gradual;
		
		Modifier(int fromYear, int toYear, double relativeChange, boolean gradual) {
			if (toYear <= fromYear) {
				throw new InvalidParameterException("The toYear parameter must be greater than the fromYear parameter!");
			}
			this.fromYear = fromYear;
			this.toYear = toYear;
			this.relativeChange = relativeChange;
			this.gradual = gradual;
			if (gradual) {
				this.slope = relativeChange / (toYear - fromYear);
			}
		}
		
		double getRelativeChangePercentPlusOne(int toThisYear) {
			if (gradual) {
				if (toThisYear >= toYear) {
					return 1d + relativeChange;
				} else if (toThisYear <= fromYear) {
					return 1d;
				} else {
					return 1d + (toThisYear - fromYear) * slope;
				}
			} else {
				return 1d + relativeChange;
			}
		}
	}


	
	
	
	
	
	final Map<Integer, Year> yearDateMap;
	final FrenchNFIThinnerStandingPriceProvider caller;
	final Map<Integer, Double> observedPriceMap;
	private List<Integer> knownYears;
	private Modifier basicTrendModifier;		// this one does not affect observed prices
	private Modifier multiplierModifier;		// this one does affect observed prices as well

	FrenchNFIThinnerStandingPriceProviderSubModel(boolean isVariabilityEnabled, FrenchNFIThinnerStandingPriceProvider caller) {
		super(false, isVariabilityEnabled, false); // although it was a residual error, it is handled through the random effects for convenience
		this.caller = caller;
		yearDateMap = new HashMap<Integer, Year>();
		observedPriceMap = new HashMap<Integer, Double>();
	}

	void setBasicTrendModifier(int year0, int year1, double relativeChange) {
		this.basicTrendModifier = new Modifier(year0, year1, relativeChange, true);
	}
	
	void setMultiplierModifier(int year0, int year1, double relativeChange) {
		this.multiplierModifier = new Modifier(year0, year1, relativeChange, false);
	}
	
	@Override
	protected void init() {
		EmpiricalDistribution empDist = new EmpiricalDistribution();
		Matrix obs;
		for (Integer year : observedPriceMap.keySet()) {
			if (year >= caller.minimumYearDate && year <= caller.maximumYearDate) {
				double price = observedPriceMap.get(year);
				obs = new Matrix(1,1);
				obs.setValueAt(0, 0, price);
				empDist.addRealization(obs);
			}
		}
		setParameterEstimates(new ModelParameterEstimates(empDist.getMean(), new SymmetricMatrix(1)));
		setDefaultRandomEffects(HierarchicalLevel.YEAR, new GaussianEstimate(new Matrix(1,1), empDist.getVariance()));
	}
	
	double getStandingPriceForThisYear(int yearDate, int monteCarloID) {
		double price;
		if (observedPriceMap.containsKey(yearDate)) {
			price = observedPriceMap.get(yearDate);
		} else {
			if (isRandomEffectsVariabilityEnabled) {
				if (!yearDateMap.containsKey(yearDate)) {
					yearDateMap.put(yearDate, new Year(yearDate));
				}
				Year year = yearDateMap.get(yearDate);
				year.monteCarloId = monteCarloID;
				price = getRandomEffectsForThisSubject(year).getValueAt(0, 0) + getBasicTrendModifier(yearDate);
			} else {
				price = getParameterEstimates().getMean().getValueAt(0, 0) + getBasicTrendModifier(yearDate);
			}
		}
		return price * getMultiplierModifier();
	}

	private double getBasicTrendModifier(int yearDate) {
		double innerModifier;
		if (basicTrendModifier == null) {
			innerModifier = 1d;
		} else {
			innerModifier = basicTrendModifier.getRelativeChangePercentPlusOne(yearDate);
		}
		return getParameterEstimates().getMean().getValueAt(0, 0) * (innerModifier - 1d);
	}
	
	private double getMultiplierModifier() {
		if (multiplierModifier == null) {
			return 1d;
		} else {
			return multiplierModifier.getRelativeChangePercentPlusOne(0); // the argument is useless here
		}
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
	protected Matrix simulateDeviatesForRandomEffectsOfThisSubject(MonteCarloSimulationCompliantObject subject, 
			Estimate<Matrix, SymmetricMatrix, ?> randomEffectsEstimate) {
		int index = (int) Math.floor(StatisticalUtility.getRandom().nextDouble() * getKnownYears().size());
		int randomYear = getKnownYears().get(index);
		Matrix randomDeviates = new Matrix(1,1);
		randomDeviates.setValueAt(0, 0, observedPriceMap.get(randomYear));
		setDeviatesForRandomEffectsOfThisSubject(subject, randomDeviates);
		return randomDeviates.getDeepClone();
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

	/*
	 * For extended visibility (non-Javadoc)
	 * @see repicea.simulation.SensitivityAnalysisParameter#getParameterEstimates()
	 */
	@Override
	protected ModelParameterEstimates getParameterEstimates() {
		return super.getParameterEstimates();
	}

	void resetModifiers() {
		basicTrendModifier = null;
		multiplierModifier = null;
	}

}
