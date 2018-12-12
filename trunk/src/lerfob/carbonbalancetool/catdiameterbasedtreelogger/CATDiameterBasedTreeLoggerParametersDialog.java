package lerfob.carbonbalancetool.catdiameterbasedtreelogger;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import lerfob.carbonbalancetool.CATSettings.CATSpecies;
import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLoggerParametersDialog;
import repicea.simulation.treelogger.TreeLoggerAWTProperty;
import repicea.util.REpiceaTranslator;

@SuppressWarnings("serial")
public class CATDiameterBasedTreeLoggerParametersDialog extends DiameterBasedTreeLoggerParametersDialog {

	
	// TODO avoid deleting the default species
	
	
	protected CATDiameterBasedTreeLoggerParametersDialog(Window window, CATDiameterBasedTreeLoggerParameters params) {
		super(window, params);
		mnFile.setEnabled(true);
		mnEdit.setEnabled(true);
		mnSpecies.setEnabled(true);			//	the species can be changed 
		mnLogGrade.setEnabled(false);			// the log grade cannot be changed either
		mnTools.setEnabled(false);
		logGradeGoDown.setEnabled(false);		// the log grade cannot be changed either
		logGradeGoUp.setEnabled(false);
	}

	
	private List<CATSpecies> retrieveCATSpeciesInCurrentList() {
		List<CATSpecies> currentCATSpecies = new ArrayList<CATSpecies>();
		for (Object key : getTreeLoggerParameters().getLogCategories().keySet()) {
			if (key instanceof CATSpecies) {
				currentCATSpecies.add((CATSpecies) key);
			}
		}
		return currentCATSpecies;
	}

	@Override
	protected Object getDefaultSpecies() {
		return CATDiameterBasedTreeLoggerParameters.DefaultSpecies.Default;
	}
	
	@Override
	protected CATDiameterBasedTreeLoggerParameters getTreeLoggerParameters() {
		return (CATDiameterBasedTreeLoggerParameters) super.getTreeLoggerParameters();
	}
	
	@Override
	protected void speciesAddAction() {
		CATSpecies newSpecies;
		List<CATSpecies> availableSpecies = Arrays.asList(CATSpecies.values());
		availableSpecies.removeAll(retrieveCATSpeciesInCurrentList());
		if (availableSpecies.isEmpty()) {
			// TODO FP display a message here
			return;
		} else {
			newSpecies = (CATSpecies) JOptionPane.showInputDialog(this, 
					REpiceaTranslator.getString(MessageID.PleaseEnterTheSpeciesCode),
					REpiceaTranslator.getString(MessageID.AddANewSpecies),
					JOptionPane.QUESTION_MESSAGE,
					null,
					availableSpecies.toArray(),
					availableSpecies.get(0));
			if (newSpecies == null) {		// means it has been canceled
				return;
			}
		}
		getTreeLoggerParameters().createLogCategoriesForThisSpecies(newSpecies);
		redefineSpeciesList();
		int selectedIndex = 0;
		for (Object speciesName : params.getLogCategories().keySet()) {
			if (speciesName.equals(newSpecies)) {
				speciesList.setSelectedIndex(selectedIndex);
				return;
			}
			selectedIndex++;
		}
		speciesList.setSelectedIndex(0);		// in case no match has been found
		firePropertyChange(TreeLoggerAWTProperty.SpeciesAdded, null, this);
	}


}
