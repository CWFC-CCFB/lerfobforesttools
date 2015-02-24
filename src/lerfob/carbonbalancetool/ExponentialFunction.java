/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.carbonbalancetool;

import repicea.math.Matrix;



/**
 * This class provides an exponential function to act as 
 * decay function in the calculation of end product lifetime.
 * @author Mathieu Fortin - July 2010
 */
public class ExponentialFunction extends DecayFunction {

	private static final long serialVersionUID = 20130128L;

	@Override
	public Double getValue() {
		return Math.exp(- getVariableValue(VariableID.X) / getParameterValue(ParameterID.Lambda));
	}

	@Override
	public double getInfiniteIntegral() {
		return getParameterValue(ParameterID.Lambda);
	}

	@Override
	public Matrix getGradient() {
		Matrix gradient = new Matrix(ParameterID.values().length, 1);
		gradient.m_afData[0][0] = getValue() * getVariableValue(VariableID.X) / (getParameterValue(ParameterID.Lambda) * getParameterValue(ParameterID.Lambda));
		return gradient;
	}

	@Override
	public Matrix getHessian() {
		Matrix hessian = new Matrix(ParameterID.values().length, ParameterID.values().length);
		double derParam = getVariableValue(VariableID.X) / (getParameterValue(ParameterID.Lambda) * getParameterValue(ParameterID.Lambda));
		double functionValue = getValue();
		double der2Param = - 2 * getVariableValue(VariableID.X) / (getParameterValue(ParameterID.Lambda) * getParameterValue(ParameterID.Lambda) * getParameterValue(ParameterID.Lambda));
		double result = functionValue * derParam * derParam + functionValue * der2Param;
		hessian.m_afData[0][0] = result; 
		return hessian;
	}
}


