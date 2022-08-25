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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.simulation.covariateproviders.plotlevel.FrenchRegion2016Provider.FrenchRegion2016;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.plotlevel.LandOwnershipProvider;
import repicea.simulation.covariateproviders.plotlevel.LandOwnershipProvider.LandOwnership;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.disturbances.DisturbanceParameter;
import repicea.simulation.thinners.REpiceaThinner;
import repicea.simulation.thinners.REpiceaTreatmentDefinition;
import repicea.util.ObjectUtility;

/**
 * This thinner is based on the NFI data for the remeasurement campaigns that took place from 2010 to 2016. Plots measured
 * 5 years earlier were revisited to record the occurrence of harvesting. 
 * 
 * @author Mathieu Fortin - May 2018
 */
@SuppressWarnings("serial")
public class FrenchNFIThinnerPredictor extends REpiceaThinner<FrenchNFIThinnerPlot, Object> {

	
	private final static Map<FrenchRegion2016, Matrix> DummyRegion = new HashMap<FrenchRegion2016, Matrix>();
	static {
		Matrix dummy = new Matrix(1,12);
		for (FrenchRegion2016 region : FrenchRegion2016.values()) {
			dummy = new Matrix(1,12);
			if (region.ordinal() > 0) {		// we skip Auvergne Rhone Alpes
				dummy.setValueAt(0, region.ordinal() - 1, 1d);
			}
			DummyRegion.put(region, dummy);
		}
	}
	
	public enum FrenchNFIThinnerSpecies implements SpeciesTypeProvider {
		Oak("Chene", SpeciesType.BroadleavedSpecies),
		Beech("Hetre", SpeciesType.BroadleavedSpecies),
		Fir("Sapin", SpeciesType.ConiferousSpecies),
		Spruce("Epicea", SpeciesType.ConiferousSpecies),
		DouglasFir("Douglas", SpeciesType.ConiferousSpecies),
		ScotsPine("Pin sylvestre", SpeciesType.ConiferousSpecies),
		MaritimePine("Pin maritime", SpeciesType.ConiferousSpecies),
		Poplar("Peuplier", SpeciesType.BroadleavedSpecies),
		Coppice("Taillis feuillus", SpeciesType.BroadleavedSpecies),
		;
		
		private static Map<String, FrenchNFIThinnerSpecies> MatchMap;
		
		private final String frenchName;
		private final SpeciesType type;
		
		FrenchNFIThinnerSpecies(String frenchName, SpeciesType type) {
			this.frenchName = frenchName;
			this.type = type;
		}
		
		
		private static Map<String, FrenchNFIThinnerSpecies> getMatchMap() {
			if (MatchMap == null) {
				MatchMap = new HashMap<String, FrenchNFIThinnerSpecies>();
				for (FrenchNFIThinnerSpecies sp : FrenchNFIThinnerSpecies.values()) {
					MatchMap.put(sp.frenchName, sp);
				}
			}
			return MatchMap;
		}
		
		static FrenchNFIThinnerSpecies getSpeciesFromFrenchName(String frenchName) {
			return getMatchMap().get(frenchName);
		}

		@Override
		public SpeciesType getSpeciesType() {return type;}
	}

	private final int NumberParmsForHazard = 10;
	protected final FrenchNFIThinnerStandingPriceProvider priceProvider;
	
	/**
	 * Constructor.
	 * @param isPredictionVariabilityEnabled true to enable the classic stochastic mode 
	 * @param isPriceVariabilityEnabled true to enable the stochasticity of the prices
	 */
	public FrenchNFIThinnerPredictor(boolean isPredictionVariabilityEnabled, boolean isPriceVariabilityEnabled) {
		super(isPredictionVariabilityEnabled, false, isPredictionVariabilityEnabled);		// no random effect in this model
		priceProvider = new FrenchNFIThinnerStandingPriceProvider(isPriceVariabilityEnabled);
		init();
	}

	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_beta.csv";
			String omegaFilename = path + "0_omega.csv";
			
