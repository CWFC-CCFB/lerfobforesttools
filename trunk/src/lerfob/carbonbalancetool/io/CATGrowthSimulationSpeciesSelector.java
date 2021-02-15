package lerfob.carbonbalancetool.io;

import java.awt.Container;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lerfob.carbonbalancetool.CATSettings.CATSpecies;
import repicea.gui.UIControlManager;
import repicea.gui.components.REpiceaMatchSelector;
import repicea.gui.components.REpiceaMatchSelectorDialog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

public class CATGrowthSimulationSpeciesSelector extends REpiceaMatchSelector<CATSpecies> {


	@SuppressWarnings("serial")
	protected static class CATGrowthSimulationSpeciesSelectorDialog extends REpiceaMatchSelectorDialog {

		protected static enum MessageID implements TextableEnum {
			Instruction("Please select the species available in CAT to match those found in your input file", 
					"Veuillez associer les esp\u00E8ces reconnues par CAT \u00E0 celles de votre fichier d'entr\u00E9e")
			;
			
			MessageID(String englishText, String frenchText) {
				setText(englishText, frenchText);
			}
					
			@Override
			public void setText(String englishText, String frenchText) {
				REpiceaTranslator.setString(this, englishText, frenchText);
			}
			
			@Override
			public String toString() {return REpiceaTranslator.getString(this);}
		}

		static {
			UIControlManager.setTitle(CATGrowthSimulationSpeciesSelectorDialog.class, "Species correspondance","Correspondance entre les esp\u00E8ces");
		}
		
		protected CATGrowthSimulationSpeciesSelectorDialog(CATGrowthSimulationSpeciesSelector caller, Window parent, Object[] columnNames) {
			super(caller, parent, columnNames);
		}

		protected JPanel getMainPanel() {
			JPanel pane = new JPanel();
			pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

			pane.add(new JLabel(MessageID.Instruction.toString()));
			pane.add(Box.createVerticalStrut(10));
			JScrollPane scrollPane = new JScrollPane(getTable());
			pane.add(createSimplePanel(scrollPane, 20));
			pane.add(Box.createVerticalStrut(10));
			return pane;

		}
		
		
		
	}
	
	
	protected static enum ColumnName implements TextableEnum {
		SpeciesNameInFile("Species name in file", "Nom d'esp\u00E8ce dans le fichier"),
		SpeciesNameInCAT("Species name in CAT", "Nom d'esp\u00E8ce dans CAT"),
		;
		
		ColumnName(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
				
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}

	protected CATGrowthSimulationSpeciesSelector(Object[] toBeMatched) {
		super(toBeMatched, CATSpecies.values(), 0, ColumnName.values());
	}

	@Override
	public CATGrowthSimulationSpeciesSelectorDialog getUI(Container parent) {
		if (this.guiInterface == null) {
			guiInterface = new CATGrowthSimulationSpeciesSelectorDialog(this, (Window) parent, columnNames);
		}
		return (CATGrowthSimulationSpeciesSelectorDialog) guiInterface;
	}
	
}
