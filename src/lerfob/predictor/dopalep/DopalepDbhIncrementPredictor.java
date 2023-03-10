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
package lerfob.predictor.dopalep;

import java.io.IOException;

import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.simulation.GrowthModel;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.ModelParameterEstimates;
import repicea.simulation.ParameterLoader;
import repicea.simulation.REpiceaPredictor;
import repicea.stats.estimates.GaussianErrorTermEstimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

@SuppressWarnings("serial")
public class DopalepDbhIncrementPredictor extends REpiceaPredictor implements GrowthModel<DopalepPlot, DopalepTree> {

	public DopalepDbhIncrementPredictor(boolean isParametersVariabilityEnabled,	boolean isRandomEffectsVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(isParametersVariabilityEnabled, isRandomEffectsVariabilityEnabled, isResidualVariabilityEnabled);
		init();
	}


	@Override
	protected void init() {
		String betaFilename = ObjectUtility.getRelativePackagePath(getClass()) + "0_DopalepDbhIncBeta.csv";
		String omegaFilename = ObjectUtility.getRelativePackagePath(getClass()) + "0_DopalepDbhIncOmega.csv";
		String covParmsFilename = ObjectUtility.getRelativePackagePath(getClass()) + "0_DopalepDbhIncCovParms.csv";
		
		try {
			Matrix beta = ParameterLoader.loadVectorFromFile(betaFilename).get();
			SymmetricMatrix omega = SymmetricMatrix.convertToSymmetricIfPossible(
					ParameterLoader.loadMatrixFromFile(omegaFilename));
			setParameterEstimates(new ModelParameterEstimates(beta, omega));
			oXVector = new Matrix(1, getParameterEstimates().getMean().m_iRows);
			
			Matrix covParms = ParameterLoader.loadVectorFromFile(covParmsFilename).get();
			SymmetricMatrix randomEffectVariance = SymmetricMatrix.convertToSymmetricIfPossible(
					covParms.getSubMatrix(0, 0, 0, 0));
			setDefaultRandomEffects(HierarchicalLevel.PLOT, new GaussianEstimate(new Matrix(1,1), randomEffectVariance));

			SymmetricMatrix sigma2 = SymmetricMatrix.convertToSymmetricIfPossible(
					covParms.getSubMatrix(1, 1, 0, 0));
			setDefaultResidualError(ErrorTermGroup.Default, new GaussianErrorTermEstimate(sigma2));
						
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Unable to read the parameters!");
		}
	}

	
	
	
	public static void main(String[] args) {
		new DopalepDbhIncrementPredictor(false, false, false);
	}


	@Override
	public double predictGrowth(DopalepPlot stand, DopalepTree tree, Object... parms) {
		double pred = getFixedEffectOnlyPrediction(stand, tree);
		
		double biasCorrection = 0d;
		
		if (isRandomEffectsVariabilityEnabled) {
			pred += getRandomEffectsForThisSubject(stand).getValueAt(0, 0);
		} else {
			biasCorrection += getDefaultRandomEffects(HierarchicalLevel.PLOT).getVariance().getValueAt(0, 0);
		}
		
		if (isResidualVariabilityEnabled) {
			pred += getResidualError().getValueAt(0, 0);
		} else {
			biasCorrection += getDefaultResidualError(ErrorTermGroup.Default).getVariance().getValueAt(0, 0);
		}
		
		double backtransformedPred = pred * pred + biasCorrection - 1;
		
		if (backtransformedPred < 0) {
			backtransformedPred = 0;
		}
		
		return backtransformedPred;
	}


	private double getFixedEffectOnlyPrediction(Matrix currentBeta, DopalepPlot stand, DopalepTree tree) {
		oXVector.resetMatrix();
		oXVector.setValueAt(0,0, 1d);
		oXVector.setValueAt(0, 1, tree.getBasalAreaLargerThanSubjectM2Ha());
		oXVector.setValueAt(0, 2, tree.getBasalAreaM2HaOtherTrees());
		oXVector.setValueAt(0, 3, tree.getDbhCm());
		oXVector.setValueAt(0, 4, tree.getDbhCm() * tree.getDbhCm());
		
		return oXVector.multiply(currentBeta).getValueAt(0, 0);
	}

	protected double getFixedEffectOnlyPrediction(DopalepPlot stand, DopalepTree tree) {
		return getFixedEffectOnlyPrediction(getParametersForThisRealization(stand), stand, tree);
	}

}
