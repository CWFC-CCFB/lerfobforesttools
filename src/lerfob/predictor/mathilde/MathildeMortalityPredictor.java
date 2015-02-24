/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Rub�n Manso for LERFOB INRA/AgroParisTech, 
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

import lerfob.predictor.mathilde.MathildeTree.MathildeTreeSpecies;
import repicea.math.EvaluableFunction;
import repicea.math.Matrix;
import repicea.simulation.LogisticModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.stats.LinearStatisticalExpression;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.LFParameter;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * This class implements the mortality submodel in Mathilde model.
 * @author Ruben Manso and Mathieu Fortin - October 2013
 */
@SuppressWarnings("serial")
public final class MathildeMortalityPredictor extends LogisticModelBasedSimulator<MathildeMortalityStand, MathildeTree> {

	
	protected static boolean isGaussianQuadratureEnabled = true;	
	
	protected class ExtendedLinkFunction implements EvaluableFunction<Double> {
		
		@Override
		public Double getValue() {
			eta.setVariableValue(1, embeddedLinkFunction.getValue());
			return linkFunction.getValue();
		}

	}
	
	
	private final LinkFunction linkFunction;
	private final LinearStatisticalExpression eta;
	private final LinkFunction embeddedLinkFunction;
	private final LinearStatisticalExpression embeddedEta;
	private final ExtendedLinkFunction extendedLinkFunction;
	protected GaussHermiteQuadrature ghq;

	/**
	 * Constructor.
	 * @param isParametersVariabilityEnabled
	 * @param isResidualVariabilityEnabled
	 */
	public MathildeMortalityPredictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);	
		init();
		oXVector = new Matrix(1,defaultBeta.getMean().m_iRows);
		linkFunction = new LinkFunction(Type.CLogLog);
		eta = new LinearStatisticalExpression();
		linkFunction.setParameterValue(LFParameter.Eta, eta);
		eta.setParameterValue(0, 1d);
		
		embeddedLinkFunction = new LinkFunction(Type.Log);
		embeddedEta = new LinearStatisticalExpression();
		embeddedLinkFunction.setParameterValue(LFParameter.Eta, embeddedEta);
		embeddedEta.setParameterValue(0, 1d);
		embeddedEta.setParameterValue(1, 1d);
		
		extendedLinkFunction = new ExtendedLinkFunction();
		
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N15);		
	}
	
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeMortalityBeta.csv";
			String omegaFilename = path + "0_MathildeMortalityOmega.csv";
//			String covparmsFilename = path + "0_MathildeMortalityCovParms.csv";

			Matrix betaPrelim = ParameterLoader.loadVectorFromFile(betaFilename).get();
			int nbParams = betaPrelim.m_iRows - 1;
			Matrix defaultBetaMean = betaPrelim.getSubMatrix(0, nbParams - 1, 0, 0);
			Matrix randomEffectVariance = betaPrelim.getSubMatrix(nbParams, nbParams, 0, 0);
			Matrix defaultBetaVariance = ParameterLoader.loadMatrixFromFile(omegaFilename).getSubMatrix(0, nbParams - 1, 0, nbParams - 1);
			defaultBeta = new GaussianEstimate(defaultBetaMean, defaultBetaVariance);
			
