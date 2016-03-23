/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2015 Rub�n Manso for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde.thinning;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTree;
import lerfob.predictor.mathilde.MathildeTree.MathildeTreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.LogisticModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * This class implements the tree level thinning submodel in Mathilde model.
 * 
 * @author Ruben Manso and Francois de Coligny - June 2015
 */
@SuppressWarnings("serial")
final class MathildeTreeThinningPredictor extends LogisticModelBasedSimulator<MathildeThinningStand, MathildeTree> {

	protected static boolean isGaussianQuadratureEnabled = true;

	private final Map<Integer, MathildeThinningSubModule> subModules;

	private final LinkFunction linkFunction;
	
	private int numberOfParameters;

	protected GaussHermiteQuadrature ghq;
	
	/**
	 * Constructor.
	 */
	public MathildeTreeThinningPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
		subModules = new HashMap<Integer, MathildeThinningSubModule>();
		init();
		oXVector = new Matrix(1, numberOfParameters);
		linkFunction = new LinkFunction(Type.Logit); // rm+fc-10.6.2015 Logit
		linkFunction.setVariableValue(0, 1d);	// variable that multiplies the xBeta
		linkFunction.setVariableValue(1, 1d);	// variable that multiplies the random effect parameter
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N5);
	}

	protected void init() {
		int excludedGroup = 0;
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeThinningBeta.csv";
			String omegaFilename = path + "0_MathildeThinningOmega.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);

			numberOfParameters = -1;
			int numberOfExcludedGroups = 10; // at max

			for (excludedGroup = 0; excludedGroup <= numberOfExcludedGroups; excludedGroup++) {

				// fc+rm-11.6.2015 numberOfExcludedGroups may be lower than 10
				if (betaMap.get(excludedGroup) == null || omegaMap.get(excludedGroup) == null) {
					break;
				}

				// // rm+fc-10.6.2015 for the thining model, betaMap contains
				Matrix defaultBetaMean = betaMap.get(excludedGroup);
				Matrix randomEffectVariance = defaultBetaMean.getSubMatrix(defaultBetaMean.m_iRows - 1, defaultBetaMean.m_iRows - 1, 0, 0); // last element
				defaultBetaMean = defaultBetaMean.getSubMatrix(MathildeStandThinningPredictor.NumberOfParameters, 
						defaultBetaMean.m_iRows - 2, 
						0, 
						0);
				if (numberOfParameters == -1) {
					numberOfParameters = defaultBetaMean.m_iRows;
				}
				
				Matrix omega = omegaMap.get(excludedGroup).squareSym();
				omega = omega.getSubMatrix(MathildeStandThinningPredictor.NumberOfParameters, 
						omega.m_iRows - 2, 
						MathildeStandThinningPredictor.NumberOfParameters, 
						omega.m_iRows - 2);
				
				MathildeThinningSubModule subModule = new MathildeThinningSubModule(isParametersVariabilityEnabled,	isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				
				subModule.setParameterEstimates(new GaussianEstimate(defaultBetaMean, omega));
				
				Matrix meanIntervalRandomEffect = new Matrix(1,1);
				subModule.setDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, new GaussianEstimate(meanIntervalRandomEffect, randomEffectVariance));
			
				subModules.put(excludedGroup, subModule);
			}
		} catch (Exception e) {
			System.out.println("MathildeTreeThinningPredictor.init() : Unable to initialize the MathildeTreeThinningPredictor module for group: "
							+ excludedGroup);
			e.printStackTrace(System.out);
		}
	}

	protected double getFixedEffectOnlyPrediction(Matrix beta, MathildeThinningStand stand, MathildeTree tree) {
		oXVector.resetMatrix();
		
		MathildeTreeSpecies species = tree.getMathildeTreeSpecies();
		double scaledDbhDg = tree.getDbhCm() / stand.getMeanQuadraticDiameterCm() - 0.9;
		double dbhDgBelow09 = 0;
		if (scaledDbhDg < 0) {
			dbhDgBelow09 = 1d;
		}
		int pointer = 0;

		oXVector.m_afData[0][pointer] = 1d;
		pointer++;

		oXVector.setSubMatrix(species.getLongDummyVariable().scalarMultiply(scaledDbhDg), 0, pointer);
		pointer += species.getLongDummyVariable().m_iCols;

		oXVector.setSubMatrix(species.getLongDummyVariable().scalarMultiply(scaledDbhDg * dbhDgBelow09), 0, pointer);
		pointer += species.getLongDummyVariable().m_iCols;

		oXVector.m_afData[0][pointer] = scaledDbhDg * scaledDbhDg;
		pointer++;

		oXVector.m_afData[0][pointer] = scaledDbhDg * scaledDbhDg * dbhDgBelow09;
		pointer++;
		
		double result = oXVector.multiply(beta).m_afData[0][0];
		return result;
	}

	@Override
	public synchronized double predictEventProbability(MathildeThinningStand stand, MathildeTree tree, Object... parms) {
		MathildeThinningSubModule subModule;
//		if (parms == null || parms.length < 1) {
//			throw new InvalidParameterException("The probability at stand level is missing!");
//		} 
//		
//		Object thinningStandEvent = parms[0];
			
		if (parms.length >= 1 && parms[0] instanceof Integer) {
			subModule = getSubModule((Integer) parms[0]);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!: " + parms[1]);
			}
		} else {
			subModule = getSubModule(0);
		}

		Matrix beta = subModule.getParameters(tree);

		double pred = getFixedEffectOnlyPrediction(beta, stand, tree);

		linkFunction.setParameterValue(0, pred);
		double prob = 0d;
		
//		if (thinningStandEvent instanceof Boolean && !((Boolean) thinningStandEvent)) {
//			return prob;
//		} else {
			if (isRandomEffectsVariabilityEnabled) {
				IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
				Matrix randomEffects = subModule.getRandomEffects(interval);
				linkFunction.setParameterValue(1, randomEffects.m_afData[0][0]);
				prob = linkFunction.getValue();
			} else {	// i.e. deterministic mode
				linkFunction.setParameterValue(1, 0d);
				List<Integer> parameterIndices = new ArrayList<Integer>();
				parameterIndices.add(1);
				prob = ghq.getIntegralApproximation(linkFunction, parameterIndices, subModule.getDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT).getDistribution().getStandardDeviation());
			}
			
//			if (thinningStandEvent instanceof Boolean) {
			return prob;
//			} else {
//				return prob * (Double) thinningStandEvent;
//			}
//		}
	}

	protected final MathildeThinningSubModule getSubModule(int subModuleId) {
		return subModules.get(subModuleId);
	}
	
	
	public static void main(String[] args) {
		new MathildeTreeThinningPredictor(false, false, false);
	}

}
