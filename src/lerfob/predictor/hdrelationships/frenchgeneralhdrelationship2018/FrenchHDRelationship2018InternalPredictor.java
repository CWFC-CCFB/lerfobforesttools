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
package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.FertilityClassEmulator;
import lerfob.predictor.hdrelationships.FrenchHDRelationshipTree.FrenchHdSpecies;
import lerfob.predictor.hdrelationships.FrenchHeightPredictor;
import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.climate.REpiceaClimateVariableMap;
import repicea.simulation.climate.REpiceaClimateVariableMap.ClimateVariable;
import repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider.SpeciesType;
import repicea.simulation.hdrelationships.HDRelationshipPredictor;
import repicea.stats.StatisticalUtility.TypeMatrixR;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.TruncatedGaussianEstimate;

@SuppressWarnings("serial")
public class FrenchHDRelationship2018InternalPredictor extends HDRelationshipPredictor<FrenchHDRelationship2018Plot, FrenchHDRelationship2018Tree> 
														implements FertilityClassEmulator,
																FrenchHeightPredictor<FrenchHDRelationship2018Plot, FrenchHDRelationship2018Tree>{

	private static final Map<SpeciesType, Double> PhiParameters = new HashMap<SpeciesType, Double>();
	static {
		PhiParameters.put(SpeciesType.ConiferousSpecies, 0.02619872948641); // taken from Quebec HD relationships
		PhiParameters.put(SpeciesType.BroadleavedSpecies, 0.04468342698978); // taken from Quebec HD relationships
	}

	private static Map<FertilityClass, TruncatedGaussianEstimate> fertilityClassMap;

	private List<Integer> effectList;
	private final FrenchHdSpecies species;
	private FertilityClass currentFertilityClass;
	private final FrenchHDRelationship2018Predictor mainPredictor;
	
	
	protected FrenchHDRelationship2018InternalPredictor(boolean isParameterVariabilityEnabled, 
			boolean isRandomEffectVariabilityEnabled, 
			boolean isResidualVariabilityEnabled, 
			FrenchHdSpecies species,
			FrenchHDRelationship2018Predictor mainPredictor) {
		super(isParameterVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);
		this.species = species;
		this.mainPredictor = mainPredictor;
		currentFertilityClass = FertilityClass.Unknown;	// default value
	}


	protected Map<FertilityClass, TruncatedGaussianEstimate> getFertilityClassMap() {
		if (fertilityClassMap == null) {
			fertilityClassMap = new HashMap<FertilityClass, TruncatedGaussianEstimate>();
			
			Estimate<? extends StandardGaussianDistribution> levelRandomEffects = getDefaultRandomEffects(HierarchicalLevel.PLOT);
			TruncatedGaussianEstimate truncatedEstimate;
			Matrix stdMatrix = levelRandomEffects.getVariance().getLowerCholTriangle();
			
			truncatedEstimate = new TruncatedGaussianEstimate(levelRandomEffects.getMean(), levelRandomEffects.getVariance());
			truncatedEstimate.setLowerBoundValue(stdMatrix.scalarMultiply(0.999));
			fertilityClassMap.put(FertilityClass.I, truncatedEstimate);

			truncatedEstimate = new TruncatedGaussianEstimate(levelRandomEffects.getMean(), levelRandomEffects.getVariance());
			truncatedEstimate.setLowerBoundValue(stdMatrix.scalarMultiply(-0.7388));
			truncatedEstimate.setUpperBoundValue(stdMatrix.scalarMultiply(0.999));
			fertilityClassMap.put(FertilityClass.II, truncatedEstimate);

			truncatedEstimate = new TruncatedGaussianEstimate(levelRandomEffects.getMean(), levelRandomEffects.getVariance());
			truncatedEstimate.setUpperBoundValue(stdMatrix.scalarMultiply(-0.7388));
			fertilityClassMap.put(FertilityClass.III, truncatedEstimate);
		}
		return fertilityClassMap;
	}
	
	/*
	 * For extended visibility
	 */
	@Override
	protected void setDefaultRandomEffects(HierarchicalLevel level, Estimate<? extends StandardGaussianDistribution> estimate) {
		super.setDefaultRandomEffects(level, estimate);
	}
	
	
	@Override
	protected void setParameterEstimates(ModelParameterEstimates defaultBeta) {
		super.setParameterEstimates(defaultBeta);
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
	}
	
	/**
	 * This method sets the fertility class. However, it must be set before estimating the random effects. 
	 * Otherwise, it has no effect.
	 */
	@Override
	public void emulateFertilityClass(FertilityClass fertilityClass) {
		if (fertilityClass != null && this.currentFertilityClass != fertilityClass) {
			currentFertilityClass = fertilityClass;
		}
	}
	
	@Override
	protected synchronized void predictHeightRandomEffects(FrenchHDRelationship2018Plot stand) {
		if (currentFertilityClass == FertilityClass.Unknown) {
			super.predictHeightRandomEffects(stand);
		} else {	// we have tweaked the plot random effect to account for the site index class
			TruncatedGaussianEstimate estimate = getFertilityClassMap().get(currentFertilityClass);
			setBlupsForThisSubject(stand, estimate);
		}
	}
	
	protected void setResidualVariance(Matrix sigma2) {
		double correlationParameters = PhiParameters.get(species.getSpeciesType());
		GaussianErrorTermEstimate estimate = new GaussianErrorTermEstimate(sigma2, correlationParameters, TypeMatrixR.LINEAR);
		setDefaultResidualError(ErrorTermGroup.Default, estimate);
	}

	protected void setEffectList(Matrix mat) {
		effectList = new ArrayList<Integer>();
		for (int i = 0; i < mat.m_iRows; i++) {
			effectList.add((int) mat.getValueAt(i, 0)); 
		}
	}
	
	/**
	 * This method computes the fixed effect prediction and put the prediction, the Z vector,
	 * and the species name into m_oRegressionOutput member. The method applies in any cases no matter
	 * it is deterministic or stochastic.
	 * @param stand a HeightableStand instance
	 * @param tree a HeightableTree instance
	 * @return a RegressionElement instance
	 */
	@Override
	protected synchronized RegressionElements fixedEffectsPrediction(FrenchHDRelationship2018Plot stand, FrenchHDRelationship2018Tree tree, Matrix beta) {
		Matrix modelParameters = beta;
		
		double basalAreaMinusSubj = stand.getBasalAreaM2HaMinusThisSubject(tree);
		if (basalAreaMinusSubj < 0d) {
			if (basalAreaMinusSubj > -1E-8) {		// negative values only due to decimal rounding
				basalAreaMinusSubj = 0d;
			} else {
				System.out.println("Error in HD relationship: The basal area of the plot has not been calculated yet!");
				throw new InvalidParameterException("The basal area of the plot has not been calculated yet!");
			}
		}
		double slope = stand.getSlopeInclinationPercent();
		
		oXVector.resetMatrix();
		int pointer = 0;
		
		double meanPrec;
		double meanTemp;
		if (stand instanceof FrenchHDRelationship2018ExtPlot) {
			FrenchHDRelationship2018ExtPlot s = (FrenchHDRelationship2018ExtPlot) stand;
			meanPrec = s.getMeanPrecipitationOfGrowingSeason();
			meanTemp = s.getMeanTemperatureOfGrowingSeason();
		} else {
			REpiceaClimateVariableMap cp = mainPredictor.getNearestClimatePoint(stand);
			meanPrec = cp.get(ClimateVariable.MeanGrowingSeasonPrecMm);
			meanTemp = cp.get(ClimateVariable.MeanGrowingSeasonTempC);
		}
		
		double lnDbh = tree.getLnDbhCmPlus1();
		double socialIndex = tree.getDbhCm() / stand.getMeanQuadraticDiameterCm(); // former version changed for next line MF2018-09-10
		
		double lnDbh2 = tree.getSquaredLnDbhCmPlus1();

		double harvested = 0d;
		if (stand.isInterventionResult()) {
			harvested = 1d;
		}
		
		for (Integer integer : effectList) {
			switch(integer) {
			case 1:
				oXVector.setValueAt(0, pointer, basalAreaMinusSubj * lnDbh2);
				pointer++;
				break;
			case 2:
				oXVector.setValueAt(0, pointer, lnDbh);
				pointer++;
				break;
			case 3:
				oXVector.setValueAt(0, pointer, basalAreaMinusSubj * lnDbh);
				pointer++;
				break;
			case 4:
				oXVector.setValueAt(0, pointer, harvested * lnDbh);
				pointer++;
				break;
			case 5:
				oXVector.setValueAt(0, pointer, lnDbh * meanPrec);
				pointer++;
				break;
			case 6:
				oXVector.setValueAt(0, pointer, lnDbh * meanTemp * meanTemp);
				pointer++;
				break;
			case 7:
				oXVector.setValueAt(0, pointer, lnDbh * meanTemp);
				pointer++;
				break;
			case 8:
				oXVector.setValueAt(0, pointer, lnDbh * slope);
				pointer++;
				break;
			case 9:
				oXVector.setValueAt(0, pointer, socialIndex * lnDbh);
				pointer++;
				break;
			case 10:
				oXVector.setValueAt(0, pointer, socialIndex * socialIndex * lnDbh);
				pointer++;
				break;
			case 11:
				oXVector.setValueAt(0, pointer, lnDbh2);
				pointer++;
				break;
			case 12:
				oXVector.setValueAt(0, pointer, socialIndex * lnDbh2);
				pointer++;
				break;
			default:
				throw new InvalidParameterException("This effect index " + integer + " is not recognized!");
			}
		}

		double fResult = 1.3 + oXVector.multiply(modelParameters).getValueAt(0, 0);
		
		Matrix Z_i = new Matrix(1,1);
		Z_i.setValueAt(0, 0, lnDbh);	// design vector for the plot random effect

		RegressionElements regElements = new RegressionElements();
		
		regElements.fixedPred = fResult;
		regElements.vectorZ = Z_i;

		return regElements;
	}

	@Override
	public Estimate<? extends StandardGaussianDistribution> getBlupsForThisSubject(FrenchHDRelationship2018Plot stand) {
		return super.getBlupsForThisSubject(stand);
	}


	@Override
	protected Collection<FrenchHDRelationship2018Tree> getTreesFromStand(FrenchHDRelationship2018Plot stand) {
		Collection<FrenchHDRelationship2018Tree> treesToBeReturned = new ArrayList<FrenchHDRelationship2018Tree>();
		Collection<?> trees = stand.getTreesForFrenchHDRelationship();
		if (trees != null && !trees.isEmpty()) {
			for (Object tree : trees) {
				if (tree instanceof FrenchHDRelationship2018Tree) {
					FrenchHDRelationship2018Tree t = (FrenchHDRelationship2018Tree) tree;
					if (t.getFrenchHDTreeSpecies() == species) {
						treesToBeReturned.add(t);
					}
				}
			}
		}
		return treesToBeReturned;
	}


	/*
	 * Useless for this class (non-Javadoc)
	 * @see repicea.simulation.ModelBasedSimulator#init()
	 */
	@Override
	protected void init() {}
	
	@Override
	public FrenchHdSpecies getSpecies() {return species;}


	boolean hasTemperatureEffect() {
		return effectList.contains(7) || effectList.contains(6);
	}
	
	boolean hasPrecipitationEffect() {
		return effectList.contains(5);
	}

	synchronized GaussianEstimate predictHeightAndVariance(FrenchHDRelationship2018Plot stand, FrenchHDRelationship2018Tree tree) {
		Matrix pred = new Matrix(1,1);
		pred.setValueAt(0, 0, predictHeightM(stand, tree));
		Matrix variance = oXVector.multiply(this.getParameterEstimates().getVariance()).multiply(oXVector.transpose());
		return new GaussianEstimate(pred, variance);
	}
	
	
	
//	GaussianEstimate getGaussianEstimateFromTemperatureEffect() {
//		List<Integer> indices = new ArrayList<Integer>();
//		if (effectList.contains(7)) {
//			indices.add(effectList.indexOf(7));
//		}
//		if (effectList.contains(6)) {
//			indices.add(effectList.indexOf(6));
//		}
//		
//		if (indices.isEmpty()) {
//			return null;
//		} else {
//			Matrix mean = getParameterEstimates().getMean().getSubMatrix(indices, new ArrayList<Integer>(0));
//			Matrix variance = getParameterEstimates().getVariance().getSubMatrix(indices, indices); 
//			return new GaussianEstimate(mean, variance);
//		}
//	
//	}
}
