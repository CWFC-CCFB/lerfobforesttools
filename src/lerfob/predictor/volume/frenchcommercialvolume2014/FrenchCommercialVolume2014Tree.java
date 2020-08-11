/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.volume.frenchcommercialvolume2014;

import repicea.math.Matrix;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.covariateproviders.treelevel.SquaredDbhCmProvider;

public interface FrenchCommercialVolume2014Tree extends DbhCmProvider,
														SquaredDbhCmProvider,
														HeightMProvider,
														MonteCarloSimulationCompliantObject {
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}

	public static enum FrenchCommercialVolume2014TreeSpecies {
		ABIES_ALBA,
		ABIES_CONCOLOR,
		ABIES_GRANDIS,
		ABIES_NORDMANNIANA,
		CARPINUS_BETULUS,
		CARYA_TOMENTOSA,
		CEDRUS_ATLANTICA_OU_LIBAN,
		FAGUS_SYLVATICA,
		FRAXINUS_EXCELSIOR,
		JUGLANS_NIGRA,
		LARIX_DECIDUA,
		LARIX_KAEMPFERI,
		PICEA_ABIES,
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
		
		private Matrix dummy;
		
		FrenchCommercialVolume2014TreeSpecies() {
			dummy = new Matrix(1,27);
			dummy.m_afData[0][ordinal()] = 1d;
		}
		
		protected Matrix getDummy() {return dummy;}
	}

	/**
	 * This method returns the species of the tree instance.
	 * @return a FrenchCommercialVolume2014TreeSpecies enum instance
	 */
	public FrenchCommercialVolume2014TreeSpecies getFrenchCommercialVolume2014TreeSpecies();
	
}
