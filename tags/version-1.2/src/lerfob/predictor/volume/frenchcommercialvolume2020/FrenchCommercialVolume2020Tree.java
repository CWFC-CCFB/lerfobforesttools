/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2020 Her Majesty the Queen in right of Canada
 * 		Mathieu Fortin for Canadian WoodFibre Centre,
 * 							Canadian Forest Service, 
 * 							Natural Resources Canada
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package lerfob.predictor.volume.frenchcommercialvolume2020;

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;

/**
 * Makes sure the tree can provide the information to run the volume model.
 * @author Mathieu Fortin - August 2020
 */
public interface FrenchCommercialVolume2020Tree extends DbhCmProvider,
														SquaredDbhCmProvider,
														HeightMProvider,
														MonteCarloSimulationCompliantObject {
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}

	public static enum FrenchCommercialVolume2020TreeSpecies {
		ABIES_ALBA,
		ABIES_CONCOLOR,
		ABIES_GRANDIS,
		ABIES_NORDMANNIANA,
		ACER_PSEUDOPLATANUS,
		BETULA_PENDULA,
		CARPINUS_BETULUS,
		CARYA_TOMENTOSA,
		CEDRUS_ATLANTICA_OU_LIBANI,
		FAGUS_SYLVATICA,
		FRAXINUS_EXCELSIOR,
		JUGLANS_NIGRA,
		LARIX_DECIDUA,
		LARIX_KAEMPFERI,
		PICEA_ABIES,
		PINUS_CEMBRA,
		PINUS_HALEPENSIS,
		PINUS_LARICIO,
		PINUS_NIGRA,
		PINUS_PINASTER,
		PINUS_STROBUS,
		PINUS_SYLVESTRIS,
		PINUS_UNCINATA,
		PSEUDOTSUGA_MENZIESII,
		QUERCUS_PALUSTRIS,
		QUERCUS_PETRAEA,
		QUERCUS_ROBUR,
		QUERCUS_ROBUR_PETRAEA,
		QUERCUS_RUBRA,
		THUYA_PLICATA;
		
	}

	/**
	 * This method returns the species of the tree instance.
	 * @return a FrenchCommercialVolume2020TreeSpecies enum instance
	 */
	public FrenchCommercialVolume2020TreeSpecies getFrenchCommercialVolume2020TreeSpecies();
	
}
