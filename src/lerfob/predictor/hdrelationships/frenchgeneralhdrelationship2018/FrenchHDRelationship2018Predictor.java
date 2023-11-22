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
package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.FertilityClassEmulator;
import lerfob.predictor.hdrelationships.FrenchHDRelationshipTree.FrenchHdSpecies;
import lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2018.FrenchHDRelationship2018ClimateGenerator.FrenchHDClimateVariableMap;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.REpiceaPredictorListener;
import repicea.simulation.SASParameterEstimates;
import repicea.simulation.climate.REpiceaClimateChangeGenerator;
import repicea.simulation.climate.REpiceaClimateChangeTrend;
import repicea.simulation.climate.REpiceaClimateVariableMap;
import repicea.simulation.covariateproviders.plotlevel.GeographicalCoordinatesProvider;
import repicea.simulation.hdrelationships.HeightPredictor;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;


/**
 * This class implements the general height-diameter relationships for the French National Forest Inventory. 
 * The 2018 version has been updated with climate variables. More specifically, the mean temperature and mean 
 * precipitation of the growth season are now explanatory variables for most species.
 * @author Mathieu Fortin - December 2017
 * 
 * @see <a href=https://link.springer.com/article/10.1007/s13595-018-0784-9> Fortin, M., R. Van Couwenberghe, V. Perez 
 * and C. Piedallu. 2019. Evidence of climate effects on the height-diameter relationships of tree species. Annals of 
 * Forest Science 76: 1. 
 * </a>
 */
