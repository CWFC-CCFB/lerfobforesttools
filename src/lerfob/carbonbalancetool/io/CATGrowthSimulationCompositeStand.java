/*
 * This file is part of the lerfob-foresttools library.
 *
 * Copyright (C) 2010-2017 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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
package lerfob.carbonbalancetool.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lerfob.carbonbalancetool.CATCompatibleStand;
import repicea.simulation.covariateproviders.standlevel.StochasticInformationProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

/**
 * This class represents the plots in a growth simulation import in CAT.
 * @author Mathieu Fortin - July 2017
 */
class CATGrowthSimulationCompositeStand implements CATCompatibleStand, StochasticInformationProvider<CATGrowthSimulationPlotSample> {

	
	private final String standIdentification;
	private final Map<Integer, CATGrowthSimulationPlotSample> realizationMap;
	private final int dateYr;
	protected final CATGrowthSimulationRecordReader reader;

	
	CATGrowthSimulationCompositeStand(int dateYr, String standIdentification, CATGrowthSimulationRecordReader reader) {
		this.dateYr = dateYr;
		this.standIdentification = standIdentification;
		this.reader = reader;
		realizationMap = new HashMap<Integer, CATGrowthSimulationPlotSample>();
	}
	
	@Override
	public double getAreaHa() {return getRealization(0).getAreaHa();}

	@Override
	public Collection<CATGrowthSimulationTree> getTrees(StatusClass statusClass) {
		Collection<CATGrowthSimulationTree> coll = new ArrayList<CATGrowthSimulationTree>();
		for (CATGrowthSimulationPlotSample plotSample : realizationMap.values()) {
			coll.addAll(plotSample.getTrees(statusClass));
		}
		return coll;
	}
		
	@Override
	public boolean isInterventionResult() {return false;}

	@Override
	public String getStandIdentification() {return standIdentification;}

	@Override
	public int getDateYr() {return dateYr;}

	@Override
	public int getNumberRealizations() {return realizationMap.size();}

	@Override
	public boolean isStochastic() {return getNumberRealizations() > 1;}

	@Override
	public CATGrowthSimulationPlotSample getRealization(int realizationID) {return realizationMap.get(realizationID);}

	void createRealization(int realization) {	
		if (!realizationMap.containsKey(realization)) {
			realizationMap.put(realization, new CATGrowthSimulationPlotSample(this));
		}
	}

	@Override
	public ManagementType getManagementType() {return ManagementType.UnevenAged;}

	@Override
	public ApplicationScale getApplicationScale() {return ApplicationScale.FMU;}

	@Override
	public CATCompatibleStand getHarvestedStand() {return null;}


}
