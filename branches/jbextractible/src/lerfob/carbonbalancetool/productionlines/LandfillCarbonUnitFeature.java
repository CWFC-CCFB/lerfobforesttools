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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class LandfillCarbonUnitFeature extends CarbonUnitFeature implements ChangeListener, ItemListener {

	private static final long serialVersionUID = 20101118L;

	public static enum LandfillType implements TextableEnum {
		MANAGED_ANAEROBIC("Managed - anaerobic", "Am\u00E9nag\u00E9 - ana\u00E9robique", 1d),
		MANAGED_SEMIANAEROBIC("Managed - semi-anaerobic","Am\u00E9nag\u00E9 - semi-ana\u00E9robique", 0.5),
		UNMANAGED_DEEP("Unmanaged - deep", "Non am\u00E9nag\u00E9 - profond", 0.8),
		UNMANAGED_SHALLOW("Unmanaged - shallow", "Non am\u00E9nag\u00E9 - superficiel", 0.4),
		UNCATEGORISED("Uncategorised", "Inconnu", 0.6);

		private double methaneCorrectionFactor;
		
		
		LandfillType(String englishText, String frenchText, double mcf) {
			setText(englishText, frenchText);
			methaneCorrectionFactor = mcf;
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

		/**
		 * This method returns the methane correction factor of the landfill site.
		 * @return a double
		 */
		public double getMethaneCorrectionFactor() {
			return methaneCorrectionFactor;
		}
	}
	
	
	private double degradableOrganicCarbonFraction = 0.4;  // Default value from Barlaz 2006 and Ximenes al. 2008

	private LandfillType landfillType = LandfillType.MANAGED_SEMIANAEROBIC;
	
	
	/**
	 * The empty constructor is handled by the interface.
	 */
	protected LandfillCarbonUnitFeature(AbstractProductionLineProcessor processor) {
		super(processor);
		setAverageLifetime(33);	// Default value in IPCC 2006 Waste p.3.17
	}

		
	@Override
	public LandfillCarbonUnitFeaturePanel getUI() {
		if (getUserInterfacePanel() == null) {
			setUserInterfacePanel(new LandfillCarbonUnitFeaturePanel(this));
		}
		return getUserInterfacePanel();
	}

	@Override
	protected LandfillCarbonUnitFeaturePanel getUserInterfacePanel() {
		if (super.getUserInterfacePanel() != null) {
			return (LandfillCarbonUnitFeaturePanel) super.getUserInterfacePanel();
		} else {
			return null;
		}
	}

	protected double getDegradableOrganicCarbonFraction() {return degradableOrganicCarbonFraction;}
	
	protected void setDegradableOrganicCarbonFraction(double degradableOrganicCarbonFraction) {
		this.degradableOrganicCarbonFraction = degradableOrganicCarbonFraction;
	}

	@Override
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource().equals(getUserInterfacePanel().degradableOrganicCarbonFractionSlider)) {
			double factor = (double) 1 / getUserInterfacePanel().degradableOrganicCarbonFractionSlider.getMaximum();
			double value = getUserInterfacePanel().degradableOrganicCarbonFractionSlider.getValue() * factor;
			if (value != degradableOrganicCarbonFraction) {
				((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
				degradableOrganicCarbonFraction = value;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LandfillCarbonUnitFeature)) {
			return false;
		} else {
			LandfillCarbonUnitFeature cuf = (LandfillCarbonUnitFeature) obj;
			if (cuf.degradableOrganicCarbonFraction != degradableOrganicCarbonFraction) {
				return false;
			}
		}
		return super.equals(obj);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(getUserInterfacePanel().landFillTypeComboBox)) {
			landfillType = (LandfillType) getUserInterfacePanel().landFillTypeComboBox.getSelectedItem();
		}
	}
	
	/**
	 * This method returns the type of landfill according to the IPCC (2006, Waste, p.3.14) 
	 * @return a LandfillType enum
	 */
	public LandfillType getLandfillType() {
		if (landfillType == null) {		// To ensure a proper deserialization
			landfillType = LandfillType.MANAGED_ANAEROBIC;
		}
		return landfillType;
	}

	
}
