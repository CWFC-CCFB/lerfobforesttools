/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.frenchgeneralhdrelationship2014;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014Tree.FrenchHdSpecies;
import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;

@SuppressWarnings("serial")
public class FrenchHDRelationship2014InternalPredictor extends ModelBasedSimulator {

	private static class RegressionElements implements Serializable {
		private static final long serialVersionUID = 20100804L;
		public Matrix Z_tree;
		public double fixedPred;
		public RegressionElements() {}
	}
	
	private List<Integer> effectList;
	private List<Integer> blupEstimationDone;
	private final FrenchHdSpecies species;

	protected FrenchHDRelationship2014InternalPredictor(boolean isParametersVariabilityEnabled,	
			boolean isRandomEffectsVariabilityEnabled, 
			boolean isResidualVariabilityEnabled,
			FrenchHdSpecies species) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		this.species = species;
		blupEstimationDone = new ArrayList<Integer>();
	}

	
	protected void setDefaultBeta(GaussianEstimate defaultBeta) {
		this.defaultBeta = defaultBeta;
		oXVector = new Matrix(1, this.defaultBeta.getMean().m_iRows);
	}
	
	protected void setDefaultRandomEffects(HierarchicalLevel level, GaussianEstimate estimate) {
		defaultRandomEffects.put(level, estimate);
	}
	
	protected void setResidualVariance(GaussianErrorTermEstimate estimate) {
		defaultResidualError.put(ErrorTermGroup.Default, estimate);
	}

	protected void setEffectList(Matrix mat) {
		effectList = new ArrayList<Integer>();
		for (int i = 0; i < mat.m_iRows; i++) {
			effectList.add((int) mat.m_afData[i][0]); 
		}
	}
	
	
	/**
	 * This method calculates the height for individual trees and also implements the 
	 * Monte Carlo simulation automatically. In case of exception, it also returns -1.
	 * If the predicted height is lower than 1.3, this method returns 1.3.
	 * @param stand a HeightableStand object
	 * @param tree a HeightableTree object
	 * @return the predicted height (m)
	 */
	protected double predictHeight(FrenchHDRelationship2014Stand stand, FrenchHDRelationship2014Tree tree) {
		try {
			if (!blupEstimationDone.contains(stand.getSubjectId())) {
				predictHeightRandomEffects(stand);
				blupEstimationDone.add(stand.getSubjectId());
			}
			
			double observedHeight = tree.getHeightM();
			double predictedHeight; 
			RegressionElements regElement = fixedEffectsPrediction(stand, tree);
			predictedHeight = regElement.fixedPred;
			predictedHeight += blupImplementation(stand, regElement);

			if (observedHeight > 1.3) {			// means that height was already observed
//				double variance = matrixR.get(regElement.species.getSpeciesType()).getVariance().m_afData[0][0];
//				double dNormResidual = (observedHeight - predictedHeight) / Math.pow(variance, 0.5);
//				setSpecificResiduals(tree, dNormResidual);	// the residual is set in the simulatedResidualError member
				return -1d;
			} else {
				predictedHeight += residualImplementation(tree, regElement);
				if (predictedHeight < 1.3) {
					predictedHeight = 1.3;
				}
				return predictedHeight;
			}
		} catch (Exception e) {
			System.out.println("Error while estimating tree height for tree " + tree.toString());
			e.printStackTrace();
			return -1d;
		}
	}

	
	/**
	 * This method accounts for a random deviate if the residual variability is enabled. Otherwise, it returns 0d. 
	 * @param tree a HeightableTree instance
	 * @param regElement a RegressionElements instance
	 * @return a simulated residual (double)
	 */
	private double residualImplementation(FrenchHDRelationship2014Tree tree, RegressionElements regElement) {
//		Matrix residuals = getSpecificResiduals(tree);

		double residualForThisPrediction = 0d; 
//		if (residuals != null) {		// residuals is null only if running in deterministic mode and the height was not initially measured
//			Matrix RChol = matrixR.get(regElement.species.getSpeciesType()).getDistribution().getLowerCholeskyTriangle();
//			int date = tree.getYear();
//			int dateIndex = measurementDates.indexOf(date);
//			for (int i = 0; i <= dateIndex; i++) {
//				residualForThisPrediction += RChol.m_afData[dateIndex][i] * residuals.m_afData[i][0];
//			}
//		} 
		
		return residualForThisPrediction;
	}

	/**
	 * This method records a normalized residuals into the simulatedResidualError member which is
	 * located in the ModelBasedSimulator class. The method asks the date from the HeightableTree
	 * instance in order to put the normalized residual at the proper location in the vector of residuals.
	 * @param tree a HeightableTree instance
	 * @param normalizedResidual a normalized residual
	 */
	private synchronized void setSpecificResiduals(FrenchHDRelationship2014Tree tree, double normalizedResiduals) {
//		long id = getSubjectPlusMonteCarloSpecificId(tree);
//		int date = tree.getYear();
//		int dateIndex = measurementDates.indexOf(date);
//		if (!simulatedResidualError.containsKey(id)) {
//			simulatedResidualError.put(id, new Matrix(measurementDates.size(),1));
//		}
//		Matrix residuals = simulatedResidualError.get(id);
//		residuals.m_afData[dateIndex][0] = normalizedResiduals;
	}

	
