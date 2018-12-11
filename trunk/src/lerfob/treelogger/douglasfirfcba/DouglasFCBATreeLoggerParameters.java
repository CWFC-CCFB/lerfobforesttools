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

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public final class DouglasFCBATreeLoggerParameters extends TreeLoggerParameters<DouglasFCBALogCategory> {

	public static enum Grade implements TextableEnum {
		Residues("Harvest residues", "Chutes"),
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

	private transient DouglasFCBATreeLoggerParametersDialog guiInterface;
	
	protected DouglasFCBATreeLoggerParameters() {
		super(DouglasFCBATreeLogger.class);
	}

	@Override
	protected void initializeDefaultLogCategories() {
		getLogCategories().clear();
		List<DouglasFCBALogCategory> categories = new ArrayList<DouglasFCBALogCategory>();
		getLogCategories().put(DouglasFCBALoggableTree.Species.DouglasFir.name(), categories);
		categories.add(new DouglasFCBALogCategory(Grade.LargeLumberWood, 42.5d));
		categories.add(new DouglasFCBALogCategory(Grade.SmallLumberWood, 17.5d));
		categories.add(new DouglasFCBALogCategory(Grade.EnergyWood, 7.5d));
		categories.add(new DouglasFCBALogCategory(Grade.Residues, 0d));
	}

	@Override
	public boolean isCorrect() {return true;}

	@Override
	public DouglasFCBATreeLoggerParametersDialog getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new DouglasFCBATreeLoggerParametersDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	public static void main(String[] args) {
		DouglasFCBATreeLoggerParameters params = new DouglasFCBATreeLoggerParameters();
		params.initializeDefaultLogCategories();
		params.setReadWritePermissionGranted(new DefaultREpiceaGUIPermission(true));
		params.showUI(null);
		params.showUI(null);
		System.exit(0);

	}
}
