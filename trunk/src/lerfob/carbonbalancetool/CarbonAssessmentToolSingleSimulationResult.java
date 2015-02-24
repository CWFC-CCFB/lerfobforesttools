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

import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.simulation.processsystem.AmountMap;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

class CarbonAssessmentToolSingleSimulationResult implements CarbonAssessmentToolSimulationResult {

	protected static enum MessageID implements TextableEnum {
		FinalAge("Final date or age", "Age ou date finale"),
		EstimateCarbonBiomass("Carbon estimation in biomass", "Estimation du carbone de la biomasse"),
		ProductionLinesLabel("Production lines", "Lignes de production"),
		Workspace("Workspace: ", "R\u00E9pertoire de travail : ");

		
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
		

		ParameterSetup(CarbonAccountingToolSettings carbonToolSettings) {
			biomassParametersName = carbonToolSettings.biomassParametersVector.get(carbonToolSettings.currentBiomassParametersIndex).toString();
			productionLinesName = carbonToolSettings.processorManagers.get(carbonToolSettings.currentProcessorManagerIndex).toString();
		}
	}

			
	private final int rotationLength;
	private final String standID;
	private final Integer[] timeScale;
	private final Map<CompartmentInfo, Double> budgetMap;
	private final Map<String, AmountMap<Element>> logGradeMap;
	private final Map<CompartmentInfo, Double[]> evolutionMap;
	private final Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> hwpContentByUseClass;
	private final Map<Integer, Map<UseClass, AmountMap<Element>>> productEvolutionMap;
	private final ParameterSetup setup;
	
	CarbonAssessmentToolSingleSimulationResult(CarbonCompartmentManager manager) {

		setup = new ParameterSetup(manager.getCarbonToolSettings());
		
		rotationLength = manager.getRotationLength();
		standID = manager.getLastStand().getStandIdentification();
		
		double plotAreaHa = manager.getLastStand().getAreaHa();
		timeScale = manager.getTimeScale();
		CarbonCompartment compartment;
		
		budgetMap = new HashMap<CompartmentInfo, Double>();
		evolutionMap = new HashMap<CompartmentInfo, Double[]>();
		
		for (CompartmentInfo compartmentID : CompartmentInfo.values()) {
			compartment = manager.getCompartments().get(compartmentID);
			double value = compartment.getIntegratedCarbon(plotAreaHa);
			budgetMap.put(compartmentID, value);
			Double[] values = compartment.getCarbonEvolution(plotAreaHa);
			evolutionMap.put(compartmentID, values);
		}
		
		logGradeMap = new TreeMap<String, AmountMap<Element>>();

		CarbonProductCompartment productCompartment = (CarbonProductCompartment) manager.getCompartments().get(CompartmentInfo.Products);
		Map<String, AmountMap<Element>> volumes = productCompartment.getVolumeByLogGradePerHa();
		AmountMap<Element> carrier;
		for (String logCategoryName : volumes.keySet()) {
			carrier = volumes.get(logCategoryName);
			logGradeMap.put(logCategoryName, carrier);
		}

		hwpContentByUseClass = productCompartment.getHWPContentByUseClassPerHa(true);
		productEvolutionMap = productCompartment.getWoodProductEvolutionPerHa();
	}

	
	@Override
	public String toString() {
		return "<html>" + CarbonAssessmentToolSingleSimulationResult.MessageID.FinalAge.toString() + ": " + rotationLength + "<br>" +
					MessageID.EstimateCarbonBiomass.toString() + ": " + setup.biomassParametersName + "<br>" +
					MessageID.ProductionLinesLabel.toString() + ": " + setup.productionLinesName + "</html>";
	}


	@Override
	public Map<CompartmentInfo, Double> getBudgetMap() {return budgetMap;}

	@Override
	public String getStandID() {return standID;}

	@Override
	public Integer[] getTimeScale() {return timeScale;}

	@Override
	public int getRotationLength() {return rotationLength;}

	@Override
	public Map<CompartmentInfo, Double[]> getEvolutionMap() {return evolutionMap;}

	@Override
	public Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> getHWPContentByUseClass() {return hwpContentByUseClass;}
	
	@Override
	public Map<String, AmountMap<Element>> getLogGradeMap() {return logGradeMap;}

	@Override
	public Map<Integer, Map<UseClass, AmountMap<Element>>> getProductEvolutionMap() {return productEvolutionMap;}
	
}
