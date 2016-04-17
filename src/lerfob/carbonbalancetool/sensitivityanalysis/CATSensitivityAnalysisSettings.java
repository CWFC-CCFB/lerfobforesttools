/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.carbonbalancetool.sensitivityanalysis;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.util.HashMap;
import java.util.Map;

import repicea.gui.ShowableObjectWithParent;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CATSensitivityAnalysisSettings implements ShowableObjectWithParent {

	
	private transient CATSensitivityAnalysisSettingsDlg guiInterface;
	
	protected static enum VariabilitySource implements TextableEnum {
		BiomassExpansionFactor("Biomass expansion factors", "Facteur d'expansion de biomasse"),
		BasicDensity("Wood basic densities", "Infradensit\u00E9s"),
		CarbonContent("Carbon content", "Teneur en carbone");
		
		VariabilitySource(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
	}
	
	protected final Map<VariabilitySource, CATSensitivityParameterWrapper> sensitivityParameterMap;
	
	
	protected CATSensitivityAnalysisSettings() {
		sensitivityParameterMap = new HashMap<VariabilitySource, CATSensitivityParameterWrapper>();
		for (VariabilitySource source : VariabilitySource.values()) {
			sensitivityParameterMap.put(source, new CATSensitivityParameterWrapper(source));
		}
	}
	
	@Override
	public Component getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new CATSensitivityAnalysisSettingsDlg(this, (Window) parent);
		}
		return guiInterface;
	}

	@Override
	public void showInterface(Window parent) {
		getGuiInterface(parent).setVisible(true);
	}

	public static void main(String[] args) {
		CATSensitivityAnalysisSettings settings = new CATSensitivityAnalysisSettings();
		settings.showInterface(null);
	}
}
