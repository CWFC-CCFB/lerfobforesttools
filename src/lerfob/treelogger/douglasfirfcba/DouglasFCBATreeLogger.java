/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.treelogger.douglasfirfcba;

import java.util.List;

import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;

public final class DouglasFCBATreeLogger extends TreeLogger<DouglasFCBATreeLoggerParameters, DouglasFCBALoggableTree> {

	@Override
	public void setTreeLoggerParameters() {
		DouglasFCBATreeLoggerParameters params = createDefaultTreeLoggerParameters();
		params.showUI(null);
		setTreeLoggerParameters(params);
	}

	@Override
	public DouglasFCBATreeLoggerParameters createDefaultTreeLoggerParameters() {
		DouglasFCBATreeLoggerParameters params = new DouglasFCBATreeLoggerParameters();
		params.initializeDefaultLogCategories();
		return params;
	}

	@Override
	public DouglasFCBALoggableTree getEligible(LoggableTree t) {
		if (t instanceof DouglasFCBALoggableTree) {
			return (DouglasFCBALoggableTree) t;
		} else {
			return null;
		}
	}

	@Override
	protected void logThisTree(DouglasFCBALoggableTree tree) {
		double largeLumberWoodProportion = 0d;
		double smallLumberWoodProportion = 0d;
		double energyWoodProportion = 0d;
		DouglasFCBAWoodPiece woodPiece;
		
		List<DouglasFCBALogCategory> logCategories = params.getLogCategories().get(DouglasFCBALoggableTree.Species.DouglasFir.name());
		double volumeM3 = tree.getCommercialVolumeM3();
		double proportion;
		for (DouglasFCBALogCategory logCategory : logCategories) {
			proportion = logCategory.getGrossProportion(tree);
			switch(logCategory.getGrade()) {
			case LargeLumberWood:
				largeLumberWoodProportion = proportion;
				break;
			case SmallLumberWood:
				proportion = proportion - largeLumberWoodProportion;
				smallLumberWoodProportion = proportion;
				break;
			case EnergyWood:
				proportion = proportion - (largeLumberWoodProportion + smallLumberWoodProportion);
				energyWoodProportion = proportion;
				break;
			case Residues:
				proportion = proportion - (largeLumberWoodProportion + smallLumberWoodProportion + energyWoodProportion);
				break;
			}
			if (proportion > 0) {
				woodPiece = new DouglasFCBAWoodPiece(logCategory, tree, volumeM3 * proportion);
				addWoodPiece(tree, woodPiece);
			}
		}
	}

	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		return check.getTreeInstance() instanceof DouglasFCBALoggableTree;
	}

	public static void main(String[] args) {
		DouglasFCBATreeLogger treeLogger = new DouglasFCBATreeLogger();
		DouglasFCBATreeLoggerParameters params = treeLogger.createDefaultTreeLoggerParameters();
		params.showUI(null);
	}

}
