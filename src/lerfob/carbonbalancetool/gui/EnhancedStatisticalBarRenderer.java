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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;

/**
 * The EnhancedStatisticalBarRenderer class makes it possible to draw histograms with asymmetrical error bars. 
 * The only method that is overriden is the drawVerticalItem method.
 * @author Mathieu Fortin - March 2016
 */
@SuppressWarnings("serial")
public class EnhancedStatisticalBarRenderer extends StatisticalBarRenderer {

	public EnhancedStatisticalBarRenderer() {
		super();
		setErrorIndicatorStroke(XYSeriesWithIntegratedRenderer.MajorStroke);
		setErrorIndicatorPaint(Color.BLACK);
	}
	
	@SuppressWarnings("rawtypes")
	public void setColors(AsymmetricalCategoryDataset dataset) {
		for (Object rowKey : dataset.getRowKeys()) {
			for (Object columnKey : dataset.getColumnKeys()) {
				int index = dataset.getRowIndex((Comparable) rowKey);
				Color color = dataset.getColor((Comparable) rowKey , (Comparable) columnKey);
				if (color != null) {
					setSeriesPaint(index, color);
				}
			}
		}
	}
	
	
	
	
	
	
	@Override
	protected void drawVerticalItem(Graphics2D g2, 
			CategoryItemRendererState state, 
			Rectangle2D dataArea, 
			CategoryPlot plot, 
			CategoryAxis domainAxis,
			ValueAxis rangeAxis, 
			StatisticalCategoryDataset dataset, 
			int visibleRow, 
			int row, 
			int column) {
		double rectX = calculateBarW0(plot, PlotOrientation.VERTICAL, dataArea, domainAxis, state, visibleRow, column);
		Number meanValue = dataset.getMeanValue(row, column);
		if (meanValue == null) {
			return;
		}
		double value = meanValue.doubleValue();
		double base = 0.0;
		double lclip = getLowerClip();
		double uclip = getUpperClip();
		if (uclip <= 0.0) {  // cases 1, 2, 3 and 4
			if (value >= uclip) {
				return; // bar is not visible
			}
		base = uclip;
		if (value <= lclip) {
			value = lclip;
		}
		} else if (lclip <= 0.0) { // cases 5, 6, 7 and 8
			if (value >= uclip) {
				value = uclip;
			} else {
				if (value <= lclip) {
					value = lclip;
				}
			}
		} else { // cases 9, 10, 11 and 12
			if (value <= lclip) {
				return; // bar is not visible
			}
			base = getLowerClip();
			if (value >= uclip) {
				value = uclip;
			}
		}
		RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double transY1 = rangeAxis.valueToJava2D(base, dataArea, yAxisLocation);
		double transY2 = rangeAxis.valueToJava2D(value, dataArea, yAxisLocation);
		double rectY = Math.min(transY2, transY1);
		double rectWidth = state.getBarWidth();
		double rectHeight = Math.abs(transY2 - transY1);
		Rectangle2D bar = new Rectangle2D.Double(rectX, rectY, rectWidth, rectHeight);
		Paint itemPaint = getItemPaint(row, column);
		GradientPaintTransformer t = getGradientPaintTransformer();
		if (t != null && itemPaint instanceof GradientPaint) {
			itemPaint = t.transform((GradientPaint) itemPaint, bar);
		}
		g2.setPaint(itemPaint);
		g2.fill(bar);
		// draw the outline...
		if (isDrawBarOutline() && state.getBarWidth() > BAR_OUTLINE_WIDTH_THRESHOLD) {
			Stroke stroke = getItemOutlineStroke(row, column);
			Paint paint = getItemOutlinePaint(row, column);
			if (stroke != null && paint != null) {
				g2.setStroke(stroke);
				g2.setPaint(paint);
				g2.draw(bar);
			}
		}
		
		// upper and lower bound
		Number lowerBound = ((AsymmetricalCategoryDataset) dataset).getStartValue(row, column);
		Number upperBound = ((AsymmetricalCategoryDataset) dataset).getEndValue(row, column);
		if (lowerBound != null && upperBound != null) {
			double highVal = rangeAxis.valueToJava2D(upperBound.doubleValue(), dataArea, yAxisLocation);
			double lowVal = rangeAxis.valueToJava2D(lowerBound.doubleValue(), dataArea, yAxisLocation);
			g2.setPaint(getErrorIndicatorPaint());
			g2.setStroke(getErrorIndicatorStroke());
			Line2D line = null;
			line = new Line2D.Double(rectX + rectWidth / 2.0d, lowVal, rectX + rectWidth / 2.0d, highVal);
			g2.draw(line);
			line = new Line2D.Double(rectX + rectWidth / 2.0d - 5.0d, highVal, rectX + rectWidth / 2.0d + 5.0d, highVal);
			g2.draw(line);
			line = new Line2D.Double(rectX + rectWidth / 2.0d - 5.0d, lowVal, rectX + rectWidth / 2.0d + 5.0d, lowVal);
			g2.draw(line);
		}

		CategoryItemLabelGenerator generator = getItemLabelGenerator(row, column);
		if (generator != null && isItemLabelVisible(row, column)) {
			drawItemLabel(g2, dataset, row, column, plot, generator, bar, (value < 0.0));
		}
		// add an item entity, if this information is being collected
		EntityCollection entities = state.getEntityCollection();
		if (entities != null) {
			addItemEntity(entities, dataset, row, column, bar);
		}
	}

}
