/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde.recruitment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.distributions.utility.NegativeBinomialUtility;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

/**
 * This class predicts the number of recruits for each species of the MATHILDE model.
 * @author Mathieu Fortin - Octobre 2017
 */
@SuppressWarnings("serial")
public class MathildeRecruitmentNumberPredictor extends REpiceaPredictor {

	private static final Map<MathildeTreeSpecies, Matrix> DummyMap = new HashMap<MathildeTreeSpecies, Matrix>();
	static {
		Matrix m = new Matrix(1,4);
		m.m_afData[0][0] = 1d;
		DummyMap.put(MathildeTreeSpecies.FAGUS, m);

		m = new Matrix(1,4);
		m.m_afData[0][1] = 1d;
		DummyMap.put(MathildeTreeSpecies.CARPINUS, m);
		
		m = new Matrix(1,4);
		m.m_afData[0][2] = 1d;
		DummyMap.put(MathildeTreeSpecies.QUERCUS, m);
		
		m = new Matrix(1,4);
		m.m_afData[0][3] = 1d;
		DummyMap.put(MathildeTreeSpecies.OTHERS, m);
	}

	public static final double REFERENCE_AREA_HA = Math.PI * 6d * 6d / 10000;
	
	
	private final Matrix oXVectorZero;
	
	private GaussianEstimate copula;

