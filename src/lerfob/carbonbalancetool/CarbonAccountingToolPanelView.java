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
import java.security.InvalidParameterException;

import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaPanel;
import repicea.gui.components.REpiceaTabbedPane;

@SuppressWarnings("serial")
class CarbonAccountingToolPanelView extends REpiceaPanel implements ChangeListener {

	protected class CarbonTabbedPane extends REpiceaTabbedPane {
		@Override
		protected void postRemovalActions() {
			CarbonAccountingToolPanelView.this.checkIfExportAndComparisonShouldBeEnabled();
		}
	}
	
	
	
	private CarbonAccountingToolOptionPanel optionPanel;
	protected CarbonTabbedPane tabbedPane;
	
	/**	
	 * Constructor
	 */
	CarbonAccountingToolPanelView(CarbonAccountingToolOptionPanel optionPanel) {
		super();
		if (optionPanel == null) {
			throw new InvalidParameterException("There is no option panel in the left panel!");
		}
		this.optionPanel = optionPanel;
		this.tabbedPane = new CarbonTabbedPane();
		tabbedPane.addChangeListener(this);
		setLayout (new BorderLayout());

		// layout parts
		JSplitPane mainPanel = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT);
		mainPanel.setResizeWeight(0);
		mainPanel.setLeftComponent(optionPanel);
		mainPanel.setRightComponent(tabbedPane);
		mainPanel.setOneTouchExpandable (true);
		int width = (int) optionPanel.getPreferredSize().getWidth();
		mainPanel.setDividerLocation (width);	// divider location

		add(mainPanel);
	}

	protected void addSimulationResult(CarbonAssessmentToolSimulationResult summary, String simulationName) {
		CarbonAccountingToolSingleViewPanel singleViewPanel = new CarbonAccountingToolSingleViewPanel(optionPanel, summary, simulationName);
		tabbedPane.insertTab(simulationName, null, singleViewPanel, summary.toString(), 0);
		tabbedPane.setSelectedIndex(0);
	}
	
	
	@Override
	public void refreshInterface() {}

	@Override
	public void listenTo () {}

	@Override
	public void doNotListenToAnymore() {}

	
	protected void checkIfExportAndComparisonShouldBeEnabled() {
		CarbonAccountingToolDialog dlg = (CarbonAccountingToolDialog) CommonGuiUtility.getParentComponent(this, CarbonAccountingToolDialog.class);
		if (tabbedPane.getTabCount() == 0) {
			dlg.setExportEnabled(false);
			dlg.setScenarioComparisonEnabled(false);
		} else {
			dlg.setExportEnabled(true);
			boolean comparisonEnabled = false;
			int nbTabs = tabbedPane.getTabCount();
			for (int i = 0; i < nbTabs; i++) {
				CarbonAccountingToolSingleViewPanel panel = (CarbonAccountingToolSingleViewPanel) tabbedPane.getComponentAt(i);
				if (panel != null && panel.getSummary() instanceof CarbonAssessmentToolSingleSimulationResult) {
					comparisonEnabled = true;	// at least one simulation result
					break;
				}
			}
			dlg.setScenarioComparisonEnabled(comparisonEnabled);
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabbedPane)) {
			checkIfExportAndComparisonShouldBeEnabled();
			if (tabbedPane.getSelectedComponent() instanceof CarbonAccountingToolSingleViewPanel) {
				((CarbonAccountingToolSingleViewPanel) tabbedPane.getSelectedComponent()).refreshInterface();
			}
		}
	}
	
}

