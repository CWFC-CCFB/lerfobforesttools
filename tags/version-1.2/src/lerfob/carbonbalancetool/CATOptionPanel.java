/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2013 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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
package lerfob.carbonbalancetool;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.BevelBorder;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.CATCompartment.PoolCategory;
import repicea.gui.UIControlManager;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * This inner class is just a JPanel with check boxes to display the compartment options. 
 * @author Mathieu Fortin - November 2012
 */
@SuppressWarnings("serial")
public class CATOptionPanel extends JScrollPane implements ItemListener {
	
	protected static final String CompartmentSelectionProperty = "compartmentSelection";
	protected static final String BySpeciesSelectionProperty = "bySpeciesSelection";
	
	
	private static enum MessageID implements TextableEnum {
		ForestPools("Forest", "For\u00EAt"),
		HWPPools("Harvested wood products (HWP)", "Produits du bois"),
		OtherPools("Other pools", "Autres pools"),
		TooltipSubstitution("Substitution is only available when comparing scenarios", "La substitution n'est affich\u00E9e que dans les comparaisons de sc\u00E9arios"),
		TooltipFossilFuelEmission("Fossil-fuel emissions are only available when visualizing a single carbon balance", "Les \u00E9missions d'origine fossile ne sont pas affich\u00E9es lors des comparaisons de sc\u00E9arios");
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

	
	
	private List<CATCompartmentCheckBox> booleanSettings;
	private final JCheckBox bySpeciesCheckBox;

