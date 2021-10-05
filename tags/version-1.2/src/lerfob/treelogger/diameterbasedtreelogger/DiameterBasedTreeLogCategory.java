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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class DiameterBasedTreeLogCategory extends LogCategory {
	
	protected boolean isChangeAllowed;
	protected double minimumDbhCm;
	protected double conversionFactor;
	protected double downgradingProportion;
	protected final boolean isConversionEnabled;
	protected final boolean isDowngradingEnabled;
	
	protected DiameterBasedTreeLogCategory subCategory;
	
	private transient DiameterBasedTreeLogCategoryPanel guiInterface;
	protected final Enum<?> logGrade;

	/**
	 * General contructor.
	 * @param logGrade an Enum that represents the log category
	 * @param species a species name
	 * @param minimumDbhCm a minimum diameter for this log grade either null or positive
	 * @param conversionFactor a conversion factor either null or within the interval [0,1]
	 * @param downgradingFactor a downgrading factor either null or within the interval [0,1]
	 * @param isFromStump a boolean
	 * @param subCategory a DiameterBasedTreeLogCategory for by products
	 */
	public DiameterBasedTreeLogCategory(Enum<?> logGrade, 
			Object species, 
			double minimumDbhCm, 
			double conversionFactor, 
			double downgradingFactor,
			boolean isFromStump, 
			DiameterBasedTreeLogCategory subCategory) {
		this(logGrade, species, minimumDbhCm, conversionFactor, true, downgradingFactor, true, isFromStump, subCategory);
	}
	
	
	private DiameterBasedTreeLogCategory(Enum<?> logGrade, 
			Object species, 
			double minimumDbhCm, 
			double conversionFactor, 
			boolean conversionEnabled,
			double downgradingFactor,
			boolean downgradingEnabled,
			boolean isFromStump, 
			DiameterBasedTreeLogCategory subCategory) {
		super(logGrade.toString(), isFromStump);
		setSpecies(species);
		this.logGrade = logGrade;
		if (minimumDbhCm < 0) {
			throw new InvalidParameterException("The minimumDbhCm parameter must be positive!");
		}
		this.minimumDbhCm = minimumDbhCm;
		
		if (conversionFactor < 0 || conversionFactor > 1) {
			throw new InvalidParameterException("The conversion factor must be within the interval [0,1]!");
		}
		this.conversionFactor = conversionFactor;
		this.isConversionEnabled = conversionEnabled;
		
		if (downgradingFactor < 0 || downgradingFactor > 1) {
			throw new InvalidParameterException("The downgrading factor must be within the interval [0,1]!");
		}
		this.downgradingProportion = downgradingFactor;
		this.isDowngradingEnabled = downgradingEnabled;
		
		this.subCategory = subCategory;
		this.isChangeAllowed = true;
	}

	/**
	 * A default constructor with 100% conversion factor and no downgrading.
	 * @param logGrade an Enum that represents the log category
	 * @param species a species name
	 * @param minimumDbhCm a minimum diameter for this log grade either null or positive
	 * @param isFromStump a boolean
	 * @param subCategory a DiameterBasedTreeLogCategory for by products
	 */
	public DiameterBasedTreeLogCategory(Enum<?> logGrade, 
			Object species, 
			double minimumDbhCm, 
			boolean isFromStump,
			DiameterBasedTreeLogCategory subCategory) {
		this(logGrade, species, minimumDbhCm, 1d, false, 0d, false, isFromStump, subCategory);
	}
	
	/**
	 * A default constructor with 100% conversion factor, no downgrading and not from stump.
	 * @param logGrade an Enum that represents the log category
	 * @param species a species name
	 * @param minimumDbhCm a minimum diameter for this log grade either null or positive
	 * @param subCategory a DiameterBasedTreeLogCategory for by products
	 */
	public DiameterBasedTreeLogCategory(Enum<?> logGrade, 
			Object species, 
			double minimumDbhCm, 
			DiameterBasedTreeLogCategory subCategory) {
		this(logGrade, species, minimumDbhCm, false, subCategory);
	}
	
		
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
		if (tree instanceof DbhCmProvider && tree.getCommercialVolumeM3() > 0d) {
			return ((DbhCmProvider) tree).getDbhCm() >= minimumDbhCm;
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
			double volumeToBeProcessed = potentialVolume * (1 - downgradingProportion) * conversionFactor;
			if (volumeToBeProcessed > 0d) {
				pieces.add(new DiameterBasedWoodPiece(this, tree, tree.isCommercialVolumeOverbark(), volumeToBeProcessed));
			}
			if (subCategory != null) {
				volumeToBeProcessed = potentialVolume - volumeToBeProcessed;
				if (volumeToBeProcessed > 0d) {
					pieces.addAll(subCategory.extractFromTree(tree, volumeToBeProcessed));
				}
			}
 		}
		return pieces;
	}

	@Override
	public boolean equals(Object obj) {
		boolean isEqual = super.equals(obj);
		if (isEqual) {
			if (obj instanceof DiameterBasedTreeLogCategory) {
				DiameterBasedTreeLogCategory lc = (DiameterBasedTreeLogCategory) obj;
				if (lc.conversionFactor == conversionFactor || (!isConversionEnabled && !lc.isConversionEnabled)) {
					if (lc.downgradingProportion == downgradingProportion || (!isDowngradingEnabled && !lc.isDowngradingEnabled)) {
						if (lc.minimumDbhCm == minimumDbhCm) {
							if (lc.subCategory == null && subCategory == null) {
								return true;
							} else if (lc.subCategory != null && lc.subCategory.equals(subCategory)) {
								return true;
							} else if (subCategory != null && subCategory.equals(lc.subCategory)) {
								return true;
							}
						}
					}
				}
			}
		} 
		return false;
	}
	
}
