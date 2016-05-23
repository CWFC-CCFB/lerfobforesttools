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
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lerfob.carbonbalancetool.CATFrame.MessageID;
import repicea.gui.AutomatedHelper;
import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaPanel;
import repicea.gui.UIControlManager;
import repicea.gui.UIControlManager.CommonControlID;
import repicea.gui.components.REpiceaSlider;
import repicea.gui.components.REpiceaTabbedPane;
import repicea.gui.popup.REpiceaPopupMenu;
import repicea.net.BrowserCaller;
import repicea.util.REpiceaTranslator;

@SuppressWarnings("serial")
class CarbonAccountingToolPanelView extends REpiceaPanel implements ChangeListener, PropertyChangeListener {

	protected class CarbonTabbedPane extends REpiceaTabbedPane implements ChangeListener {

		private JMenuItem exportMenuItem;
		private JMenuItem compareScenarioMenuItem;
		
		protected CarbonTabbedPane() {
			super();
			addChangeListener(this);
		}
		
		@Override
		protected REpiceaPopupMenu setPopupMenu() {
			exportMenuItem = UIControlManager.createCommonMenuItem(CommonControlID.Export);
			compareScenarioMenuItem = new JMenuItem(MessageID.CompareScenario.toString());
			ImageIcon compareScenariosIcon = CommonGuiUtility.retrieveIcon(getClass(), "compareScenariosIcon.png");
			compareScenarioMenuItem.setIcon(compareScenariosIcon);
			return new REpiceaPopupMenu(this, compareScenarioMenuItem, exportMenuItem, closeButton, closeAllButton, closeOtherButton);
		}

		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if (arg0.getSource().equals(exportMenuItem)) {
				CarbonAccountingToolSingleViewPanel panel = (CarbonAccountingToolSingleViewPanel) getSelectedComponent();
				CATFrame dlg = (CATFrame) CommonGuiUtility.getParentComponent(this, CATFrame.class);
				CATExportTool exportTool;
				try {
					exportTool = new CATExportTool(dlg.caller.getCarbonToolSettings(), panel.getSummary());
					Method callHelp = BrowserCaller.class.getMethod("openUrl", String.class);
					String url = "http://www.inra.fr/capsis/help_"+ 
							REpiceaTranslator.getCurrentLanguage().getLocale().getLanguage() +
							"/capsis/extension/modeltool/carbonaccountingtool/export";
					AutomatedHelper helper = new AutomatedHelper(callHelp, new Object[]{url});
					exportTool.setHelper(helper);
					exportTool.showInterface(dlg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (arg0.getSource().equals(compareScenarioMenuItem)) {
				CATFrame dlg = (CATFrame) CommonGuiUtility.getParentComponent(this, CATFrame.class);
				new CATScenarioComparisonDialog(dlg, CarbonAccountingToolPanelView.this);
			} else {
				super.actionPerformed(arg0);
			} 
		}
		
		
		protected void checkIfComparisonShouldBeEnabled() {
			int nbValidComponents = 0;
			int nbTabs = tabbedPane.getTabCount();
			for (int i = 0; i < nbTabs; i++) {
//				if (i != tabbedPane.getSelectedIndex()) {		// is there anything to compare with
	 				if (isASingleResultSummary(i)) {
						nbValidComponents++;
					}
//				}
			}
			compareScenarioMenuItem.setEnabled(nbValidComponents >= 1 && isASingleResultSummary(tabbedPane.getSelectedIndex()));
		}

		private boolean isASingleResultSummary(int selectedIndex) {
			CarbonAccountingToolSingleViewPanel panel = (CarbonAccountingToolSingleViewPanel) tabbedPane.getComponentAt(selectedIndex);
			if (panel != null && panel.getSummary() instanceof CarbonAssessmentToolSingleSimulationResult) {
				return true;
			}
			return false;
		}
		
		
		@Override
		public void stateChanged(ChangeEvent e) {
			if (e.getSource().equals(this)) {
				checkIfComparisonShouldBeEnabled();
			}
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

	protected void addSimulationResult(CarbonAssessmentToolSimulationResult summary) {
		CarbonAccountingToolSingleViewPanel singleViewPanel = new CarbonAccountingToolSingleViewPanel(optionPanel, summary);
		tabbedPane.insertTab(summary.getResultId(), null, singleViewPanel, summary.toString(), 0);
		tabbedPane.setSelectedIndex(0);
	}
	
	
	@Override
	public void refreshInterface() {
		if (tabbedPane.getSelectedComponent() != null && tabbedPane.getSelectedComponent() instanceof CarbonAccountingToolSingleViewPanel) {
			((CarbonAccountingToolSingleViewPanel) tabbedPane.getSelectedComponent()).refreshInterface();
		}
	}

	@Override
	public void listenTo () {}

	@Override
	public void doNotListenToAnymore() {}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabbedPane)) {
			refreshInterface();
		} else if (e.getSource() instanceof JMenuItem) {
			refreshInterface();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		if (arg0.getPropertyName().equals(REpiceaSlider.SLIDER_CHANGE)) {
			refreshInterface();
		}
	}
	
}

