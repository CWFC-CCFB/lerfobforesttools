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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.CATUtilityMaps.CATSpeciesAmountMap;
import lerfob.carbonbalancetool.CATUtilityMaps.CATUseClassSpeciesAmountMap;
import lerfob.carbonbalancetool.CATUtilityMaps.SpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.CATUtilityMaps.UseClassSpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import repicea.math.Matrix;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

class CATSingleSimulationResult implements CATSimulationResult {

	protected static enum MessageID implements TextableEnum {
		StandIdentification("Project name", "Nom du projet"),
		FinalAge("Projection length", "Dur\u00E9e de la projection"),
		EstimateCarbonBiomass("Carbon estimation in biomass", "Estimation du carbone de la biomasse"),
		ProductionLinesLabel("Production lines", "Lignes de production"),
		Workspace("Workspace: ", "R\u00E9pertoire de travail : "),
		DifferenceBetween("Difference between ", "Difference entre "),
		And(" and ", " et "),
		Years("years", "ans");

		
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

	static class ParameterSetup {
		final String productionLinesName;
		final String biomassParametersName;
		

		ParameterSetup(CATSettings carbonToolSettings) {
			biomassParametersName = carbonToolSettings.biomassParametersMap.get(carbonToolSettings.currentBiomassParameters).toString();
			productionLinesName = carbonToolSettings.productionManagerMap.get(carbonToolSettings.currentProcessorManager).toString();
		}
	}
			
	private final int rotationLength;
	private final String standID;
	private final CATTimeTable timeTable;
	private final boolean isEvenAged;
	private final Map<CompartmentInfo, Estimate<?>> budgetMap;
	private final Map<String, SpeciesMonteCarloEstimateMap> logGradeMap;
	private final Map<CompartmentInfo, MonteCarloEstimate> evolutionMap;
	private final Map<CarbonUnitStatus, UseClassSpeciesMonteCarloEstimateMap> hwpContentByUseClass;
	private final Map<Integer, UseClassSpeciesMonteCarloEstimateMap> productEvolutionMap;
	private final MonteCarloEstimate heatProductionEvolutionKWhHa;
	private final MonteCarloEstimate totalHeatProductionKWhHa;
	private final ParameterSetup setup;
	private final String resultId;
	private boolean isValid;
		
	CATSingleSimulationResult(String resultId, CATCompartmentManager manager) {

		isValid = true;
		
		isEvenAged = manager.isEvenAged();
		setup = new ParameterSetup(manager.getCarbonToolSettings());
		
		rotationLength = manager.getRotationLength();
		standID = manager.getLastStand().getStandIdentification();
		
		timeTable = manager.getTimeTable();
		
		budgetMap = new HashMap<CompartmentInfo, Estimate<?>>();
		evolutionMap = new HashMap<CompartmentInfo, MonteCarloEstimate>();
		logGradeMap = new TreeMap<String, SpeciesMonteCarloEstimateMap>();
		
		hwpContentByUseClass = new HashMap<CarbonUnitStatus, UseClassSpeciesMonteCarloEstimateMap>();
		productEvolutionMap = new HashMap<Integer, UseClassSpeciesMonteCarloEstimateMap>();
		
		heatProductionEvolutionKWhHa = new MonteCarloEstimate();
		totalHeatProductionKWhHa = new MonteCarloEstimate();
		
		this.resultId = resultId;
	}
	
