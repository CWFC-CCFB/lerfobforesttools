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
package lerfob.predictor.mathilde.diameterincrement;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.hdrelationships.FrenchHDRelationshipTree.FrenchHdSpecies;
import lerfob.predictor.hdrelationships.FrenchHeightPredictor;
import lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2014.FrenchHDRelationship2014Stand;
import lerfob.predictor.mathilde.MathildeTree;
import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.GrowthModel;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.REpiceaPredictorEvent;
import repicea.simulation.REpiceaPredictorEvent.ModelBasedSimulatorEventProperty;
import repicea.simulation.REpiceaPredictorListener;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.TruncatedGaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * This class contains the diameter increment module of Mathilde growth simulator.
 * @see <a href=https://academic.oup.com/forestry/article/88/5/540/521744/Effect-of-climate-and-intra-and-inter-specific> 
	 Manso, R., Morneau, F., Ningre, F., and Fortin, M. 2015. Effect of climate and intra- and inter-specific competition on diameter 
	 increment in beech and oak stands. Forestry 88: 540-551</a>
 * @author Mathieu Fortin and Ruben Manso - August 2013
 */
public final class MathildeDiameterIncrementPredictor extends REpiceaPredictor implements GrowthModel<MathildeDiameterIncrementStand, MathildeTree>, REpiceaPredictorListener {

	private static final long serialVersionUID = 20130627L;

	private static final Map<MathildeTreeSpecies,Double> MAX_ANNUAL_INCREMENT = new HashMap<MathildeTreeSpecies, Double>();		// according to Manso et al. 2015 Forestry
	static {
		MAX_ANNUAL_INCREMENT.put(MathildeTreeSpecies.FAGUS, 10.5 * .2);
		MAX_ANNUAL_INCREMENT.put(MathildeTreeSpecies.QUERCUS, 8.0 * .2);
		MAX_ANNUAL_INCREMENT.put(MathildeTreeSpecies.CARPINUS, 4.2 * .2);
		MAX_ANNUAL_INCREMENT.put(MathildeTreeSpecies.OTHERS, 1.2 * .2);
	}
	
	private static double CorrelationRandomEffectsHeightDiameterGrowth = 0.4;

