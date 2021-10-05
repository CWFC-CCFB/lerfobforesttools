/*
 * This file is part of the lerfob-foresttools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.carbonbalancetool.io;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;

class CATGrowthSimulationTreeWithDBH extends CATGrowthSimulationTree implements DbhCmProvider {

	private final double dbhCm;
	
	CATGrowthSimulationTreeWithDBH(CATGrowthSimulationPlot plot, 
			StatusClass statusClass, 
			double treeVolumeDm3,
			double numberOfTrees, 
			String originalSpeciesName,
			double dbhCm) {
		super(plot, statusClass, treeVolumeDm3, numberOfTrees, originalSpeciesName);
		this.dbhCm = dbhCm;
	}

	@Override
	public double getDbhCm() {return dbhCm;}

}
