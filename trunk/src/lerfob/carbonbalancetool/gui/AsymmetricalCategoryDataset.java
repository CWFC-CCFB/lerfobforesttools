/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2016 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.carbonbalancetool.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JDialog;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.statistics.StatisticalCategoryDataset;

import repicea.math.Matrix;
import repicea.stats.estimates.ConfidenceInterval;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.GaussianEstimate;
import repicea.stats.estimates.MonteCarloEstimate;

/**
 * The AsymmetricalCategoryDataset class is the Dataset class required to draw histograms with
 * asymmetrical error bars.
 * @author Mathieu Fortin - March 2016
 *
 */
@SuppressWarnings("rawtypes")
public class AsymmetricalCategoryDataset implements StatisticalCategoryDataset, IntervalCategoryDataset {

	class EstimateWrapper {
		
		private final Estimate<?> estimate;
		private final Color color;
		
		EstimateWrapper(Estimate<?> estimate, Color color) {
			this.estimate = estimate;
			this.color = color;
		}
	}
	
	private final double percentile;
	private final double carbonFactor;

	private final List<Comparable> rowKeys; 
	private final List<Comparable> columnKeys;
	private final Map<Comparable, Map<Comparable, EstimateWrapper>> estimateMap;
	private final Map<Comparable, Map<Comparable, Boolean>> ciToBeDisplayedMap;
	
	public AsymmetricalCategoryDataset(double carbonFactor, double percentile) {
		super();
		this.percentile = percentile;
		this.carbonFactor = carbonFactor;
		rowKeys = new ArrayList<Comparable>();
		columnKeys = new ArrayList<Comparable>();
		estimateMap = new HashMap<Comparable, Map<Comparable, EstimateWrapper>>();
		ciToBeDisplayedMap = new HashMap<Comparable, Map<Comparable, Boolean>>(); 
	}

	public void add(Estimate<?> estimate, Color color, Comparable category, Comparable group) {
		if (estimate.getMean().m_afData[0][0] > 0d) {
			if (!estimateMap.containsKey(category)) {
				if (!rowKeys.contains(category)) {
					rowKeys.add(category);
				}
				estimateMap.put(category, new HashMap<Comparable, EstimateWrapper>());
			}
			Map<Comparable, EstimateWrapper> innerMap = estimateMap.get(category);
			if (!innerMap.containsKey(group)) {
				if (!columnKeys.contains(group)) {
					columnKeys.add(group);
				}
			}
			innerMap.put(group, new EstimateWrapper(estimate.getProductEstimate(carbonFactor), color));
		}
	}
	
	@Override
	public Number getEndValue(int arg0, int arg1) {
		if (arg0 >= 0 && arg0 < rowKeys.size()) {
			if (arg1 >= 0 && arg1 < columnKeys.size()) {
				Comparable rowComparable = rowKeys.get(arg0);
				Comparable columnComparable = columnKeys.get(arg1);
				return getEndValue(rowComparable, columnComparable);
			}
		}
		return null;
	}

	@Override
	public Number getEndValue(Comparable arg0, Comparable arg1) {
		Estimate<?> estimate = getEstimate(arg0, arg1);
		if (estimate != null) {
			ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(percentile);
			if (ci != null && !ci.isThereAnyNaN()) {
				recordBoolean(arg0, arg1, true);
				return ci.getUpperLimit().m_afData[0][0];
			} else {
				recordBoolean(arg0, arg1, false);
				return estimate.getMean().m_afData[0][0];
			}
		}
		return null;
	}

	private void recordBoolean(Comparable arg0, Comparable arg1, boolean b) {
		if (!ciToBeDisplayedMap.containsKey(arg0)) {
			ciToBeDisplayedMap.put(arg0, new HashMap<Comparable, Boolean>());
		}
		Map<Comparable, Boolean> innerMap = ciToBeDisplayedMap.get(arg0);
		innerMap.put(arg1, b);
 	} 

	@Override
	public Number getStartValue(int arg0, int arg1) {
		if (arg0 >= 0 && arg0 < rowKeys.size()) {
			if (arg1 >= 0 && arg1 < columnKeys.size()) {
				Comparable rowComparable = rowKeys.get(arg0);
				Comparable columnComparable = columnKeys.get(arg1);
				return getStartValue(rowComparable, columnComparable);
			}
		}
		return null;
	}

	protected boolean isCIToBeDisplayed(int row, int col) {
		Comparable rowComparable = rowKeys.get(row);
		Comparable columnComparable = columnKeys.get(col);
		if (ciToBeDisplayedMap.containsKey(rowComparable)) {
			Map<Comparable, Boolean> innerMap = ciToBeDisplayedMap.get(rowComparable);
			if (innerMap.containsKey(columnComparable)) {
				return innerMap.get(columnComparable);
			}
		}
		return false;
	}
	