	protected final Map<Integer, MathildeDiameterIncrementSubModule> subModules;

	
	/**
	 * The MathildeDiameterIncrementPredictor class implements the diameter increment model fitted with the
	 * LERFoB database.
	 * @param isVariabilityEnabled true to enable the stochastic mode
	 */
	public MathildeDiameterIncrementPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
	}

	/**
	 * The MathildeDiameterIncrementPredictor class implements the diameter increment model fitted with the
	 * LERFoB database.
	 * @param isParametersVariabilityEnabled a boolean
	 * @param isRandomEffectsVariabilityEnabled a boolean
	 * @param isResidualVariabilityEnabled a boolean
	 */
	MathildeDiameterIncrementPredictor(boolean isParameterVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
		subModules = new HashMap<Integer, MathildeDiameterIncrementSubModule>();
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
				SymmetricMatrix omega = omegaMap.get(excludedGroup).squareSym();

				MathildeDiameterIncrementSubModule subModule = new MathildeDiameterIncrementSubModule(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				subModules.put(excludedGroup, subModule);

				subModule.setParameterEstimates(new ModelParameterEstimates(defaultBetaMean, omega));
	
				Matrix covParms = covparmsMap.get(excludedGroup);
				
				Matrix meanPlotRandomEffect = new Matrix(1,1);
				SymmetricMatrix varPlotRandomEffect = SymmetricMatrix.convertToSymmetricIfPossible(covParms.getSubMatrix(0, 0, 0, 0));
				subModule.setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(meanPlotRandomEffect, varPlotRandomEffect));
				
				Matrix meanTreeRandomEffect = new Matrix(1,1);
				SymmetricMatrix varTreeRandomEffect = SymmetricMatrix.convertToSymmetricIfPossible(covParms.getSubMatrix(1, 1, 0, 0));
				subModule.setDefaultRandomEffects(HierarchicalLevel.TREE, new GaussianEstimate(meanTreeRandomEffect, varTreeRandomEffect));
				
				SymmetricMatrix varResidualError = SymmetricMatrix.convertToSymmetricIfPossible(covParms.getSubMatrix(2, 2, 0, 0));
				
				subModule.setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(varResidualError));
				
				subModule.errorTotalVariance = covParms.getValueAt(0, 0) + covParms.getValueAt(1, 0) + covParms.getValueAt(2, 0);
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
	protected double getFixedEffectOnlyPrediction(MathildeDiameterIncrementStand stand, MathildeTree tree) {
		Matrix currentBeta = subModules.get(0).getParameters(stand);
		return getFixedEffectOnlyPrediction(currentBeta, stand, tree);
	}
	
	protected synchronized double getFixedEffectOnlyPrediction(Matrix currentBeta, MathildeDiameterIncrementStand stand, MathildeTree tree) {
		oXVector.resetMatrix();

		double upcomingCut = 0d;
		if (stand.isGoingToBeHarvested()) {
			upcomingCut = 1d;
		}
		Matrix dummySpecies = tree.getMathildeTreeSpecies().getShortDummyVariable();
		
		double bal22 = tree.getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies.QUERCUS);
		double bal42 = tree.getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies.FAGUS); 
		double bal422 = bal42 * bal42;
		
		int pointer = 0;
		oXVector.setValueAt(0, pointer, 1d);
		pointer++;
		oXVector.setSubMatrix(dummySpecies, 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.setValueAt(0, pointer, tree.getLnDbhCm());
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(tree.getLnDbhCm()), 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.setValueAt(0, pointer, tree.getDbhCm());
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(tree.getDbhCm()), 0, pointer);
		pointer += dummySpecies.m_iCols;

		oXVector.setValueAt(0, pointer, upcomingCut);
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(upcomingCut), 0, pointer);
		pointer += dummySpecies.m_iCols;

		oXVector.setValueAt(0, pointer, bal22);
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(bal22), 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.setValueAt(0, pointer, bal42);
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(bal42), 0, pointer);
		pointer += dummySpecies.m_iCols;
		
		oXVector.setValueAt(0, pointer, bal422);
		pointer++;
		oXVector.setSubMatrix(dummySpecies.scalarMultiply(bal422), 0, pointer);
		pointer += dummySpecies.m_iCols;

		
		double predUptoNow = oXVector.getSubMatrix(0, 0, 0, pointer-1).multiply(currentBeta.getSubMatrix(0, pointer-1, 0, 0)).getValueAt(0, 0);
		
		double tIntervalVeg6 = stand.getMeanSeasonalTemperatureCelsius();
		
		double b91, b81, b82, b3, b32;
		b91 = currentBeta.getValueAt(pointer++, 0);
		b81 = currentBeta.getValueAt(pointer++, 0);
		b82 = currentBeta.getValueAt(pointer++, 0);
		b3 = currentBeta.getValueAt(pointer++, 0);
		b32 = currentBeta.getValueAt(pointer++, 0);
		
		predUptoNow += b91 * stand.getGrowthStepLengthYr();

		predUptoNow += b81 * tIntervalVeg6 + b82 * tIntervalVeg6 * tIntervalVeg6;

		predUptoNow += b3 * Math.exp(b32 * stand.getBasalAreaM2Ha());
		
		return predUptoNow;
		
	}

	private MathildeDiameterIncrementSubModule getSubModule(int subModuleVersion) {
		return subModules.get(subModuleVersion);
	}

	private MathildeDiameterIncrementSubModule getSubModule() {
		return getSubModule(0);
	}
	

	@Override
	public double predictGrowth(MathildeDiameterIncrementStand stand, MathildeTree tree, Object... parms) {
		MathildeDiameterIncrementSubModule subModule;
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
		
		pred += subModule.getRandomEffects(tree).getValueAt(0, 0);
		pred += subModule.getRandomEffects(stand).getValueAt(0, 0);
		
		if (!isRandomEffectsVariabilityEnabled) {
			if (subModule.getBlupsForThisSubject(stand) != null) {
				pred += subModule.getBlupsForThisSubject(stand).getVariance().getValueAt(0, 0) * .5;
			} else {
				pred += subModule.getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().getValueAt(0, 0) * .5;
			}
			
			pred += subModule.getDefaultRandomEffects(HierarchicalLevel.TREE).getVariance().getValueAt(0, 0) * .5;
		}
		
		if (isResidualVariabilityEnabled) {
			pred += subModule.getResidualErrorForThisVersion().getValueAt(0, 0);
		} else {
			pred += subModule.getDefaultResidualError(ErrorTermGroup.Default).getVariance().getValueAt(0, 0) * .5;
		}
		
		double backtransformedPred = Math.exp(pred) - 1;
		double limit = MAX_ANNUAL_INCREMENT.get(tree.getMathildeTreeSpecies()) * stand.getGrowthStepLengthYr();
		if (backtransformedPred > limit) {
			backtransformedPred = limit;
		}
		
		if (backtransformedPred < 0) {
			backtransformedPred = 0;
		}
		
		return backtransformedPred;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void modelBasedSimulatorDidThis(REpiceaPredictorEvent event) {
		if (event.getSource() instanceof FrenchHeightPredictor) {
			FrenchHeightPredictor hdPredictor = (FrenchHeightPredictor) event.getSource();
			FrenchHdSpecies species = hdPredictor.getSpecies();
			if (species == FrenchHdSpecies.CHENE_SESSILE || 
					species == FrenchHdSpecies.CHENE_PEDONCULE || 
					species == FrenchHdSpecies.HETRE) {
				if (event.getPropertyName().equals(ModelBasedSimulatorEventProperty.BLUPS_JUST_SET.getPropertyName())) {
					Object[] newValue = (Object[]) event.getNewValue();
					Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> defaultHeightRandomEffects = (Estimate) newValue[0];
					MonteCarloSimulationCompliantObject subject = (MonteCarloSimulationCompliantObject) newValue[1]; 
					if (!getSubModule().doBlupsExistForThisSubject(subject)) {
						setDiameterBlupFromHeightBlup(subject, defaultHeightRandomEffects, hdPredictor);
					}
				} else if (event.getPropertyName().equals(ModelBasedSimulatorEventProperty.DEFAULT_RANDOM_EFFECT_AT_THIS_LEVEL_JUST_SET.getPropertyName())) {
					Object[] newValue = (Object[]) event.getNewValue();
					HierarchicalLevel level = (HierarchicalLevel) newValue[0];
					Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> formerEstimate = (Estimate) newValue[1];
					Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> newEstimate = (Estimate) newValue[2];
					if (newEstimate instanceof TruncatedGaussianEstimate) {
						GaussianEstimate newDiamRandomEffect = setRandomEffectsAccordingToHeightDeviate(formerEstimate,
								getSubModule().getDefaultRandomEffects(level),
								newEstimate.getMean());
						getSubModule().setDefaultRandomEffects(level, newDiamRandomEffect);
					}
				} else if (event.getPropertyName().equals(ModelBasedSimulatorEventProperty.RANDOM_EFFECT_DEVIATE_JUST_GENERATED.getPropertyName())) {
					Object[] newValue = (Object[]) event.getNewValue();
					MonteCarloSimulationCompliantObject subject = (MonteCarloSimulationCompliantObject) newValue[0];
					Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> originalHeightRandomEffect = (Estimate) newValue[1];
					Matrix deviates = (Matrix) newValue[2];
					if (subject.getHierarchicalLevel().equals(HierarchicalLevel.PLOT)) {
						if (!doRandomDeviatesExistForThisSubject(subject)) {
							GaussianEstimate blupsForThisSubject = setRandomEffectsAccordingToHeightDeviate(originalHeightRandomEffect,
									getSubModule().getDefaultRandomEffects(subject.getHierarchicalLevel()), 
									deviates); 
							getSubModule().simulateDeviatesForRandomEffectsOfThisSubject(subject, blupsForThisSubject);
						}
					}					
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setDiameterBlupFromHeightBlup(MonteCarloSimulationCompliantObject subject,
			Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> defaultHeightRandomEffects,
			FrenchHeightPredictor hdPredictor) {
		if (subject instanceof FrenchHDRelationship2014Stand) {
			if (subject.getHierarchicalLevel().equals(HierarchicalLevel.PLOT)) {
				Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> heightBlups = hdPredictor.getBlupsForThisSubject((FrenchHDRelationship2014Stand) subject);
				GaussianEstimate blupsForThisSubject = setRandomEffectsAccordingToHeightDeviate(defaultHeightRandomEffects,
						getSubModule().getDefaultRandomEffects(subject.getHierarchicalLevel()), 
						heightBlups.getMean()); 
				getSubModule().registerBlupsForThisSubject(subject, blupsForThisSubject);
			}
		}
	}
	
	protected final GaussianEstimate setRandomEffectsAccordingToHeightDeviate(Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> heightRandomEffect,
			Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> diamIncRandomEffect, 
			Matrix blupMean) {
		double varianceRandomEffectHeight = heightRandomEffect.getVariance().getValueAt(0, 0);
		double varianceRandomEffectDiameterGrowth = diamIncRandomEffect.getVariance().getValueAt(0, 0);
		double covariance = Math.sqrt(varianceRandomEffectHeight * varianceRandomEffectDiameterGrowth) * CorrelationRandomEffectsHeightDiameterGrowth; 
		
		Matrix mean = new Matrix(1,1);
		mean.setValueAt(0, 0, covariance / varianceRandomEffectHeight * blupMean.getValueAt(0, 0));
		SymmetricMatrix variance = new SymmetricMatrix(1);
		variance.setValueAt(0, 0, varianceRandomEffectDiameterGrowth - covariance * covariance / varianceRandomEffectHeight);
		return new GaussianEstimate(mean, variance);
	}
	
//	@Override
//	public void clearDeviates() {
//		for (MathildeDiameterIncrementSubModule p : subModules.values()) {
//			p.clearDeviates();
//		}
//	}


//	public static void main(String[] args) {
//		MathildeDiameterIncrementPredictor pred = new MathildeDiameterIncrementPredictor(false, false, false);
//	}

}
