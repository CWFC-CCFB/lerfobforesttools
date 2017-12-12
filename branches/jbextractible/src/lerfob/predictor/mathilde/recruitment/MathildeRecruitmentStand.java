/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.mathilde.recruitment;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.standlevel.GrowthStepLengthYrProvider;
import repicea.simulation.covariateproviders.standlevel.InterventionPlannedProvider;

/**
 * This interface ensures that the Stand instance is compatible with the MathildeRecruitmentNumberPredictor.
 * @author Mathieu Fortin - October2017
 */
public interface MathildeRecruitmentStand extends MonteCarloSimulationCompliantObject, 
													GrowthStepLengthYrProvider, 
													BasalAreaM2HaProvider,
													InterventionPlannedProvider {
	/**
	 * This method returns the basal area of a particular species in the stand.
	 * @param species a MathildeTreeSpecies instance
	 * @return the basal area (m2/ha)
	 */
	public double getBasalAreaM2HaOfThisSpecies(MathildeTreeSpecies species);

}
