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
import java.util.Map;

import lerfob.carbonbalancetool.gui.AsymmetricalCategoryDataset;
import lerfob.carbonbalancetool.gui.EnhancedStatisticalBarRenderer;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CarbonAccountingToolProductViewer extends CarbonAccountingToolViewer {

	protected static enum MessageID implements TextableEnum {
		Title("Harvested Wood Products Distribution without recycling", "R\u00E9partition des produits bois sans recyclage"),
		YAxis("Percentage (%)", "Pourcentage (%)"),
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

	
	protected CarbonAccountingToolProductViewer(CarbonAssessmentToolSimulationResult summary) {
		super(summary);
	}

	protected Map<UseClass, Map<Element, MonteCarloEstimate>> getAppropriateMap() {
		return summary.getHWPPerHaByUseClass().get(CarbonUnitStatus.EndUseWoodProduct);
	}
	
	@Override
	protected final ChartPanel createChart() {

		AsymmetricalCategoryDataset dataset = new AsymmetricalCategoryDataset(0.95);
		Map<UseClass, Map<Element, MonteCarloEstimate>> oMap = getAppropriateMap();
		
		MonteCarloEstimate sum = null;
		int i = 0;
		for (Map<Element, MonteCarloEstimate> carrier : oMap.values()) {
			if (i == 0) {
				sum = carrier.get(Element.Volume);
			} else {
				sum = MonteCarloEstimate.add(sum, carrier.get(Element.Volume));
			}
			i++;
		}
		
		MonteCarloEstimate relativeEstimate = new MonteCarloEstimate();
		for (UseClass useClass : UseClass.values()) {
			if (oMap.containsKey(useClass)) {
				MonteCarloEstimate basicEstimate = oMap.get(useClass).get(Element.Volume);
				for (i = 0; i < basicEstimate.getNumberOfRealizations(); i++) {
					double sumValueForThisRealization = sum.getRealizations().get(i).m_afData[0][0];
					relativeEstimate.addRealization(basicEstimate.getRealizations().get(i).scalarMultiply(100d / sumValueForThisRealization));
				}
				dataset.add(relativeEstimate,
						getColor(useClass.ordinal()),
						useClass.toString(), 
						summary.getResultId());
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
		ValueAxis axis = plot.getRangeAxis();
		axis.setRange(0, 100);

		
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