//			Matrix randomEffectVariance = ParameterLoader.loadVectorFromFile(covparmsFilename).get();
			GaussianEstimate randomEffect = new GaussianEstimate(new Matrix(randomEffectVariance.m_iRows,1), randomEffectVariance);
			defaultRandomEffects.put(HierarchicalLevel.IntervalNestedInPlot, randomEffect);
			
		} catch (Exception e) {
			System.out.println("MathildeMortalityPredictor.init() : Unable to initialize the MathildeMortalityPredictor module");
		}
	}

	protected double getFixedEffectOnlyPrediction(Matrix beta, MathildeMortalityStand stand, MathildeTree tree) {
		oXVector.resetMatrix();
		
		double dbh = tree.getDbhCm();
		double logDbh = tree.getLnDbhCm();
		double upcomingCut = 0d;
		if (stand.isGoingToBeHarvested()) {
			upcomingCut = 1d;
		} 

		double upcomingDrought = 0d;
		if (stand.isADroughtGoingToOccur()) {
			upcomingDrought = 1d;
		} 

		MathildeTreeSpecies species = tree.getMathildeTreeSpecies();
		double bal22 = tree.getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies.QUERCUS);
		double bal42 = tree.getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies.FAGUS);
		
		double gr17 = 0d;
		if (species == MathildeTreeSpecies.getSpecies(17)) {
			gr17 = 1d;
		}
		
		int pointer = 0;
		
		oXVector.m_afData[0][pointer] = 1d;
		pointer++;
		
		oXVector.setSubMatrix(species.getDummyVariable(), 0, pointer);
		pointer += species.getDummyVariable().m_iCols;

		oXVector.m_afData[0][pointer] = dbh;
		pointer++;

		oXVector.m_afData[0][pointer] = logDbh * gr17;
		pointer++;

		oXVector.setSubMatrix(species.getDummyVariable().scalarMultiply(logDbh), 0, pointer);
		pointer += species.getDummyVariable().m_iCols;
		
		oXVector.m_afData[0][pointer] = bal42;
		pointer++;

		oXVector.m_afData[0][pointer] = bal22 * gr17;
		pointer++;

		oXVector.setSubMatrix(species.getDummyVariable().scalarMultiply(bal22), 0, pointer);
		pointer += species.getDummyVariable().m_iCols;

		pointer++; // to skip b14
		
		oXVector.m_afData[0][pointer] = upcomingDrought;
		pointer++;
		
		oXVector.m_afData[0][pointer] = upcomingCut * gr17;
		pointer++;

		oXVector.setSubMatrix(species.getDummyVariable().scalarMultiply(upcomingCut), 0, pointer);
		pointer += species.getDummyVariable().m_iCols;
		
		oXVector.m_afData[0][pointer] = Math.log(stand.getGrowthStepLengthYrs());
		pointer++;
		
		double result = oXVector.multiply(beta).m_afData[0][0];
		return result;
	}
	
	@Override
	public synchronized double predictEventProbability(MathildeMortalityStand stand, MathildeTree tree, Object... parms) {
		double upcomingWindstorm = 0d;
		if (stand.isAWindstormGoingToOccur()) {
			upcomingWindstorm = 1d;
		} 
		
		Matrix beta = getParametersForThisRealization(stand);
		eta.setParameterValue(1, upcomingWindstorm);
		
		double pred = getFixedEffectOnlyPrediction(beta, stand, tree);
		eta.setVariableValue(0, pred);

		double prob;
		embeddedEta.setVariableValue(0, beta.m_afData[14][0]);
		if (isRandomEffectsVariabilityEnabled && stand.isAWindstormGoingToOccur()) {	// no need to draw a random effect if there is no windstorm
			IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
			Matrix randomEffects = getRandomEffectsForThisSubject(interval);
			embeddedEta.setVariableValue(1, randomEffects.m_afData[0][0]);
			prob = extendedLinkFunction.getValue();
		} else {
			embeddedEta.setVariableValue(1, 0d);		// random effect arbitrarily set to 0
			if (stand.isAWindstormGoingToOccur() && isGaussianQuadratureEnabled) {
				prob = ghq.getOneDimensionIntegral(extendedLinkFunction, embeddedEta, 1, defaultRandomEffects.get(HierarchicalLevel.IntervalNestedInPlot).getDistribution().getLowerCholeskyTriangle().m_afData[0][0]);
			} else {									// no need to evaluate the quadrature when there is no windstorm
				prob = extendedLinkFunction.getValue();
			}
		}
		
		return prob;
	}

	
	public static void main (String[] args) {
		new MathildeMortalityPredictor(false, false, false); 
	}
}