	protected void updateResult(CATCompartmentManager manager) {
		try {
			CATCompartment compartment;
			Matrix value;
			double plotAreaHa = manager.getTimeTable().getLastStandForThisRealization().getAreaHa();

			if (manager.getCarbonToolSettings().isVerboseEnabled()) {
				System.out.println("Updating results... Plot area (ha) is " + plotAreaHa);
			}

			for (CompartmentInfo compartmentID : CompartmentInfo.values()) {
				compartment = manager.getCompartments().get(compartmentID);
				
				value = new Matrix(1,1);
				value.setValueAt(0, 0, compartment.getIntegratedCarbon(plotAreaHa));
				if (!budgetMap.containsKey(compartmentID)) {
					budgetMap.put(compartmentID, new MonteCarloEstimate());
				}
				((MonteCarloEstimate) budgetMap.get(compartmentID)).addRealization(value);
				
				value = compartment.getCarbonEvolution(plotAreaHa);
				if (!evolutionMap.containsKey(compartmentID)) {
					evolutionMap.put(compartmentID, new MonteCarloEstimate());
				}
				evolutionMap.get(compartmentID).addRealization(value);
				
				if (compartmentID == CompartmentInfo.WComb) {
					CATProductCompartment productCompartment = (CATProductCompartment) compartment;
					heatProductionEvolutionKWhHa.addRealization(productCompartment.getHeatProductionEvolutionMgWhHa(plotAreaHa));
					totalHeatProductionKWhHa.addRealization(productCompartment.getTotalHeatProductionMgWhHa(plotAreaHa));
				}
			}

			CATProductCompartment productCompartment = (CATProductCompartment) manager.getCompartments().get(CompartmentInfo.Products);
			
			Map<String, CATSpeciesAmountMap> volumes = productCompartment.getVolumeByLogGradePerHa();
			for (String key : volumes.keySet()) {
				CATSpeciesAmountMap aMap = volumes.get(key);
				if (!logGradeMap.containsKey(key)) {
					logGradeMap.put(key, new SpeciesMonteCarloEstimateMap());
				}
				SpeciesMonteCarloEstimateMap estimateMap = logGradeMap.get(key);
				aMap.recordAsRealization(estimateMap);
			}
			
			Map<CarbonUnitStatus, CATUseClassSpeciesAmountMap> hWPMap = productCompartment.getHWPContentByUseClassPerHa(true);		// true : with recycling
			for (CarbonUnitStatus type : hWPMap.keySet()) {
				if (!hwpContentByUseClass.containsKey(type)) {
					hwpContentByUseClass.put(type, new UseClassSpeciesMonteCarloEstimateMap());
				}
				CATUseClassSpeciesAmountMap innerHWPMap = hWPMap.get(type);
				UseClassSpeciesMonteCarloEstimateMap innerMap = hwpContentByUseClass.get(type);
				innerHWPMap.recordAsRealization(innerMap);
			}
			
			Map<Integer, CATUseClassSpeciesAmountMap> tmpMap = productCompartment.getWoodProductEvolutionPerHa();
			for (Integer year : tmpMap.keySet()) {
				if (!productEvolutionMap.containsKey(year)) {
					productEvolutionMap.put(year, new UseClassSpeciesMonteCarloEstimateMap());
				}
				UseClassSpeciesMonteCarloEstimateMap innerMap = productEvolutionMap.get(year);
				CATUseClassSpeciesAmountMap innerTmpMap = tmpMap.get(year);
				innerTmpMap.recordAsRealization(innerMap);
			}
		} catch (Exception e) {
			isValid = false;
			throw e;
		}
	}
	
	
	@Override
	public String toString() {
		return "<html>" + CATSingleSimulationResult.MessageID.StandIdentification.toString() + ": " + standID + "<br>" +
					CATSingleSimulationResult.MessageID.FinalAge.toString() + ": " + rotationLength + "<br>" +
					MessageID.EstimateCarbonBiomass.toString() + ": " + setup.biomassParametersName + "<br>" +
					MessageID.ProductionLinesLabel.toString() + ": " + setup.productionLinesName + "</html>";
	}


	@Override
	public Map<CompartmentInfo, Estimate<?>> getBudgetMap() {return budgetMap;}

	@Override
	public String getStandID() {return standID;}

	@Override
	public CATTimeTable getTimeTable() {return timeTable;}

	@Override
	public int getRotationLength() {return rotationLength;}

	@Override
	public Map<CompartmentInfo, MonteCarloEstimate> getEvolutionMap() {return evolutionMap;}

	@Override
	public Map<CarbonUnitStatus, UseClassSpeciesMonteCarloEstimateMap> getHWPPerHaByUseClass() {return hwpContentByUseClass;}
	
	@Override
	public Map<String, SpeciesMonteCarloEstimateMap> getLogGradePerHa() {return logGradeMap;}

	@Override
	public Map<Integer, UseClassSpeciesMonteCarloEstimateMap> getProductEvolutionPerHa() {return productEvolutionMap;}
	
	@Override
	public String getResultId() {return resultId;}
	
	@Override
	public UseClassSpeciesMonteCarloEstimateMap getHWPSummaryPerHa(boolean includeRecycling) {
		if (includeRecycling) {
			UseClassSpeciesMonteCarloEstimateMap oMapProduct = getHWPPerHaByUseClass().get(CarbonUnitStatus.EndUseWoodProduct);
			UseClassSpeciesMonteCarloEstimateMap oMapRecycling = getHWPPerHaByUseClass().get(CarbonUnitStatus.Recycled);
			return oMapProduct.mergeWith(oMapRecycling);
		} else {
			return getHWPPerHaByUseClass().get(CarbonUnitStatus.EndUseWoodProduct);
		}
	}

	@Override
	public boolean isEvenAged() {return isEvenAged;}

	@Override
	public MonteCarloEstimate getHeatProductionEvolutionKWhPerHa() {return heatProductionEvolutionKWhHa;}

	@Override
	public boolean isValid() {
		return isValid;
	}

}
