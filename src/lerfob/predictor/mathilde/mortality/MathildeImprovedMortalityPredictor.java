/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Ruben Manso for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde.mortality;

import java.security.InvalidParameterException;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTree;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class MathildeImprovedMortalityPredictor extends	MathildeMortalityPredictor {

	public MathildeImprovedMortalityPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled);
	}

	MathildeImprovedMortalityPredictor(boolean isParameterVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
	}
	
	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeMortality2Beta.csv";
			String omegaFilename = path + "0_MathildeMortality2Omega.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1,betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);		
			
			numberOfParameters = -1;
			int numberOfExcludedGroups = 10;		
			
			for (int excludedGroup = 0; excludedGroup <= numberOfExcludedGroups; excludedGroup++) {			
				Matrix betaPrelim = betaMap.get(excludedGroup);
				if (numberOfParameters == -1) {
					numberOfParameters = betaPrelim.m_iRows - 1;
				}
				Matrix defaultBetaMean = betaPrelim.getSubMatrix(0, numberOfParameters - 1, 0, 0);
				SymmetricMatrix randomEffectVariance = SymmetricMatrix.convertToSymmetricIfPossible(
						betaPrelim.getSubMatrix(numberOfParameters, numberOfParameters, 0, 0));
				SymmetricMatrix omega = SymmetricMatrix.convertToSymmetricIfPossible(
						omegaMap.get(excludedGroup).squareSym().getSubMatrix(0, numberOfParameters - 1, 0, numberOfParameters - 1));		
				MathildeMortalitySubModule subModule = new MathildeMortalitySubModule(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				subModule.setParameterEstimates(new ModelParameterEstimates(defaultBetaMean, omega));
				subModule.setDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, new GaussianEstimate(new Matrix(randomEffectVariance.m_iRows,1), randomEffectVariance));
				subModules.put(excludedGroup, subModule);
			}
		} catch (Exception e) {
			System.out.println("MathildeMortalityPredictor.init() : Unable to initialize the MathildeMortalityPredictor module");
		}
	}

	@Override
	public synchronized double predictEventProbability(MathildeMortalityStand stand, MathildeTree tree, Map<String, Object> parms) {
		boolean windstormDisabledOverride = false;
		if (parms != null && parms.containsKey(MathildeMortalityPredictor.ParmWindstormDisabledOverride)) {
			windstormDisabledOverride = (Boolean) parms.get(MathildeMortalityPredictor.ParmWindstormDisabledOverride);
		}
		double upcomingWindstorm = 0d;
		if (stand.isAWindstormGoingToOccur() && !windstormDisabledOverride) {
			upcomingWindstorm = 1d;
		} 
		
		MathildeMortalitySubModule subModule;
		if (parms != null && parms.containsKey(MathildeMortalityPredictor.ParmSubmoduleFromCrossValidation)) {
			int subModuleId = (Integer) parms.get(MathildeMortalityPredictor.ParmSubmoduleFromCrossValidation);
			subModule = subModules.get(subModuleId);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!");
			} 
		} else {
			subModule = subModules.get(0);
		}
		
		Matrix beta = subModule.getParameters(stand);
		linkFunction.setVariableValue(1, upcomingWindstorm * tree.getLnDbhCm());
		
		double pred = getFixedEffectOnlyPrediction(beta, stand, tree);
		linkFunction.setParameterValue(0, pred);

		double prob;
		linkFunction.setParameterValue(1, beta.getValueAt(14, 0));
		if (isRandomEffectsVariabilityEnabled && stand.isAWindstormGoingToOccur()) {	// no need to draw a random effect if there is no windstorm
			IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
			Matrix randomEffects = subModule.getRandomEffects(interval);
			linkFunction.setParameterValue(2, randomEffects.getValueAt(0, 0));
			prob = linkFunction.getValue();
		} else {
			linkFunction.setParameterValue(2, 0d);		// random effect arbitrarily set to 0
			if (stand.isAWindstormGoingToOccur() && isGaussianQuadratureEnabled) {
				linkFunction.standardDeviation = subModule.getDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT).getDistribution().getStandardDeviation().getValueAt(0, 0);
				prob = ghq.getIntegralApproximation(linkFunction, IndexParameterToBeIntegrated, true);
			} else {									// no need to evaluate the quadrature when there is no windstorm
				prob = linkFunction.getValue();
			}
		}
		return prob;
	}

}
