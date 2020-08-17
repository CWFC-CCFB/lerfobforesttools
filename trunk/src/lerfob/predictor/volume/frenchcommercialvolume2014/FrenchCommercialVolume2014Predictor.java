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
package lerfob.predictor.volume.frenchcommercialvolume2014;

import repicea.math.Matrix;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaPredictor;
import repicea.simulation.SASParameterEstimates;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.util.ObjectUtility;

/**
 * The FrenchCommercialVolume2014Predictor class implements a model of volume for 
 * individual trees. It predicts the commercial volume from the stump height to a
 * small diameter of 7 cm without eventual branches. It refers to "volume bois fort 
 * tige" in French. It is the OVER bark volume.
 * @author Mathieu Fortin - 2014
 */
@SuppressWarnings("serial")
public final class FrenchCommercialVolume2014Predictor extends REpiceaPredictor {

	/**
	 * General constructor.
	 * @param isVariabilityEnabled a boolean (true: stochastic mode, false: deterministic mode)
	 */
	public FrenchCommercialVolume2014Predictor(boolean isVariabilityEnabled) {
		super(isVariabilityEnabled, false, isVariabilityEnabled);	 // no random effect
		init();
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
	}
	
	/**
	 * Default constructor for deterministic mode.
	 */
	public FrenchCommercialVolume2014Predictor() {
		this(false);
	}
	
	@Override
	protected final void init() {
		String path = ObjectUtility.getRelativePackagePath(getClass());
		String betaFilename = path + "0_beta.csv";
		String omegaFilename = path + "0_omega.csv";
		String covparmsFilename = path + "0_covparms.csv";
		
		try {
			Matrix beta = ParameterLoader.loadVectorFromFile(betaFilename).get();
			Matrix omega = ParameterLoader.loadVectorFromFile(omegaFilename).get().squareSym();
			Matrix covparms = ParameterLoader.loadVectorFromFile(covparmsFilename).get().matrixDiagonal();
			setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(covparms));
			setParameterEstimates(new SASParameterEstimates(beta, omega));
		} catch (Exception e) {
			System.out.println("Unable to load parameters of the FrenchCommercialVolume2014Predictor class");
		}
	}

	/**
	 * This method return the over-bark volume estimate for an individual tree. 
	 * The stochastic implementation is handled through the general constructor.
	 * 
	 * It is thread safe ONLY if the Monte Carlo Id of trees is not modified. Basically,
	 * there should be as many instances of FrenchCommercialVolume2014 as the number of 
	 * realizations. This is the implementation of the Extended type in the CAPSIS platform.
	 * 
	 * Another implementation is available through the predictTreeCommercialVolumeDm3(tree, id)
	 * method. It is specifically designed for FrenchCommercialVolume2014TreeImpl instance and 
	 * it assumes that the Monte Carlo id of these instances can change. It is thread safe.
	 * 
	 * The method returns 0 if the tree is smaller than 7 cm in dbh. It returns -1
	 * if tree height is not available.
	 * 
	 * @param tree a FrenchCommercialVolume2014Tree object
	 * @return the commercial volume (dm3)
	 */
	public double predictTreeCommercialOverbarkVolumeDm3(FrenchCommercialVolume2014Tree tree) {
		double dbhCm = tree.getDbhCm();
		if (dbhCm < 7) {	// means this is a sapling
			return 0d;
		}

		if (tree.getHeightM() == -1) {	// means the height has not been calculated
			return -1d;
		}

		double volume = fixedEffectPrediction(tree);
		double residualError = 0d;
		
		if (isResidualVariabilityEnabled) {
			residualError = tree.getFrenchCommercialVolume2014TreeSpecies().getDummy().multiply(getResidualError()).scalarMultiply(dbhCm).m_afData[0][0];
			volume += residualError;
		}
		if (volume < 0) {		
			volume = 0.1; 		// default value if the residual error is inconsistently large and yields a negative volume
		}
		return volume;
	}

	/**
	 * This method return the over-bark volume estimate for an individual tree. 
	 * The stochastic implementation is handled through the general constructor.
	 * 
	 * 
	 * It is specifically designed for FrenchCommercialVolume2014TreeImpl instance and 
	 * it assumes that the Monte Carlo id of these instances can change. It is thread safe.
	 * 
	 * The method returns 0 if the tree is smaller than 7 cm in dbh. It returns -1
	 * if tree height is not available.
	 * 
	 * @param tree a FrenchCommercialVolume2014TreeImpl object
	 * @param id the Monte Carlo id (the realization id)
	 * @return the commercial volume (dm3)
	 */
	public synchronized double predictTreeCommercialOverbarkVolumeDm3(FrenchCommercialVolume2014TreeImpl tree, int id) {
		tree.setMonteCarloId(id);
		return(predictTreeCommercialOverbarkVolumeDm3(tree));
	}
	
	
	private synchronized double fixedEffectPrediction(FrenchCommercialVolume2014Tree tree) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(tree);
		
		double hdratio = tree.getHeightM() / tree.getDbhCm();
		double cylinder = 3.14159 * tree.getSquaredDbhCm() * tree.getHeightM() * .025;		// the value of 3.14159 has been used in SAS and not the PI
		
		int pointer = 0;
		
		oXVector.m_afData[0][0] = hdratio;
		pointer++;
		
		Matrix tmp = tree.getFrenchCommercialVolume2014TreeSpecies().getDummy().scalarMultiply(cylinder);
		oXVector.setSubMatrix(tmp, 0, pointer);
		pointer += tmp.m_iCols;
		
		tmp = tree.getFrenchCommercialVolume2014TreeSpecies().getDummy().scalarMultiply(cylinder * tree.getDbhCm());
		oXVector.setSubMatrix(tmp, 0, pointer);
		pointer += tmp.m_iCols;
		
		return oXVector.multiply(beta).m_afData[0][0];
	}
 

}
