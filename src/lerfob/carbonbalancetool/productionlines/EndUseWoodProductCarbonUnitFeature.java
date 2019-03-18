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


import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lerfob.carbonbalancetool.CATCompartmentManager;
import lerfob.carbonbalancetool.productionlines.combustion.CombustionEmissions;
import lerfob.carbonbalancetool.productionlines.combustion.CombustionEmissions.CombustionProcess;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import repicea.gui.components.NumberFormatFieldFactory.NumberFieldDocument.NumberFieldEvent;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;


/**
 * An EndProductFeature instance contains four elements : a use class, a lifecycle analysis, a lifetime duration, and a substitution factor.
 * @author M. Fortin - October 2010
 */
public class EndUseWoodProductCarbonUnitFeature extends CarbonUnitFeature implements ChangeListener, 
																					ItemListener {

	private static final long serialVersionUID = 20101020L;
	
	public static enum UseClass implements TextableEnum {
		NONE("Industrial loss", "Perte industrielle", true), 
		ENERGY("Energy wood", "Bois \u00E9nergie", true), 
		PAPER("Paper", "Papier", false), 
		WRAPPING("Packages", "Emballages", false), 
		FURNITURE("Furniture", "Ameublement", false), 
		BARREL("Staves", "Tonnellerie", false), 
		BUILDING("Building", "Construction", false),
		FIREWOOD("Fire wood", "Bois de feu", true),
		RESIDUALS_FOR_ENERGY("Residues energy", "R\u00E9sidus pour \u00E9nergie", true),
		BRANCHES_FOR_ENERGY("Branches", "Menus bois", true),
		STUMPS_FOR_ENERGY("Stumps", "Souches", true),
		EXTRACTIVE("Wood extractives", "Extractibles du bois", false);

		private final boolean meantForEnergyProduction;
		
		UseClass(String englishText, String frenchText, boolean meantForEnergyProduction) {
			setText(englishText, frenchText);
			this.meantForEnergyProduction = meantForEnergyProduction;
		}
		
		public boolean isMeantForEnergyProduction() {return meantForEnergyProduction;}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
	}
	
	
	
	@Deprecated
	private boolean disposable;
	@Deprecated
	private double disposableProportion;
	private UseClass useClass;
	
	private CombustionProcess combustionProcess;
	
	/**
	 * The average C to m3 of raw material substitution ratio.
	 */
	@Deprecated
	private double averageSubstitution;
	
	private double relativeSubstitutionCO2EqFonctionalUnit;
	
	private double biomassOfFunctionalUnit;		// in Mg
	private double emissionsByFunctionalUnit;	// in Mg
	

	@Deprecated
	private LifeCycleAnalysis lca;

	
	
	/**
	 * The empty constructor is handled by the interface.
	 */
	protected EndUseWoodProductCarbonUnitFeature(ProductionLineProcessor processor) {
		super(processor);
		useClass = UseClass.NONE;
	}
	
	
	protected double getEmissionsMgCO2ByFunctionalUnit() {return emissionsByFunctionalUnit;}
	
	protected UseClass getUseClass() {
		if (useClass == null) {
			useClass = UseClass.NONE;
		}
		return this.useClass;
	}
	
	protected void setUseClass(UseClass useClass) {this.useClass = useClass;}

	@Override
	public EndUseWoodProductCarbonUnitFeaturePanel getUI() {
		if (getUserInterfacePanel() == null) {
			setUserInterfacePanel(new EndUseWoodProductCarbonUnitFeaturePanel(this));
		}
		return getUserInterfacePanel();
	}

	protected double getSubstitutionCO2EqFunctionalUnit(CATCompartmentManager manager) {
		if (manager != null) {
			return relativeSubstitutionCO2EqFonctionalUnit * CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.SubstitutionFactors, manager, toString());
		} else {
			return relativeSubstitutionCO2EqFonctionalUnit;
		}
	}
	
	protected double getBiomassOfFunctionalUnitMg() {
		return biomassOfFunctionalUnit;
	}
	
	@Deprecated
	protected double getAverageSubstitution() {
		return averageSubstitution;
	}
	
	@Deprecated
	protected void setAverageSubstitution(double d) {averageSubstitution = d;}
	
	/**
	 * This method returns true if the lost carbon can be sent to the landfill site.
	 * @return a boolean
	 */
	protected boolean isDisposed() {
		boolean isDisposed = disposable || // former implementation
				((ProductionLineProcessor) getProcessor()).disposedToProcessor != null; // new implementation
		return isDisposed;
	}
	
	@Deprecated
	protected void setDisposable(boolean disposable) {this.disposable = disposable;}
	
	@Deprecated
	protected double getDisposableProportion() {
		if (((ProductionLineProcessor) getProcessor()).disposedToProcessor != null) {		// new implementation
			return 1;
		} else {
			return disposableProportion;		// former implementation
		}
	}
	protected void setDisposableProportion(double disposableProportion) {this.disposableProportion = disposableProportion;} 
	
	/**
	 * Returns the combustion emission factors in CO2 eq. for one Mg of dry biomass.
	 * @return a double
	 */
	protected double getCombustionEmissionFactorsInCO2Eq() {
		if (combustionProcess != null && combustionProcess != CombustionProcess.None) {
			return CombustionEmissions.CombustionEmissionsMap.get(combustionProcess).getEmissionFactorInCO2EqForOneMgOfDryBiomass();
		} else {
			return 0d;
		}
	}
	
	/**
	 * Returns the combustion emission factors in CO2 eq. for one Mg of dry biomass.
	 * @return a double
	 */
	protected double getHeatProductionKWh() {
		if (combustionProcess != null && combustionProcess != CombustionProcess.None) {
			return CombustionEmissions.CombustionEmissionsMap.get(combustionProcess).getHeatProductionInKWhForOneMgOfDryBiomass();
		} else {
			return 0d;
		}
	}
	
	/*
	 * Called when the document in the averageSubstitutionTextField member is updated.
	 */
	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(getUI().substitutionTextField)) {
			double value = Double.parseDouble(getUI().substitutionTextField.getText());
			if (value != relativeSubstitutionCO2EqFonctionalUnit) {
				((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
				relativeSubstitutionCO2EqFonctionalUnit = value;
			}
		} else if (e.getSource().equals(getUI().biomassFUTextField)) {
			double value = Double.parseDouble(getUI().biomassFUTextField.getText());
			if (value != biomassOfFunctionalUnit) {
				((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
				biomassOfFunctionalUnit = value;
			}
		} else if (e.getSource().equals(getUI().emissionsByFUField)) {
			double value = Double.parseDouble(getUI().emissionsByFUField.getText());
			if (value != emissionsByFunctionalUnit) {
				((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
				emissionsByFunctionalUnit = value;
			}
		} else {
			super.numberChanged(e);
		}
	}


	@Override
	public void stateChanged(ChangeEvent evt) {
		if (evt.getSource().equals(getUserInterfacePanel().disposableProportionSlider)) {
			double factor = (double) 1 / getUserInterfacePanel().disposableProportionSlider.getMaximum();
			disposableProportion = getUserInterfacePanel().disposableProportionSlider.getValue() * factor;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void itemStateChanged(ItemEvent evt) {
		if (evt.getSource() instanceof JComboBox) {
			Object obj = ((JComboBox) evt.getSource()).getSelectedItem();
			if (obj instanceof UseClass) {
				UseClass newUseClass = (UseClass) obj;
				if (newUseClass != useClass) {
					((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
					useClass = newUseClass;
				}
			} else if (obj instanceof LifetimeMode) {
				super.itemStateChanged(evt);
			} else if (obj instanceof CombustionProcess) {
				CombustionProcess newCombustionProcess = (CombustionProcess) obj;
				if (newCombustionProcess != combustionProcess) {
					((AbstractProcessorButton) getProcessor().getUI()).setChanged(true);
					combustionProcess = newCombustionProcess;
				}
			}
		} else if (evt.getSource().equals(getUserInterfacePanel().isDisposableCheckBox)) {
			if (getUserInterfacePanel().isEnabled()) {
				disposable = getUserInterfacePanel().isDisposableCheckBox.isSelected();
			}
		}
	}
	
	@Override
	protected EndUseWoodProductCarbonUnitFeaturePanel getUserInterfacePanel() {
		if (super.getUserInterfacePanel() != null) {
			return (EndUseWoodProductCarbonUnitFeaturePanel) super.getUserInterfacePanel();
		} else {
			return null;
		}
	}
	
	@Deprecated
	protected LifeCycleAnalysis getLCA() {return lca;}
	
	@Deprecated
	protected void setLCA(LifeCycleAnalysis lca) {
		this.lca = lca;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof EndUseWoodProductCarbonUnitFeature)) {
			return false;
		} else {
			EndUseWoodProductCarbonUnitFeature cuf = (EndUseWoodProductCarbonUnitFeature) obj;
			if (cuf.disposable != disposable) {
				return false;
			}
			if (cuf.disposableProportion != disposableProportion) {
				return false;
			}
			if (cuf.useClass != useClass) {
				return false;
			}
			if (cuf.averageSubstitution != averageSubstitution) {
				return false;
			}
			if (cuf.relativeSubstitutionCO2EqFonctionalUnit != this.relativeSubstitutionCO2EqFonctionalUnit) {
				return false;
			}
			if (cuf.biomassOfFunctionalUnit != this.biomassOfFunctionalUnit) {
				return false;
			}
			if (cuf.emissionsByFunctionalUnit != this.emissionsByFunctionalUnit) {
				return false;
			}
			if (cuf.lca != null) {
				if (!cuf.lca.equals(lca)) {
					return false;
				}
			}
		}
		return super.equals(obj);
	}

}
