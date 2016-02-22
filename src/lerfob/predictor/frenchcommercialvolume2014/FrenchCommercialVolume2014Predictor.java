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
package lerfob.predictor.frenchcommercialvolume2014;

import repicea.math.Matrix;
import repicea.simulation.ModelBasedSimulator;
import repicea.simulation.ParameterLoader;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public final class FrenchCommercialVolume2014Predictor extends ModelBasedSimulator {

	public FrenchCommercialVolume2014Predictor(boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);	 // no random effect
		init();
		oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
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
			setParameterEstimates(new SASParameterEstimate(beta, omega));
		} catch (Exception e) {
			System.out.println("Unable to load parameters of the FrenchCommercialVolume2014Predictor class");
		}
	}

	/**
	 * This method return the volume estimate for an individual trees. 
	 * NOTE: Stochastic implementation is handled through the general constructor.
	 * The method returns 0 if the tree is smaller than 7 cm in dbh. It returns -1
	 * if the tree height has not been calculated.
	 * @param tree a TreeVolumable object
	 * @return the commercial volume (dm3)
	 */
	public double predictTreeCommercialVolumeDm3(FrenchCommercialVolume2014Tree tree) {
		double dbhCm = tree.getDbhCm();
		if (dbhCm < 7) {	// means this is a sapling
			return 0d;
		}

		if (tree.getHeightM() == -1) {	// means the height has not been calculated
			return -1d;
		}

		double volume = fixedEffectPrediction(tree);

		if (isResidualVariabilityEnabled) {
			double residualError = tree.getFrenchCommercialVolume2014TreeSpecies().getDummy().multiply(getResidualError()).scalarMultiply(dbhCm * dbhCm).m_afData[0][0];
			volume += residualError;
		}
		return volume;
	}

	private double fixedEffectPrediction(FrenchCommercialVolume2014Tree tree) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(tree);
		
		double hdratio = tree.getHeightM() / tree.getDbhCm();
//		double cylinder = Math.PI * tree.getSquaredDbhCm() * tree.getHeightM() * .025;
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
