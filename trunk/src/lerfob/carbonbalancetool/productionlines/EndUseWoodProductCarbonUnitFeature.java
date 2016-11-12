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
		NONE("Industrial loss", "Perte industrielle"), 
		ENERGY("Energy wood", "Bois \u00E9nergie"), 
		PAPER("Paper", "Papier"), 
		WRAPPING("Packages", "Emballages"), 
		FURNITURE("Furniture", "Ameublement"), 
		BARREL("Staves", "Tonnellerie"), 
		BUILDING("Building", "Construction"),
		FIREWOOD("Fire wood", "Bois de feu"),
		RESIDUALS_FOR_ENERGY("Residues energy", "R\u00E9sidus pour \u00E9nergie"),
		BRANCHES_FOR_ENERGY("Branches", "Menus bois"),
		STUMPS_FOR_ENERGY("Stumps", "Souches");

		UseClass(String englishText, String frenchText) {
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
	@Deprecated
	private boolean disposable;
	@Deprecated
	private double disposableProportion;
	private UseClass useClass;
	
	/**
	 * The average C to m3 of raw material substitution ratio.
	 */
	@Deprecated
	private double averageSubstitution;
	
	private double relativeSubstitutionCO2EqFonctionalUnit;
	
	private double biomassOfFunctionalUnit;

	@Deprecated
	private LifeCycleAnalysis lca;

	
	/**
	 * The empty constructor is handled by the interface.
	 */
	protected EndUseWoodProductCarbonUnitFeature(ProductionLineProcessor processor) {
		super(processor);
		useClass = UseClass.NONE;
	}
	
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
			return relativeSubstitutionCO2EqFonctionalUnit * CATSensitivityAnalysisSettings.getInstance().getModifier(VariabilitySource.SubstitutionFactors, manager);
		} else {
			return relativeSubstitutionCO2EqFonctionalUnit;
		}
	}
	
	protected double getBiomassOfFunctionalUnit() {
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
	
	protected double getDisposableProportion() {
		if (((ProductionLineProcessor) getProcessor()).disposedToProcessor != null) {		// new implementation
			return 1;
		} else {
			return disposableProportion;		// former implementation
		}
	}
	protected void setDisposableProportion(double disposableProportion) {this.disposableProportion = disposableProportion;} 
	
	/*
	 * Called when the document in the averageSubstitutionTextField member is updated.
	 */
	@Override
	public void numberChanged(NumberFieldEvent e) {
		if (e.getSource().equals(getUI().substitutionTextField)) {
			relativeSubstitutionCO2EqFonctionalUnit = Double.parseDouble(getUI().substitutionTextField.getText());
		} else if (e.getSource().equals(getUI().biomassFUTextField)) {
			biomassOfFunctionalUnit = Double.parseDouble(getUI().biomassFUTextField.getText());
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
				useClass = (UseClass) obj;
//				System.out.println("Use class changed for " + useClass.name());
//			} else if (obj instanceof LifeCycleAnalysis.ReferenceLCA) {
//				lca = (LifeCycleAnalysis) ((LifeCycleAnalysis.ReferenceLCA) obj).getLCA();
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
			if (cuf.lca != null) {
				if (!cuf.lca.equals(lca)) {
					return false;
				}
			}
		}
		return super.equals(obj);
	}

}
