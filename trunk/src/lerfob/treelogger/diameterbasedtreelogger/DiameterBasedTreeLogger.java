/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge-Epicea
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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

import java.util.List;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;

public class DiameterBasedTreeLogger extends TreeLogger<DiameterBasedTreeLoggerParameters, LoggableTree> {

	protected final boolean shouldBreakAfterGettingPieces;
	
	public DiameterBasedTreeLogger() {
		this(true);
	}

	/**
	 * Constructor for derived class. 
	 * @param shouldBreakAfterGettingPieces true should be preferred to false which is a former implementation
	 */
	protected DiameterBasedTreeLogger(boolean shouldBreakAfterGettingPieces) {
		super();
		this.shouldBreakAfterGettingPieces = shouldBreakAfterGettingPieces;
	}
	
	@Override
	protected void logThisTree(LoggableTree tree) {
		List<DiameterBasedTreeLogCategory> logCategories = params.getSpeciesLogCategories(params.getDefaultSpecies());
		List<DiameterBasedWoodPiece> pieces;
		for (DiameterBasedTreeLogCategory logCategory : logCategories) {
			pieces = logCategory.extractFromTree(tree, params);
			if (pieces != null && !pieces.isEmpty()) {
				for (DiameterBasedWoodPiece piece : pieces) {
					addWoodPiece(tree, piece);	
				}
				if (shouldBreakAfterGettingPieces) {
					break;
				}
			} 
		}
	}

	@Override
	public void setTreeLoggerParameters() {}

	@Override
	public DiameterBasedTreeLoggerParameters createDefaultTreeLoggerParameters() {
		return new DiameterBasedTreeLoggerParameters();
	}
	
	@Override
	public LoggableTree getEligible(LoggableTree t) {
		if (t instanceof DbhCmProvider) {
//			return (DiameterBasedLoggableTree) t;
			return t;
		} else {
			return null;
		}
	}

	@Override
	public boolean isCompatibleWith(TreeLoggerCompatibilityCheck check) {
		Object instance = check.getTreeInstance();
		return instance instanceof LoggableTree && instance instanceof DbhCmProvider;
	}
}

