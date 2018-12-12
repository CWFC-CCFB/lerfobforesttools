/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2018 Mathieu Fortin 
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
package lerfob.carbonbalancetool.catdiameterbasedtreelogger;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLoggerParameters;
import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class CATDiameterBasedTreeLoggerParameters extends DiameterBasedTreeLoggerParameters {

	protected static enum DefaultSpecies implements TextableEnum {
		Default("By default", "Par d\u00E9faut");

		DefaultSpecies(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	private transient CATDiameterBasedTreeLoggerParametersDialog guiInterface;


	/**
	 * General constructor.
	 */
	public CATDiameterBasedTreeLoggerParameters() {
		super(CATDiameterBasedTreeLogger.class);
	}
	
	@Override
	protected void initializeDefaultLogCategories() {
		getLogCategories().clear();
//		List<DiameterBasedTreeLogCategory> categories = new ArrayList<DiameterBasedTreeLogCategory>();
//		getLogCategories().put(DefaultSpecies.Default, categories);
//		CATDiameterBasedTreeLogCategory energyWood = new CATDiameterBasedTreeLogCategory(Grade.EnergyWood, DefaultSpecies.Default, 7.0d, 1d, 0d, false, null);
//		categories.add(new CATDiameterBasedTreeLogCategory(Grade.LargeLumberWood, DefaultSpecies.Default, 37.5, 0.84, 0d, false, energyWood));	
//		categories.add(new CATDiameterBasedTreeLogCategory(Grade.SmallLumberWood, DefaultSpecies.Default, 27.5, 0.50, 0d, false, energyWood));
//		categories.add(energyWood);
		createLogCategoriesForThisSpecies(DefaultSpecies.Default);
	}
	
	
	
	protected final void createLogCategoriesForThisSpecies(Enum<?> species) {
		List<DiameterBasedTreeLogCategory> categories = new ArrayList<DiameterBasedTreeLogCategory>();
		getLogCategories().put(species, categories);
		CATDiameterBasedTreeLogCategory energyWood = new CATDiameterBasedTreeLogCategory(Grade.EnergyWood, species, 7.0d, 1d, 0d, false, null);
		categories.add(new CATDiameterBasedTreeLogCategory(Grade.LargeLumberWood, species, 37.5, 0.84, 0d, false, energyWood));	
		categories.add(new CATDiameterBasedTreeLogCategory(Grade.SmallLumberWood, species, 27.5, 0.50, 0d, false, energyWood));
		categories.add(energyWood);
	}
	
	
	
	@Override
	public CATDiameterBasedTreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new CATDiameterBasedTreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	public static void main(String[] args) {
		CATDiameterBasedTreeLoggerParameters params = new CATDiameterBasedTreeLoggerParameters();
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
		params.showUI(null);
		params.showUI(null);
		System.exit(0);
	}
}
