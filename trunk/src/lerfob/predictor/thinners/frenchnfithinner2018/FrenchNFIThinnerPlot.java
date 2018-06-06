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

import lerfob.simulation.covariateproviders.standlevel.FrenchRegion2016Provider;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.BasalAreaM2HaProvider;
import repicea.simulation.covariateproviders.standlevel.SlopeInclinationPercentProvider;
import repicea.simulation.covariateproviders.standlevel.StemDensityHaProvider;

public interface FrenchNFIThinnerPlot extends FrenchRegion2016Provider,
												BasalAreaM2HaProvider,
												StemDensityHaProvider,
												SlopeInclinationPercentProvider,
//												SpeciesCompositionProvider,
												MonteCarloSimulationCompliantObject {
//	public enum Composition {
//		BroadleavedDominated,
//		ConiferousDominated,
//		Mixed;
//	}
	
	
//	/**
//	 * This method returns the composition of the plots based on basal area. Plots
//	 * with 75% or more of their basal area in broadleaved species are considered 
//	 * broadleaved dominated. Plots with 75% or more of their basal area in coniferous
//	 * species are considered coniferous dominated. Plots that do fall into these
//	 * two categories are considered as mixed stands. 
//	 * @return a Composition enum variable
//	 */
//	public Composition getComposition();
	
	/**
	 * This method returns true if any silvicultural treatment was carried out in the
	 * plot during the last five years.
	 * @return a boolean
	 */
	public boolean wasThereAnySiliviculturalTreatmentInTheLast5Years();
	

	/**
	 * This method returns the target species of the management. This is most likely
	 * the species with the current highest value.
	 * @return a Species enum
	 */
	public FrenchNFIThinnerStandingPriceProvider.Species getTargetSpecies();
	
	

}
