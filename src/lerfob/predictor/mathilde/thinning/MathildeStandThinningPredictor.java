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
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.thinners.REpiceaThinner;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * This class implements the stand level thinning submodel in Mathilde model.
 * 
 * @author Ruben Manso and Francois de Coligny - June 2015
 */
@SuppressWarnings("serial")
public final class MathildeStandThinningPredictor extends REpiceaThinner<MathildeThinningStand, Object> {

	public final static String ParmSubModuleID = "SubmoduleId";
	
	private final Map<Integer, MathildeThinningSubModule> subModules;

	private final LinkFunction linkFunction;
	protected static final int NumberOfParameters = 4;


	/**
	 * Constructor.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public MathildeStandThinningPredictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);
		subModules = new HashMap<Integer, MathildeThinningSubModule>();
		init();
		oXVector = new Matrix(1, NumberOfParameters);
		linkFunction = new LinkFunction(Type.Logit); // rm+fc-10.6.2015 Logit
		linkFunction.setParameterValue(0, 1d);
	}

	protected void init() {
		int excludedGroup = 0;
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeThinningBeta.csv";
			String omegaFilename = path + "0_MathildeThinningOmega.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);

			
			int numberOfExcludedGroups = 10; // at max

			for (excludedGroup = 0; excludedGroup <= numberOfExcludedGroups; excludedGroup++) {

				// fc+rm-11.6.2015 numberOfExcludedGroups may be lower than 10
				if (betaMap.get(excludedGroup) == null || omegaMap.get(excludedGroup) == null) {
					break;
				}

				// // rm+fc-10.6.2015 for the thining model, betaMap contains
				// only fixed effects
				Matrix defaultBetaMean = betaMap.get(excludedGroup).getSubMatrix(0, NumberOfParameters - 1, 0, 0);
				Matrix omega = omegaMap.get(excludedGroup).squareSym().getSubMatrix(0, NumberOfParameters - 1, 0, NumberOfParameters - 1);

				MathildeThinningSubModule subModule = new MathildeThinningSubModule(isParametersVariabilityEnabled,	isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				subModule.setParameterEstimates(new ModelParameterEstimates(defaultBetaMean, omega));
				
				subModules.put(excludedGroup, subModule);
			}
		} catch (Exception e) {
			System.out.println("MathildeStandThinningPredictor.init() : Unable to initialize the MathildeStandThinningPredictor module for group: "
							+ excludedGroup);
			e.printStackTrace(System.out);
		}
	}

	protected double getFixedEffectOnlyPrediction(Matrix beta, MathildeThinningStand stand) {
		oXVector.resetMatrix();

		double timeSinceLastCutYr = stand.getTimeSinceLastCutYr() + stand.getGrowthStepLengthYr();
		int alreadyCut = 1;
		if (Double.isInfinite(timeSinceLastCutYr)) {
			alreadyCut = 0;
		}
		
		int notYetCut = 1 - alreadyCut;
		
		int pointer = 0;

		oXVector.setValueAt(0, pointer, notYetCut);
		pointer++;

		oXVector.setValueAt(0, pointer, notYetCut * stand.getBasalAreaM2Ha());
		pointer++;

		if (alreadyCut != 0) {
			oXVector.setValueAt(0, pointer, alreadyCut);
			pointer++;
			
			oXVector.setValueAt(0, pointer, alreadyCut * timeSinceLastCutYr);
			pointer++;
		}
		
		double result = oXVector.multiply(beta).getValueAt(0, 0);
		return result;
	}

	@Override
	public synchronized double predictEventProbability(MathildeThinningStand stand, Object tree, Map<String, Object> parms) {
		MathildeThinningSubModule subModule;
		if (parms != null && parms.containsKey(ParmSubModuleID)) {
			int subModuleId = (Integer) parms.get(ParmSubModuleID);
			subModule = subModules.get(subModuleId);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!: " + subModuleId);
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

	@Override
	public Object getInformationOnThinning(MathildeThinningStand stand) {return null;}

//	@Override
//	public void clearDeviates() {
//		for (MathildeThinningSubModule p : subModules.values()) {
//			p.clearDeviates();
//		}
//	}

//	public static void main(String[] args) {
//		new MathildeStandThinningPredictor(false, false);
//	}

}
