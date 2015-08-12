/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin AgroParisTech/INRA UMR LERFoB, 
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

import java.util.HashMap;
import java.util.Map;

import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.simulation.processsystem.AmountMap;

/**
 * The CarbonAssessmentToolSimulationDifference class handles differences between two scenarios and provides the results.
 * @author Mathieu Fortin - February 2014
 */
class CarbonAssessmentToolSimulationDifference implements CarbonAssessmentToolSimulationResult {

	private final CarbonAssessmentToolSingleSimulationResult scenToCompare;
	private final CarbonAssessmentToolSingleSimulationResult baseline;
	
	CarbonAssessmentToolSimulationDifference(CarbonAssessmentToolSingleSimulationResult scenToCompare,
			CarbonAssessmentToolSingleSimulationResult baseline) {
		this.scenToCompare = scenToCompare;
		this.baseline = baseline;
	}
	
	
	@Override
	public Map<CompartmentInfo, Double> getBudgetMap() {
		Map<CompartmentInfo, Double> outputMap = new HashMap<CompartmentInfo, Double>();
		for (CompartmentInfo comp : scenToCompare.getBudgetMap().keySet()) {
			if (baseline.getBudgetMap().containsKey(comp)) {
				outputMap.put(comp, scenToCompare.getBudgetMap().get(comp) - baseline.getBudgetMap().get(comp));
			} else {
				outputMap.put(comp, scenToCompare.getBudgetMap().get(comp));
			}
		}
		for (CompartmentInfo comp : baseline.getBudgetMap().keySet()) {
			if (!outputMap.containsKey(comp)) {
				outputMap.put(comp, - baseline.getBudgetMap().get(comp));
			} 
		}
		return outputMap;
	}

	@Override
	public String getStandID() {return scenToCompare.getStandID() + " - " + baseline.getStandID();}

	@Override
	public Integer[] getTimeScale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRotationLength() {
		if (scenToCompare.getRotationLength() > baseline.getRotationLength()) {
			return scenToCompare.getRotationLength();
		} else {
			return baseline.getRotationLength();
		}
	}

	@Override
	public Map<CompartmentInfo, Double[]> getEvolutionMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> getHWPContentByUseClass() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Map<String, AmountMap<Element>> getLogGradeMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Integer, Map<UseClass, AmountMap<Element>>> getProductEvolutionMap() {
		// TODO Auto-generated method stub
		return null;
	}

}