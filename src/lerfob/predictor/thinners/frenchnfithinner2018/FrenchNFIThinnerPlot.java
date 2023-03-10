/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.thinners.frenchnfithinner2018;

import java.util.Map;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;
import lerfob.simulation.covariateproviders.plotlevel.FrenchRegion2016Provider;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.plotlevel.SlopeInclinationPercentProvider;
import repicea.simulation.covariateproviders.plotlevel.StemDensityHaProvider;

public interface FrenchNFIThinnerPlot extends FrenchRegion2016Provider,
												BasalAreaM2HaProvider,
												StemDensityHaProvider,
												SlopeInclinationPercentProvider,
												MonteCarloSimulationCompliantObject {
	
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.PLOT;
	}

	/**
	 * This method returns true if any silvicultural treatment was carried out in the
	 * plot during the last five years.
	 * @return a boolean
	 */
	public boolean wasThereAnySiliviculturalTreatmentInTheLast5Years();
	

	/**
	 * This method returns the probability that the plot is located on private land. This method 
	 * reflects the uncertainty around the location of the plot in the French NFI. In case the 
	 * model is used with other data, the value of 1 should be returned when the plot is located on 
	 * private land or 0 if it is on public land. If the plot instance implements the LandOwnershipProvider
	 * interface available in repicea.jar, the interface is used in priority over the current method.
	 * @return a double (a probability)
	 */
	public double getProbabilityOfBeingOnPrivateLand();
	
	
	/**
	 * This method returns a Map with the species as keys and the volumes (m3) as values. It is required
	 * to identify the target species.
	 * @return a Map instance
	 */
	public Map<FrenchNFIThinnerSpecies, Double> getOverbarkVolumeM3BySpecies();
	
	
	

}
