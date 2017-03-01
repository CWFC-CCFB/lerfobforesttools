/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.carbonbalancetool.sensitivityanalysis;

import repicea.math.Matrix;
import repicea.stats.estimates.UniformEstimate;

@SuppressWarnings("serial")
public class CATUniformSensitivityAnalysisParameter extends CATSensitivityAnalysisParameter<UniformEstimate> {

	protected CATUniformSensitivityAnalysisParameter() {
		super(false);
		Matrix lowerBoundValue = new Matrix(1,1);
		lowerBoundValue.m_afData[0][0] = -1d;
		Matrix upperBoundValue = new Matrix(1,1);
		upperBoundValue.m_afData[0][0] = 1d;
		setParameterEstimates(new UniformEstimate(lowerBoundValue, upperBoundValue));
		setMultiplier(.1);
	}

//	@Override
//	protected void setMultiplier(double multiplier) {
//		super.setMultiplier(multiplier);
//		Matrix lowerBoundValue = new Matrix(1,1);
//		lowerBoundValue.m_afData[0][0] = 1 - multiplier;
//		getParameterEstimates().setLowerBoundValue(lowerBoundValue);
//		Matrix upperBoundValue = new Matrix(1,1);
//		upperBoundValue.m_afData[0][0] = 1 + multiplier;
//		getParameterEstimates().setUpperBoundValue(upperBoundValue);
//	}
	
//	@Override
//	protected Matrix getParameterValueForThisSubject(MonteCarloSimulationCompliantObject subject) {
//		return this.getParametersForThisRealization(subject);
//	}
}
