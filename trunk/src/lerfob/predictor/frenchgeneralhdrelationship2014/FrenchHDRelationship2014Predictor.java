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

import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.frenchgeneralhdrelationship2014.FrenchHDRelationship2014Tree.FrenchHdSpecies;
import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;


/**
 * This class implements the general height-diameter relationships for the French National Forest Inventory.
 * @author Mathieu Fortin - June 2012
 */
@SuppressWarnings("serial")
public final class FrenchHDRelationship2014Predictor extends ModelBasedSimulator {

	public static enum SiteIndexClass {
		Unknown,
		I,
		II,
		III;
	}
	
	private final Map<FrenchHdSpecies, FrenchHDRelationship2014InternalPredictor> predictorMap;
		
	
	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isParametersVariabilityEnabled a boolean that enables the variability at the parameter level
	 * @param isRandomEffectsVariabilityEnabled a boolean that enables the variability at the random effect level
	 * @param isResidualVariabilityEnabled a boolean that enables the variability at the tree level
	 */
	public FrenchHDRelationship2014Predictor(boolean isParametersVariabilityEnabled, boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		predictorMap = new HashMap<FrenchHdSpecies, FrenchHDRelationship2014InternalPredictor>();
		init();
	}

	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public FrenchHDRelationship2014Predictor() {
		this(false, false, false);
	}

	/**
	 * This method allows to tweak the plot random effect in order to reproduce a sort of site index.
	 * @param siteIndexClass a SiteIndexClass enum
	 */
	public void emulateSiteIndexClassForThisSpecies(SiteIndexClass siteIndexClass, FrenchHdSpecies species) {
		this.predictorMap.get(species).emulateSiteIndexClassForThisSpecies(siteIndexClass);
	}

//	/**
//	 * This method set the random effect predictor for emulation of site index class.
//	 * @param randomEffectPredictor
//	 */
//	private synchronized void setPlotBlups(MonteCarloSimulationCompliantObject plot) {
//		Map<HierarchicalLevel, Map<Integer, GaussianEstimate>> blupsLibrary = getSubModule().getBlupsLibrary();
//		if (!blupsLibrary.containsKey(HierarchicalLevel.Plot)) {
//			blupsLibrary.put(HierarchicalLevel.Plot, new HashMap<Integer, GaussianEstimate>());
//		}
//		Map<Integer, GaussianEstimate> innerMap = blupsLibrary.get(HierarchicalLevel.Plot);
//		if (innerMap.containsKey(plot.getSubjectId())) {
//			innerMap.put(plot.getSubjectId(), siteIndexClass.getEstimate());
//		}
//	}

//	private FrenchHDRelationship2014InternalPredictor getSpeciesPreSubModule(int subModuleVersion) {
//		return predictorMap.get(subModuleVersion);
//	}
//
//	private FrenchHDRelationship2014InternalPredictor getSubModule() {
//		return getSubModule(0);
//	}
	

	private void init() {
		try {
			String path = ObjectUtility.getRelativePackagePath(getClass());
			String betaFilename = path + "0_beta.csv";
			String omegaFilename = path + "0_omega.csv";
			String covparmsFilename = path + "0_covparms.csv";
			String effectListFilename = path + "0_effectlist.csv";

			ParameterMap betaMap = ParameterLoader.loadVectorFromFile(1, betaFilename);
			ParameterMap omegaMap = ParameterLoader.loadVectorFromFile(1, omegaFilename);
			ParameterMap covparmMap = ParameterLoader.loadVectorFromFile(1, covparmsFilename);
			ParameterMap effectList = ParameterLoader.loadVectorFromFile(1, effectListFilename);

			for (FrenchHdSpecies species : FrenchHdSpecies.values()) {
				FrenchHDRelationship2014InternalPredictor internalPredictor = new FrenchHDRelationship2014InternalPredictor(isParametersVariabilityEnabled, 
						isRandomEffectsVariabilityEnabled, 
						isResidualVariabilityEnabled,
						species);
				int index = species.ordinal() + 1;
				
				Matrix mean = betaMap.get(index);
				Matrix variance = omegaMap.get(index).squareSym();
				GaussianEstimate defaultBeta = new SASParameterEstimate(mean, variance);
				internalPredictor.setDefaultBeta(defaultBeta);
				
				Matrix covparms = covparmMap.get(index);
				
				Matrix matrixG = covparms.getSubMatrix(0, 0, 0, 0);
				Matrix defaultRandomEffectsMean = new Matrix(1, 1);
				internalPredictor.setDefaultRandomEffects(HierarchicalLevel.Plot, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
				
				Matrix residualVariance = covparms.getSubMatrix(1, 1, 0, 0);
				internalPredictor.setResidualVariance(new GaussianErrorTermEstimate(residualVariance));
				
				internalPredictor.setEffectList(effectList.get(index));
				predictorMap.put(species, internalPredictor);
			}
		} catch (Exception e) {
			System.out.println("GeneralHDRelation Class : Unable to initialize the general height-diameter relationship");
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
	public double predictHeightM(FrenchHDRelationship2014Stand stand, FrenchHDRelationship2014Tree tree) {
		FrenchHDRelationship2014InternalPredictor internalPred = predictorMap.get(tree.getFrenchHDTreeSpecies());
		double prediction = internalPred.predictHeight(stand, tree);
		return prediction;
	}	


	
	/**
	 * For testing purpose only
	 * @param stand
	 */
	protected Matrix getBlups(FrenchHDRelationship2014Stand stand, FrenchHDRelationship2014Tree tree) {
		FrenchHDRelationship2014InternalPredictor internalPred = predictorMap.get(tree.getFrenchHDTreeSpecies());
		return internalPred.getBlups(stand);
	}
	
}