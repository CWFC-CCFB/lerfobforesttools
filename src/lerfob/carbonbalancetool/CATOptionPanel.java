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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import repicea.gui.UIControlManager;

/**
 * This inner class is just a JPanel with check boxes to display the compartment options. 
 * @author Mathieu Fortin - November 2012
 */
public class CATOptionPanel extends JScrollPane implements ItemListener {
	
	private static final long serialVersionUID = 1L;
	private List<CATCompartmentCheckBox> booleanSettings;

	public CATOptionPanel() {
		booleanSettings = new ArrayList<CATCompartmentCheckBox>();

		JPanel optionPanel = new JPanel(new BorderLayout());

		JLabel showSomethingLabel = UIControlManager.getLabel(CATFrame.MessageID.ShowSomethingOptions);
		JPanel showSomethingLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		showSomethingLabelPanel.add(showSomethingLabel);
		optionPanel.add(showSomethingLabelPanel, BorderLayout.NORTH);

		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));

		JPanel stockLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel stockLabel = UIControlManager.getLabel ("Stock");
		stockLabelPanel.add(stockLabel);
		checkBoxPanel.add(stockLabelPanel);
		
		for (CompartmentInfo compartmentID : CompartmentInfo.getNaturalOrder()) {
			if (!compartmentID.isFlux()) {
				checkBoxPanel.add(getCheckBoxPanel(compartmentID, !compartmentID.isPrimaryCompartment()));		// no offset since this is a main compartment
			}
		}
		
		JPanel fluxLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel fluxLabel = UIControlManager.getLabel("Flux");
		fluxLabelPanel.add(fluxLabel);
		checkBoxPanel.add(fluxLabelPanel);

		for (CompartmentInfo compartmentID : CompartmentInfo.getNaturalOrder()) {
			if (compartmentID.isFlux()) {
				checkBoxPanel.add(getCheckBoxPanel(compartmentID, !compartmentID.isPrimaryCompartment()));		// no offset since this is a main compartment
			}
		}
				
		JPanel intermediatePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		intermediatePanel.add(Box.createHorizontalStrut(20));
		intermediatePanel.add(checkBoxPanel);
		
		optionPanel.add(intermediatePanel, BorderLayout.CENTER);
		setViewportView(optionPanel);
		Dimension preferredSize = new Dimension(300, 668);
		setPreferredSize(preferredSize);
	}
	
	
	private JPanel getCheckBoxPanel(CompartmentInfo compartmentID, boolean offset) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		CATCompartmentCheckBox checkBox = new CATCompartmentCheckBox(compartmentID);
		booleanSettings.add(checkBox);
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
			if (chkBox.isSelected()) {
				oVec.add(chkBox.getCompartmentID());
			}
		}
		return oVec;
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		firePropertyChange("compartmentSelection", null, null);
	}
	
	
}
