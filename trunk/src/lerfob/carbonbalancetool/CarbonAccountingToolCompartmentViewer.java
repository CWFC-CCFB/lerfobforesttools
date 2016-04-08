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
import lerfob.carbonbalancetool.gui.XYSeriesWithIntegratedRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CarbonAccountingToolCompartmentViewer extends CarbonAccountingToolViewer {

	protected static enum MessageID implements TextableEnum {
		Title("Carbon stock evolution", "Evolution des stocks de carbone"),
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

	private CarbonAccountingToolOptionPanel optionPanel;

	protected CarbonAccountingToolCompartmentViewer(CarbonAssessmentToolSimulationResult summary, 	CarbonAccountingToolOptionPanel optionPanel) {
		super(summary);
		this.optionPanel = optionPanel;
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
			double lowerPercentile = (1d - ciCoverage) * .5;
			double upperPercentile = 1d - lowerPercentile;
//			System.out.println("Lower and upper percentiles set to : " + lowerPercentile + "; " + upperPercentile);
			Matrix lowerBound = estimate.getPercentile(lowerPercentile);
			Matrix upperBound = estimate.getPercentile(upperPercentile);
			XYSeriesWithIntegratedRenderer meanSeries = new XYSeriesWithIntegratedRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Mean.toString(), compartmentID, true, getCarbonFactor());
			XYSeriesWithIntegratedRenderer lowerSeries = new XYSeriesWithIntegratedRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Lower.toString(), compartmentID, false, getCarbonFactor());
			XYSeriesWithIntegratedRenderer upperSeries = new XYSeriesWithIntegratedRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Upper.toString(), compartmentID, false, getCarbonFactor());
			for (int i = 0; i < summary.getTimeScale().length; i++) {
				meanSeries.add((double) summary.getTimeScale()[i], mean.m_afData[i][0]);
				lowerSeries.add((double) summary.getTimeScale()[i], lowerBound.m_afData[i][0]);
				upperSeries.add((double) summary.getTimeScale()[i], upperBound.m_afData[i][0]);
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
	protected String getTitle() {return REpiceaTranslator.getString(MessageID.Title);}

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

