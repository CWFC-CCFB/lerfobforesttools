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
import java.util.HashMap;
import java.util.Map;

import repicea.math.Matrix;
import repicea.simulation.LogisticModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * This class implements the stand level thinning submodel in Mathilde model.
 * 
 * @author Ruben Manso and Francois de Coligny - June 2015
 */
@SuppressWarnings("serial")
public final class MathildeStandThinningPredictor extends LogisticModelBasedSimulator<MathildeThinningStand, Object> {

	private final Map<Integer, MathildeThinningSubModule> subModules;

	private final LinkFunction linkFunction;
//	private final LinearStatisticalExpression eta;
	private int numberOfParameters;


	/**
	 * Constructor.
	 * 
	 * @param isParametersVariabilityEnabled
	 * @param isResidualVariabilityEnabled
	 */
	public MathildeStandThinningPredictor(boolean isParametersVariabilityEnabled,
			boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
		subModules = new HashMap<Integer, MathildeThinningSubModule>();
		init();
		oXVector = new Matrix(1, numberOfParameters);
		linkFunction = new LinkFunction(Type.Logit); // rm+fc-10.6.2015 Logit
		linkFunction.setParameterValue(0, 1d);
	}

	protected void init() {
		int excludedGroup = 0;
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeStandThinningBeta.csv";
			String omegaFilename = path + "0_MathildeStandThinningOmega.csv";

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
				// only fixed effects
				Matrix defaultBetaMean = betaMap.get(excludedGroup);
				// Matrix betaPrelim = betaMap.get(excludedGroup);
				if (numberOfParameters == -1) {
					numberOfParameters = defaultBetaMean.m_iRows;
				}
				Matrix omega = omegaMap.get(excludedGroup).squareSym();


				MathildeThinningSubModule subModule = new MathildeThinningSubModule(isParametersVariabilityEnabled,	isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				subModule.setParameterEstimates(new GaussianEstimate(defaultBetaMean, omega));
				// rm+fc-10.6.2015 No random effect for the thinning stand level
				// model
				// subModule.getDefaultRandomEffects().put(HierarchicalLevel.IntervalNestedInPlot,
				// new GaussianEstimate(new
				// Matrix(randomEffectVariance.m_iRows,1),
				// randomEffectVariance));
				subModules.put(excludedGroup, subModule);
			}
		} catch (Exception e) {
			System.out.println("MathildeStandThinningPredictor.init() : Unable to initialize the MathildeStandThinningPredictor module for group: "
							+ excludedGroup);
			e.printStackTrace(System.out);
		}
	}

	protected double getFixedEffectOnlyPrediction(Matrix beta, MathildeThinningStand stand) {
		// protected double getFixedEffectOnlyPrediction(Matrix beta,
		// MathildeMortalityStand stand, MathildeTree tree) {
		oXVector.resetMatrix();

		int pointer = 0;

		oXVector.m_afData[0][pointer] = 1d;
		pointer++;

		oXVector.m_afData[0][pointer] = stand.getGrowthStepLengthYr(); // timeInterval

		double result = oXVector.multiply(beta).m_afData[0][0];
		return result;
	}

	@Override
	public synchronized double predictEventProbability(MathildeThinningStand stand, Object tree, Object... parms) {
		MathildeThinningSubModule subModule;
		if (parms.length > 0 && parms[0] instanceof Integer) {
			subModule = subModules.get(parms[0]);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!: " + parms[0]);
			}
		} else {
			subModule = subModules.get(0);
		}

		Matrix beta = subModule.getParameters(stand);

		double pred = getFixedEffectOnlyPrediction(beta, stand);

		linkFunction.setVariableValue(0, pred);
		double prob = linkFunction.getValue();


		return prob;
	}

	public static void main(String[] args) {
		new MathildeStandThinningPredictor(false, false, false);
	}

}
