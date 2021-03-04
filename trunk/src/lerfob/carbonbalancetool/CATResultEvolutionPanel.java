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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.io.CATExportTool.ExportOption;
import lerfob.carbonbalancetool.gui.XYSeriesWithIntegratedRenderer;
import repicea.math.Matrix;
import repicea.stats.estimates.ConfidenceInterval;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CATResultEvolutionPanel extends CATResultPanel {

	protected static enum MessageID implements TextableEnum {
		YAxis("Carbon (Mg/ha of C)", "Carbone (Mg/ha de C)"),
		YCO2Axis("Carbon (Mg/ha of CO2 Eq.)", "Carbone (Mg/ha de CO2 Eq.)"),
		XAxis("Year", "Ann\u00E9e");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
	}

	protected CATResultEvolutionPanel(CATSimulationResult summary, 	CATOptionPanel optionPanel) {
		super(summary, optionPanel);
	}

	


	@Override
	protected ChartPanel createChart () {

		XYSeriesCollection dataset = new XYSeriesCollection ();

		JFreeChart chart = ChartFactory.createXYLineChart (getTitle(), 
				getXAxisLabel(), 
				getYAxisLabel(),
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);
		double ciCoverage = getCICoverage();
		for (CompartmentInfo compartmentID : optionPanel.getCompartmentToBeShown()) {
			MonteCarloEstimate estimate = summary.getEvolutionMap().get(compartmentID);
			Matrix mean = estimate.getMean();
			ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(ciCoverage);
			
			Matrix lowerBound = ci.getLowerLimit();
			Matrix upperBound = ci.getUpperLimit();
			XYSeriesWithIntegratedRenderer meanSeries = new XYSeriesWithIntegratedRenderer(dataset, compartmentID.toString(), compartmentID, true, getCarbonFactor());
			XYSeriesWithIntegratedRenderer lowerSeries = new XYSeriesWithIntegratedRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Lower.toString(), compartmentID, false, getCarbonFactor());
			XYSeriesWithIntegratedRenderer upperSeries = new XYSeriesWithIntegratedRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Upper.toString(), compartmentID, false, getCarbonFactor());
			for (int i = 0; i < summary.getTimeTable().size(); i++) {
				meanSeries.add((double) summary.getTimeTable().getDateYrAtThisIndex(i), mean.getValueAt(i, 0));
				lowerSeries.add((double) summary.getTimeTable().getDateYrAtThisIndex(i), lowerBound.getValueAt(i, 0));
				upperSeries.add((double) summary.getTimeTable().getDateYrAtThisIndex(i), upperBound.getValueAt(i, 0));
			}
		}


		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.BLACK);
		XYItemRenderer renderer = plot.getRenderer();
		
		for (Object obj : dataset.getSeries()) {
			if (obj instanceof XYSeriesWithIntegratedRenderer) {
				((XYSeriesWithIntegratedRenderer) obj).setStrokeAndColor(renderer);
			}
		}
		
		ChartPanel chartPanel = new ChartPanel (chart);
		return chartPanel;
	}


	@Override
	protected String getTitle() {return ExportOption.CarbonStockAndFluxEvolution.toString();}

	@Override
	protected String getXAxisLabel() {return REpiceaTranslator.getString(MessageID.XAxis);}

	@Override
	protected String getYAxisLabel() {
		if (isInCO2()) {
			return REpiceaTranslator.getString(MessageID.YCO2Axis);
		} else {
			return REpiceaTranslator.getString(MessageID.YAxis);
		}
	}


}

