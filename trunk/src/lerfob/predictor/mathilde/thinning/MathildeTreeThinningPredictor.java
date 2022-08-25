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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTree;
import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.thinners.REpiceaThinner;
import repicea.simulation.thinners.REpiceaTreatmentDefinition;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussHermiteQuadrature.GaussHermiteQuadratureCompatibleFunction;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This class implements the tree level thinning submodel in Mathilde model.
 * 
 * @author Ruben Manso and Francois de Coligny - June 2015
 */
@SuppressWarnings("serial")
public final class MathildeTreeThinningPredictor extends REpiceaThinner<MathildeThinningStand, MathildeTree> {

	protected static boolean isGaussianQuadratureEnabled = true;

	private enum Treatment implements TextableEnum {
		Thinning("Thinning", "Eclaircie");
		
		Treatment(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	private static final double SqrtTwo = Math.sqrt(2d);
	protected static final int IndexParameterToBeIntegrated = 1;
	
	class EmbeddedLinkFunction extends LinkFunction implements GaussHermiteQuadratureCompatibleFunction<Double> {

		double standardDeviation;
		
		public EmbeddedLinkFunction(Type type) {
			super(type);
		}

		@Override
		public double convertFromGaussToOriginal(double x, double mu, int covarianceIndexI, int covarianceIndexJ) {
			return SqrtTwo * standardDeviation * x + mu;
		}

		@Override
		public double getIntegralAdjustment(int dimensions) {
			return Math.pow(Math.PI, -dimensions/2d);
		}
		
	}

	
	private final Map<Integer, MathildeThinningSubModule> subModules;

	private final EmbeddedLinkFunction linkFunction;
	
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
		linkFunction = new EmbeddedLinkFunction(Type.Logit); // rm+fc-10.6.2015 Logit
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
				SymmetricMatrix randomEffectVariance = SymmetricMatrix.convertToSymmetricIfPossible(
						defaultBetaMean.getSubMatrix(defaultBetaMean.m_iRows - 1, defaultBetaMean.m_iRows - 1, 0, 0)); // last element
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
				
				subModule.setParameterEstimates(new ModelParameterEstimates(defaultBetaMean, 
						SymmetricMatrix.convertToSymmetricIfPossible(omega)));
				
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

		oXVector.setValueAt(0, pointer, 1d);
		pointer++;

		oXVector.setSubMatrix(species.getLongDummyVariable().scalarMultiply(scaledDbhDg), 0, pointer);
		pointer += species.getLongDummyVariable().m_iCols;

		oXVector.setSubMatrix(species.getLongDummyVariable().scalarMultiply(scaledDbhDg * dbhDgBelow09), 0, pointer);
		pointer += species.getLongDummyVariable().m_iCols;

		oXVector.setValueAt(0, pointer, scaledDbhDg * scaledDbhDg);
		pointer++;

		oXVector.setValueAt(0, pointer, scaledDbhDg * scaledDbhDg * dbhDgBelow09);
		pointer++;
		
		double result = oXVector.multiply(beta).getValueAt(0, 0);
		return result;
	}

	@Override
	public synchronized double predictEventProbability(MathildeThinningStand stand, MathildeTree tree, Map<String, Object> parms) {
		MathildeThinningSubModule subModule;
			
		if (parms != null && parms.containsKey(MathildeStandThinningPredictor.ParmSubModuleID)) {
			int subModuleId = (Integer) parms.get(MathildeStandThinningPredictor.ParmSubModuleID);
			subModule = getSubModule(subModuleId);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!: " + subModuleId);
			}
		} else {
			subModule = getSubModule(0);
		}

		Matrix beta = subModule.getParameters(tree);

		double pred = getFixedEffectOnlyPrediction(beta, stand, tree);

		linkFunction.setParameterValue(0, pred);
		double prob = 0d;
		
			if (isRandomEffectsVariabilityEnabled) {
				IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
				Matrix randomEffects = subModule.getRandomEffects(interval);
				linkFunction.setParameterValue(1, randomEffects.getValueAt(0, 0));
				prob = linkFunction.getValue();
			} else {	// i.e. deterministic mode
				linkFunction.setParameterValue(1, 0d);
//				List<Integer> parameterIndices = new ArrayList<Integer>();
//				parameterIndices.add(1);
				linkFunction.standardDeviation = subModule.getDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT).getDistribution().getStandardDeviation().getValueAt(0, 0);
				prob = ghq.getIntegralApproximation(linkFunction, IndexParameterToBeIntegrated, true);
			}
			
		return prob;
	}

	protected final MathildeThinningSubModule getSubModule(int subModuleId) {
		return subModules.get(subModuleId);
	}


	@Override
	public REpiceaTreatmentDefinition getTreatmentDefinitionForThisHarvestedStand(MathildeThinningStand stand) {return null;}

	@Override
	public List<Enum> getTreatmentList() {
		return Arrays.asList(Treatment.values());
	}
	

}
