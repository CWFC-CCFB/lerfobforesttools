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

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerStandingPriceProvider.Species;
import lerfob.simulation.covariateproviders.standlevel.FrenchRegion2016Provider.FrenchRegion2016;
import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaLogisticPredictor;
import repicea.simulation.SASParameterEstimates;
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
	
	
	
	private final int NumberParmsForHazard = 10;
	
	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode or false for deterministic predictions.
	 */
	public FrenchNFIThinnerPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);		// no random effect in this model
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

	private double getBaseline(Matrix beta, double[] prices, Species targetSpecies) {
		int parameterIndex = targetSpecies.ordinal() - 1;
		
		double intercept = beta.m_afData[0][0];
		double slope = beta.m_afData[1][0];
		if (parameterIndex >= 0) { // if oak then it is smaller than 0
			slope += beta.m_afData[parameterIndex + 2][0];
		}
		
		double baselineResult = 0;
		for (double p : prices) {
			baselineResult += Math.exp(intercept + slope * p);
		}
		
		return baselineResult;
	}

	private double getProportionalPart(FrenchNFIThinnerPlot stand, Matrix beta) {
		double basalAreaM2Ha = stand.getBasalAreaM2Ha();
		
		int index = 0;
		if (stand.wasThereAnySiliviculturalTreatmentInTheLast5Years()) {
			oXVector.m_afData[0][index] = 1d;
		}
		index++;

		oXVector.m_afData[0][index] = basalAreaM2Ha;
		index++;

		oXVector.m_afData[0][index] = stand.getNumberOfStemsHa() * basalAreaM2Ha * .001;
		index++;

		oXVector.m_afData[0][index] = stand.getSlopeInclinationPercent();
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
	 * @param parms should contain two integers being the initial and the final dates
	 */
	@Override
	public synchronized double predictEventProbability(FrenchNFIThinnerPlot stand, Object tree, Object... parms) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(stand);
		double proportionalPart = getProportionalPart(stand, beta);

		Species targetSpecies = stand.getTargetSpecies();
		
		double[] prices;
		if (parms[0] instanceof double[]) {
			prices = (double[]) parms[0];
		} else {
			int year0 = (Integer) parms[0];
			int year1 = (Integer) parms[1];
			prices = FrenchNFIThinnerStandingPriceProvider.getInstance().getStandingPrices(targetSpecies, year0, year1);
		}
		
		double baseline = getBaseline(beta, prices, targetSpecies);

		double survival = Math.exp(-proportionalPart * baseline);

		return 1 - survival;
	}

	
	
}
