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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.MonteCarloEstimate;

/**
 * The CarbonAssessmentToolSimulationDifference class handles differences between two scenarios and provides the results.
 * @author Mathieu Fortin - February 2014
 */
class CarbonAssessmentToolSimulationDifference implements CarbonAssessmentToolSimulationResult {

	private final CarbonAssessmentToolSingleSimulationResult scenToCompare;
	private final CarbonAssessmentToolSingleSimulationResult baseline;
	private final String resultId;
	private final Map<CompartmentInfo, Estimate<?>> budgetMap;
	private final Integer refDate;
	private final Integer altDate;
	
	CarbonAssessmentToolSimulationDifference(String resultId, 
			CarbonAssessmentToolSingleSimulationResult baseline,
			Integer refDate,
			CarbonAssessmentToolSingleSimulationResult scenToCompare, 
			Integer altDate) {
		this.resultId = resultId;
		this.baseline = baseline;
		this.refDate = refDate;
		this.scenToCompare = scenToCompare;
		this.altDate = altDate;
		budgetMap = new HashMap<CompartmentInfo, Estimate<?>>();
		setBudgetMap();
	}
	
	
	private void setBudgetMap() {
		Map<CompartmentInfo, Estimate<?>> refMap;
		if (refDate == null) {
			refMap = baseline.getBudgetMap();
		} else {
			refMap = new HashMap<CompartmentInfo, Estimate<?>>();
			refMap.putAll(getEstimateForThisDate(baseline, refDate));
			System.out.println("Just extracted date " + refDate + " in baseline");
		}
		
		Map<CompartmentInfo, Estimate<?>> altMap;
		if (altDate == null) {
			altMap = scenToCompare.getBudgetMap();
		} else {
			altMap = new HashMap<CompartmentInfo, Estimate<?>>();
			altMap.putAll(getEstimateForThisDate(scenToCompare, altDate));
			System.out.println("Just extracted date " + altDate + " in alternative scenario");
		}
		
		for (CompartmentInfo comp : altMap.keySet()) {
			if (refMap.containsKey(comp)) {
				Estimate<?> diffEst = altMap.get(comp).getDifferenceEstimate(refMap.get(comp));
				budgetMap.put(comp, diffEst);
			} else {
				budgetMap.put(comp, altMap.get(comp));
			}
		}
		
		for (CompartmentInfo comp : refMap.keySet()) {
			if (!budgetMap.containsKey(comp)) {
				budgetMap.put(comp, refMap.get(comp).getProductEstimate(-1d));
			} 
		}
	}

	private Map<CompartmentInfo, MonteCarloEstimate> getEstimateForThisDate(CarbonAssessmentToolSingleSimulationResult result, int date) {
		Map<CompartmentInfo, MonteCarloEstimate> outputMap = new HashMap<CompartmentInfo, MonteCarloEstimate>();
		int index = result.getTimeTable().lastIndexOf(date);
		List<Integer> indices = new ArrayList<Integer>();
		indices.add(index);
		for (CompartmentInfo comp : result.getEvolutionMap().keySet()) {
			MonteCarloEstimate currentEstimate = result.getEvolutionMap().get(comp);
			outputMap.put(comp, currentEstimate.extractSubEstimate(indices));
		}
		return outputMap;
	}
		
	@Override
	public Map<CompartmentInfo, Estimate<?>> getBudgetMap() {return budgetMap;}

	@Override
	public String getStandID() {return scenToCompare.getStandID() + " - " + baseline.getStandID();}

	@Override
	public CarbonAccountingToolTimeTable getTimeTable() {return null;}

	@Override
	public int getRotationLength() {
		if (scenToCompare.getRotationLength() > baseline.getRotationLength()) {
			return scenToCompare.getRotationLength();
		} else {
			return baseline.getRotationLength();
		}
	}

	@Override
	public Map<CompartmentInfo, MonteCarloEstimate> getEvolutionMap() {
		return null;
	}

	@Override
	public Map<CarbonUnitStatus, Map<UseClass, Map<Element, MonteCarloEstimate>>> getHWPPerHaByUseClass() {
		return null;
	}


	@Override
	public Map<String, Map<Element, MonteCarloEstimate>> getLogGradePerHa() {
		return null;
	}

	@Override
	public Map<Integer, Map<UseClass, Map<Element, MonteCarloEstimate>>> getProductEvolutionPerHa() {
		return null;
	}

	@Override
	public String toString() {
		return "<html>" + CarbonAssessmentToolSingleSimulationResult.MessageID.DifferenceBetween.toString() + ": " + scenToCompare.getStandID() + "<br>" +
					CarbonAssessmentToolSingleSimulationResult.MessageID.And.toString() + baseline.getStandID() + "</html>";
	}


	@Override
	public String getResultId() {return resultId;}


	@Override
	public Map<UseClass, Map<Element, MonteCarloEstimate>> getHWPSummaryPerHa(boolean includeRecycling) {
		return null;
	}


	@Override
	public boolean isEvenAged() {return baseline.isEvenAged();}

}
