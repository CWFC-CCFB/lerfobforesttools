/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.nutrientmodel;

import repicea.math.Matrix;
import repicea.stats.estimates.GaussianEstimate;

/**
 * The NutrientSubversionS is the concentration model for sulfur. 
 * @author Mathieu Fortin - March 2013
 */
class NutrientSubversionS extends NutrientConcentrationSubversionModel {

	private static final long serialVersionUID = 20130325L;

	protected NutrientSubversionS(NutrientConcentrationPredictionModel owner, boolean isParametersVariabilityEnabled, boolean isResidualVariabilityEnabled) {
		super(owner, isParametersVariabilityEnabled, isResidualVariabilityEnabled);
		init();
	}

	@Override
	protected void init() {
		Matrix betaReference = new Matrix(6,1);
		betaReference.m_afData[0][0] = 0.114688;
		betaReference.m_afData[1][0] = 0.147543;
		betaReference.m_afData[2][0] = -0.31139;
		betaReference.m_afData[3][0] = 0.549526;
		betaReference.m_afData[4][0] = -0.00731;
		
		defaultBeta = new GaussianEstimate(betaReference, null);
		
		// TODO implement the residual errors
				
	}

	@Override
	protected Matrix getConcentrations(double midDiameterCm, double barkRatio) {
		Matrix y = new Matrix(3,1);

		Matrix beta = defaultBeta.getMean();

		y.m_afData[0][0] = beta.m_afData[0][0] + beta.m_afData[1][0] * Math.exp(beta.m_afData[2][0] * midDiameterCm);
		y.m_afData[1][0] = beta.m_afData[3][0] * Math.exp(beta.m_afData[4][0] * midDiameterCm);
		y.m_afData[2][0] = (1 - barkRatio) * y.m_afData[0][0] + barkRatio * y.m_afData[1][0];

		if (isResidualVariabilityEnabled) {
			Matrix errors = getResidualError();
			y = y.add(errors);
		}
		
		return y;
	}
	
	
}
