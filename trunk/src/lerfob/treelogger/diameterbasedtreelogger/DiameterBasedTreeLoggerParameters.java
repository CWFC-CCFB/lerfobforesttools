/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.treelogger.diameterbasedtreelogger;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class DiameterBasedTreeLoggerParameters extends TreeLoggerParameters<DiameterBasedTreeLogCategory> {

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

	private transient DiameterBasedTreeLoggerParametersDialog guiInterface;
	
	/**
	 * Constructor for derived classes
	 * @param clazz a TreeLogger-derived class
	 */
	protected DiameterBasedTreeLoggerParameters(Class<? extends TreeLogger<?,?>> clazz) {
		super(clazz);
		initializeDefaultLogCategories();
	}
	
	/**
	 * Official constructor.
	 */
	protected DiameterBasedTreeLoggerParameters() {
		this(DiameterBasedTreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		List<DiameterBasedTreeLogCategory> categories = new ArrayList<DiameterBasedTreeLogCategory>();
		Object species = getDefaultSpecies();
		getLogCategories().clear();
		getLogCategories().put(species, categories);
		DiameterBasedTreeLogCategory energyWood = new DiameterBasedTreeLogCategory(Grade.EnergyWood, species, 7.0d, 1d, 0d, false, null);
		categories.add(new DiameterBasedTreeLogCategory(Grade.LargeLumberWood, species, 37.5, 0.84, 0d, false, energyWood));	// not small end but dbh in this case
		categories.add(new DiameterBasedTreeLogCategory(Grade.SmallLumberWood, species, 27.5, 0.50, 0d, false, energyWood));
		categories.add(energyWood);
	}

	@Override
	public boolean isCorrect() {return true;}

	
	@Override
	public DiameterBasedTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new DiameterBasedTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

//	protected DiameterBasedTreeLogCategory getLargeLumberWoodLogCategory() {
//		if (LargeLumber == null) {
//			for (DiameterBasedTreeLogCategory lc : this.getLogCategories().get(TreeLoggerParameters.ANY_SPECIES)) {
//				if (lc.getGrade() == Grade.LargeLumberWood) {
//					LargeLumber = lc;
//				}
//			}
//		}
//		return LargeLumber;
//	}

	@Override
	protected Object getDefaultSpecies() {
		return TreeLoggerParameters.ANY_SPECIES;
	}

	public static void main(String[] args) {
		DiameterBasedTreeLoggerParameters params = new DiameterBasedTreeLoggerParameters(DiameterBasedTreeLogger.class);
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
		params.showUI(null);
		params.showUI(null);
		System.exit(0);
	}

}
