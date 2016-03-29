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
		mnFile.setEnabled(false);
		mnEdit.setEnabled(false);
		mnSpecies.setEnabled(false);			//	the species cannot be changed 
		mnLogGrade.setEnabled(false);			// the log grade cannot be changed either
		mnTools.setEnabled(false);
		logGradeGoDown.setEnabled(false);		// the log grade cannot be changed either
		logGradeGoUp.setEnabled(false);
	}

	@Override
	protected void settingsAction() {}

}
