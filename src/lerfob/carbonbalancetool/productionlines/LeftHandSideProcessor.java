/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2015 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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

import repicea.gui.permissions.REpiceaGUIPermission;
import repicea.simulation.processsystem.Processor;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.SystemPanel;

@SuppressWarnings("serial")
public abstract class LeftHandSideProcessor extends Processor {

	
	protected static class CustomizedREpiceaGUIPermission implements REpiceaGUIPermission {

		@Override
		public boolean isDragAndDropGranted() {
			return true;
		}

		@Override
		public boolean isSelectionGranted() {
			return false;
		}

		@Override
		public boolean isEnablingGranted() {
			return false;
		}
		
	}
	
	
	
	public static class LeftHandSideProcessorButton extends ProcessorButton {
				
		protected LeftHandSideProcessorButton(SystemPanel panel, Processor process) {
			super(panel, process, new CustomizedREpiceaGUIPermission());
		}

		@SuppressWarnings("rawtypes")
		protected void setDragMode(Enum mode) {
			super.setDragMode(mode);
			buttonMoveRecognizer.setEnabled(false);		// no matter the selection, this button does not move
		}

		@Override
		public void setSelected(boolean bool) {}

	}
	
	
}
