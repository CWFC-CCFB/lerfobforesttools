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

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
class MathildeTreeLogCategory extends LogCategory {

	protected final double minimumDbhCm;
	private transient MathildeTreeLogCategoryPanel guiInterface;
	protected double conversionFactor; 
	protected double downgradingProportion;
	
	protected MathildeTreeLogCategory(MathildeTreeSpecies species, String name, double minimumDiameter, double conversionFactor) {
		super(name, false);
		setSpecies(species.name());
		this.minimumDbhCm = minimumDiameter;
		this.conversionFactor = conversionFactor;
	}
	
	@Override
	public MathildeTreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new MathildeTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {return 1d;}

	protected boolean isEligible(MathildeLoggableTree tree) {
		return tree.getDbhCm() >= minimumDbhCm;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		if (obj instanceof MathildeTreeLogCategory) {
			MathildeTreeLogCategory refCategory = (MathildeTreeLogCategory) obj;
			
			if (refCategory.minimumDbhCm != this.minimumDbhCm) {
				return false;
			}
			
			if (refCategory.conversionFactor != this.conversionFactor) {
				return false;
			}
			
			if (refCategory.downgradingProportion != this.downgradingProportion) {
				return false;
			}
			
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

}
