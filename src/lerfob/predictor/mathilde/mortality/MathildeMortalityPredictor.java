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
package lerfob.predictor.mathilde.mortality;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.mathilde.MathildeTree;
import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.math.AbstractMathematicalFunction;
import repicea.math.MathematicalFunction;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaBinaryEventPredictor;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.stats.StatisticalUtility;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussHermiteQuadrature.GaussHermiteQuadratureCompatibleFunction;
import repicea.stats.model.glm.LinkFunction;
import repicea.stats.model.glm.LinkFunction.Type;
import repicea.util.ObjectUtility;

/**
 * This class implements the mortality submodel in Mathilde model. All the details of this module are available in 
 * Manso et al. 2015. Forest Ecology and Management 354: 243-253. 
 * @see <a href=http://www.sciencedirect.com/science/article/pii/S0378112715003345> 
	 Manso, R., Morneau, F., Ningre, F., and Fortin, M. 2015. Incorporating stochasticity from extreme climatic events
	 and multi-species competition relationships into single-tree mortality models. Forest Ecology and Management 354: 243-253</a>
 * @author Ruben Manso and Mathieu Fortin - October 2013
 */
@SuppressWarnings("serial")
public class MathildeMortalityPredictor extends REpiceaBinaryEventPredictor<MathildeMortalityStand, MathildeTree> {

	protected final static String ParmWindstormDisabledOverride = "WindstormDisabledOverride";
	public final static String ParmSubmoduleFromCrossValidation = "SubmoduleFromCrossValidation";
	
	protected static final Map<String, Object> ParmsToDisableWindstorm = new HashMap<String, Object>();
	static {
		ParmsToDisableWindstorm.put(ParmWindstormDisabledOverride, true);
	}
	
	protected static boolean isGaussianQuadratureEnabled = true;	
	
	private static final double SqrtTwo = Math.sqrt(2d);
	protected static final int IndexParameterToBeIntegrated = 2;
	
	class EmbeddedLinkFunction extends LinkFunction implements GaussHermiteQuadratureCompatibleFunction<Double> {

		double standardDeviation;
		
		public EmbeddedLinkFunction(Type type, MathematicalFunction eta) {
			super(type, eta);
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

	protected class InternalMathematicalFunction extends AbstractMathematicalFunction {
		
		private InternalMathematicalFunction() {
			super();
		}
		
		@Override
		public Double getValue() {
			double fixedPart = getParameterValue(0);
			double dummyWindstorm = getVariableValue(1);
			double windstormParameter = getParameterValue(1);
			double randomEffect = getParameterValue(2);
			return fixedPart + dummyWindstorm * Math.exp(windstormParameter + randomEffect);
		}

		@Override
		public Matrix getGradient() {
			double dummyWindstorm = getVariableValue(1);
			double windstormParameter = getParameterValue(1);
			double randomEffect = getParameterValue(2);
			Matrix gradient = new Matrix(3,1);
			gradient.setValueAt(0, 0, 1d);
			gradient.setValueAt(1, 0, dummyWindstorm * Math.exp(windstormParameter + randomEffect));
			gradient.setValueAt(2, 0, dummyWindstorm * Math.exp(windstormParameter + randomEffect));
			return gradient;
		}

		@Override
		public SymmetricMatrix getHessian() {
			double dummyWindstorm = getVariableValue(1);
			double windstormParameter = getParameterValue(1);
			double randomEffect = getParameterValue(2);
			SymmetricMatrix hessian = new SymmetricMatrix(3);
			hessian.setValueAt(0, 0, 0d);
//			hessian.setValueAt(1, 0, 0d);
//			hessian.setValueAt(2, 0, 0d);
			hessian.setValueAt(0, 1, 0d);
			hessian.setValueAt(0, 2, 0d);
			hessian.setValueAt(1, 1, dummyWindstorm * Math.exp(windstormParameter + randomEffect));
//			hessian.setValueAt(2, 1, dummyWindstorm * Math.exp(windstormParameter + randomEffect));
			hessian.setValueAt(1, 2, dummyWindstorm * Math.exp(windstormParameter + randomEffect));
			hessian.setValueAt(2, 2, dummyWindstorm * Math.exp(windstormParameter + randomEffect));
			return hessian;
		}

	}
	
	protected final Map<Integer, MathildeMortalitySubModule> subModules;
	
	protected final EmbeddedLinkFunction linkFunction;
	protected int numberOfParameters;
	protected GaussHermiteQuadrature ghq;

	/**
	 * Constructor.
	 * @param isVariabilityEnabled a boolean true to enable the stochasticity of the model
	 */
	public MathildeMortalityPredictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);	
	}

