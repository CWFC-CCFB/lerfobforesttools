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

import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider;
import repicea.simulation.treelogger.LoggableTree;

/**
 * The CarbonToolCompatibleTree interface ensures the tree is compatible with
 * the carbon assessment tool.
 * @author Mathieu Fortin - January 2013
 */
public interface CarbonToolCompatibleTree extends LoggableTree, TreeStatusProvider {


	
	/**
	 * This method returns the species type, either coniferous or broadleaved.
	 * @return a SpeciesType Enum
	 */
	public SpeciesType getSpeciesType();
	
	
}
