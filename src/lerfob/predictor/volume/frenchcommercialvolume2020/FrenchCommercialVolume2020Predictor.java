/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2020 Her Majesty the Queen in right of Canada
 * 		Mathieu Fortin for Canadian WoodFibre Centre,
 * 							Canadian Forest Service, 
 * 							Natural Resources Canada
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.predictor.volume.frenchcommercialvolume2020;

import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.volume.frenchcommercialvolume2020.FrenchCommercialVolume2020Tree.FrenchCommercialVolume2020TreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.simulation.REpiceaPredictor;
import repicea.util.ObjectUtility;

/**
 * The FrenchCommercialVolume2020Predictor class implements a model of volume for 
 * individual trees. It predicts the commercial volume from the base to a
 * small-end diameter of 7 cm without branches. It refers to "volume bois fort 
 * tige" in French.
 * 
 * <br>
 * <br>
 * 
 * This model is an update of a preliminary model fitted in 2014, which can be found in the
 * frenchcommercialvolume2014 package.
 * 
 * @author Mathieu Fortin - August 2020
 */
@SuppressWarnings("serial")
public final class FrenchCommercialVolume2020Predictor extends REpiceaPredictor {

	
	private final Map<FrenchCommercialVolume2020TreeSpecies, FrenchCommercialVolume2020InternalPredictor> internalPredictorMap;
	
	/**
	 * General constructor.
	 * @param isVariabilityEnabled a boolean (true: stochastic mode, false: deterministic mode)
	 */
	public FrenchCommercialVolume2020Predictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);	 // no random effect
		internalPredictorMap = new HashMap<FrenchCommercialVolume2020TreeSpecies, FrenchCommercialVolume2020InternalPredictor>();
		init();
	}
	
	/**
	 * Default constructor for deterministic mode.
	 */
	public FrenchCommercialVolume2020Predictor() {
		this(false);
	}
	
	@Override
	protected final void init() {
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String betaFilename = path + "0_beta.csv";
		String omegaFilename = path + "0_omega.csv";
		String covparmsFilename = path + "0_covparms.csv";
		String speciesEffectMatchFilename = path + "0_speciesEffectMatch.csv";
		
		try {
			ParameterMap beta = ParameterLoader.loadVectorFromFile(1,betaFilename);
			ParameterMap omega = ParameterLoader.loadVectorFromFile(1, omegaFilename);
			ParameterMap covparms = ParameterLoader.loadVectorFromFile(1, covparmsFilename);
			ParameterMap speciesEffectMatch = ParameterLoader.loadVectorFromFile(1, speciesEffectMatchFilename);
			for (FrenchCommercialVolume2020TreeSpecies species : FrenchCommercialVolume2020TreeSpecies.values()) {
				int index = species.ordinal() + 1;
				Matrix betaSpecies = beta.get(index);
				Matrix omegaSpecies = omega.get(index).squareSym();
				ModelParameterEstimates parms = new ModelParameterEstimates(betaSpecies, omegaSpecies);
				Matrix sem = speciesEffectMatch.get(index);
				double residualVariance = covparms.get(index).m_afData[0][0];
				FrenchCommercialVolume2020InternalPredictor predictor = new FrenchCommercialVolume2020InternalPredictor(isParametersVariabilityEnabled,
						isResidualVariabilityEnabled,
						species,
						sem,
						parms,
						residualVariance);
				internalPredictorMap.put(species, predictor);
			}
		} catch (Exception e) {
			System.out.println("Unable to load parameters of the FrenchCommercialVolume2014Predictor class");
		}
	}

	/**
	 * This method return the over-bark volume estimate for an individual tree. 
	 * The stochastic implementation is handled through the general constructor.
	 * <br>
	 * <br>
	 * It is thread safe ONLY if the Monte Carlo Id of trees is not modified. Basically,
	 * there should be as many instances of FrenchCommercialVolume2020 as the number of 
	 * realizations. This is the implementation of the Extended type in the CAPSIS platform.
	 * <br>
	 * <br>
	 * Another implementation is available through the predictTreeCommercialVolumeDm3(tree, id)
	 * method. It is specifically designed for FrenchCommercialVolume2020TreeImpl instance and 
	 * it assumes that the Monte Carlo id of these instances can change. It is thread safe.
	 * <br>
	 * <br>
	 * The method returns 0 if the tree is smaller than 7.5 cm in dbh. It returns -1
	 * if tree height is not available.
	 * 
	 * @param tree a FrenchCommercialVolume2020Tree object
	 * @return the commercial volume (dm3)
	 */
	public double predictTreeCommercialVolumeDm3(FrenchCommercialVolume2020Tree tree) {
		FrenchCommercialVolume2020InternalPredictor internalPred = internalPredictorMap.get(tree.getFrenchCommercialVolume2020TreeSpecies());
		return internalPred.predictTreeCommercialVolumeDm3(tree);
	}

	/*
	 * For test purpose.
	 */
	double getVarianceOfTheMean(FrenchCommercialVolume2020TreeImpl tree) {
		FrenchCommercialVolume2020InternalPredictor internalPred = internalPredictorMap.get(tree.getFrenchCommercialVolume2020TreeSpecies());
		return internalPred.getVarianceOfTheMean(tree);
	}

	/*
	 * For test purpose.
	 */
	double getPredVariance(FrenchCommercialVolume2020TreeImpl tree) {
		FrenchCommercialVolume2020InternalPredictor internalPred = internalPredictorMap.get(tree.getFrenchCommercialVolume2020TreeSpecies());
		return internalPred.getPredVariance(tree);
	}

	/**
	 * This method return the over-bark volume estimate for an individual tree. 
	 * The stochastic implementation is handled through the general constructor.
	 * <br> 
	 * <br> 
	 * It is specifically designed for FrenchCommercialVolume2020TreeImpl instance and 
	 * it assumes that the Monte Carlo id of these instances can change. It is thread safe.
	 * <br> 
	 * <br> 
	 * The method returns 0 if the tree is smaller than 7.5 cm in dbh. It returns -1
	 * if tree height is not available.
	 * 
	 * @param tree a FrenchCommercialVolume2020TreeImpl object
	 * @param id the Monte Carlo id (the realization id)
	 * @return the commercial volume (dm3)
	 */
	public synchronized double predictTreeCommercialVolumeDm3(FrenchCommercialVolume2020TreeImpl tree, int id) {
		tree.setMonteCarloId(id);
		return(predictTreeCommercialVolumeDm3(tree));
	}
	
 
	public static void main(String[] args) {
		new FrenchCommercialVolume2020Predictor();
	}

}