//	protected Matrix getSpecificResiduals(FrenchHDRelationship2014Tree tree) {
//		if (isResidualVariabilityEnabled) {				// running in Monte Carlo mode
//			double dNormResidual = random.nextGaussian();
//			setSpecificResiduals(tree, dNormResidual);
//		} 
//		
//		long id = getSubjectPlusMonteCarloSpecificId(tree);
//		if (simulatedResidualError.containsKey(id)) {
//			return simulatedResidualError.get(id);
//		} else {
//			return null;
//		}
//	}

	
	/**
	 * This method accounts for the random effects in the predictions if the random effect variability is enabled. Otherwise, it returns 0d.
	 * @param stand = a HeightableStand object
	 * @param regElement = a RegressionElements object
	 * @return a simulated random effect (double)
	 */
	private double blupImplementation(FrenchHDRelationship2014Stand stand, RegressionElements regElement) {
		Matrix randomEffects = getRandomEffectsForThisSubject(stand);
		return regElement.Z_tree.multiply(randomEffects).m_afData[0][0];
	}
	
	/**
	 * This method computes the best linear unbiased predictors of the random effects
	 * @param stand a HeightableStand instance
	 */
	private synchronized void predictHeightRandomEffects(FrenchHDRelationship2014Stand stand) {
		boolean originalIsParameterVariabilityEnabled = isParametersVariabilityEnabled;
		isParametersVariabilityEnabled = false; // temporarily disabled for the prediction of the random effects
		
		Matrix matrixG = defaultRandomEffects.get(HierarchicalLevel.Plot).getVariance();
		
		Matrix blups;
		Matrix blupsVariance;

		RegressionElements regElement;
		
		// put all the trees for which the height is available in a Vector
		List<FrenchHDRelationship2014Tree> heightableTrees = new ArrayList<FrenchHDRelationship2014Tree>();
		if (!stand.getTrees().isEmpty()) {
			for (Object t : stand.getTrees()) {
				if (t instanceof FrenchHDRelationship2014Tree) {
					FrenchHDRelationship2014Tree tree = (FrenchHDRelationship2014Tree) t;
					if (tree.getFrenchHDTreeSpecies() == species) {		// only if the species matches the species of this internal predictor (all species are dealt with independently)
						double height = tree.getHeightM();
						if (height > 1.3) {
							heightableTrees.add(tree);
						}
					}
				}
			}
		}			

		if (!heightableTrees.isEmpty()) {
			// matrices for the blup calculation
			int nbObs = heightableTrees.size();
			Matrix matZ = new Matrix(nbObs, matrixG.m_iRows);		// design matrix for random effects 
			Matrix matR = new Matrix(nbObs, nbObs);					// within-tree variance-covariance matrix  
			Matrix matRes = new Matrix(nbObs, 1);						// vector of residuals

			for (int i = 0; i < nbObs; i++) {
				FrenchHDRelationship2014Tree t = heightableTrees.get(i);
				double height = t.getHeightM();
				
				regElement = fixedEffectsPrediction(stand, t);
				matZ.setSubMatrix(regElement.Z_tree, i, 0);
				double variance = defaultResidualError.get(ErrorTermGroup.Default).getVariance().m_afData[0][0];
				matR.m_afData[i][i] = variance;
				double residual = height - regElement.fixedPred;
				matRes.m_afData[i][0] = residual;
			}
			Matrix matV = matZ.multiply(matrixG).multiply(matZ.transpose()).add(matR);	// variance - covariance matrix
			blups = matrixG.multiply(matZ.transpose()).multiply(matV.getInverseMatrix()).multiply(matRes);							// blup_essHD is redefined according to observed values
			blupsVariance = matZ.transpose().multiply(matR.getInverseMatrix()).multiply(matZ).add(matrixG.getInverseMatrix()).getInverseMatrix();			// blup_essHDvar is redefined according to observed values
			Map<Integer, GaussianEstimate> randomEffectsMap = blupsLibrary.get(HierarchicalLevel.Plot);
			if (randomEffectsMap == null) {
				randomEffectsMap = new HashMap<Integer, GaussianEstimate>();
				blupsLibrary.put(HierarchicalLevel.Plot, randomEffectsMap);
			}
			randomEffectsMap.put(stand.getSubjectId(), new GaussianEstimate(blups, blupsVariance));
		}
		
		isParametersVariabilityEnabled = originalIsParameterVariabilityEnabled; // set the parameter variability to its original value;
	}

	
	/**
	 * This method computes the fixed effect prediction and put the prediction, the Z vector,
	 * and the species name into m_oRegressionOutput member. The method applies in any cases no matter
	 * it is deterministic or stochastic.
	 * @param stand a HeightableStand instance
	 * @param tree a HeightableTree instance
	 * @return a RegressionElement instance
	 */
	protected synchronized RegressionElements fixedEffectsPrediction(FrenchHDRelationship2014Stand stand, FrenchHDRelationship2014Tree tree) {
		Matrix modelParameters = getParametersForThisRealization(stand);
		
		double basalAreaMinusSubj = stand.getBasalAreaM2HaMinusThisSubject(tree);
		double slope = stand.getSlopePercent();
		
		oXVector.resetMatrix();
		int pointer = 0;
		
		double lnDbh = tree.getLnDbhCmPlus1();
		double socialIndex = tree.getDbhCm() - stand.getMeanQuadraticDiameterCm();
		double lnDbh2 = tree.getSquaredLnDbhCmPlus1();

		for (Integer integer : effectList) {
			switch(integer) {
			case 1:
				oXVector.m_afData[0][pointer] = basalAreaMinusSubj * lnDbh2;
				pointer++;
				break;
			case 2:
				oXVector.m_afData[0][pointer] = lnDbh;
				pointer++;
				break;
			case 3:
				oXVector.m_afData[0][pointer] = basalAreaMinusSubj * lnDbh;
				pointer++;
				break;
			case 4:
				oXVector.m_afData[0][pointer] = lnDbh * slope;
				pointer++;
				break;
			case 5:
				oXVector.m_afData[0][pointer] = socialIndex * lnDbh;
				pointer++;
				break;
			case 6:
				oXVector.m_afData[0][pointer] = lnDbh2;
				pointer++;
				break;
			default:
				throw new InvalidParameterException("This effect index " + integer + " is not recognized!");
			}
		}

		double fResult = 1.3 + oXVector.multiply(modelParameters).m_afData[0][0];
		
		Matrix Z_i = new Matrix(1,1);
		Z_i.m_afData[0][0] = lnDbh;	// design vector for the plot random effect

		RegressionElements regElements = new RegressionElements();
		
		regElements.fixedPred = fResult;
		regElements.Z_tree = Z_i;

		return regElements;
	}


	protected Matrix getBlups(FrenchHDRelationship2014Stand stand) {
		if (blupsLibrary.get(HierarchicalLevel.Plot) != null) {
			return blupsLibrary.get(HierarchicalLevel.Plot).get(stand.getSubjectId()).getMean();
		} else {
			return null;
		}
	}

	
	
}
