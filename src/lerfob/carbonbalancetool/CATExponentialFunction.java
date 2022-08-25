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
import repicea.math.SymmetricMatrix;



/**
 * This class provides an exponential function to act as 
 * decay function in the calculation of end product lifetime.
 * @author Mathieu Fortin - July 2010
 */
public class CATExponentialFunction extends CATDecayFunction {

	private static final long serialVersionUID = 20130128L;

	@Override
	public Double getValue() {
		return Math.exp(- getX() / getLambda());
	}

	private double getX() {return getVariableValue(0);}
	private double getLambda() {return getParameterValue(0);}
	
	
	@Override
	public double getInfiniteIntegral() {
		return getLambda();
	}

	@Override
	public Matrix getGradient() {
		Matrix gradient = new Matrix(ParameterID.values().length, 1);
		gradient.setValueAt(0, 0, getValue() * getX() / (getLambda() * getLambda()));
		return gradient;
	}

	@Override
	public SymmetricMatrix getHessian() {
		SymmetricMatrix hessian = new SymmetricMatrix(ParameterID.values().length);
		double derParam = getX() / (getLambda() * getLambda());
		double functionValue = getValue();
		double der2Param = - 2 * getX() / (getLambda() * getLambda() * getLambda());
		double result = functionValue * derParam * derParam + functionValue * der2Param;
		hessian.setValueAt(0, 0, result); 
		return hessian;
	}
}


