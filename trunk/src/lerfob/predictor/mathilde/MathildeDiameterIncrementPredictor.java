/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014InternalPredictor;
import lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014Tree.FrenchHdSpecies;
import lerfob.predictor.mathilde.MathildeTree.MathildeTreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.GrowthModel;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.ModelBasedSimulatorEvent;
import repicea.simulation.ModelBasedSimulatorEvent.ModelBasedSimulatorEventProperty;
import repicea.simulation.ModelBasedSimulatorListener;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * This class contains the diameter increment module of Mathilde growth simulator.
 * @authors Mathieu Fortin and Ruben Manso - August 2013
 */
public final class MathildeDiameterIncrementPredictor extends ModelBasedSimulator implements GrowthModel<MathildeStand, MathildeTree>, ModelBasedSimulatorListener {

	private static final long serialVersionUID = 20130627L;

	private static double MAX_ANNUAL_INCREMENT = 10.4 * .2;		// according to Manso et al. 2015 Forestry
	
	private static double CorrelationRandomEffectsHeightDiameterGrowth = 0.4;

	protected final Map<Integer, MathildeSubModule> subModules;
	
	/**
	 * The MathildeDiameterIncrementPredictor class implements the diameter increment model fitted with the
	 * LERFoB database.
	 * @param isParametersVariabilityEnabled a boolean
	 * @param isRandomEffectsVariabilityEnabled a boolean
	 * @param isResidualVariabilityEnabled a boolean
	 */
	public MathildeDiameterIncrementPredictor(boolean isParametersVariabilityEnabled,boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		subModules = new HashMap<Integer, MathildeSubModule>();
		init();
	}

	
	@Override
	protected final void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeDbhIncBeta.csv";
			String omegaFilename = path + "0_MathildeDbhIncOmega.csv";
			String covparmsFilename = path + "0_MathildeDbhIncCovParms.csv";
			
			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1,betaFilename);
			ParameterMap covparmsMap = ParameterLoader.loadVectorFromFile(1,covparmsFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);	
			
			int numberOfParameters = -1;
			int numberOfExcludedGroups = 10;		
			
			for (int excludedGroup = 0; excludedGroup <= numberOfExcludedGroups; excludedGroup++) {			//
				Matrix defaultBetaMean = betaMap.get(excludedGroup);
				if (numberOfParameters == -1) {
					numberOfParameters = defaultBetaMean.m_iRows;
				}
				Matrix omega = omegaMap.get(excludedGroup).squareSym();

				MathildeSubModule subModule = new MathildeSubModule(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				subModules.put(excludedGroup, subModule);

				subModule.setDefaultBeta(new GaussianEstimate(defaultBetaMean, omega));
	
				Matrix covParms = covparmsMap.get(excludedGroup);
				
				Matrix meanPlotRandomEffect = new Matrix(1,1);
				Matrix varPlotRandomEffect = covParms.getSubMatrix(0, 0, 0, 0);
				subModule.setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(meanPlotRandomEffect, varPlotRandomEffect));
				
				
				Matrix meanTreeRandomEffect = new Matrix(1,1);
				Matrix varTreeRandomEffect = covParms.getSubMatrix(1, 1, 0, 0);
				subModule.setDefaultRandomEffects(HierarchicalLevel.TREE, new GaussianEstimate(meanTreeRandomEffect, varTreeRandomEffect));
				
				Matrix varResidualError = covParms.getSubMatrix(2, 2, 0, 0);
				
				subModule.setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(varResidualError));
				
				subModule.errorTotalVariance = covParms.m_afData[0][0] + covParms.m_afData[1][0] + covParms.m_afData[2][0];
