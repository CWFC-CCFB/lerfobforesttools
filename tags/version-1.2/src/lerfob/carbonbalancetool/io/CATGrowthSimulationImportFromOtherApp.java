/*
 * This file is part of the lerfob-foresttools library.
 *
 * Copyright (C) 2010-2019 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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

import java.util.List;

import lerfob.carbonbalancetool.CATCompatibleStand;
import lerfob.carbonbalancetool.CATSettings.CATSpecies;
import repicea.gui.components.REpiceaMatchSelector;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

/**
 * The CATGrowthSimulationImportFromOtherApp class makes it possible to import 
 * data from other applications. While the CATGrowthSimulationRecordReader class assumes that
 * the data come from a file, this class assumes the data are already formatted. 
 * 
 * @author Mathieu Fortin - January 2019
 *
 */
public class CATGrowthSimulationImportFromOtherApp {

	private final CATGrowthSimulationRecordReader recordReader;
	
	
	public CATGrowthSimulationImportFromOtherApp() {
		recordReader = new CATGrowthSimulationRecordReader();
	}
	

	public void instantiatePlotAndTree(String standIdentification, int dateYr, int realization, String plotID, double plotAreaHa,
			StatusClass statusClass, double treeOverbarkVolumeDm3, double numberOfTrees, String originalSpeciesName, Double dbhCm) {
		recordReader.instantiatePlotAndTree(standIdentification, dateYr, realization, plotID, plotAreaHa, statusClass, treeOverbarkVolumeDm3, numberOfTrees, originalSpeciesName, dbhCm);
	}
	
	/**
	 * This method returns the stand list that was last read.
	 * @return a list of CATCompatibleStand instances
	 */
	public List<CATCompatibleStand> getStandList() {
		return recordReader.getStandList();
	}	
	
	/**
	 * This method returns the selector for the species in CAT.
	 * @return a REpiceaMatchSelector instance
	 */
	public REpiceaMatchSelector<CATSpecies> getSelector() {
		return recordReader.getSelector();
	}

	
}
