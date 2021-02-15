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
package lerfob.predictor.mathilde.thinningbeta;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTree;
import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.thinners.REpiceaThinner;
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
@SuppressWarnings({ "serial"})
@Deprecated
public final class MathildeTreeThinningPredictor extends REpiceaThinner<MathildeThinningStand, MathildeTree> {

	protected static boolean isGaussianQuadratureEnabled = true;

	private final Map<Integer, MathildeThinningSubModule> subModules;

	private final LinkFunction linkFunction;
//	private final LinearStatisticalExpression eta;
	
	private int numberOfParameters;

	protected GaussHermiteQuadrature ghq;

	/**
	 * Constructor.
	 */
	public MathildeTreeThinningPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
		subModules = new HashMap<Integer, MathildeThinningSubModule>();
		init();
		oXVector = new Matrix(1, numberOfParameters);
		linkFunction = new LinkFunction(Type.Logit); // rm+fc-10.6.2015 Logit
		linkFunction.setVariableValue(0, 1d);	// variable that multiplies the xBeta
		linkFunction.setVariableValue(1, 1d);	// variable that multiplies the random effect parameter
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N15);
	}

	protected void init() {
		int excludedGroup = 0;
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeTreeThinningBeta.csv";
			String omegaFilename = path + "0_MathildeTreeThinningOmega.csv";
			String covparmsFilename = path + "0_MathildeThinningCovParms.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
			ParameterMap covparmsMap = ParameterLoader.loadVectorFromFile(1,covparmsFilename);

			numberOfParameters = -1;
			int numberOfExcludedGroups = 10; // at max

			for (excludedGroup = 0; excludedGroup <= numberOfExcludedGroups; excludedGroup++) {

				// fc+rm-11.6.2015 numberOfExcludedGroups may be lower than 10
				if (betaMap.get(excludedGroup) == null || omegaMap.get(excludedGroup) == null) {
					break;
				}

				// // rm+fc-10.6.2015 for the thining model, betaMap contains
				Matrix defaultBetaMean = betaMap.get(excludedGroup);
				if (numberOfParameters == -1) {
					numberOfParameters = defaultBetaMean.m_iRows;
				}
				
				Matrix omega = omegaMap.get(excludedGroup).squareSym();

				MathildeThinningSubModule subModule = new MathildeThinningSubModule(isParametersVariabilityEnabled,	isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				
				subModule.setParameterEstimates(new ModelParameterEstimates(defaultBetaMean, omega));
				
				Matrix covParms = covparmsMap.get(excludedGroup);
				
				Matrix meanIntervalRandomEffect = new Matrix(1,1);
				subModule.setDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, new GaussianEstimate(meanIntervalRandomEffect, covParms));
			
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
			dbhDgBelow09 = 1;
		}
		double dbh_m = tree.getDbhCm() / 100d;
		
		int pointer = 0;

		oXVector.m_afData[0][pointer] = 1d;
		pointer++;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(scaledDbhDg), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(dbhDgBelow09 * scaledDbhDg), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(scaledDbhDg * scaledDbhDg), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;
		
		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(dbhDgBelow09 * scaledDbhDg * scaledDbhDg), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(dbh_m), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(dbh_m * dbh_m), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;
		
		double result = oXVector.multiply(beta).m_afData[0][0];
		return result;
	}

	@Override
	public synchronized double predictEventProbability(MathildeThinningStand stand, MathildeTree tree, Map<Integer, Object> parms) {
		MathildeThinningSubModule subModule;
		
		if (parms!= null && parms.containsKey(lerfob.predictor.mathilde.thinning.MathildeStandThinningPredictor.ParmSubModuleID)) {
			int subModuleId = (Integer) parms.get(lerfob.predictor.mathilde.thinning.MathildeStandThinningPredictor.ParmSubModuleID);
			subModule = subModules.get(subModuleId);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!: " + subModuleId);
			}
		} else {
			subModule = subModules.get(0);
		}

		Matrix beta = subModule.getParameters(stand);

		double pred = getFixedEffectOnlyPrediction(beta, stand, tree);

		linkFunction.setParameterValue(0, pred);
		double prob = 0d;

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

		return prob;
	}

	@Override
	public Object getInformationOnThinning(MathildeThinningStand stand) {return null;}

//	@Override
//	public void clearDeviates() {
//		for (MathildeThinningSubModule p : subModules.values()) {
//			p.clearDeviates();
//		}
//	}

//	public static void main(String[] args) {
//		new MathildeTreeThinningPredictor(false, false, false);
//	}

}
