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

import java.awt.BasicStroke;
import java.awt.Stroke;

import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;

import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

@SuppressWarnings("serial")
public class XYSeriesWithIntegratedRenderer extends XYSeries {

	public static final Stroke MajorStroke = new BasicStroke(3f); // width of the lines
	public static final Stroke MinorStroke = new BasicStroke(1,	BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,	1, new float[]{12,12}, 0); // width of the lines
	
	private final boolean isMean;
	private final double carbonFactor;
	private final CompartmentInfo compartmentInfo;
	private final XYSeriesCollection dataset;
	
	public XYSeriesWithIntegratedRenderer(XYSeriesCollection dataset, String title, CompartmentInfo compartmentInfo, boolean isMean, double carbonFactor) {
		super(title);
		this.isMean = isMean;
		this.carbonFactor = carbonFactor;
		this.compartmentInfo = compartmentInfo;
		this.dataset = dataset;
		dataset.addSeries(this);
	}
	
	public void setStrokeAndColor(XYItemRenderer renderer) {
		int indexOfThisSeries = dataset.getSeries().indexOf(this);
		renderer.setSeriesPaint(indexOfThisSeries, compartmentInfo.getColor());
		if (isMean) {
			renderer.setSeriesStroke(indexOfThisSeries, MajorStroke);
		} else {
			renderer.setSeriesStroke(indexOfThisSeries, MinorStroke);
		}
	}
	
	@Override
	public void add(double xValue, double yValue) {
		super.add(xValue, yValue * carbonFactor);
	}
}