			Matrix beta = ParameterLoader.loadVectorFromFile(betaFilename).get();
			SymmetricMatrix omega = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			
			ModelParameterEstimates estimate = new SASParameterEstimates(beta, omega);
			setParameterEstimates(estimate); 
			oXVector = new Matrix(1, estimate.getMean().m_iRows - NumberParmsForHazard);
			
		} catch (Exception e) {
			System.out.println("FrenchNFIThinner.init() : Unable to read parameter files!");
		}
		
	}

	private double getBaseline(Matrix beta, double[] prices, FrenchNFIThinnerPlot plot, int year0) {
		FrenchNFIThinnerSpecies targetSpecies = getTargetSpecies(plot, year0);
		int parameterIndex = targetSpecies.ordinal() - 1;
		
		double intercept = beta.getValueAt(0, 0);
		
		double slope = beta.getValueAt(1, 0);
		if (parameterIndex >= 0) { // if oak then it is smaller than 0
			slope += beta.getValueAt(parameterIndex + 2, 0);
		}
		
		double baselineResult = 0;
		for (double p : prices) {
			baselineResult += Math.exp(intercept + slope * p);
		}
		
		return baselineResult;
	}

	private double getProportionalPart(FrenchNFIThinnerPlot plot, Matrix beta, int year0) {
		FrenchNFIThinnerSpecies targetSpecies = getTargetSpecies(plot, year0);
		double basalAreaM2Ha = plot.getBasalAreaM2Ha();
		double probabilityPrivateLand;
		if (plot instanceof LandOwnershipProvider) {		// priority is given to the interface
			boolean isPrivate = ((LandOwnershipProvider) plot).getLandOwnership() == LandOwnership.Private;
			if (isPrivate) {
				probabilityPrivateLand = 1d;
			} else {
				probabilityPrivateLand = 0d;
			}
		} else {
			probabilityPrivateLand = plot.getProbabilityOfBeingOnPrivateLand();
		}
		int dummy_res = 0;
		if (targetSpecies.getSpeciesType() == SpeciesType.ConiferousSpecies) {
			dummy_res = 1;
		}
		
		int index = 0;
		oXVector.setValueAt(0, index, dummy_res);
		index++;
		
		oXVector.setValueAt(0, index, basalAreaM2Ha);
		index++;

		oXVector.setValueAt(0, index, plot.getNumberOfStemsHa() * basalAreaM2Ha * .001);
		index++;

		oXVector.setValueAt(0, index, plot.getSlopeInclinationPercent());
		index++;

		oXVector.setValueAt(0, index, plot.getSlopeInclinationPercent() * dummy_res);
		index++;
		
		oXVector.setValueAt(0, index, probabilityPrivateLand);
		index++;
		
		if (plot.wasThereAnySiliviculturalTreatmentInTheLast5Years()) {
			oXVector.setValueAt(0, index, 1d * probabilityPrivateLand);
		}
		index++;
		
		Matrix dummy = DummyRegion.get(plot.getFrenchRegion2016());
		oXVector.setSubMatrix(dummy, 0, index);
		index += dummy.m_iCols;
		
		Matrix subBeta = beta.getSubMatrix(NumberParmsForHazard, beta.m_iRows - 1, 0, 0);
		
		double xBeta = oXVector.multiply(subBeta).getValueAt(0, 0);
		return Math.exp(xBeta);
	}
	
	/**
	 * This method returns the probability of harvest occurrence at the plot level.
	 * @param plot a FrenchNFIThinnerPlot instance
	 * @param tree USELESS can be set to NULL
	 * @param parms should contain two integers being the initial and the final dates. Note that the price of the 
	 * initial year does not contribute to the probability of harvesting. For example, if one provides 2012 and 
	 * 2017 as initial and final dates, then the prices of 2013, 2014, 2015, 2016 and 2017 will contribute to the 
	 * probability of harvesting. 
	 */
	@Override
	public synchronized double predictEventProbability(FrenchNFIThinnerPlot plot, Object tree, Map<String, Object> parms) {
		if (plot.getBasalAreaM2Ha() == 0d) {	// if the plot is empty then no need for calculating whatsoever
			return 0d;
		}
		oXVector.resetMatrix();
		
		int year0 = (Integer) parms.get(DisturbanceParameter.ParmYear0);
		int year1 = (Integer) parms.get(DisturbanceParameter.ParmYear1);

		Matrix beta = getParametersForThisRealization(plot);
		double proportionalPart = getProportionalPart(plot, beta, year0);
		double[] prices = getStandingPrices(getTargetSpecies(plot, year0), 
				year0, 
				year1, 
				plot.getMonteCarloRealizationId());
		double baseline = getBaseline(beta, prices, plot, year0);
		double survival = Math.exp(-proportionalPart * baseline);

		return 1 - survival;
	}

	private FrenchNFIThinnerSpecies getTargetSpecies(FrenchNFIThinnerPlot plot, int year0) {
		FrenchNFIThinnerSpecies targetSpecies;
		if (plot instanceof InnerValidationPlot) {
			targetSpecies = ((InnerValidationPlot) plot).getTargetSpecies(); 
		} else {
			targetSpecies = priceProvider.getTargetSpecies(plot, year0);
		}
		return targetSpecies;
	}

	/**
	 * This method makes it possible to retrieve the prices for a given period and a given
	 * target species.
	 * @param targetSpecies a FrenchNFIThinnerSpecies enum 
	 * @param year0 the beginning of the interval
	 * @param year1 the end of the interval
	 * @param monteCarloRealizationId the id of the Monte Carlo realization if needed
	 * @return an array of doubles
	 */
	public double[] getStandingPrices(FrenchNFIThinnerSpecies targetSpecies, int year0, int year1, int monteCarloRealizationId) {
		return priceProvider.getStandingPrices(targetSpecies, year0, year1, monteCarloRealizationId);
	}
	
	/**
	 * This method makes it possible to induce price changes over time for a particular species.
	 * Note that this change DOES not affect observed prices
	 * @param species a FrenchNFIThinnerSpecies instance
	 * @param fromYear the starting year 
	 * @param toYear the final year
	 * @param relativeChange the relative change over the period. For example, 0.1 would be a 10% increase over the period.
	 */
	public void setBasicTrendModifier(FrenchNFIThinnerSpecies species, int fromYear, int toYear, double relativeChange) {
		priceProvider.setBasicTrendModifier(species, fromYear, toYear, relativeChange);
	}

	/**
	 * This method makes it possible to induce price changes over time for all species. Note that this
	 * change DOES not affect observed prices.
	 * @param fromYear the starting year 
	 * @param toYear the final year
	 * @param relativeChange the relative change over the period. For example, 0.1 would be a 10% increase over the period.
	 */
	public void setBasicTrendModifier(int fromYear, int toYear, double relativeChange) {
		for (FrenchNFIThinnerSpecies species : FrenchNFIThinnerSpecies.values()) {
			setBasicTrendModifier(species, fromYear, toYear, relativeChange);
		}
	}
	
	/**
	 * This method makes it possible to induce a proportional price changes for a given period and a give species. 
	 * Note that this change DOES affect observed prices.
	 * @param species a FrenchNFIThinnerSpecies instance
	 * @param fromYear the starting year 
	 * @param toYear the final year
	 * @param relativeChange the relative change over the period. For example, 0.1 would be a 10% increase over the period.
	 */
	public void setMultiplierModifier(FrenchNFIThinnerSpecies species, int fromYear, int toYear, double relativeChange) {
		priceProvider.setMultiplierModifier(species, fromYear, toYear, relativeChange);
	}

	/**
	 * This method makes it possible to induce a proportional price changes for a given period for all species. 
	 * Note that this change DOES affect observed prices.
	 * @param fromYear the starting year 
	 * @param toYear the final year
	 * @param relativeChange the relative change over the period. For example, 0.1 would be a 10% increase over the period.
	 */
	public void setMultiplierModifier(int fromYear, int toYear, double relativeChange) {
		for (FrenchNFIThinnerSpecies species : FrenchNFIThinnerSpecies.values()) {
			setMultiplierModifier(species, fromYear, toYear, relativeChange);
		}
	}

	public void resetModifiers() {
		priceProvider.resetModifiers();
	}
	
	
	/**
	 * This method returns a FrenchNFIThinnerSpecies instance that matches the species name. 
	 * The species name is formatted. 
	 * @param speciesName a String that represents the common species name in French.
	 * @return a FrenchNFIThinnerSpecies enum
	 */
	protected static FrenchNFIThinnerSpecies getFrenchNFIThinnerSpeciesFromThisString(String speciesName) {
		String firstLetter = speciesName.substring(0, 1).toUpperCase();
		String rest = speciesName.substring(1).trim();
		String formattedSpeciesName = firstLetter.concat(rest);
		return FrenchNFIThinnerSpecies.valueOf(formattedSpeciesName);
	}

	@Override
	public REpiceaTreatmentDefinition getTreatmentDefinitionForThisHarvestedStand(FrenchNFIThinnerPlot stand) {return null;}

	/*
	 * This class does not make any distinction as to the treatment applied in the plots.
	 */
	@Override
	public List<Enum> getTreatmentList() {return null;}
	
	
}
