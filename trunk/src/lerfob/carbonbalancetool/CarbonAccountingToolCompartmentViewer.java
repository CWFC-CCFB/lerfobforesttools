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
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CarbonAccountingToolCompartmentViewer extends CarbonAccountingToolViewer {

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

//		Map<CompartmentInfo, XYSeries> indexMap = new HashMap<CompartmentInfo, XYSeries> ();
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
			Matrix randomVariables = summary.getEvolutionMap().get(compartmentID).getMean();
			XYSeries s = new XYSeries(compartmentID.toString());
			for (int i = 0; i < summary.getTimeScale().length; i++) {
				s.add((double) summary.getTimeScale()[i], randomVariables.m_afData[i][0]);
			}

			dataset.addSeries(s);
//			indexMap.put (compartmentID, s);
		}


		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.BLACK);
		XYItemRenderer renderer = plot.getRenderer();
		Stroke stroke = new BasicStroke(3f); // width of the lines

		for (int i = 0; i < optionPanel.getCompartmentToBeShown().size(); i++) {
			renderer.setSeriesPaint(i, optionPanel.getCompartmentToBeShown().get(i).getColor());
			renderer.setSeriesStroke(i, stroke);
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

