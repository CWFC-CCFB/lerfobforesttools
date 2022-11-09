/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.treelogger.diameterbasedtreelogger;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.species.REpiceaSpecies;
import repicea.simulation.treelogger.LoggableTree;

class DiameterBasedLoggableTreeImpl implements LoggableTree, DbhCmProvider {

	final double dbhCm;
	
	DiameterBasedLoggableTreeImpl(double dbhCm) {
		this.dbhCm = dbhCm;
	}
	
	@Override
	public double getCommercialVolumeM3() {return 1d;}

	@Override
	public String getSpeciesName() {return REpiceaSpecies.Species.Pinus_pinaster.toString();}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getBarkProportionOfWoodVolume() {
		return REpiceaSpecies.Species.Pinus_pinaster.getBarkProportionOfWoodVolume();
	}

	@Override
	public boolean isCommercialVolumeOverbark() {
		return true;
	}


}
