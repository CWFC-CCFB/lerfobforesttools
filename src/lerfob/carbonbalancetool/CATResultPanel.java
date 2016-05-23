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
import java.awt.Color;
import java.awt.Container;
import java.util.Vector;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;

import repicea.gui.CommonGuiUtility;
import repicea.gui.REpiceaPanel;

abstract class CATResultPanel extends REpiceaPanel { 
	
	private static final long serialVersionUID = 20130911L;
	
	private static Vector<Color> COLORS = new Vector<Color>();
	static {
		COLORS.add(Color.BLUE);
		COLORS.add(Color.LIGHT_GRAY);
		COLORS.add(Color.RED);
		COLORS.add(Color.GRAY);
		COLORS.add(Color.YELLOW);
		COLORS.add(Color.GREEN);
		COLORS.add(Color.DARK_GRAY);
		COLORS.add(Color.MAGENTA);
		COLORS.add(Color.ORANGE);
		COLORS.add(Color.PINK);
		COLORS.add(Color.CYAN);
	}

	private JPanel viewer;
	protected CarbonAssessmentToolSimulationResult summary;
	
	protected CATResultPanel(CarbonAssessmentToolSimulationResult summary) {
		super();
		this.summary = summary;
		createUI();
	}
	

	/**
	 * This method creates the panels that compose the UI.
	 */
	protected void createUI() {
		setLayout(new BorderLayout());
		viewer = new JPanel(new BorderLayout()); 
		add(viewer, BorderLayout.CENTER);
	}
	
	/**
	 * This method returns the appropriate graph for the CarbonViewer instance.
	 * @return a JComponent
	 */
	protected abstract ChartPanel createChart();
	
	protected String getPrefix() {return this.getClass().getSimpleName() + ".";}
	
	/**
	 * This method returns a color for a particular compartment.
	 * @param compartmentInfo = a CompartmentInfo instance
	 * @return a Color object
	 */
	protected Color getColor(int i) {
		if (i < COLORS.size()) {
			return COLORS.get(i);
		} else {
			return COLORS.get(0);
		}
	}
	

	protected abstract String getTitle();
	protected abstract String getXAxisLabel();
	protected abstract String getYAxisLabel();

	@Override
	public String toString() {return getTitle();}
		
	@Override
	public final void refreshInterface() {
		viewer.removeAll();
		ChartPanel panel = createChart();
		viewer.add(panel, BorderLayout.CENTER);
	}

	@Override
	public void listenTo () {}

	@Override
	public void doNotListenToAnymore () {}

	protected double getCarbonFactor() {
		CATFrame dlg = getMainDialog();
		if (dlg != null && dlg.calculateInCO2.isSelected()) {
			return CarbonAccountingToolSettings.C_C02_FACTOR;
		} else {
			return 1d;
		}
	}
	
	private CATFrame getMainDialog() {
		Container window = CommonGuiUtility.getParentComponent(this, CATFrame.class);
		if (window != null) {
			return (CATFrame) window;
		} else {
			return null;
		}
	}
	
	protected boolean isInCO2() {
		return getCarbonFactor() == CarbonAccountingToolSettings.C_C02_FACTOR;
	}
	
	protected double getCICoverage() {
		CATFrame dlg = getMainDialog();
		if (dlg != null) {
			return dlg.confidenceIntervalSlider.getValue() *.01;
		} else {
			return .95;
		}
	}
}