	/**
	 * Constructor.
	 * @param isVariabilityEnabled
	 */
	MathildeMortalityPredictor(boolean isParameterVariabilityEnabled, boolean isRandomEffectVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParameterVariabilityEnabled, isRandomEffectVariabilityEnabled, isResidualVariabilityEnabled);	
		subModules = new HashMap<Integer, MathildeMortalitySubModule>();
		init();
		oXVector = new Matrix(1,numberOfParameters);
		linkFunction = new EmbeddedLinkFunction(Type.CLogLog, new InternalMathematicalFunction());
		linkFunction.setVariableValue(0, 1d);
		ghq = new GaussHermiteQuadrature(NumberOfPoints.N15);		
	}

	
	@Override
	protected void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_MathildeMortalityBeta.csv";
			String omegaFilename = path + "0_MathildeMortalityOmega.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1,betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);		
			
			numberOfParameters = -1;
			int numberOfExcludedGroups = 10;		
			
			for (int excludedGroup = 0; excludedGroup <= numberOfExcludedGroups; excludedGroup++) {			//
				Matrix betaPrelim = betaMap.get(excludedGroup);
				if (numberOfParameters == -1) {
					numberOfParameters = betaPrelim.m_iRows - 1;
				}
				Matrix defaultBetaMean = betaPrelim.getSubMatrix(0, numberOfParameters - 1, 0, 0);
				SymmetricMatrix randomEffectVariance = SymmetricMatrix.convertToSymmetricIfPossible(
						betaPrelim.getSubMatrix(numberOfParameters, numberOfParameters, 0, 0));
				SymmetricMatrix omega = SymmetricMatrix.convertToSymmetricIfPossible(
						omegaMap.get(excludedGroup).squareSym().getSubMatrix(0, numberOfParameters - 1, 0, numberOfParameters - 1));		
				MathildeMortalitySubModule subModule = new MathildeMortalitySubModule(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
				subModule.setParameterEstimates(new ModelParameterEstimates(defaultBetaMean, omega));
				subModule.setDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT, new GaussianEstimate(new Matrix(randomEffectVariance.m_iRows,1), randomEffectVariance));
				subModules.put(excludedGroup, subModule);
			}
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
		
		oXVector.setValueAt(0, pointer, 1d);
		pointer++;
		
		oXVector.setSubMatrix(species.getShortDummyVariable(), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;

		oXVector.setValueAt(0, pointer, dbh);
		pointer++;

		oXVector.setValueAt(0, pointer, logDbh * gr17);
		pointer++;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(logDbh), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;
		
		oXVector.setValueAt(0, pointer, bal42);
		pointer++;

		oXVector.setValueAt(0, pointer, bal22 * gr17);
		pointer++;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(bal22), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;

		pointer++; // to skip b14
		
		oXVector.setValueAt(0, pointer, upcomingDrought);
		pointer++;
		
		oXVector.setValueAt(0, pointer, upcomingCut * gr17);
		pointer++;

		oXVector.setSubMatrix(species.getShortDummyVariable().scalarMultiply(upcomingCut), 0, pointer);
		pointer += species.getShortDummyVariable().m_iCols;
		
		oXVector.setValueAt(0, pointer, Math.log(stand.getGrowthStepLengthYr()));
		pointer++;
		
		double result = oXVector.multiply(beta).getValueAt(0, 0);
		return result;
	}
	
	@Override
	public synchronized double predictEventProbability(MathildeMortalityStand stand, MathildeTree tree, Map<String, Object> parms) {
		boolean windstormDisabledOverride = false;
		if (parms != null && parms.containsKey(ParmWindstormDisabledOverride)) {
			windstormDisabledOverride = (Boolean) parms.get(ParmWindstormDisabledOverride);
		}
		double upcomingWindstorm = 0d;
		if (stand.isAWindstormGoingToOccur() && !windstormDisabledOverride) {
			upcomingWindstorm = 1d;
		} 
		
		MathildeMortalitySubModule subModule;
		if (parms != null && parms.containsKey(ParmSubmoduleFromCrossValidation)) {
			int subModuleId = (Integer) parms.get(ParmSubmoduleFromCrossValidation);
			subModule = subModules.get(subModuleId);
			if (subModule == null) {
				throw new InvalidParameterException("The integer in the parms parameter is not valid!");
			} 
		} else {
			subModule = subModules.get(0);
		}
		
		Matrix beta = subModule.getParameters(stand);
		linkFunction.setVariableValue(1, upcomingWindstorm);
		
		double pred = getFixedEffectOnlyPrediction(beta, stand, tree);
		linkFunction.setParameterValue(0, pred);

		double prob;
		linkFunction.setParameterValue(1, beta.getValueAt(14, 0));
		if (isRandomEffectsVariabilityEnabled && stand.isAWindstormGoingToOccur()) {	// no need to draw a random effect if there is no windstorm
			IntervalNestedInPlotDefinition interval = getIntervalNestedInPlotDefinition(stand, stand.getDateYr());
			Matrix randomEffects = subModule.getRandomEffects(interval);
			linkFunction.setParameterValue(2, randomEffects.getValueAt(0, 0));
			prob = linkFunction.getValue();
		} else {
			linkFunction.setParameterValue(2, 0d);		// random effect arbitrarily set to 0
			if (stand.isAWindstormGoingToOccur() && isGaussianQuadratureEnabled) {
//				List<Integer> parameterIndices = new ArrayList<Integer>();
//				parameterIndices.add(2);
				linkFunction.standardDeviation = subModule.getDefaultRandomEffects(HierarchicalLevel.INTERVAL_NESTED_IN_PLOT).getDistribution().getStandardDeviation().getValueAt(0, 0);
				prob = ghq.getIntegralApproximation(linkFunction, IndexParameterToBeIntegrated, true);
			} else {									// no need to evaluate the quadrature when there is no windstorm
				prob = linkFunction.getValue();
			}
		}
		return prob;
	}

	
	
	/**
	 * This method returns either a boolean if isResidualVariabilityEnabled was set to true
	 * or the probability otherwise.
	 * @param stand a S-derived instance
	 * @param tree a T-derived instance
	 * @param parms some additional parameters
	 * @return a Boolean or a double
	 */
	@Override
	public Object predictEvent(MathildeMortalityStand stand, MathildeTree tree, Map<String, Object> parms) {
		double eventProbability = predictEventProbability(stand, tree, parms);
		if (eventProbability < 0 || eventProbability > 1) {
			return null;
		} else if (isResidualVariabilityEnabled) {
			double residualError = StatisticalUtility.getRandom().nextDouble();
			if (residualError < eventProbability) {
				if (stand.isAWindstormGoingToOccur()) {
					double eventProbabilityWithoutWindstorm = predictEventProbability(stand, tree, ParmsToDisableWindstorm); // to disable the windstorm
					if (StatisticalUtility.getRandom().nextDouble() < eventProbabilityWithoutWindstorm/eventProbability) {
						return StatusClass.dead;
					} else {
						return StatusClass.windfall;
					}
				} else {
					return StatusClass.dead;
				}
			} else {
				return StatusClass.alive;
			}
		} else {
			return eventProbability;
		}
	}

//	@Override
//	public void clearDeviates() {
//		for (MathildeMortalitySubModule p : subModules.values()) {
//			p.clearDeviates();
//		}
//	}

	
//	public static void main (String[] args) {
//		new MathildeMortalityPredictor(false); 
//	}
}

