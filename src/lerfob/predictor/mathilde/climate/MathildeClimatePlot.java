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
package lerfob.predictor.mathilde.climate;

import java.util.List;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.DateYrProvider;
import repicea.simulation.covariateproviders.plotlevel.GeographicalCoordinatesProvider;
import repicea.simulation.covariateproviders.plotlevel.GrowthStepLengthYrProvider;

/**
 * This interface ensures that the Stand instance is compatible with the MathildeDiameterIncrementPredictor.
 * @author Mathieu Fortin - June 2013
 */
public interface MathildeClimatePlot extends MonteCarloSimulationCompliantObject, 
										DateYrProvider,
										GeographicalCoordinatesProvider,
										GrowthStepLengthYrProvider {

	@Override
	default public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.PLOT;
	}

	/**
	 * This method returns all the plots of the sample so that it is possible
	 * to generate the blups with respect to the reference stands in the 
	 * MathildeClimatePredictor class.
	 * @return a List of MathildeClimateStand which will be concatenated with the reference stands.
	 */
	public List<MathildeClimatePlot> getAllMathildeClimatePlots();

	
	/**
	 * This method returns the number of droughts that will occur during the upcoming
	 * growth step.
	 * @return an integer
	 */
	public int getNumberOfDroughtsDuringUpcomingGrowthStep();
}
