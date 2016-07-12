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
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.math.Matrix;
import repicea.simulation.processsystem.AmountMap;
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
		And(" and ", " et ");

		
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
	private final Map<String, Map<Element, MonteCarloEstimate>> logGradeMap;
	private final Map<CompartmentInfo, MonteCarloEstimate> evolutionMap;
	private final Map<CarbonUnitStatus, Map<UseClass, Map<Element, MonteCarloEstimate>>> hwpContentByUseClass;
	private final Map<Integer, Map<UseClass, Map<Element, MonteCarloEstimate>>> productEvolutionMap;
	private final ParameterSetup setup;
	private final String resultId;
		
	CATSingleSimulationResult(String resultId, CATCompartmentManager manager) {

		isEvenAged = manager.isEvenAged();
		setup = new ParameterSetup(manager.getCarbonToolSettings());
		
		rotationLength = manager.getRotationLength();
		standID = manager.getLastStand().getStandIdentification();
		
		timeTable = manager.getTimeTable();
		
		budgetMap = new HashMap<CompartmentInfo, Estimate<?>>();
		evolutionMap = new HashMap<CompartmentInfo, MonteCarloEstimate>();
		logGradeMap = new TreeMap<String, Map<Element, MonteCarloEstimate>>();
		
		hwpContentByUseClass = new HashMap<CarbonUnitStatus, Map<UseClass, Map<Element, MonteCarloEstimate>>>();
		productEvolutionMap = new HashMap<Integer, Map<UseClass, Map<Element, MonteCarloEstimate>>>();
		
		this.resultId = resultId;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateResult(CATCompartmentManager manager) {
		CATCompartment compartment;
		Matrix value;
		double plotAreaHa = manager.getLastStand().getAreaHa();
		for (CompartmentInfo compartmentID : CompartmentInfo.values()) {
			compartment = manager.getCompartments().get(compartmentID);
			
			value = new Matrix(1,1);
			value.m_afData[0][0] = compartment.getIntegratedCarbon(plotAreaHa);
			if (!budgetMap.containsKey(compartmentID)) {
				budgetMap.put(compartmentID, new MonteCarloEstimate());
			}
			((MonteCarloEstimate) budgetMap.get(compartmentID)).addRealization(value);
			
			value = compartment.getCarbonEvolution(plotAreaHa);
			if (!evolutionMap.containsKey(compartmentID)) {
				evolutionMap.put(compartmentID, new MonteCarloEstimate());
			}
			evolutionMap.get(compartmentID).addRealization(value);
		}

		CATProductCompartment productCompartment = (CATProductCompartment) manager.getCompartments().get(CompartmentInfo.Products);
		Map<String, AmountMap<Element>> volumes = productCompartment.getVolumeByLogGradePerHa();
		addOneLevelMapToRealization((Map) logGradeMap, (Map) volumes);
		
		Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> hWPMap = productCompartment.getHWPContentByUseClassPerHa(true);		
		for (CarbonUnitStatus type : hWPMap.keySet()) {
			if (!hwpContentByUseClass.containsKey(type)) {
				hwpContentByUseClass.put(type, new HashMap<UseClass, Map<Element, MonteCarloEstimate>>());
			}
			Map<UseClass, AmountMap<Element>> innerHWPMap = hWPMap.get(type);
			Map<UseClass, Map<Element, MonteCarloEstimate>> innerMap = hwpContentByUseClass.get(type);
			addOneLevelMapToRealization((Map) innerMap, (Map) innerHWPMap);
		}
		
		Map<Integer, Map<UseClass, AmountMap<Element>>> tmpMap = productCompartment.getWoodProductEvolutionPerHa();
		for (Integer year : tmpMap.keySet()) {
			if (!productEvolutionMap.containsKey(year)) {
				productEvolutionMap.put(year, new HashMap<UseClass, Map<Element, MonteCarloEstimate>>());
			}
			Map<UseClass, Map<Element, MonteCarloEstimate>> innerMap = productEvolutionMap.get(year);
			Map<UseClass, AmountMap<Element>> innerTmpMap = tmpMap.get(year);
			addOneLevelMapToRealization((Map) innerMap, (Map) innerTmpMap);
		}
	}
	
	private void addAmountMapToRealization(Map<Element, MonteCarloEstimate> receivingMap, AmountMap<Element> amountMap) {
		Matrix value;
		for (Element element : amountMap.keySet()) {
			value = new Matrix(1,1);
			value.m_afData[0][0] = amountMap.get(element);
			if (!receivingMap.containsKey(element)) {
				receivingMap.put(element, new MonteCarloEstimate());
			}
			receivingMap.get(element).addRealization(value);
		}
	}
	
	private void addOneLevelMapToRealization(Map<Object, Map<Element, MonteCarloEstimate>> receivingMap, Map<Object, AmountMap<Element>> oMap) {
		for (Object key : oMap.keySet()) {
			if (!receivingMap.containsKey(key)) {
				receivingMap.put(key, new HashMap<Element, MonteCarloEstimate>());
			}
			addAmountMapToRealization(receivingMap.get(key), oMap.get(key));
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
	public Map<CarbonUnitStatus, Map<UseClass, Map<Element, MonteCarloEstimate>>> getHWPPerHaByUseClass() {return hwpContentByUseClass;}
	
	@Override
	public Map<String, Map<Element, MonteCarloEstimate>> getLogGradePerHa() {return logGradeMap;}

	@Override
	public Map<Integer, Map<UseClass, Map<Element, MonteCarloEstimate>>> getProductEvolutionPerHa() {return productEvolutionMap;}
	
	@Override
	public String getResultId() {return resultId;}
	
	@Override
	public Map<UseClass, Map<Element, MonteCarloEstimate>> getHWPSummaryPerHa(boolean includeRecycling) {
		if (includeRecycling) {
			Map<UseClass, Map<Element, MonteCarloEstimate>> outputMap = new TreeMap<UseClass, Map<Element, MonteCarloEstimate>>();
			Map<UseClass, Map<Element, MonteCarloEstimate>> oMapProduct = getHWPPerHaByUseClass().get(CarbonUnitStatus.EndUseWoodProduct);
			Map<UseClass, Map<Element, MonteCarloEstimate>> oMapRecycling = getHWPPerHaByUseClass().get(CarbonUnitStatus.Recycled);
			for (UseClass useClass : oMapProduct.keySet()) {
				Map<Element, MonteCarloEstimate> carrier = oMapProduct.get(useClass);
				Map<Element, MonteCarloEstimate> newCarrier = new HashMap<Element, MonteCarloEstimate>();
				outputMap.put(useClass, newCarrier);
				newCarrier.putAll(carrier);
			}

			for (UseClass useClass : oMapRecycling.keySet()) {
				Map<Element, MonteCarloEstimate> carrier = oMapRecycling.get(useClass);
				Map<Element, MonteCarloEstimate> newCarrier = outputMap.get(useClass);
				if (newCarrier != null) {
					for (Element element : Element.values()) {
						MonteCarloEstimate estimate1 = carrier.get(element);
						MonteCarloEstimate estimate2 = newCarrier.get(element);
						if (estimate1 != null) {
							if (estimate2 == null) {
								newCarrier.put(element, estimate1);
							} else {
								newCarrier.put(element, (MonteCarloEstimate) estimate1.getSumEstimate(estimate2));
							}
						}
					}
				} else {
					newCarrier = new HashMap<Element, MonteCarloEstimate>();
					outputMap.put(useClass, newCarrier);
					newCarrier.putAll(carrier);
				}
			}
			return outputMap;
		} else {
			return getHWPPerHaByUseClass().get(CarbonUnitStatus.EndUseWoodProduct);
		}
	}

	@Override
	public boolean isEvenAged() {return isEvenAged;}

}
