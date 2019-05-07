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

import lerfob.carbonbalancetool.CATCompatibleTree;
import lerfob.carbonbalancetool.CATSettings.CATSpecies;

/**
 * This class represents the trees in a growth simulation import in CAT.
 * @author Mathieu Fortin - July 2017
 */
class CATGrowthSimulationTree implements CATCompatibleTree {

	private final double commercialVolumeM3;
	private final double numberOfTrees;
	private StatusClass statusClass;
	private final String originalSpeciesName;
	protected final CATGrowthSimulationPlot plot;
	
	CATGrowthSimulationTree(CATGrowthSimulationPlot plot, 
			StatusClass statusClass, 
			double treeVolumeDm3, 
			double numberOfTrees, 
			String originalSpeciesName) {
		this.plot = plot;
		commercialVolumeM3 = treeVolumeDm3 * .001;
		this.numberOfTrees = numberOfTrees;
		this.originalSpeciesName = originalSpeciesName;
		setStatusClass(statusClass);
	}
	
	@Override
	public double getCommercialVolumeM3() {return commercialVolumeM3;}

	@Override
	public String getSpeciesName() {return getCATSpecies().toString();}

//	@Override
//	public SpeciesType getSpeciesType() {return getCATSpecies().getSpeciesType();}

	@Override
	public CATSpecies getCATSpecies() {return (CATSpecies) plot.plotSample.compositeStand.reader.getSelector().getMatch(originalSpeciesName);}
	
	@Override
	public double getNumber() {return numberOfTrees;}

	@Override
	public void setStatusClass(StatusClass statusClass) {this.statusClass = statusClass;}

	@Override
	public StatusClass getStatusClass() {return statusClass;}

}
