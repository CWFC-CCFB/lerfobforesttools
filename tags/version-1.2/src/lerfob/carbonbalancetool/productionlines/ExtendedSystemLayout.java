/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;

import lerfob.carbonbalancetool.productionlines.LeftHandSideProcessor.LeftHandSideProcessorButton;
import repicea.simulation.processsystem.SystemLayout;
import repicea.simulation.processsystem.UISetup;

public class ExtendedSystemLayout extends SystemLayout {


	protected ExtendedSystemLayout() {
		super();
	}

	@Override
	public void layoutContainer(Container container) {
		super.layoutContainer(container);
		int j = 1;
		for (int i = 0; i < container.getComponentCount(); i++) {
			Component comp = container.getComponent(i);
			if (comp.isVisible()) {
				if (comp instanceof LeftHandSideProcessorButton) {
					Point tmpPoint = new Point(10, j++ * convertToRelative(UISetup.YGap));
					setInternalSize(tmpPoint);
					LeftHandSideProcessorButton lhsButton = (LeftHandSideProcessorButton) comp;
					lhsButton.setLocation(tmpPoint);
				}
			}
		}
	}

	
}
