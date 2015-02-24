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

import lerfob.predictor.mathilde.MathildeStand;
import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;

/**
 * This interface ensures the model can provide all the information required by the diameter increment submodel.
 * @author Mathieu Fortin - October 2013
 */
public interface MathildeDiameterIncrementStand extends MathildeStand, BasalAreaM2HaProvider {
	
	/**
	 * This method returns the monthly mean temperature above 6 degrees Celsius.
	 * @return a double
	 */
	public double getMeanAnnualTempAbove6C();

}
