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

import java.io.Serializable;

import lerfob.carbonbalancetool.DecayFunction.ParameterID;
import lerfob.carbonbalancetool.DecayFunction.VariableID;
import repicea.math.AbstractMathematicalFunction;

/**
 * The decay function interface is designated for lifetime decreasing functions. This interface serves 
 * to actualize the EndProduct instance throughout their useful lifetime. 
 * @author Mathieu Fortin - October 2010
 */
@SuppressWarnings("serial")
public abstract class DecayFunction extends AbstractMathematicalFunction<ParameterID, Double, VariableID, Double> implements Serializable {

	public static enum ParameterID {Lambda}
	public static enum VariableID {X}

	protected DecayFunction() {}
	
	@Override
	public abstract Double getValue();
	
	
	/**
	 * This method returns the value of the infinite integral of the decay function.
	 * @return the value of the infinite integral (double)
	 */
	public abstract double getInfiniteIntegral();

}
