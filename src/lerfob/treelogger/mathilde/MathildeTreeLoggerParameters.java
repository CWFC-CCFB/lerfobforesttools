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

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public final class MathildeTreeLoggerParameters extends TreeLoggerParameters<MathildeTreeLogCategory> {

	public static enum Grade implements TextableEnum {
		EnergyWood("Industry and energy wood", "Bois d'industrie et bois \u00E9nergie (BIBE)"),
		SmallLumberWood("Small lumber wood", "Petit bois d'oeuvre (BO)"),
		LargeLumberWood("Lumber wood", "Bois d'oeuvre (BO)"),
		;

		Grade(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}

		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	private transient MathildeTreeLoggerParametersDialog guiInterface;
	
	protected MathildeTreeLoggerParameters() {
		super(MathildeTreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<MathildeTreeLogCategory> categories;
		getLogCategories().clear();
		for (MathildeTreeSpecies species : MathildeTreeSpecies.values()) {
			categories = new ArrayList<MathildeTreeLogCategory>();
			getLogCategories().put(species.name(), categories);
			double largeLumberWoodLimit = 37.5;
			if (species == MathildeTreeSpecies.QUERCUS) {
				largeLumberWoodLimit = 47.5;
			}
			categories.add(new MathildeTreeLogCategory(species, Grade.LargeLumberWood.name(), largeLumberWoodLimit, 0.84));
			categories.add(new MathildeTreeLogCategory(species, Grade.SmallLumberWood.name(), 27.5, 0.50));
			categories.add(new MathildeTreeLogCategory(species, Grade.EnergyWood.name(), 7d, 1d));
		}
	}

	@Override
	public boolean isCorrect() {return true;}

	@Override
	public MathildeTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new MathildeTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

}
