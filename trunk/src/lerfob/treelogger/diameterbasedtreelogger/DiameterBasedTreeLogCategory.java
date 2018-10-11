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

import java.util.ArrayList;
import java.util.List;

import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class DiameterBasedTreeLogCategory extends LogCategory {
	
	protected final Double minimumDbhCm;
	protected double conversionFactor;
	
	protected DiameterBasedTreeLogCategory subCategory;
	
	private transient DiameterBasedTreeLogCategoryPanel guiInterface;
	protected final Enum<?> logGrade;

	/**
	 * Constructor under the assumption that all the commercial volume falls into this category.
	 * @param logGrade an Enum defined in the parameter
	 * @param species the species name
	 * @param minimumDbhCm the minimum dbh for the tree to be eligible
	 * @param isFromStump true if this volume comes from the stump
	 */
	public DiameterBasedTreeLogCategory(Enum<?> logGrade, 
			String species, 
			double minimumDbhCm, 
			double conversionFactor, 
			boolean isFromStump, 
			DiameterBasedTreeLogCategory subCategory) {
		super(logGrade.toString(), isFromStump);
		setSpecies(species);
		this.logGrade = logGrade;
		if (minimumDbhCm == -1) {
			this.minimumDbhCm = Double.NaN;
		} else {
			this.minimumDbhCm = minimumDbhCm;
		}
		this.conversionFactor = conversionFactor;
		this.subCategory = subCategory;
	}

		
	/*
	 * Useless for this class (non-Javadoc)
	 * @see capsis.extension.treelogger.TreeLogCategory#getTreeLogCategoryPanel()
	 */
	@Override
	public DiameterBasedTreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new DiameterBasedTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {return 1d;}

	public Enum<?> getGrade() {return logGrade;}

	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	
	protected boolean isEligible(LoggableTree tree) {
		if (tree instanceof DiameterBasedLoggableTree && tree.getCommercialVolumeM3() > 0d) {
			return ((DiameterBasedLoggableTree) tree).getDbhCm() >= minimumDbhCm;
		} else {
			return false;
		}
	}

	@Override
	protected List<DiameterBasedWoodPiece> extractFromTree(LoggableTree tree, Object... parms) {
		List<DiameterBasedWoodPiece> pieces = null;
		if (isEligible(tree)) {
			pieces = new ArrayList<DiameterBasedWoodPiece>();
			double potentialVolume = tree.getCommercialVolumeM3();
			if (parms != null && parms[0] instanceof Double) {
				potentialVolume = (Double) parms[0];
			}
//			DiameterBasedTreeLoggerParameters parameters = (DiameterBasedTreeLoggerParameters) parms[0];
//			boolean isEligibleToSmallLumberWood = !parameters.getLargeLumberWoodLogCategory().isEligible(tree);
//			double volumeToBeProcessed = 0d;
//			switch((DiameterBasedTreeLoggerParameters.Grade) logGrade) {
//			case EnergyWood:
//				volumeToBeProcessed = tree.getCommercialVolumeM3(); 
//				break;
//			case SmallLumberWood:
//				if (isEligibleToSmallLumberWood) {
//					volumeToBeProcessed = tree.getCommercialVolumeM3() * conversionFactor; 
//				}
//				break;
//			case LargeLumberWood:
//				volumeToBeProcessed = tree.getCommercialVolumeM3() * conversionFactor; 
//				break;
//			}
//			if (volumeToBeProcessed > 0) {
//				piece = new DiameterBasedWoodPiece(this, tree, volumeToBeProcessed);
//			} 
			double volumeToBeProcessed = potentialVolume * conversionFactor;
			if (volumeToBeProcessed > 0d) {
				pieces.add(new DiameterBasedWoodPiece(this, tree, volumeToBeProcessed));
			}
			if (subCategory != null) {
				volumeToBeProcessed = potentialVolume * (1 - conversionFactor);
				pieces.addAll(subCategory.extractFromTree(tree, volumeToBeProcessed));
			}
 		}
		return pieces;
	}

	
	
}
