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
package lerfob.treelogger.mathilde;

import java.util.List;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import lerfob.treelogger.mathilde.MathildeTreeLoggerParameters.Grade;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;

public final class MathildeTreeLogger extends TreeLogger<MathildeTreeLoggerParameters, MathildeLoggableTree> {

	static {
		TreeLogger.registerTreeLogger(MathildeTreeLogger.class);
	}

	@Override
	public void setTreeLoggerParameters() {
		MathildeTreeLoggerParameters params = createDefaultTreeLoggerParameters();
		params.showInterface(null);
		setTreeLoggerParameters(params);
	}

	@Override
	public MathildeTreeLoggerParameters createDefaultTreeLoggerParameters() {
		MathildeTreeLoggerParameters params = new MathildeTreeLoggerParameters();
		params.initializeDefaultLogCategories();
		return params;
	}

	@Override
	public MathildeLoggableTree getEligible(LoggableTree t) {
		if (t instanceof MathildeLoggableTree) {
			return (MathildeLoggableTree) t;
		} else {
			return null;
		}
	}

	@Override
	protected void logThisTree(MathildeLoggableTree tree) {
		MathildeTreeSpecies species = tree.getMathildeTreeSpecies();
		List<MathildeTreeLogCategory> logCategories = params.getLogCategories().get(species.name());
		double volume = tree.getCommercialVolumeM3();
		MathildeWoodPiece woodPiece;
		boolean largeLumberProduced = false;
		for (MathildeTreeLogCategory logCategory : logCategories) {
			if (volume > 0d && logCategory.isEligible(tree)) {
				if (!logCategory.getName().equals(Grade.SmallLumberWood.name()) || !largeLumberProduced) {
					woodPiece = new MathildeWoodPiece(logCategory, tree);
					addWoodPiece(tree, woodPiece);
					if (logCategory.getName().equals(Grade.LargeLumberWood.name())) {
						largeLumberProduced = true;
					}
					volume -= woodPiece.getVolumeM3();
				}
			}
		}
	}

	public static void main(String[] args) {
		MathildeTreeLogger treeLogger = new MathildeTreeLogger();
		MathildeTreeLoggerParameters params = treeLogger.createDefaultTreeLoggerParameters();
		params.showInterface(null);
	}

	@Override
	public boolean isCompatibleWith(Object referent) {
		return referent instanceof MathildeLoggableTree;
	}

}