public final class FrenchHDRelationship2018Predictor extends REpiceaPredictor implements FertilityClassEmulator,
																						HeightPredictor<FrenchHDRelationship2018Plot, FrenchHDRelationship2018Tree> {

	private static final long serialVersionUID = -8769528746292724237L;
	
	
	private final Map<FrenchHdSpecies, FrenchHDRelationship2018InternalPredictor> predictorMap;
		
	private final Map<String, FrenchHDClimateVariableMap> originalClimateVariableMap;
	private final FrenchHDRelationship2018ClimateGenerator climateGenerator;
	private REpiceaClimateChangeGenerator<? extends GeographicalCoordinatesProvider> climateChangeGenerator; 
	private Map<Integer, REpiceaClimateChangeTrend> climateTrendMap;

	/**
	 * General constructor for all combinations of uncertainty sources.
	 * @param isVariabilityEnabled a boolean that enables the variability at the parameter level
	 */
	public FrenchHDRelationship2018Predictor(boolean isVariabilityEnabled) {
		this(isVariabilityEnabled, isVariabilityEnabled, isVariabilityEnabled);
	}

	FrenchHDRelationship2018Predictor(boolean isParameterVariabilityEnabled, boolean isRandomEffectVariablityEnabled, boolean isResidualErrorVariabilityEnabled) {
		super(isParameterVariabilityEnabled, isRandomEffectVariablityEnabled, isResidualErrorVariabilityEnabled);
		predictorMap = new HashMap<FrenchHdSpecies, FrenchHDRelationship2018InternalPredictor>();
		originalClimateVariableMap = new HashMap<String, FrenchHDClimateVariableMap>();
		climateGenerator = new FrenchHDRelationship2018ClimateGenerator();
		init();
	}
	
	/**
	 * Default constructor with all sources of uncertainty disabled.
	 */
	public FrenchHDRelationship2018Predictor() {
		this(false);
	}

	
	REpiceaClimateVariableMap getNearestClimatePoint(FrenchHDRelationship2018Plot stand) { 
		FrenchHDClimateVariableMap cp;
		if (originalClimateVariableMap.containsKey(stand.getSubjectId())) {
			cp = originalClimateVariableMap.get(stand.getSubjectId());
		} else {
			cp = climateGenerator.getClimateVariables(stand);
			if (cp == null) {
				throw new InvalidParameterException("The geographical coordinates are not located in France!");
			} else {
				originalClimateVariableMap.put(stand.getSubjectId(), cp);
			}
		}
		if (climateChangeGenerator == null) {
			return cp;
		} else {
			REpiceaClimateChangeTrend trend =  getClimateTrend(stand);
			REpiceaClimateVariableMap updatedClimateVariableMap = cp.getUpdatedClimateVariableMap(trend, stand.getDateYr()); // TODO this object could be stored somewhere to avoid reconstructing it every time 
			return updatedClimateVariableMap;
		}
	}
	
	
	private synchronized REpiceaClimateChangeTrend getClimateTrend(FrenchHDRelationship2018Plot stand) {
		int id = stand.getMonteCarloRealizationId();
		Map<Integer, REpiceaClimateChangeTrend> oMap = getClimateTrendMap();
		if (!oMap.containsKey(id)) {
			oMap.put(id, climateChangeGenerator.getClimateTrendForThisStand(stand));
		} 
		return oMap.get(id);
	}

	private Map<Integer, REpiceaClimateChangeTrend> getClimateTrendMap() {
		if (climateTrendMap == null) {
			climateTrendMap = new HashMap<Integer, REpiceaClimateChangeTrend>();
		}
		return climateTrendMap;
	}

	@Override
	public void emulateFertilityClass(FertilityClass fertilityClass) {
		for (FrenchHDRelationship2018InternalPredictor internalPredictor : getInternalPredictorMap().values()) {
			internalPredictor.emulateFertilityClass(fertilityClass);
		}
	}

	@Override
	protected final void init() {
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
				FrenchHDRelationship2018InternalPredictor internalPredictor = new FrenchHDRelationship2018InternalPredictor(isParametersVariabilityEnabled,
						isRandomEffectsVariabilityEnabled,
						isResidualVariabilityEnabled,
						species,
						this);
				int index = species.getIndex();
				
				Matrix mean = betaMap.get(index);
				SymmetricMatrix variance = omegaMap.get(index).squareSym();
				ModelParameterEstimates defaultBeta = new SASParameterEstimates(mean, variance);
				internalPredictor.setParameterEstimates(defaultBeta);
				
				Matrix covparms = covparmMap.get(index);
				
				SymmetricMatrix matrixG = SymmetricMatrix.convertToSymmetricIfPossible(covparms.getSubMatrix(0, 0, 0, 0));
				Matrix defaultRandomEffectsMean = new Matrix(1, 1);
				internalPredictor.setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(defaultRandomEffectsMean, matrixG));
				
				SymmetricMatrix residualVariance = SymmetricMatrix.convertToSymmetricIfPossible(covparms.getSubMatrix(1, 1, 0, 0));
				internalPredictor.setResidualVariance(residualVariance);
				
				internalPredictor.setEffectList(effectList.get(index));
				getInternalPredictorMap().put(species, internalPredictor);
			}
		} catch (Exception e) {
			System.out.println("GeneralHDRelation Class : Unable to initialize the general height-diameter relationship");
		}
	}
	
	@Override
	public double predictHeightM(FrenchHDRelationship2018Plot stand, FrenchHDRelationship2018Tree tree) {
		FrenchHDRelationship2018InternalPredictor internalPred = getInternalPredictorMap().get(tree.getFrenchHDTreeSpecies());
		double prediction = internalPred.predictHeightM(stand, tree);
		return prediction;
	}	

	/**
	 * For testing purpose only
	 * @param stand the Stand instance
	 * @param tree the Tree instance
	 * @return an Estimate instance
	 */
	protected Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> getBlups(FrenchHDRelationship2018Plot stand, FrenchHDRelationship2018Tree tree) {
		FrenchHDRelationship2018InternalPredictor internalPred = getInternalPredictorMap().get(tree.getFrenchHDTreeSpecies());
		return internalPred.getBlupsForThisSubject(stand);
	}
	
	@Override
	public void addModelBasedSimulatorListener(REpiceaPredictorListener listener) {
		for (FrenchHDRelationship2018InternalPredictor internalPredictor : getInternalPredictorMap().values()) {
			internalPredictor.addModelBasedSimulatorListener(listener);
		}
	}
	
	@Override
	public void removeModelBasedSimulatorListener(REpiceaPredictorListener listener) {
		for (FrenchHDRelationship2018InternalPredictor internalPredictor : getInternalPredictorMap().values()) {
			internalPredictor.removeModelBasedSimulatorListener(listener);
		}
	}
	
	Map<FrenchHdSpecies, FrenchHDRelationship2018InternalPredictor> getInternalPredictorMap() {
		return predictorMap;
	}

	/**
	 * Sets a climate change generator that will impact the internal climate generator for the 
	 * HD relationship.
	 * @param generator a REpiceaClimateChangeGenerator instance
	 */
	public void setClimateChangeGenerator(REpiceaClimateChangeGenerator<? extends GeographicalCoordinatesProvider> generator) {
		this.climateChangeGenerator = generator;
	}
	
	
	
}