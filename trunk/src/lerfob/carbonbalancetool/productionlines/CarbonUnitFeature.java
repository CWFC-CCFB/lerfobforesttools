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

import java.io.Serializable;


import repicea.gui.UserInterfaceableObject;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;

public class CarbonUnitFeature implements Serializable, 
										UserInterfaceableObject, 
										NumberFieldListener {

	private static final long serialVersionUID = 20101118L;

	private double averageLifetime;

	private AbstractProductionLineProcessor processor;
	
	private transient CarbonUnitFeaturePanel userInterfacePanel;

	protected CarbonUnitFeature(AbstractProductionLineProcessor processor) {
		setProcessor(processor);
	}
	
	/*
	 * Accessors
	 */
	protected double getAverageLifetime() {return averageLifetime;}
	protected void setAverageLifetime(double d) {averageLifetime = d;}
	
	protected CarbonUnitFeaturePanel getUserInterfacePanel() {return userInterfacePanel;}
	protected void setUserInterfacePanel(CarbonUnitFeaturePanel panel) {this.userInterfacePanel = panel;}
	
	protected AbstractProductionLineProcessor getProcessor() {return processor;}
	protected void setProcessor(AbstractProductionLineProcessor processor) {this.processor = processor;}
	
	

	@Override
	public CarbonUnitFeaturePanel getGuiInterface() {
		if (getUserInterfacePanel() == null) {
			setUserInterfacePanel(new CarbonUnitFeaturePanel(this));
		}
		return getUserInterfacePanel();
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
		if (e.getSource().equals(getGuiInterface().averageLifetimeTextField)) {
			setAverageLifetime(Double.parseDouble(getGuiInterface().averageLifetimeTextField.getText()));
		} 
	}
	
}
