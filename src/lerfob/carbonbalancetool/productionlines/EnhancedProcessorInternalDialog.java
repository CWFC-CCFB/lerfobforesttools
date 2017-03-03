package lerfob.carbonbalancetool.productionlines;

import java.awt.Window;

import javax.swing.Box;
import javax.swing.JPanel;

import repicea.gui.CommonGuiUtility;
import repicea.gui.UIControlManager;
import repicea.gui.components.NumberFormatFieldFactory;
import repicea.gui.components.NumberFormatFieldFactory.JFormattedNumericField;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldListener;
import repicea.simulation.processsystem.ProcessorButton;
import repicea.simulation.processsystem.ProcessorInternalDialog;
import repicea.simulation.processsystem.SystemManager;
import repicea.simulation.processsystem.SystemManagerDialog;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
public class EnhancedProcessorInternalDialog extends ProcessorInternalDialog implements NumberFieldListener {

	protected static enum MessageID implements TextableEnum {
		FunctionalUnitBiomassLabel("Dry biomass per functional unit (Mg)", "Biomasse s\u00E8che de l'unit\u00E9 fonctionnelle (Mg)"),
		EmissionsLabel("Emissions per functional unit (Mg CO2 Eq.)", "Emission par unit\u00E9 fonctionelle (Mg CO2 Eq.)")
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
	
	protected JFormattedNumericField functionUnitBiomass;
	protected JFormattedNumericField emissionsByFunctionUnit;
	
	protected EnhancedProcessorInternalDialog(Window parent, ProcessorButton callerButton) {
		super(parent, callerButton);
	}
	
	@Override
	protected void initializeComponents() {
		super.initializeComponents();	
		functionUnitBiomass = NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.Positive,
				false);
		functionUnitBiomass.setColumns(5);
		functionUnitBiomass.setText(((Double) getCaller().functionUnitBiomass).toString());
		emissionsByFunctionUnit = NumberFormatFieldFactory.createNumberFormatField(NumberFormatFieldFactory.Type.Double,
				NumberFormatFieldFactory.Range.Positive,
				false);
		emissionsByFunctionUnit.setColumns(5);
		emissionsByFunctionUnit.setText(((Double) getCaller().emissionsByFunctionalUnit).toString());
	}
	
	@Override
	public void setVisible(boolean bool) {
		if (!isVisible() && bool) {
			SystemManagerDialog dlg = (SystemManagerDialog) CommonGuiUtility.getParentComponent(this, SystemManagerDialog.class);
			boolean isEnablingGranted = ((SystemManager) dlg.getWindowOwner()).getGUIPermission().isEnablingGranted();
			functionUnitBiomass.setEnabled(isEnablingGranted);
			emissionsByFunctionUnit.setEnabled(isEnablingGranted);
		}
		super.setVisible(bool);
	}
		
	
	@Override
	protected AbstractProcessor getCaller() {
		return (AbstractProcessor) super.getCaller();
	}
	
	@Override
	protected JPanel setTopComponent() {
		JPanel topComponent = super.setTopComponent();
		if (getCaller().hasSubProcessors()) {
			JPanel panel = UIControlManager.createSimpleHorizontalPanel(MessageID.FunctionalUnitBiomassLabel, functionUnitBiomass, 5, true);
			topComponent.add(panel);
			topComponent.add(Box.createVerticalStrut(5));

			panel = UIControlManager.createSimpleHorizontalPanel(MessageID.EmissionsLabel, emissionsByFunctionUnit, 5, true);
			topComponent.add(panel);
			topComponent.add(Box.createVerticalStrut(5));
		} else {
			topComponent.add(new JPanel());
		}
		return topComponent;
	}


	@Override
	public void listenTo() {
		super.listenTo();
		functionUnitBiomass.addNumberFieldListener(this);
		emissionsByFunctionUnit.addNumberFieldListener(this);
	}

	@Override
	public void doNotListenToAnymore() {
		super.doNotListenToAnymore();
		functionUnitBiomass.removeNumberFieldListener(this);
		emissionsByFunctionUnit.removeNumberFieldListener(this);
	}


	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(functionUnitBiomass)) {
			double value = (Double) functionUnitBiomass.getValue();
			if (value != getCaller().functionUnitBiomass) {
				((AbstractProcessorButton) getCaller().getUI()).setChanged(true);
				getCaller().functionUnitBiomass = value;
			}
		} else if (e.getSource().equals(emissionsByFunctionUnit)) {
			double value = (Double) emissionsByFunctionUnit.getValue();
			if (value != getCaller().emissionsByFunctionalUnit) {
				((AbstractProcessorButton) getCaller().getUI()).setChanged(true);
				getCaller().emissionsByFunctionalUnit = value; 
			}
		}
	}
	
}
