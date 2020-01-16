/*
 * This file is part of the lerfob-foresttools library.
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
package lerfob.treelogger.europeanbeech;

import java.util.ArrayList;
import java.util.List;

import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedWoodPiece;
import lerfob.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerParameters.Grade;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.DbhCmStandardDeviationProvider;
import repicea.simulation.treelogger.LoggableTree;
import repicea.stats.distributions.utility.GaussianUtility;

@SuppressWarnings("serial")
public class EuropeanBeechBasicTreeLogCategory extends DiameterBasedTreeLogCategory {

	
	/**
	 * Constructor.
	 * @param logGrade a Grade enum 
	 * @param species the species name
	 * @param smallEndDiameter the small end diameter (cm)
	 */
	protected EuropeanBeechBasicTreeLogCategory(Grade logGrade, Object species, double smallEndDiameter) {
		super(logGrade, species, smallEndDiameter, null);	// conversion factors are not used in this TreeLogger class
		this.isChangeAllowed = false;
	}

	@Override
	protected boolean isEligible(LoggableTree tree) {
		if (tree instanceof EuropeanBeechBasicTree) {
			boolean subjectToMinimumDiameter = true;
			if (tree instanceof DbhCmStandardDeviationProvider) {
				subjectToMinimumDiameter = ((DbhCmStandardDeviationProvider) tree).getDbhCmStandardDeviation() <= 0d;
			} 
			if (subjectToMinimumDiameter) {		// only enabled if the standard deviation is 0
				return ((EuropeanBeechBasicTree) tree).getDbhCm() >= minimumDbhCm;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	
	
	
	@Override
	protected List<DiameterBasedWoodPiece> extractFromTree(LoggableTree tree, Object... parms) {
		List<DiameterBasedWoodPiece> pieces = null;
		if (isEligible(tree)) {
			pieces = new ArrayList<DiameterBasedWoodPiece>();
			double mqd = ((DbhCmProvider) tree).getDbhCm();
			double dbhStandardDeviation = 0d;
			if (tree instanceof DbhCmStandardDeviationProvider) {
				dbhStandardDeviation = ((DbhCmStandardDeviationProvider) tree).getDbhCmStandardDeviation();
			}
			
			double energyWoodProportion;
			double industryWoodProportion;
			double veneerProportion;
			double regularQualitySawlogProportion;
			double lowQualitySawlogProportion;

			if (dbhStandardDeviation > 0) {
				// Assumption of a normal distribution for stem distribution
				energyWoodProportion = GaussianUtility.getCumulativeProbability((17.5 - mqd)/dbhStandardDeviation);
				industryWoodProportion = GaussianUtility.getCumulativeProbability((27.5 - mqd)/dbhStandardDeviation) - energyWoodProportion;
				lowQualitySawlogProportion = GaussianUtility.getCumulativeProbability((37.5 - mqd)/dbhStandardDeviation) - energyWoodProportion - industryWoodProportion;
				regularQualitySawlogProportion = GaussianUtility.getCumulativeProbability((47.5 - mqd)/dbhStandardDeviation) - lowQualitySawlogProportion - energyWoodProportion - industryWoodProportion; 
				veneerProportion = GaussianUtility.getCumulativeProbability((47.5 - mqd)/dbhStandardDeviation, true);
			} else {	// no standard deviation
				if (mqd < 17.5) {
					energyWoodProportion = 1d;
					industryWoodProportion = 0d;
					lowQualitySawlogProportion = 0d;
					regularQualitySawlogProportion = 0d;
					veneerProportion = 0d;
				} else  if (mqd < 27.5) {
					energyWoodProportion = 0d;
					industryWoodProportion = 1d;
					lowQualitySawlogProportion = 0d;
					regularQualitySawlogProportion = 0d;
					veneerProportion = 0d;
				} else  if (mqd < 37.5) {
					energyWoodProportion = 0d;
					industryWoodProportion = 0d;
					lowQualitySawlogProportion = 1d;
					regularQualitySawlogProportion = 0d;
					veneerProportion = 0d;
				} else  if (mqd < 47.5) {
					energyWoodProportion = 0d;
					industryWoodProportion = 0d;
					lowQualitySawlogProportion = 0d;
					regularQualitySawlogProportion = 1d;
					veneerProportion = 0d;
				} else { 
					energyWoodProportion = 0d;
					industryWoodProportion = 0d;
					lowQualitySawlogProportion = 0d;
					regularQualitySawlogProportion = 0d;
					veneerProportion = 1d;
				} 
			}
			
			Grade grade = (Grade) getGrade();
			switch(grade) {
			case EnergyWood:
				if (energyWoodProportion > 0) {
					pieces.add(new DiameterBasedWoodPiece(this, tree, energyWoodProportion * tree.getCommercialVolumeM3()));
				} 
				break;
			case IndustryWood:
				if (industryWoodProportion > 0) {
						pieces.add(new DiameterBasedWoodPiece(this, tree, industryWoodProportion * tree.getCommercialVolumeM3()));
				}
				break;
			case SawlogLowQuality:
				if (lowQualitySawlogProportion > 0) {
						pieces.add(new DiameterBasedWoodPiece(this, tree, lowQualitySawlogProportion * tree.getCommercialVolumeM3()));
				}
				break;
			case SawlogRegularQuality:
				if (regularQualitySawlogProportion > 0) {
						pieces.add(new DiameterBasedWoodPiece(this, tree, regularQualitySawlogProportion * tree.getCommercialVolumeM3()));
				}
				break;
			case VeneerQuality: 
				if (veneerProportion > 0) {
					pieces.add(new DiameterBasedWoodPiece(this, tree, veneerProportion * tree.getCommercialVolumeM3()));
				}
				break;
			}
		}
		return pieces;
	}

}
