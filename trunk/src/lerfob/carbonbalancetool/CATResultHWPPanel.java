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
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

import lerfob.carbonbalancetool.CATUtilityMaps.SpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.CATUtilityMaps.UseClassSpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.gui.AsymmetricalCategoryDataset;
import lerfob.carbonbalancetool.gui.EnhancedStatisticalBarRenderer;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CATResultHWPPanel extends CATResultPanel {

	protected static enum MessageID implements TextableEnum {
		Title("Harvested Wood Products Distribution without recycling", "R\u00E9partition des produits bois sans recyclage"),
		YAxis("Volume (m3/ha/yr)", "Volume (m3/ha/an)"),
		XAxis("Product class", "Cat\u00E9gorie de produits");

		MessageID(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}
		
	}

	private final boolean includeRecycling;
	
	protected CATResultHWPPanel(CATSimulationResult summary, CATOptionPanel optionPanel) {
		this(summary, optionPanel, false);
	}

	protected CATResultHWPPanel(CATSimulationResult summary, CATOptionPanel optionPanel, boolean includeRecycling) {
		super(summary, optionPanel);
		this.includeRecycling = includeRecycling;
	}

	@Override
	protected final ChartPanel createChart() {
		AsymmetricalCategoryDataset dataset = new AsymmetricalCategoryDataset(1d, getCICoverage());
		UseClassSpeciesMonteCarloEstimateMap oMap = ((CATSingleSimulationResult) summary).getHWPSummaryPerHa(includeRecycling);
		
		for (UseClass useClass : UseClass.values()) {
			if (oMap.containsKey(useClass)) {
				SpeciesMonteCarloEstimateMap smcem = oMap.get(useClass);
				MonteCarloEstimate estimate;
				if (optionPanel.isBySpeciesEnabled()) {
					for (String speciesName : smcem.keySet()) {
						estimate = smcem.get(speciesName).get(Element.Volume);
						dataset.add((MonteCarloEstimate) estimate.getProductEstimate(1d / summary.getRotationLength()),
								getColor(useClass.ordinal()),
								useClass.toString(), 
								speciesName);
					}
				}  else {
					estimate = smcem.getSumAcrossSpecies().get(Element.Volume);
					dataset.add((MonteCarloEstimate) estimate.getProductEstimate(1d / summary.getRotationLength()),
							getColor(useClass.ordinal()),
							useClass.toString(), 
							summary.getResultId());
				}
			}
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
		plot.setRenderer(new EnhancedStatisticalBarRenderer());
		EnhancedStatisticalBarRenderer renderer = (EnhancedStatisticalBarRenderer) plot.getRenderer();

		renderer.setShadowVisible(true);
		renderer.setMaximumBarWidth(0.1);
		renderer.setColors(dataset);
		
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
