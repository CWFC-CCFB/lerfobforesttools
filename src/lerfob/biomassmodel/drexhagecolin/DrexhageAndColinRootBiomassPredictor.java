/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.biomassmodel.drexhagecolin;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;

/**
 * This class implements two models for predicting the below ground dry biomass of oak and beech tree.
 * @author Mathieu Fortin - August 2013
 */
public class DrexhageAndColinRootBiomassPredictor {

	/**
	 * This static method returns the root biomass for Oak trees in kg.
	 * It is based on the equations in Drexhage & Colin 2001 (see Table 2, eq. 9).
	 * @see <a href=http://forestry.oxfordjournals.org/content/74/5/491.full.pdf+html> 
	 * Drexhage, M., and Colin, F. 2001. Estimating root system biomass from breast-height diameters. Forestry 74(5): 491-497.
	 * </a>
	 * @param tree a DbhCmProvider instance
	 */
	public static double getRootBiomassContentForOakKg(DbhCmProvider tree) { 
		if (tree == null) {return -1d;}
		double treeRootBiomass = -1.;
		treeRootBiomass = Math.pow(10., -1.56 + 2.44 * Math.log10(tree.getDbhCm()));
		return treeRootBiomass;
	}

	/**
	 * This static method returns the root dry biomass for Beech trees in kg.
	 * It is based on the equation for Fagus sylvatica (NE France) 
	 * in Drexhage & Colin 2001 (see Table 2, eq. 8).
	 * @see <a href=http://forestry.oxfordjournals.org/content/74/5/491.full.pdf+html> 
	 * Drexhage, M., and Colin, F. 2001. Estimating root system biomass from breast-height diameters. Forestry 74(5): 491-497.
	 * </a>
	 * @param tree a DbhCmProvider instance
	 */
	public static double getRootBiomassForBeechKg(DbhCmProvider tree) {
		if (tree == null) {return -1d;}
		double treeRootBiomass = -1.;
		treeRootBiomass = Math.pow(10., -1.66 + 2.54 * Math.log10(tree.getDbhCm()));
		return treeRootBiomass;
	}


}
