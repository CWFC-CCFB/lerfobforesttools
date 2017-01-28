/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.predictor.dopalep;

import repicea.simulation.HierarchicalLevel;

class DopalepTreeImpl implements DopalepTree {

	private final double BAL;
	private final double gOthers;
	private final double dbhCm;
	private final double pred;
	final DopalepPlotImpl plot;
	
	DopalepTreeImpl(DopalepPlotImpl plot, double dbhCm, double BAL, double gOthers, double pred) {
		this.plot = plot;
		this.plot.trees.add(this);
		this.dbhCm = dbhCm;
		this.BAL = BAL;
		this.gOthers = gOthers;
		this.pred = pred;
	}
	
	@Override
	public double getDbhCm() {
		return dbhCm;
	}

	@Override
	public String getSubjectId() {return "";}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	@Override
	public int getMonteCarloRealizationId() {return plot.getMonteCarloRealizationId();}

	protected double getPred() {return pred;}

	@Override
	public double getBasalAreaLargerThanSubjectM2Ha() {
		return BAL;
	}

	@Override
	public double getBasalAreaM2HaOtherTrees() {
		return gOthers;
	}

}
