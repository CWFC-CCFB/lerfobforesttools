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
package lerfob.carbonbalancetool.productionlines;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;

import javax.swing.JComboBox;

import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import repicea.gui.REpiceaUIObject;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CarbonUnitFeature implements Serializable, REpiceaUIObject, NumberFieldListener, ItemListener {

	private static final long serialVersionUID = 20101118L;
	
	private static final double HALFLIFE_TO_MEANLIFETIME_CONSTANT = 1d / Math.log(2d);

	protected static enum LifetimeMode implements TextableEnum {
		HALFLIFE("Half-life (yr)", "Demi-vie (ann\u00E9es)"),
		AVERAGE("Average lifetime (yr)", "Dur\u00E9e de vie moyenne (ann\u00E9es)"); 

		LifetimeMode(String englishText, String frenchText) {
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

	/**
	 * IMPORTANT: This field can be either the average lifetime or the half-life. The conversion is handled 
	 * by the getAverageLifetime() method.
	 */
	protected double averageLifetime;

	private AbstractProductionLineProcessor processor;
	
	private transient CarbonUnitFeaturePanel userInterfacePanel;

	private LifetimeMode lifetimeMode;

	protected CarbonUnitFeature(AbstractProductionLineProcessor processor) {
		setProcessor(processor);
		lifetimeMode = LifetimeMode.HALFLIFE; // default value
	}
	
	protected LifetimeMode getLifetimeMode() {
		if (lifetimeMode == null) {
			lifetimeMode = LifetimeMode.AVERAGE;	// for former implementation
		}
		return lifetimeMode;
	}

	/*
	 * Accessors
	 */
	protected double getAverageLifetime(MonteCarloSimulationCompliantObject subject) {
		double meanLifetime; 
		if (getLifetimeMode() == LifetimeMode.AVERAGE) {
			meanLifetime = averageLifetime;
		} else {
			meanLifetime = averageLifetime * HALFLIFE_TO_MEANLIFETIME_CONSTANT;
		}
		if (subject != null) {
			return meanLifetime * CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.Lifetime, subject, toString());
		} else {
			return meanLifetime;
		}
	}
	
	protected void setAverageLifetime(double d) {averageLifetime = d;}
	
	protected CarbonUnitFeaturePanel getUserInterfacePanel() {return userInterfacePanel;}
	protected void setUserInterfacePanel(CarbonUnitFeaturePanel panel) {this.userInterfacePanel = panel;}
	
	protected AbstractProductionLineProcessor getProcessor() {return processor;}
	protected void setProcessor(AbstractProductionLineProcessor processor) {this.processor = processor;}
	
	

	@Override
	public CarbonUnitFeaturePanel getUI() {
		if (getUserInterfacePanel() == null) {
			setUserInterfacePanel(new CarbonUnitFeaturePanel(this));
		}
		return getUserInterfacePanel();
	}

	@Override 
	public boolean isVisible() {
		return getUserInterfacePanel() != null && getUserInterfacePanel().isVisible();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CarbonUnitFeature)) {
			return false;
		} else {
			CarbonUnitFeature cuf = (CarbonUnitFeature) obj;
			if (cuf.averageLifetime != averageLifetime) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(getUI().averageLifetimeTextField)) {
			double value = Double.parseDouble(getUI().averageLifetimeTextField.getText());
			if (value != averageLifetime) {
				((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
				setAverageLifetime(value);
			}
		} 
	}
	
	@Override
	public String toString() {
		return getProcessor().getName() + "_" + averageLifetime;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			Object obj = ((JComboBox) evt.getSource()).getSelectedItem();
			if (obj instanceof LifetimeMode) {
				LifetimeMode newLifetimeMode = (LifetimeMode) obj;
				if (newLifetimeMode != lifetimeMode) {
					((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
					lifetimeMode = newLifetimeMode;
					System.out.println("Lifetime mode switch to " + lifetimeMode);
				}
			}
		}	
	}
	
}