	@Override
	public Number getStartValue(Comparable arg0, Comparable arg1) {
		Estimate<?> estimate = getEstimate(arg0, arg1);
		if (estimate != null) {
			ConfidenceInterval ci = estimate.getConfidenceIntervalBounds(percentile);
			if (ci != null && !ci.isThereAnyNaN()) {
				recordBoolean(arg0, arg1, true);
				return ci.getLowerLimit().m_afData[0][0];
			} else {
				recordBoolean(arg0, arg1, false);
				return estimate.getMean().m_afData[0][0];
			}
		}
		return null;
	}

	@Override
	public int getColumnIndex(Comparable arg0) {return columnKeys.indexOf(arg0);}

	@Override
	public Comparable getColumnKey(int arg0) {return columnKeys.get(arg0);}

	@Override
	public List getColumnKeys() {
		List<Comparable> copyList = new ArrayList<Comparable>();
		copyList.addAll(columnKeys);
		return copyList;
	}

	@Override
	public int getRowIndex(Comparable arg0) {return rowKeys.indexOf(arg0);}

	@Override
	public Comparable getRowKey(int arg0) {return rowKeys.get(arg0);}

	@Override
	public List getRowKeys() {
		List<Comparable> copyList = new ArrayList<Comparable>();
		copyList.addAll(rowKeys);
		return copyList;
	}

	@Override
	public Number getValue(Comparable arg0, Comparable arg1) {
		Estimate<?> estimate = getEstimate(arg0, arg1);
		if (estimate != null) {
			return estimate.getMean().m_afData[0][0];
		}
		return null;
	}
	
	protected final Estimate<?> getEstimate(Comparable arg0, Comparable arg1) {
		EstimateWrapper wrapper = getWrapper(arg0, arg1);
		if (wrapper != null) { 
			return wrapper.estimate;
		}
		return null;
	}

	private EstimateWrapper getWrapper(Comparable arg0, Comparable arg1) {
		if (estimateMap.containsKey(arg0)) {
			if (estimateMap.get(arg0).containsKey(arg1)) {
				return estimateMap.get(arg0).get(arg1);
			}
		}
		return null;
	}
	
	protected final Color getColor(Comparable arg0, Comparable arg1) {
		EstimateWrapper wrapper = getWrapper(arg0, arg1);
		if (wrapper != null) { 
			return wrapper.color;
		}
		return null;
	}
	
	
	
	@Override
	public int getColumnCount() {return columnKeys.size();}

	@Override
	public int getRowCount() {return rowKeys.size();}

	@Override
	public Number getValue(int arg0, int arg1) {
		if (arg0 >= 0 && arg0 < rowKeys.size()) {
			if (arg1 >= 0 && arg1 < columnKeys.size()) {
				Comparable rowComparable = rowKeys.get(arg0);
				Comparable columnComparable = columnKeys.get(arg1);
				return getValue(rowComparable, columnComparable);
			}
		}
		return null;
	}

	@Override
	public void addChangeListener(DatasetChangeListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatasetGroup getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeChangeListener(DatasetChangeListener arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setGroup(DatasetGroup arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public Number getMeanValue(int arg0, int arg1) {
		return getValue(arg0, arg1);
	}

	@Override
	public Number getMeanValue(Comparable arg0, Comparable arg1) {
		return getValue(arg0, arg1);
	}

	@Override
	public Number getStdDevValue(int arg0, int arg1) {
		return .1;
	}

	@Override
	public Number getStdDevValue(Comparable arg0, Comparable arg1) {
		return .1;
	}
	
	public static void main(String[] arg) {
		AsymmetricalCategoryDataset dataset = new AsymmetricalCategoryDataset(1d, 0.95);
		Random random = new Random();
		
		MonteCarloEstimate estimate1 = new MonteCarloEstimate();
		MonteCarloEstimate estimate2 = new MonteCarloEstimate();
		Matrix mean = new Matrix(1,1);
		mean.m_afData[0][0] = 10d;
		Matrix variance = new Matrix(1,1);
		variance.m_afData[0][0] = Double.NaN;
		GaussianEstimate estimate3 = new GaussianEstimate(mean, variance);
		
		Matrix mat1;
		Matrix mat2;
		for (int i = 0; i < 10000; i++) {
			mat1 = new Matrix(1,1);
			mat1.m_afData[0][0] = random.nextDouble();
			estimate1.addRealization(mat1);
			mat2 = new Matrix(1,1);
			mat2.m_afData[0][0] = random.nextDouble() + 2;
			estimate2.addRealization(mat2);
		}
		
		dataset.add(estimate1, Color.RED, "Estimate1", "group1");		
		dataset.add(estimate2, Color.GREEN, "Estimate2", "group1");		
		dataset.add(estimate3, Color.BLUE, "Estimate2", "group2");		
		
		JFreeChart chart = ChartFactory.createBarChart("My title", 
				"Labels", 
				"Values",
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
//		chartPanel.setPopupMenu(new REpiceaPopupMenu(dataset, new JMenuItem("Test")));
		JDialog dialog = new JDialog();
		dialog.setModal(true);
		dialog.getContentPane().add(chartPanel);
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

}