//				errorVariance = covParms.m_afData[0][0] + covParms.m_afData[1][0] + covParms.m_afData[2][0];
			}

			oXVector = new Matrix(1, numberOfParameters);
		} catch (Exception e) {
			System.out.println("MathildeDiameterIncrementPredictor.init() : Unable to initialize the MathildeDiameterIncrementPredictor module");
		}
	}

	/*
	 * For tests only
	 * @param stand
	 * @param tree
	 * @return
	 */
	protected double getFixedEffectOnlyPrediction(MathildeStand stand, MathildeTree tree) {
		Matrix currentBeta = subModules.get(0).getParameters(stand);
		return getFixedEffectOnlyPrediction(currentBeta, stand, tree);
	}
	
	protected synchronized double getFixedEffectOnlyPrediction(Matrix currentBeta, MathildeStand stand, MathildeTree tree) {
		oXVector.resetMatrix();

		double upcomingCut = 0d;
		if (stand.isGoingToBeHarvested()) {
			upcomingCut = 1d;
		}
		Matrix dummySpecies = tree.getMathildeTreeSpecies().getDummyVariable();
		
		double bal22 = tree.getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies.QUERCUS);
		double bal42 = tree.getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies.FAGUS); 
		double bal422 = bal42 * bal42;
		
		int pointer = 0;
		oXVector.m_afData[0][pointer] = 1d;
		pointer++;
		oXVector.setSubMatrix(dummySpecies, 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.m_afData[0][pointer] = tree.getLnDbhCm();
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(tree.getLnDbhCm()), 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.m_afData[0][pointer] = tree.getDbhCm();
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(tree.getDbhCm()), 0, pointer);
		pointer += dummySpecies.m_iCols;

		oXVector.m_afData[0][pointer] = upcomingCut;
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(upcomingCut), 0, pointer);
		pointer += dummySpecies.m_iCols;

		oXVector.m_afData[0][pointer] = bal22;
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(bal22), 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.m_afData[0][pointer] = bal42;
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(bal42), 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.m_afData[0][pointer] = bal422;
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(bal422), 0, pointer);
		pointer += dummySpecies.m_iCols;

		
		double predUptoNow = oXVector.getSubMatrix(0, 0, 0, pointer-1).multiply(currentBeta.getSubMatrix(0, pointer-1, 0, 0)).m_afData[0][0];
		
		double tIntervalVeg6 = stand.getMeanAnnualTempAbove6C();
		
		double b91, b81, b82, b3, b32;
		b91 = currentBeta.m_afData[pointer++][0];
		b81 = currentBeta.m_afData[pointer++][0];
		b82 = currentBeta.m_afData[pointer++][0];
		b3 = currentBeta.m_afData[pointer++][0];
		b32 = currentBeta.m_afData[pointer++][0];
		
		predUptoNow += b91 * stand.getGrowthStepLengthYr();

		predUptoNow += b81 * tIntervalVeg6 + b82 * tIntervalVeg6 * tIntervalVeg6;

		predUptoNow += b3 * Math.exp(b32 * stand.getBasalAreaM2Ha());
		
		return predUptoNow;
		
	}

	private MathildeSubModule getSubModule(int subModuleVersion) {
		return subModules.get(subModuleVersion);
	}

	private MathildeSubModule getSubModule() {
		return getSubModule(0);
	}
	
	@Override
	public double predictGrowth(MathildeStand stand, MathildeTree tree, Object... parms) {
		MathildeSubModule subModule;
		if (parms.length > 0 && parms[0] instanceof Integer) {
			subModule = getSubModule((Integer) parms[0]);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!");
			} 
		} else {
			subModule = getSubModule();
		}
		
		Matrix currentBeta = subModule.getParameters(stand);

		double pred = getFixedEffectOnlyPrediction(currentBeta, stand, tree);
		
		pred += subModule.getRandomEffects(tree).m_afData[0][0];
		pred += subModule.getRandomEffects(stand).m_afData[0][0];
		
		if (!isRandomEffectsVariabilityEnabled) {
			if (subModule.getBlupsForThisSubject(stand) != null) {
				pred += subModule.getBlupsForThisSubject(stand).getVariance().m_afData[0][0] * .5;
			} else {
				pred += subModule.getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().m_afData[0][0] * .5;
			}
			
			pred += subModule.getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance().m_afData[0][0] * .5;
		}
		
		if (isResidualVariabilityEnabled) {
			pred += subModule.getResidualErrorForThisVersion().m_afData[0][0];
		} else {
			pred += subModule.getDefaultResidualError(ErrorTermGroup.Default).getVariance().m_afData[0][0] * .5;
		}
		
		double backtransformedPred = Math.exp(pred) - 1;
		if (backtransformedPred > MAX_ANNUAL_INCREMENT * stand.getGrowthStepLengthYr()) {
		//	System.out.println("Max increment has been reached and truncated");
			backtransformedPred = MAX_ANNUAL_INCREMENT * stand.getGrowthStepLengthYr();
		}
		
		return backtransformedPred;
	}

	@Override
	public void modelBasedSimulatorDidThis(ModelBasedSimulatorEvent event) {
		if (event.getSource() instanceof FrenchHDRelationship2014InternalPredictor) {
			FrenchHDRelationship2014InternalPredictor hdPredictor = (FrenchHDRelationship2014InternalPredictor) event.getSource();
			FrenchHdSpecies species = hdPredictor.getSpecies();
			if (species == FrenchHdSpecies.CHENE_SESSILE || 
					species == FrenchHdSpecies.CHENE_PEDONCULE || 
					species == FrenchHdSpecies.HETRE) {
				if (event.getPropertyName().equals(ModelBasedSimulatorEventProperty.BLUPS_AT_THIS_LEVEL_JUST_SET.getPropertyName())) {
					Object[] newValue = (Object[]) event.getNewValue();
					MonteCarloSimulationCompliantObject subject = (MonteCarloSimulationCompliantObject) newValue[0]; 
					if (subject.getHierarchicalLevel().equals(HierarchicalLevel.PLOT) && getSubModule().getBlupsForThisSubject(subject) == null) {
						GaussianEstimate blupsForThisSubject = setRandomEffectsAccordingToHeightDeviate((GaussianEstimate) newValue[1],
								getSubModule().getDefaultRandomEffects(subject.getHierarchicalLevel()), 
								((Estimate<?>) newValue[2]).getMean()); 
						getSubModule().setBlupsForThisSubject(subject, blupsForThisSubject);
					}
				} else if (event.getPropertyName().equals(ModelBasedSimulatorEventProperty.RANDOM_EFFECT_DEVIATE_JUST_GENERATED.getPropertyName())) {
					Object[] newValue = (Object[]) event.getNewValue();
					MonteCarloSimulationCompliantObject subject = (MonteCarloSimulationCompliantObject) newValue[0]; 
					if (subject.getHierarchicalLevel().equals(HierarchicalLevel.PLOT)) {
						if (!doRandomDeviatesExistForThisSubject(subject)) {
							GaussianEstimate blupsForThisSubject = setRandomEffectsAccordingToHeightDeviate((GaussianEstimate) newValue[1],
									getSubModule().getDefaultRandomEffects(subject.getHierarchicalLevel()), 
									(Matrix) newValue[2]); 
							getSubModule().simulateDeviatesForRandomEffectsOfThisSubject(subject, blupsForThisSubject);
						}
					}					
				}
			}
		}
	}
	
	
	protected final GaussianEstimate setRandomEffectsAccordingToHeightDeviate(GaussianEstimate heightRandomEffect, GaussianEstimate diamIncRandomEffect, Matrix blupMean) {
		double varianceRandomEffectHeight = heightRandomEffect.getVariance().m_afData[0][0];
		double varianceRandomEffectDiameterGrowth = diamIncRandomEffect.getVariance().m_afData[0][0];
		double covariance = Math.sqrt(varianceRandomEffectHeight * varianceRandomEffectDiameterGrowth) * CorrelationRandomEffectsHeightDiameterGrowth; 
		
		Matrix mean = new Matrix(1,1);
		mean.m_afData[0][0] = covariance / varianceRandomEffectHeight * blupMean.m_afData[0][0];
		Matrix variance = new Matrix(1,1);
		variance.m_afData[0][0] = varianceRandomEffectDiameterGrowth - covariance * covariance / varianceRandomEffectHeight;
		return new GaussianEstimate(mean, variance);
	}
	


//	public static void main(String[] args) {
//		MathildeDiameterIncrementPredictor pred = new MathildeDiameterIncrementPredictor(false, false, false);
//	}

}
