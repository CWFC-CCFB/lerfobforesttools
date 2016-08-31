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

import lerfob.treelogger.douglasfirfcba.DouglasFCBATreeLoggerParameters.Grade;
import repicea.simulation.treelogger.LogCategory;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
class DouglasFCBALogCategory extends LogCategory {

	protected final double minimumDbhCm;
	protected final Grade grade;
	private transient DouglasFCBALogCategoryPanel guiInterface;
	
	protected DouglasFCBALogCategory(Grade grade, double minimumDiameter) {
		super(grade.name(), false);
		setSpecies(DouglasFCBALoggableTree.Species.DouglasFir.name());
		this.minimumDbhCm = minimumDiameter;
		this.grade = grade;
	}
	
	@Override
	public DouglasFCBALogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new DouglasFCBALogCategoryPanel(this);
		}
		return guiInterface;
	}

	protected Grade getGrade() {
		return grade;
	}
	
	/**
	 * This method returns the proportion of the commercial volume that can be used for the log grade.
	 * IMPORTANT: this is a gross proportion. For instance, the proportion of large lumber wood must be subtracted
	 * from the proportion of small lumber wood and so on.
	 * @param tree a DouglasFCBALoggableTree instance
	 * @return a double between 0 and 1
	 */
	protected double getGrossProportion(DouglasFCBALoggableTree tree) {
		double grossProportion = 0d;
		double treeDbhCm = tree.getDbhCm();
		switch(grade) {
		case LargeLumberWood:
			if (isEligible(tree)) {
				grossProportion = 1.80306 - (53.52431/treeDbhCm) - (1182.78539/(treeDbhCm * treeDbhCm));
			}
			break;
		case SmallLumberWood:
			if (isEligible(tree)) {
				grossProportion = 0.76104 + (25.23841/treeDbhCm) - (764.19392/(treeDbhCm * treeDbhCm));
			}
			break;
		case EnergyWood:
			if (isEligible(tree)) {
				grossProportion = 1.00532 - (0.17086/treeDbhCm) - (9.5151/(treeDbhCm * treeDbhCm));
			}
			break;
		case Residues:
			if (isEligible(tree)) {
				grossProportion = 1d;
			}
			break;
		}
		if (grossProportion > 1) {
			grossProportion = 1d;
		}
		return grossProportion;
	}
	
	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {return 1d;}

	protected boolean isEligible(DouglasFCBALoggableTree tree) {
		return tree.getDbhCm() > minimumDbhCm;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		
		if (obj instanceof DouglasFCBALogCategory) {
			DouglasFCBALogCategory refCategory = (DouglasFCBALogCategory) obj;
			
			if (refCategory.minimumDbhCm != this.minimumDbhCm) {
				return false;
			}
			
			if (refCategory.grade != this.grade) {
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
