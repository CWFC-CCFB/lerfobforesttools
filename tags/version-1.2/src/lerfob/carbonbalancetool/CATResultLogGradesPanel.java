package lerfob.carbonbalancetool;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;

import lerfob.carbonbalancetool.CATUtilityMaps.SpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.gui.AsymmetricalCategoryDataset;
import lerfob.carbonbalancetool.gui.EnhancedStatisticalBarRenderer;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

@SuppressWarnings("serial")
class CATResultLogGradesPanel extends CATResultPanel {

	protected static enum MessageID implements TextableEnum {
		Title("Log Category Volumes", "Volumes des cat\u00E9gories de billons"),
		YAxis("Volume (m3/ha/yr)", "Volume (m3/ha/an)"),
		XAxis("Log Category", "Cat\u00E9gorie de billons");

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

	protected CATResultLogGradesPanel(CATSimulationResult summary, CATOptionPanel optionPanel) {
		super(summary, optionPanel);
	}

	@Override
	protected ChartPanel createChart () {
		
		AsymmetricalCategoryDataset dataset = new AsymmetricalCategoryDataset(1d, getCICoverage());
		Map<String, SpeciesMonteCarloEstimateMap> oMap = summary.getLogGradePerHa();
		List<String> logCategoryNames = new ArrayList<String>(oMap.keySet());
		Collections.sort(logCategoryNames);
		
		for (String logCategoryName : logCategoryNames) {
			int index = logCategoryNames.indexOf(logCategoryName);
			SpeciesMonteCarloEstimateMap smcem = oMap.get(logCategoryName);
			MonteCarloEstimate estimate;
			if (optionPanel.isBySpeciesEnabled()) {
				for (String speciesName : smcem.keySet()) {
					estimate = smcem.get(speciesName).get(Element.Volume);
					dataset.add((MonteCarloEstimate) estimate.getProductEstimate(1d / summary.getRotationLength()),
							getColor(index),
							logCategoryName, 
							speciesName);
				}
			} else {
				estimate = smcem.getSumAcrossSpecies().get(Element.Volume);
				dataset.add((MonteCarloEstimate) estimate.getProductEstimate(1d / summary.getRotationLength()),
						getColor(index),
						logCategoryName, 
						summary.getResultId());
			}
		}

		JFreeChart chart = ChartFactory.createBarChart (getTitle(), 
				getXAxisLabel(), 
				getYAxisLabel(),
				dataset, 
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot ();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setRenderer(new EnhancedStatisticalBarRenderer());
		EnhancedStatisticalBarRenderer renderer = (EnhancedStatisticalBarRenderer) plot.getRenderer();

		renderer.setShadowVisible (true);
		renderer.setMaximumBarWidth (0.1);
		renderer.setColors(dataset);
		
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

