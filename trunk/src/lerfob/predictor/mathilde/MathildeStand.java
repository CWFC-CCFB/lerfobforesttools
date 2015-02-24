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
package lerfob.predictor.mathilde;

import repicea.simulation.MonteCarloSimulationCompliantObject;

/**
 * This interface ensures that the Stand instance is compatible with the MathildeDiameterIncrementPredictor.
 * @author Mathieu Fortin - June 2013
 */
public interface MathildeStand extends MonteCarloSimulationCompliantObject {

	/**
	 * This method returns true if a cut was planned in the upcoming growth interval.
	 * @return a boolean
	 */
	public boolean isGoingToBeHarvested();
	
	
	/**
	 * This method returns the growth step length in years.
	 * @return a double
	 */
	public double getGrowthStepLengthYrs();
	
	
}
