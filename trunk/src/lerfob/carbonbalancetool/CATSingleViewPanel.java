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
class CATSingleViewPanel extends REpiceaPanel implements ItemListener, PropertyChangeListener {

	@SuppressWarnings("rawtypes")
	private JComboBox selector;
	private JScrollPane viewerScrollPane;		
	private CATOptionPanel optionPanel;
	private CATSimulationResult summary;
	private final String name;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	CATSingleViewPanel(CATOptionPanel optionPanel, CATSimulationResult summary) {
		super();
		this.summary = summary;
		this.optionPanel = optionPanel;
		this.name = summary.getResultId();
		selector = new JComboBox();
		viewerScrollPane = new JScrollPane();
		setLayout(new BorderLayout());
		add(selector, BorderLayout.NORTH);
		add(viewerScrollPane, BorderLayout.CENTER);
		if (summary instanceof CATSimulationDifference) {
			selector.addItem(new CATResultBudgetPanel(summary, optionPanel));
		} else {
			selector.addItem(new CATResultEvolutionPanel(summary, optionPanel));
			selector.addItem(new CATResultLogGradesPanel(summary));
			selector.addItem(new CATResultHWPPanel(summary));
			selector.addItem(new CATResultHWPWithRecyclingPanel(summary));
			selector.addItem(new CATResultKWhHeatProduction(summary));
			if (summary.isEvenAged()) {
				selector.addItem(new CATResultBudgetPanel(summary, optionPanel));
			}
		}
		selector.setSelectedIndex(0);
	}
	
	
	protected CATSimulationResult getSummary() {return summary;}
	
	private boolean isComparison() {return getSummary() instanceof CATSimulationDifference;}

	
	@Override
	public void refreshInterface() {
		CATResultPanel viewer = (CATResultPanel) selector.getSelectedItem();
		optionPanel.enableOnlyIfComparison(isComparison());
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
