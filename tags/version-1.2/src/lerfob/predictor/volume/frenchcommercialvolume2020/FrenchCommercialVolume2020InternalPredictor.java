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

import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.volume.frenchcommercialvolume2020.FrenchCommercialVolume2020Tree.FrenchCommercialVolume2020TreeSpecies;
import repicea.math.Matrix;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.StatisticalUtility;

@SuppressWarnings("serial")
final class FrenchCommercialVolume2020InternalPredictor extends REpiceaPredictor {

	final double resStdDev;
	final FrenchCommercialVolume2020TreeSpecies species;
	final List<Integer> effectList;
	
	FrenchCommercialVolume2020InternalPredictor(boolean isParametersVariabilityEnabled,	
			boolean isResidualVariabilityEnabled, 
			FrenchCommercialVolume2020TreeSpecies species,
			Matrix effects,
			ModelParameterEstimates parms, 
			double residualVariance) {
		super(isParametersVariabilityEnabled, false, isResidualVariabilityEnabled);
		this.species = species;
		setParameterEstimates(parms);
		this.resStdDev = Math.sqrt(residualVariance);
		effectList = new ArrayList<Integer>();
		for (int i = 0; i < effects.m_iRows; i++) {
			effectList.add(((Double) effects.getValueAt(i, 0)).intValue());
		}
		init();
	}

	@Override
	protected void init() {
		int nbParms = this.getParameterEstimates().getMean().m_iRows;
		oXVector = new Matrix(1, nbParms);
	}
	
	double predictTreeCommercialVolumeDm3(FrenchCommercialVolume2020Tree tree) {
		double dbhCm = tree.getDbhCm();
		if (dbhCm < 7.5) {	// means this is a sapling
			return 0d;
		}

		if (tree.getHeightM() == -1) {	// means the height has not been calculated
			return -1d;
		}

		double volume = fixedEffectPrediction(tree);
		
		if (isResidualVariabilityEnabled) {
			double residualError = StatisticalUtility.getRandom().nextGaussian() * resStdDev * tree.getSquaredDbhCm();
			volume += residualError;
		}
		if (volume < 0) {		
			volume = 0.1; 		// default value if the residual error is inconsistently large and yields a negative volume
		}
		return volume;
	}


	private synchronized double fixedEffectPrediction(FrenchCommercialVolume2020Tree tree) {
		oXVector.resetMatrix();
		Matrix beta = getParametersForThisRealization(tree);
		
		double hdratio = tree.getHeightM() / tree.getDbhCm();
		double cylinder = Math.PI * tree.getSquaredDbhCm() * tree.getHeightM() * .025;		
		
		int pointer = 0;

		for (Integer effectID : effectList) {

			switch(effectID) {
			case 1:
				oXVector.setValueAt(0, pointer, hdratio);
				pointer++;
				break;
			case 2:
				oXVector.setValueAt(0, pointer, cylinder);
				pointer++;
				break;
			case 3:
				oXVector.setValueAt(0, pointer, cylinder * tree.getDbhCm());
				pointer++;
				break;
			}
		}
		
		
		return oXVector.multiply(beta).getValueAt(0, 0);
	}

	double getVarianceOfTheMean(FrenchCommercialVolume2020TreeImpl tree) {
		Matrix omega = getParameterEstimates().getVariance();
		fixedEffectPrediction(tree);
		Matrix stdErrMat = oXVector.multiply(omega).multiply(oXVector.transpose());
		double varianceOfTheMean = stdErrMat.getValueAt(0, 0);
		return varianceOfTheMean;
	}

	double getPredVariance(FrenchCommercialVolume2020TreeImpl tree) {
		double varianceOfTheMean = this.getVarianceOfTheMean(tree);
		double resVariance = resStdDev * resStdDev * tree.getSquaredDbhCm() * tree.getSquaredDbhCm();
		return varianceOfTheMean + resVariance;
	}
	
}
