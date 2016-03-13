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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CarbonAccountingToolCompartmentViewer extends CarbonAccountingToolViewer {

	private static class XYSeriesWithRenderer extends XYSeries {

		private static final Stroke MeanStroke = new BasicStroke(3f); // width of the lines
		private static final Stroke BoundStroke = new BasicStroke(1f); // width of the lines

		
		private final boolean isMean;
		private final CompartmentInfo compartmentInfo;
		private final XYSeriesCollection dataset;
		
		public XYSeriesWithRenderer(XYSeriesCollection dataset, String title, CompartmentInfo compartmentInfo, boolean isMean) {
			super(title);
			this.isMean = isMean;
			this.compartmentInfo = compartmentInfo;
			this.dataset = dataset;
			dataset.addSeries(this);
		}
		
		protected void setStrokeAndColor(XYItemRenderer renderer) {
			int indexOfThisSeries = dataset.getSeries().indexOf(this);
			renderer.setSeriesPaint(indexOfThisSeries, compartmentInfo.getColor());
			if (isMean) {
				renderer.setSeriesStroke(indexOfThisSeries, MeanStroke);
			} else {
				renderer.setSeriesStroke(indexOfThisSeries, BoundStroke);
			}
		}
	}
	
	
	protected static enum MessageID implements TextableEnum {
		Title("Carbon stock evolution", "Evolution des stocks de carbone"),
		YAxis("Carbon (Mg C/ha)", "Carbone (Mg C/ha)"),
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

		for (CompartmentInfo compartmentID : optionPanel.getCompartmentToBeShown()) {
			MonteCarloEstimate estimate = summary.getEvolutionMap().get(compartmentID);
			Matrix mean = estimate.getMean();
			Matrix lowerBound = estimate.getPercentile(0.025);
			Matrix upperBound = estimate.getPercentile(0.975);
			XYSeriesWithRenderer meanSeries = new XYSeriesWithRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Mean.toString(), compartmentID, true);
			XYSeriesWithRenderer lowerSeries = new XYSeriesWithRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Lower.toString(), compartmentID, false);
			XYSeriesWithRenderer upperSeries = new XYSeriesWithRenderer(dataset, compartmentID.toString() + "_" + MonteCarloEstimate.MessageID.Upper.toString(), compartmentID, false);
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
			if (obj instanceof XYSeriesWithRenderer) {
				((XYSeriesWithRenderer) obj).setStrokeAndColor(renderer);
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
	protected String getYAxisLabel() {return REpiceaTranslator.getString(MessageID.YAxis);}


}

