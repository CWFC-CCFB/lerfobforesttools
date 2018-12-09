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

import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.LnDbhCmProvider;

/**
 * This interface ensures that the Tree instance is compatible with all Mathilde predictors.
 * @author Mathieu Fortin - June 2013
 */
public interface MathildeTree extends DbhCmProvider, 
									LnDbhCmProvider, 
									MonteCarloSimulationCompliantObject,
									MathildeTreeSpeciesProvider {
	
	@Override
	default public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.TREE;
	}

	/**
	 * This method returns the basal area of all the trees of the species with dbh larger than this tree instance.
	 * @param species the species that is to be considered
	 * @return basal area in m2/ha
	 */
	public double getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies species);

	
}