	public CATOptionPanel() {
		booleanSettings = new ArrayList<CATCompartmentCheckBox>();

		JPanel topContainer = new JPanel();
		topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
		
		JPanel optionPanel = new JPanel(new BorderLayout());
		optionPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		topContainer.add(optionPanel);
		
		JLabel showSomethingLabel = UIControlManager.getLabel(CATFrame.MessageID.ShowSomethingOptions);
		JPanel showSomethingLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		showSomethingLabelPanel.add(showSomethingLabel);
		optionPanel.add(showSomethingLabelPanel, BorderLayout.NORTH);

		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		checkBoxPanel.add(new JSeparator());

		JPanel forestPoolLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel forestPoolLabel = UIControlManager.getLabel(MessageID.ForestPools);
		forestPoolLabelPanel.add(forestPoolLabel);
		checkBoxPanel.add(forestPoolLabelPanel);
		
		for (CompartmentInfo compartmentID : CompartmentInfo.getNaturalOrder()) {
			if (compartmentID.getPoolCategory() == PoolCategory.Forest) {
				checkBoxPanel.add(getCheckBoxPanel(compartmentID, !compartmentID.isPrimaryCompartment()));		// no offset since this is a main compartment
			}
		}

		checkBoxPanel.add(Box.createVerticalStrut(10));
		checkBoxPanel.add(new JSeparator());
		
		JPanel hwpPoolLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel hwpPoolLabel = UIControlManager.getLabel(MessageID.HWPPools);
		hwpPoolLabelPanel.add(hwpPoolLabel);
		checkBoxPanel.add(hwpPoolLabelPanel);
		
		for (CompartmentInfo compartmentID : CompartmentInfo.getNaturalOrder()) {
			if (compartmentID.getPoolCategory() == PoolCategory.HarvestedWoodProducts) {
				checkBoxPanel.add(getCheckBoxPanel(compartmentID, !compartmentID.isPrimaryCompartment()));		// no offset since this is a main compartment
			}
		}

		checkBoxPanel.add(Box.createVerticalStrut(10));
		checkBoxPanel.add(new JSeparator());

		JPanel fluxLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel fluxLabel = UIControlManager.getLabel(MessageID.OtherPools);
		fluxLabelPanel.add(fluxLabel);
		checkBoxPanel.add(fluxLabelPanel);

		for (CompartmentInfo compartmentID : CompartmentInfo.getNaturalOrder()) {
			if (compartmentID.getPoolCategory() == PoolCategory.Others) {
				if (compartmentID != CompartmentInfo.NetSubs) {		// net flux should not be displayed because it implies double counting (substitution already accounts for emissions)
					checkBoxPanel.add(getCheckBoxPanel(compartmentID, !compartmentID.isPrimaryCompartment()));		// no offset since this is a main compartment
				}
			}
		}

		checkBoxPanel.add(Box.createVerticalStrut(10));
		checkBoxPanel.add(new JSeparator());

		JPanel intermediatePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		intermediatePanel.add(Box.createHorizontalStrut(20));
		intermediatePanel.add(checkBoxPanel);
		
		optionPanel.add(intermediatePanel, BorderLayout.CENTER);
		
		
		JPanel otherOptionsPanel = new JPanel(new BorderLayout());
		otherOptionsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		topContainer.add(otherOptionsPanel);
		
		JLabel otherOptionsLabel = UIControlManager.getLabel(CATFrame.MessageID.OtherOpptions);
		JPanel otherOptionsLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		otherOptionsLabelPanel.add(otherOptionsLabel);
		otherOptionsPanel.add(otherOptionsLabelPanel, BorderLayout.NORTH);

		checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		checkBoxPanel.add(new JSeparator());

		bySpeciesCheckBox = new JCheckBox(CATFrame.MessageID.BySpecies.toString()); 
		checkBoxPanel.add(getCheckBoxPanel(bySpeciesCheckBox, false));
		
		intermediatePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		intermediatePanel.add(Box.createHorizontalStrut(20));
		intermediatePanel.add(checkBoxPanel);
		
		otherOptionsPanel.add(intermediatePanel, BorderLayout.CENTER);
		
		setViewportView(topContainer);
		Dimension preferredSize = new Dimension(300, 668);
		setPreferredSize(preferredSize);
	}
	
	
	private JPanel getCheckBoxPanel(CompartmentInfo compartmentID, boolean offset) {
		CATCompartmentCheckBox checkBox = new CATCompartmentCheckBox(compartmentID);
		booleanSettings.add(checkBox);
		if (compartmentID == CompartmentInfo.EnerSubs) {
			checkBox.setToolTipText(MessageID.TooltipSubstitution.toString());
		}
		if (compartmentID == CompartmentInfo.CarbEmis) {
			checkBox.setToolTipText(MessageID.TooltipFossilFuelEmission.toString());
		}
		return getCheckBoxPanel(checkBox, offset);
	}

	
	private JPanel getCheckBoxPanel(JCheckBox checkBox, boolean offset) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		checkBox.addItemListener(this);
		if (offset) {
			panel.add(Box.createHorizontalStrut(10));
		}
		panel.add(checkBox);
		return panel;
	}

	
	
	/**
	 * This method iterates on the Setting map to find the compartment 
	 * that are requested by the used
	 * @return a List of the compartments to be shown
	 */
	protected List<CompartmentInfo> getCompartmentToBeShown() {
		List<CompartmentInfo> oVec = new ArrayList<CompartmentInfo>();
		for (CATCompartmentCheckBox chkBox : booleanSettings) {
			if (chkBox.isEnabled() && chkBox.isSelected()) {
				oVec.add(chkBox.getCompartmentID());
			}
		}
		return oVec;
	}
	
	protected void enableOnlyIfComparison(boolean isComparison) {
		for (CATCompartmentCheckBox chkBox : booleanSettings) {
			if (chkBox.getCompartmentID() == CompartmentInfo.CarbEmis) {
				chkBox.setEnabled(!isComparison);
			} 
			if (chkBox.getCompartmentID() == CompartmentInfo.EnerSubs) {
				chkBox.setEnabled(isComparison);
			}
		}
	}
	
	
	

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof CATCompartmentCheckBox) {
			firePropertyChange(CompartmentSelectionProperty, null, null);
		} else if (e.getSource().equals(bySpeciesCheckBox)) {
			firePropertyChange(BySpeciesSelectionProperty, null, null);
		}
	}
	
	protected boolean isBySpeciesEnabled() {return bySpeciesCheckBox.isSelected();}
	
}