	/**
	 * Constructor
	 * @param isVariabilityEnabled true to enable the stochastic variability.
	 */
	public MathildeRecruitmentNumberPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled); // there is no random effect in this model
	}

	protected MathildeRecruitmentNumberPredictor(boolean isParameterVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, false, isResidualVariabilityEnabled); // there is no random effect in this model
		init();
		oXVector = new Matrix(1,12);
		oXVectorZero = new Matrix(1,20);
	}

	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_zinb_beta.csv";
			String thetaFilename = path + "0_zinb_theta.csv";
			String omegaFilename = path + "0_zinb_omega.csv";
			String copulaFilename = path + "0_zinb_copula.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(betaFilename);
			
			Matrix thetaMat = ParameterLoader.loadVectorFromFile(thetaFilename).get();
			Matrix theta = new Matrix(1,1);
			theta.m_afData[0][0] = thetaMat.m_afData[0][0];
			
			Matrix thetaVar = new Matrix(1,1);
			thetaVar.m_afData[0][0] = thetaMat.m_afData[1][0] * thetaMat.m_afData[1][0];
			Matrix beta = betaMap.get().matrixStack(theta, true);
			Matrix omega = ParameterLoader.loadMatrixFromFile(omegaFilename);	
			omega = omega.matrixDiagBlock(thetaVar);
			setParameterEstimates(new ModelParameterEstimates(beta, omega));

			Matrix copula = ParameterLoader.loadMatrixFromFile(copulaFilename);
			Matrix meanCopula = new Matrix(copula.m_iRows, 1);
			this.copula = new GaussianEstimate(meanCopula, copula);

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("An error ocurred while reading the parameters!");
		}
	}

	/**
	 * This method returns the number of recruits for each species.
	 * @param stand a MathildeRecruitmentStand instance
	 * @return a Matrix 
	 */
	public synchronized Matrix predictNumberOfRecruits(MathildeRecruitmentStand stand) {
		Matrix deterministicPred = null;
		for (MathildeTreeSpecies species : MathildeTreeSpecies.values()) {
			Matrix mat = getMarginalPredictionsForThisStandAndSpecies(stand, species, 15, isResidualVariabilityEnabled);
			if (deterministicPred == null) {
				deterministicPred = mat;
			} else {
				deterministicPred = deterministicPred.matrixStack(mat, true);
			}
		}
		
		if (deterministicPred.m_iCols == 1) { // running without stochastic variability
			return deterministicPred;
		} else {
			Matrix m = copula.getRandomDeviate();		// watchout the copula was fitted with the species in this order : beech, hornbeam, oak, others
			Matrix stochasticPred = new Matrix(4,1);
			stochasticPred.m_afData[0][0] = getNumberOfRecruit(GaussianUtility.getCumulativeProbability(m.m_afData[1][0]), 
					deterministicPred.getSubMatrix(0, 0, 0, deterministicPred.m_iCols - 1)); // hornbeam
			stochasticPred.m_afData[1][0] = getNumberOfRecruit(GaussianUtility.getCumulativeProbability(m.m_afData[2][0]), 
					deterministicPred.getSubMatrix(1, 1, 0, deterministicPred.m_iCols - 1)); // oak
			stochasticPred.m_afData[2][0] = getNumberOfRecruit(GaussianUtility.getCumulativeProbability(m.m_afData[0][0]), 
					deterministicPred.getSubMatrix(2, 2, 0, deterministicPred.m_iCols - 1)); // beech
			stochasticPred.m_afData[3][0] = getNumberOfRecruit(GaussianUtility.getCumulativeProbability(m.m_afData[3][0]), 
					deterministicPred.getSubMatrix(3, 3, 0, deterministicPred.m_iCols - 1)); // others
			return stochasticPred;
		}
	}

	private double getNumberOfRecruit(double cdf, Matrix predictions) {
		double cumProb = 0d;
		double newCumProb;
		int i;
		for (i = 0; i < predictions.m_iCols; i++) {
			newCumProb = cumProb + predictions.m_afData[0][i];
			if (cdf > cumProb && cdf <= newCumProb) {
				break;
			} else {
				cumProb = newCumProb;
			}
		}
		return i;
	}
	/**
	 * This method returns the marginal prediction for a single species. If the isResidualVariabilityEnabled
	 * option is set to false then the resolution parameter is useless.
	 * @param stand a MathildeRecruitmentStand instance
	 * @param species a MathildeTreeSpecies instance
	 * @param resolution the maximum number of recruits
	 * @param isResidualVariabilityEnabled true to enabled the stochastic variability.  
	 * @return a row vector (the column index is the number of recruits 
	 */
	protected Matrix getMarginalPredictionsForThisStandAndSpecies(MathildeRecruitmentStand stand, MathildeTreeSpecies species, int resolution, boolean isResidualVariabilityEnabled) {
		Matrix betaForThisStand = getParametersForThisRealization(stand);
		Matrix betaCount = betaForThisStand.getSubMatrix(0, 11, 0, 0);
		double theta = Math.exp(betaForThisStand.m_afData[betaForThisStand.m_iRows - 1][0]);
		Matrix betaZero = betaForThisStand.getSubMatrix(12, betaForThisStand.m_iRows - 2, 0, 0);
		double negBinMean = getNegativeBinomialMean(betaCount, stand, species);
		double nonZeroProb = getFalseZeroProbability(betaZero, stand, species);
		double dispersion = 1d/theta;
		double truncationFactor = 1 - NegativeBinomialUtility.getMassProbability(0, negBinMean, dispersion);
				
		Matrix output;
		if (isResidualVariabilityEnabled) {
			output = new Matrix(1,resolution);
			double p;
			
			for (int j = 0; j < output.m_iCols; j++) {
				if (j==0) {
					p = (1 - nonZeroProb);
				} else {
					p =  nonZeroProb * NegativeBinomialUtility.getMassProbability(j, negBinMean, dispersion) / truncationFactor;
				}
				output.m_afData[0][j] = p;
			}
		} else {
			output = new Matrix(1,1);
			double overallMean = nonZeroProb * negBinMean / truncationFactor;
			output.m_afData[0][0] = overallMean;
		}
		return output;
	}
	
	
	
	
	protected double getNegativeBinomialMean(Matrix betaCount, MathildeRecruitmentStand stand, MathildeTreeSpecies species) {
		oXVector.resetMatrix();

		Matrix speciesDummy = DummyMap.get(species);
		double basalAreaM2Ha = stand.getBasalAreaM2Ha();
		double basalAreaM2HaOfThisSpecies = stand.getBasalAreaM2HaOfThisSpecies(species);
		
		int index = 0;
		oXVector.setSubMatrix(speciesDummy, 0, index);
		index += speciesDummy.m_iCols;
		
		oXVector.setSubMatrix(speciesDummy.scalarMultiply(basalAreaM2Ha), 0, index);
		index += speciesDummy.m_iCols;

		oXVector.setSubMatrix(speciesDummy.scalarMultiply(basalAreaM2HaOfThisSpecies), 0, index);
		index += speciesDummy.m_iCols;
		
		double linearPredictor = oXVector.multiply(betaCount).m_afData[0][0];
		return Math.exp(linearPredictor);
	}

	protected double getFalseZeroProbability(Matrix betaZero, MathildeRecruitmentStand stand, MathildeTreeSpecies species) {
		oXVectorZero.resetMatrix();

		Matrix speciesDummy = DummyMap.get(species);
		double basalAreaM2Ha = stand.getBasalAreaM2Ha();
		double basalAreaM2HaOfThisSpecies = stand.getBasalAreaM2HaOfThisSpecies(species);
		
		int index = 0;
		oXVectorZero.setSubMatrix(speciesDummy, 0, index);
		index += speciesDummy.m_iCols;
		
		oXVectorZero.setSubMatrix(speciesDummy.scalarMultiply(basalAreaM2Ha), 0, index);
		index += speciesDummy.m_iCols;

		oXVectorZero.setSubMatrix(speciesDummy.scalarMultiply(basalAreaM2Ha).scalarAdd(1d).logMatrix(), 0, index);
		index += speciesDummy.m_iCols;

		oXVectorZero.setSubMatrix(speciesDummy.scalarMultiply(basalAreaM2HaOfThisSpecies), 0, index);
		index += speciesDummy.m_iCols;

		oXVectorZero.setSubMatrix(speciesDummy.scalarMultiply(basalAreaM2HaOfThisSpecies).scalarAdd(1d).logMatrix(), 0, index);
		index += speciesDummy.m_iCols;

		double linearPredictor = oXVectorZero.multiply(betaZero).m_afData[0][0];
		double expTerm = Math.exp(linearPredictor);
		return expTerm / (1 + expTerm);
	}
	
	
//	public static void main(String[] args) {
//		MathildeRecruitmentNumberPredictor p = new MathildeRecruitmentNumberPredictor(false);
//	}
}
