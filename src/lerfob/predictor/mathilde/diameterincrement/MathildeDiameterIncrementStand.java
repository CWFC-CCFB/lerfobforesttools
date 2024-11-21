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
package lerfob.predictor.mathilde.diameterincrement;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.plotlevel.InterventionPlannedProvider;
import repicea.simulation.covariateproviders.plotlevel.MeanSeasonalTemperatureCelsiusProvider;

/**
 * This interface ensures that the Stand instance is compatible with the MathildeDiameterIncrementPredictor.
 * @author Mathieu Fortin - June 2013
 */
public interface MathildeDiameterIncrementStand extends MonteCarloSimulationCompliantObject, 
										GrowthStepLengthYrProvider, 
										BasalAreaM2HaProvider,
										InterventionPlannedProvider,
										MeanSeasonalTemperatureCelsiusProvider {

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.PLOT;
	}

	
//	/**
//	 * This method returns the monthly mean temperature above 6 degrees Celsius.
//	 * @return a double
//	 */
//	public double getMeanAnnualTempAbove6C();

}
