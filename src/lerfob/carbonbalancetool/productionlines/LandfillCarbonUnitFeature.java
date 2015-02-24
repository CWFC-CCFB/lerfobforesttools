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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LandfillCarbonUnitFeature extends CarbonUnitFeature implements ChangeListener {

	private static final long serialVersionUID = 20101118L;

	private double degradableOrganicCarbonFraction = 0.4;

	/**
	 * The empty constructor is handled by the interface.
	 */
	protected LandfillCarbonUnitFeature(AbstractProductionLineProcessor processor) {
		super(processor);
		setAverageLifetime(25);
	}

		
	@Override
	public LandfillCarbonUnitFeaturePanel getGuiInterface() {
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
			degradableOrganicCarbonFraction = getUserInterfacePanel().degradableOrganicCarbonFractionSlider.getValue() * factor;
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

	
}
