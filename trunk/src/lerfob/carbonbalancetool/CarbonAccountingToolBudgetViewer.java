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

import java.awt.Color;

import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CarbonAccountingToolBudgetViewer extends CarbonAccountingToolViewer {
	
	protected static enum MessageID implements TextableEnum {
		Title("Carbon budget", "Bilan carbone"),
		YAxis("Average Carbon (Mg C/ha or Mg C/ha/yr)", "Carbone moyen (Mg C/ha ou Mg C/ha/an)"),
		XAxis("Compartment", "Compartiment");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
	}

	private CarbonAccountingToolOptionPanel optionPanel;
	
	/**
	 * Default constructor.
	 */
	protected CarbonAccountingToolBudgetViewer(CarbonAssessmentToolSimulationResult summary, CarbonAccountingToolOptionPanel optionPanel) {
		super(summary);
		this.optionPanel = optionPanel;
	}


	@Override
	protected ChartPanel createChart() {

		DefaultCategoryDataset dataset = new DefaultCategoryDataset ();

		for (CompartmentInfo compartmentID : optionPanel.getCompartmentToBeShown()) {
			dataset.addValue(summary.getBudgetMap().get(compartmentID), compartmentID.toString(),	"");
		}
		
		JFreeChart chart = ChartFactory.createBarChart(getTitle(), 
				getXAxisLabel(), 
				getYAxisLabel(),
				dataset, 
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.BLACK);
		BarRenderer renderer = (BarRenderer) plot.getRenderer();

		renderer.setShadowVisible(true);
		renderer.setMaximumBarWidth(0.1);

		for (CompartmentInfo compartmentID : optionPanel.getCompartmentToBeShown()) {
			Color color = getColor(compartmentID.ordinal());
			int index = dataset.getRowKeys().indexOf(compartmentID.toString());
			renderer.setSeriesPaint(index, color);
		}

		ChartPanel chartPanel = new ChartPanel(chart);
		return chartPanel;

	}


	@Override
	protected String getTitle() {return REpiceaTranslator.getString(MessageID.Title);}

	@Override
	protected String getXAxisLabel() {return REpiceaTranslator.getString(MessageID.XAxis);}

	@Override
	protected String getYAxisLabel() {return REpiceaTranslator.getString(MessageID.YAxis);}



}

