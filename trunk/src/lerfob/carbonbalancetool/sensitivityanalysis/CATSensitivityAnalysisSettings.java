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
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import repicea.gui.REpiceaShowableUIWithParent;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.stats.Distribution;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CATSensitivityAnalysisSettings implements REpiceaShowableUIWithParent {
	
	
	private static CATSensitivityAnalysisSettings settings; 
	
	private transient CATSensitivityAnalysisSettingsDlg guiInterface;
	
	public static enum VariabilitySource implements TextableEnum {
		BiomassExpansionFactor("Biomass expansion factors", "Facteur d'expansion de biomasse", 0, 15, 30),
		BasicDensity("Wood basic densities", "Infradensit\u00E9s", 0, 20, 40),
		CarbonContent("Carbon content", "Teneur en carbone", 0, 5, 10),
		Lifetime("HWP lifetimes", "Dur\u00E9es de vie des produits", 0, 50, 70),
		SubstitutionFactors("Substitution factors", "Facteurs de substitution", 0, 50, 100);
		
		private final int min;
		private final int max;
		private final int suggestedIPCCValue;
		
		VariabilitySource(String englishText, String frenchText, int min, int suggestedIPCCValue, int max) {
			setText(englishText, frenchText);
			this.min = min;
			this.max = max;
			this.suggestedIPCCValue = suggestedIPCCValue;
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
		
		protected int getMinimumValue() {return min;}
		protected int getMaximumValue() {return max;}
		public int getSuggestedIPCCValue() {return suggestedIPCCValue;}
	}

	
	
	
	protected final Map<VariabilitySource, CATSensitivityAnalysisParameterWrapper> sensitivityParameterMap;
	
	protected int nbMonteCarloRealizations;
	protected boolean isModelStochastic;
	
	private CATSensitivityAnalysisSettings() {
		sensitivityParameterMap = new HashMap<VariabilitySource, CATSensitivityAnalysisParameterWrapper>();
		for (VariabilitySource source : VariabilitySource.values()) {
			sensitivityParameterMap.put(source, new CATSensitivityAnalysisParameterWrapper(source));
		}
	}
	
	@Override
	public Component getUI(Container parent) {
		if (guiInterface == null) {
			guiInterface = new CATSensitivityAnalysisSettingsDlg(this, (Window) parent);
		}
		return guiInterface;
	}

	@Override
	public void showUI(Window parent) {
		getUI(parent).setVisible(true);
	}

	/**
	 * This method returns the singleton instance of the CATSensitivityAnalysisSettings class.
	 * @return a CATSensitivityAnalysisSettings instance
	 */
	public static CATSensitivityAnalysisSettings getInstance() {
		if (settings == null) {
			settings = new CATSensitivityAnalysisSettings();
		}
		return settings;
	}

	/**
	 * This method returns the multiplicative modifier for sensitivity analysis.
	 * @param source the source of variability (a VariabilitySource enum)
	 * @param subject a MonteCarloSimulationCompliantObject instance
	 * @return a double
	 */
	public double getModifier(VariabilitySource source, MonteCarloSimulationCompliantObject subject, String subjectID) {
		return sensitivityParameterMap.get(source).getValue(subject, subjectID);
	}
	
	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	/**
	 * This method returns the number of Monte Carlo realizations for the sensitivity analysis.
	 * @return an Integer between 1 and 1000
	 */
	public int getNumberOfMonteCarloRealizations() {return nbMonteCarloRealizations;}
	
	/**
	 * This method sets the number of Monte Carlo realizations for the sensitivity analysis.
	 * @param nbMonteCarloRealizations a positive integer between 1 and 1000
	 */
	public void setNumberOfMonteCarloRealizations(int nbMonteCarloRealizations) {
		if (nbMonteCarloRealizations < 1 || nbMonteCarloRealizations > 1000) {
			throw new InvalidParameterException("The number of Monte Carlo realizations must be between 1 and 1000");
		}
		this.nbMonteCarloRealizations = nbMonteCarloRealizations;
		if (guiInterface != null) {
			guiInterface.synchronizeUIWithOwner();
		}
	}
	
	/**
	 * This method sets the model to stochastic. 
	 * @param isModelStochastic a boolean
	 */
	public void setModelStochastic(boolean isModelStochastic) {
		this.isModelStochastic = isModelStochastic;
		if (guiInterface != null) {
			guiInterface.synchronizeUIWithOwner();
		}
	}
	
	public boolean isModelStochastic() {return isModelStochastic;}
	
	/**
	 * This method sets the different parameters of the sensitivity analysis
	 * @param source the source of variability
	 * @param type the distribution (either uniform (default) or Gaussian
	 * @param enabled true to enable or false to disable
	 * @param multiplier a value between 0.0 and 0.5 (50%)
	 */
	public void setVariabilitySource(VariabilitySource source, Distribution.Type type, boolean enabled, double multiplier) {
		if (multiplier > 0.5 || multiplier < 0d) {
			throw new InvalidParameterException("The multiplier must be a value between 0.0 and 0.5");
		}
		CATSensitivityAnalysisParameterWrapper wrapper = sensitivityParameterMap.get(source);
		CATSensitivityAnalysisParameter<?> parm = wrapper.getParameter(type);
		parm.setParametersVariabilityEnabled(enabled);
		wrapper.selectedDistributionType = type;	// at the point we are sure that type is either Gaussian or uniform
		parm.setMultiplier(multiplier);
	}

	public static void main(String[] args) {
		REpiceaTranslator.setCurrentLanguage(Language.French);
		CATSensitivityAnalysisSettings settings = new CATSensitivityAnalysisSettings();
		settings.showUI(null);
	}
}
