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
package lerfob.carbonbalancetool;

import lerfob.carbonbalancetool.CATSettings.CATSpecies;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider;
import repicea.simulation.treelogger.LoggableTree;

/**
 * The CarbonToolCompatibleTree interface ensures the tree is compatible with
 * the carbon assessment tool.
 * @author Mathieu Fortin - January 2013
 */
public interface CATCompatibleTree extends LoggableTree, TreeStatusProvider {

	
	/**
	 * This method returns the species of the tree.
	 * @return a CATSpecies instance
	 */
	public CATSpecies getCATSpecies();

	@Override
	public default double getBarkProportionOfWoodVolume() {
		return getCATSpecies().getBarkProportionOfWoodVolume();
	}
	
}
