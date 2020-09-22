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

import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.simulation.processsystem.SystemPanel;

/**
 * The ForkOperationAbstractProcessor class defines a process that is carried out before splitting
 * the ElementUnit instance (e.g. debarking).
 * @author Mathieu Fortin - September 2020
 */
public abstract class AbstractForkOperationProcessor extends AbstractProcessor {

	
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
	protected static class AbstractForkOperationProcessorButton extends AbstractProcessorButton {
		protected AbstractForkOperationProcessorButton(SystemPanel panel, AbstractProductionLineProcessor process) {
			super(panel, process, new CustomizedREpiceaGUIPermission());
		}
	}
	

	
}
