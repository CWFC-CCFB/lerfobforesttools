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
import java.util.Map;

import lerfob.simulation.covariateproviders.standlevel.FrenchRegion2016Provider.FrenchRegion2016;
import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaLogisticPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.covariateproviders.standlevel.LandOwnershipProvider;
import repicea.simulation.covariateproviders.standlevel.LandOwnershipProvider.LandOwnership;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * This thinner is based on the NFI data for the remeasurement campaigns that took place from 2010 to 2016. Plots measured
 * 5 years earlier were revisited to record the occurrence of harvesting. 
 * 
 * @author Mathieu Fortin - May 2018
 */
@SuppressWarnings("serial")
public class FrenchNFIThinnerPredictor extends REpiceaLogisticPredictor<FrenchNFIThinnerPlot, Object> {

	private final static Map<FrenchRegion2016, Matrix> DummyRegion = new HashMap<FrenchRegion2016, Matrix>();
	static {
		Matrix dummy = new Matrix(1,12);
		for (FrenchRegion2016 region : FrenchRegion2016.values()) {
			dummy = new Matrix(1,12);
			if (region.ordinal() > 0) {		// we skip Auvergne Rhone Alpes
				dummy.m_afData[0][region.ordinal()-1] = 1d;
			}
			DummyRegion.put(region, dummy);
		}
	}
	
	public enum Species implements SpeciesTypeProvider {
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
		
		private static Map<String, Species> MatchMap;
		
		private final String frenchName;
		private final SpeciesType type;
		
		Species(String frenchName, SpeciesType type) {
			this.frenchName = frenchName;
			this.type = type;
		}
		
		
		private static Map<String, Species> getMatchMap() {
			if (MatchMap == null) {
				MatchMap = new HashMap<String, Species>();
				for (Species sp : Species.values()) {
					MatchMap.put(sp.frenchName, sp);
				}
			}
			return MatchMap;
		}
		
		static Species getSpeciesFromFrenchName(String frenchName) {
			return getMatchMap().get(frenchName);
		}

		@Override
		public SpeciesType getSpeciesType() {return type;}
	}

	private final int NumberParmsForHazard = 11;
	protected final FrenchNFIThinnerStandingPriceProvider priceProvider;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode or false for deterministic predictions.
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
			Matrix omega = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			
			GaussianEstimate estimate = new SASParameterEstimates(beta, omega);
			setParameterEstimates(estimate); 
			oXVector = new Matrix(1, estimate.getMean().m_iRows - NumberParmsForHazard);
			
		} catch (Exception e) {
			System.out.println("FrenchNFIThinner.init() : Unable to read parameter files!");
		}
		
	}

	private double getBaseline(Matrix beta, double[] prices, FrenchNFIThinnerPlot stand, Species targetSpecies) {
		int parameterIndex = targetSpecies.ordinal() - 1;
		
		double intercept = beta.m_afData[0][0];
		if (targetSpecies.getSpeciesType() == SpeciesType.ConiferousSpecies) {
			intercept += beta.m_afData[1][0];
		}
		
		double slope = beta.m_afData[2][0];
		if (parameterIndex >= 0) { // if oak then it is smaller than 0
			slope += beta.m_afData[parameterIndex + 3][0];
		}
		
		double baselineResult = 0;
		for (double p : prices) {
			baselineResult += Math.exp(intercept + slope * p);
		}
		
		return baselineResult;
	}

	private double getProportionalPart(FrenchNFIThinnerPlot stand, Matrix beta, Species targetSpecies) {
		double basalAreaM2Ha = stand.getBasalAreaM2Ha();
		double probabilityPrivateLand;
		if (stand instanceof LandOwnershipProvider) {		// priority is given to the interface
			boolean isPrivate = ((LandOwnershipProvider) stand).getLandOwnership() == LandOwnership.Private;
			if (isPrivate) {
				probabilityPrivateLand = 1d;
			} else {
				probabilityPrivateLand = 0d;
			}
		} else {
			probabilityPrivateLand = stand.getProbabilityOfBeingOnPrivateLand();
		}
		int dummy_res = 0;
		if (targetSpecies.getSpeciesType() == SpeciesType.ConiferousSpecies) {
			dummy_res = 1;
		}

		
		
		
		
		int index = 0;
		oXVector.m_afData[0][index] = basalAreaM2Ha;
		index++;

		oXVector.m_afData[0][index] = stand.getNumberOfStemsHa() * basalAreaM2Ha * .001;
		index++;

		oXVector.m_afData[0][index] = stand.getSlopeInclinationPercent();
		index++;

		oXVector.m_afData[0][index] = stand.getSlopeInclinationPercent() * dummy_res;
		index++;
		
		oXVector.m_afData[0][index] = probabilityPrivateLand;
		index++;
		
		if (stand.wasThereAnySiliviculturalTreatmentInTheLast5Years()) {
			oXVector.m_afData[0][index] = 1d * probabilityPrivateLand;
		}
		index++;
		
		Matrix dummy = DummyRegion.get(stand.getFrenchRegion2016());
		oXVector.setSubMatrix(dummy, 0, index);
		index += dummy.m_iCols;
		
		Matrix subBeta = beta.getSubMatrix(NumberParmsForHazard, beta.m_iRows - 1, 0, 0);
		
		double xBeta = oXVector.multiply(subBeta).m_afData[0][0];
		return Math.exp(xBeta);
	}
	
	/**
	 * This method returns the probability of harvest occurrence at the plot level.
	 * @param stand a FrenchNFIThinnerPlot instance
	 * @param tree USELESS can be set to NULL
	 * @param parms should contain two integers being the initial and the final dates. Note that the price of the 
	 * initial year does not contribute to the probability of harvesting. For example, if one provides 2012 and 
	 * 2017 as initial and final dates, then the prices of 2013, 2014, 2015, 2016 and 2017 will contribute to the 
	 * probability of harvesting. 
	 */
	@Override
	public synchronized double predictEventProbability(FrenchNFIThinnerPlot stand, Object tree, Object... parms) {
		oXVector.resetMatrix();
		
		int year0 = (Integer) parms[0];
		int year1 = (Integer) parms[1];
		
		Species targetSpecies;
		if (stand instanceof InnerValidationPlot) {
			targetSpecies = ((InnerValidationPlot) stand).getTargetSpecies(); 
		} else {
			targetSpecies = priceProvider.getTargetSpecies(stand, year0);
		}

		Matrix beta = getParametersForThisRealization(stand);
		double proportionalPart = getProportionalPart(stand, beta, targetSpecies);
		double[] prices = priceProvider.getStandingPrices(targetSpecies, year0, year1, stand.getMonteCarloRealizationId());
		double baseline = getBaseline(beta, prices, stand, targetSpecies);
		double survival = Math.exp(-proportionalPart * baseline);

		return 1 - survival;
	}

	
	
}
