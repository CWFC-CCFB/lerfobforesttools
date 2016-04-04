/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.treelogger.mathilde;

import java.awt.Window;

import repicea.gui.UIControlManager;
import repicea.simulation.treelogger.TreeLoggerParametersDialog;

@SuppressWarnings("serial")
class MathildeTreeLoggerParametersDialog extends TreeLoggerParametersDialog<MathildeTreeLogCategory> {

	static {
		UIControlManager.setTitle(MathildeTreeLoggerParametersDialog.class, "Mathilde Tree Logger", "Module de billonnage Mathilde");
	}

	protected MathildeTreeLoggerParametersDialog(Window window,	MathildeTreeLoggerParameters params) {
		super(window, params);
		logGradePriorityChangeEnabled = false; 
		mnFile.setEnabled(true);
		mnEdit.setEnabled(true);
		mnSpecies.setEnabled(false);			//	the species cannot be changed 
		mnLogGrade.setEnabled(false);			// the log grade cannot be changed either
		mnTools.setEnabled(false);
		logGradeGoDown.setEnabled(false);		// the log grade cannot be changed either
		logGradeGoUp.setEnabled(false);
	}

	@Override
	protected void settingsAction() {}

}
