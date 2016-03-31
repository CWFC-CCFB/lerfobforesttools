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
import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class MathildeTreeLogCategory extends TreeLogCategory {

	protected final double minimumDbhCm;
	private transient MathildeTreeLogCategoryPanel guiInterface;
	protected final TextableEnum explanation;
	protected final double conversionFactor; 
	
	protected MathildeTreeLogCategory(MathildeTreeSpecies species, String name, TextableEnum explanation, double minimumDiameter, double conversionFactor) {
		super(name);
		setSpecies(species.name());
		this.minimumDbhCm = minimumDiameter;
		this.explanation = explanation;
		this.conversionFactor = conversionFactor;
	}
	
	
	@Override
	public MathildeTreeLogCategoryPanel getGuiInterface() {
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
}
