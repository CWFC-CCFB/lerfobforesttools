/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2020 Mathieu Fortin for Canadian Forest Service, 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
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

import java.awt.Color;
import java.awt.Graphics;
import java.util.Collection;
import java.util.List;

import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager.CarbonTestProcessUnit;
import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.simulation.processsystem.ProcessUnit;
import repicea.simulation.processsystem.Processor;
import repicea.simulation.processsystem.SystemPanel;

/**
 * The AbstractExtractionProcessor class defines a process that extracts something before splitting
 * the ElementUnit instance (e.g. debarking).
 * @author Mathieu Fortin - September 2020
 */
public abstract class AbstractExtractionProcessor extends AbstractProcessor {

	
	protected static class CustomizedREpiceaGUIPermission implements REpiceaGUIPermission {

		@Override
		public boolean isDragGranted() {
			return true;
		}

		@Override
		public boolean isDropGranted() {
			return false;
		}

		@Override
		public boolean isSelectionGranted() {
			return true;
		}

		@Override
		public boolean isEnablingGranted() {
			return true;
		}
	}

	/**
	 * The LandfillProcessorButton has a specific icon for GUI.
	 * @author Mathieu Fortin - May 2014
	 */
	protected static class ExtractionProcessorButton extends AbstractProcessorButton {
		protected ExtractionProcessorButton(SystemPanel panel, AbstractExtractionProcessor process) {
			super(panel, process, new CustomizedREpiceaGUIPermission());
		}
		
		
		@Override
		public void paint(Graphics g) {
			if (!getOwner().hasSubProcessors()) {
				setBorderColor(Color.RED);
				setBorderWidth(2);
			} else {
				setBorderColor(Color.BLACK);
				setBorderWidth(1);
			}
			super.paint(g);
		}

	}
	
	protected Collection<ProcessUnit> extractAndProcess(Processor fatherProcessor, List<ProcessUnit> processUnits) {
		List<ProcessUnit> extractedUnits = extract(processUnits);
		if (!extractedUnits.isEmpty()) {
			for (ProcessUnit pu : extractedUnits) {
				if (pu instanceof CarbonTestProcessUnit) {
					((CarbonTestProcessUnit) pu).recordProcessor(fatherProcessor);	// we must also record the father processor otherwise it is simply skipped
					((CarbonTestProcessUnit) pu).recordProcessor(this);
				}
			}
			return doProcess(extractedUnits);
		} else {
			return extractedUnits;
		}
	}

	protected abstract List<ProcessUnit> extract(List<ProcessUnit> processUnits);
	
}
