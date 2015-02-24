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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;

import repicea.gui.REpiceaPanel;

@SuppressWarnings("serial")
class CarbonAccountingToolSingleViewPanel extends REpiceaPanel implements ItemListener, PropertyChangeListener {

	@SuppressWarnings("rawtypes")
	private JComboBox selector;
	private JScrollPane viewerScrollPane;		
	private CarbonAccountingToolOptionPanel optionPanel;
	private CarbonAssessmentToolSimulationResult summary;
	private final String name;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	CarbonAccountingToolSingleViewPanel(CarbonAccountingToolOptionPanel optionPanel, 
			CarbonAssessmentToolSimulationResult summary,
			String name) {
		super();
		this.summary = summary;
		this.optionPanel = optionPanel;
		this.name = name;
		selector = new JComboBox();
		viewerScrollPane = new JScrollPane();
		setLayout(new BorderLayout());
		add(selector, BorderLayout.NORTH);
		add(viewerScrollPane, BorderLayout.CENTER);
		selector.addItem(new CarbonAccountingToolBudgetViewer(summary, optionPanel));
		if (summary instanceof CarbonAssessmentToolSingleSimulationResult) {
			selector.addItem(new CarbonAccountingToolCompartmentViewer(summary, optionPanel));
			selector.addItem(new CarbonAccountingToolLogGradeViewer(summary));
			selector.addItem(new CarbonAccountingToolProductViewer(summary));
			selector.addItem(new CarbonAccountingToolProductWithRecyclingViewer(summary));
		}
		selector.setSelectedIndex(0);
	}
	
	
	protected CarbonAssessmentToolSimulationResult getSummary() {return summary;}
	
	@Override
	public void refreshInterface() {
		CarbonAccountingToolViewer viewer = (CarbonAccountingToolViewer) selector.getSelectedItem();
		viewerScrollPane.getViewport().setView(viewer);
		viewer.refreshInterface();
		repaint();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		refreshInterface();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		refreshInterface();
	}

	@Override
	public void listenTo() {
		selector.addItemListener(this);
		optionPanel.addPropertyChangeListener("compartmentSelection", this);
	}

	@Override
	public void doNotListenToAnymore() {
		selector.removeItemListener(this);
		optionPanel.removePropertyChangeListener("compartmentSelection", this);
	}

	@Override
	public String toString() {
		return name;
	}
	
}
