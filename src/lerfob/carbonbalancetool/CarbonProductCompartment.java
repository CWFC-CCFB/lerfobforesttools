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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.biomassparameters.BiomassParameters;
import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.CarbonUnitList;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnit;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.WoodPiece;
//import lerfob.carbonbalancetool.productionlines.ProductionLineManager.CarbonUnitType;

/**
 * This method takes in charge the evolution of the carbon in the wood products
 * @author Mathieu Fortin - March 2010
 */
public class CarbonProductCompartment extends CarbonCompartment {


	/**
	 * Constructor.
	 * @param compartmentManager the CarbonCompartmentManager instance that drives this compartment
	 * @param compartment a CompartmentInfo enum variable 
	 */
	protected CarbonProductCompartment(CarbonCompartmentManager compartmentManager,	CompartmentInfo compartment) {
		super(compartmentManager, compartment);
	}


	private ProductionProcessorManager getProductionProcessorManager() {return getCompartmentManager().getCarbonToolSettings().getCurrentProductionProcessorManager();}
	
	
	@SuppressWarnings("deprecation")
	private Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> getAmountByUseClass(boolean withRecycling) {
		Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> outputMap = new HashMap<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>>();
		if (getCompartmentManager().getCarbonToolSettings().formerImplementation) {
			CarbonUnitList endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct);
			outputMap.put(CarbonUnitStatus.EndUseWoodProduct, summarizeWoodProductsInMap(endUseProductList));
			endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.IndustrialLosses);
			outputMap.put(CarbonUnitStatus.IndustrialLosses, summarizeWoodProductsInMap(endUseProductList));
			if (withRecycling) {
				endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.Recycled);
				outputMap.put (CarbonUnitStatus.Recycled, summarizeWoodProductsInMap(endUseProductList));
				endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.RecycledLosses);
				outputMap.put (CarbonUnitStatus.RecycledLosses, summarizeWoodProductsInMap(endUseProductList));
			}
		} else {
			CarbonUnitList endUseProductList = getProductionProcessorManager().getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct);
			outputMap.put(CarbonUnitStatus.EndUseWoodProduct, summarizeWoodProductsInMap(endUseProductList));
			endUseProductList = getProductionProcessorManager().getCarbonUnits(CarbonUnitStatus.IndustrialLosses);
			outputMap.put(CarbonUnitStatus.IndustrialLosses, summarizeWoodProductsInMap(endUseProductList));
			if (withRecycling) {
				endUseProductList = getProductionProcessorManager().getCarbonUnits(CarbonUnitStatus.Recycled);
				outputMap.put(CarbonUnitStatus.Recycled, summarizeWoodProductsInMap(endUseProductList));
				endUseProductList = getProductionProcessorManager().getCarbonUnits(CarbonUnitStatus.RecycledLosses);
				outputMap.put(CarbonUnitStatus.RecycledLosses, summarizeWoodProductsInMap(endUseProductList));
			}
			
		}
		return outputMap;
	}

	
	private Map<UseClass, AmountMap<Element>> summarizeWoodProductsInMap(CarbonUnitList carbonUnits) {
		Map<UseClass, AmountMap<Element>> outputMap = new HashMap<UseClass, AmountMap<Element>>();
		if (carbonUnits != null && !carbonUnits.isEmpty()) {
			for (CarbonUnit carbonUnit : carbonUnits) {
				EndUseWoodProductCarbonUnit endProduct = (EndUseWoodProductCarbonUnit) carbonUnit; 
				UseClass useClass = endProduct.getUseClass();
				if (!outputMap.containsKey(useClass)) {
					outputMap.put(useClass, new AmountMap<Element>());
				}
				AmountMap<Element> carrier = outputMap.get(useClass);
				carrier.putAll(endProduct.getAmountMap());
			}
		}
		return outputMap;
	}
	

	@SuppressWarnings("deprecation")
	private Map<Integer, Map<UseClass, AmountMap<Element>>> getWoodProductEvolution() {
		CarbonUnitList endUseProductList;
		CarbonUnitList industrialLosses;
		if (getCompartmentManager().getCarbonToolSettings().formerImplementation) {
			endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct);
			industrialLosses = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.IndustrialLosses);
		} else {
			endUseProductList = getProductionProcessorManager().getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct);
			industrialLosses = getProductionProcessorManager().getCarbonUnits(CarbonUnitStatus.IndustrialLosses);
		}
		endUseProductList.addAll(industrialLosses);
		return summarizeWoodProductEvolution(endUseProductList);
	}

	private Map<Integer, Map<UseClass, AmountMap<Element>>> summarizeWoodProductEvolution(CarbonUnitList carbonUnits) {
		CarbonAccountingToolTimeTable timeScale = getCompartmentManager().getTimeTable();
		Map<Integer, Map<UseClass, AmountMap<Element>>> outputMap = new HashMap<Integer, Map<UseClass, AmountMap<Element>>>();
		if (carbonUnits != null && !carbonUnits.isEmpty()) {
			int date;
			AmountMap<Element> carrier;
			for (CarbonUnit carbonUnit : carbonUnits) {
				EndUseWoodProductCarbonUnit endProduct = (EndUseWoodProductCarbonUnit) carbonUnit; 
				UseClass useClass = endProduct.getUseClass();
				date = timeScale.get(endProduct.getIndexInTimeScale());
				if (!outputMap.containsKey(date)) {
					outputMap.put(date, new HashMap<UseClass,AmountMap<Element>>());
				}
				Map<UseClass,AmountMap<Element>> innerMap = outputMap.get(date);
				if (!innerMap.containsKey(useClass)) {
					innerMap.put (useClass, new AmountMap<Element>());
				}
				carrier = innerMap.get(useClass);
				carrier.putAll(endProduct.getAmountMap());
			}
		}
		return outputMap;
	}



	/**
	 * This method returns the proportion of use class.
	 * @param withRecycling a boolean that takes the value true if recycled products are to be included
	 * @return a TreeMap instance
	 */
	protected TreeMap<UseClass, Double> getProductProportions(boolean withRecycling) {
		TreeMap<UseClass, Double> outputMap = new TreeMap<UseClass, Double>();
		Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> tmpMap = getAmountByUseClass(withRecycling);

		Map<UseClass, AmountMap<Element>> oMap;
		if (tmpMap.get(CarbonUnitStatus.Recycled) != null) {
			oMap = getMergedMap(tmpMap.get(CarbonUnitStatus.EndUseWoodProduct), tmpMap.get(CarbonUnitStatus.Recycled));
		} else {
			oMap = tmpMap.get(CarbonUnitStatus.EndUseWoodProduct);
		}

		if (!oMap.isEmpty()) {
			double sum = 0d;
			for (AmountMap<Element> carrier : oMap.values()) {
				sum += carrier.get(Element.Volume);
			}
			for (UseClass useClass : oMap.keySet()) {
				outputMap.put(useClass, oMap.get(useClass).get(Element.Volume) / sum);
			}
		}
		return outputMap;
	}

	/**
	 * This method merges the two maps of recycled and first use products.
	 * @param oMap1
	 * @param oMap2
	 * @return a new Map instance
	 */
	private Map<UseClass, AmountMap<Element>> getMergedMap(Map<UseClass, AmountMap<Element>> oMap1, Map<UseClass, AmountMap<Element>> oMap2) {
		Map<UseClass, AmountMap<Element>> outputMap = new HashMap<UseClass, AmountMap<Element>>();
		AmountMap<Element> amountMap;
		for (UseClass key : oMap1.keySet()) {
			amountMap = new AmountMap<Element>();
			outputMap.put(key, amountMap);
			amountMap.putAll(oMap1.get(key));
			amountMap.putAll(oMap2.get(key));
		}
		for (UseClass key : oMap2.keySet()) {
			if (!outputMap.containsKey(key)) {
				amountMap = new AmountMap<Element>();
				outputMap.put(key, amountMap);
				amountMap.putAll(oMap2.get(key));
			}
		}
		return outputMap;
	}


	/**
	 * This method returns the amount of nutrients per hectare (kg/ha) for each use class.
	 * @param plotArea the plot area in m2 in order to scale the result at the hectare level.
	 * @return a Map instance
	 */
	protected Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> getHWPContentByUseClassPerHa(boolean withRecycling) {
		double areaFactor = 1d / getCompartmentManager().getLastStand().getAreaHa();
		Map<CarbonUnitStatus, Map<UseClass, AmountMap<Element>>> outputMap = getAmountByUseClass(withRecycling);
		for (CarbonUnitStatus type : outputMap.keySet()) {
			Map<UseClass, AmountMap<Element>> innerMap = outputMap.get(type);
			for (UseClass useClass : innerMap.keySet()) {
				AmountMap<Element> innerInnerMap = innerMap.get(useClass);
				innerMap.put(useClass, innerInnerMap.multiplyByAScalar(areaFactor));
			}
		}
		return outputMap;
	}


	/**
	 * This method returns the list of the different log grades and their associated volumes and biomasses.
	 * @return a TreeMap instance
	 */
	protected TreeMap<String, AmountMap<Element>> getVolumeByLogGradePerHa() {
		double areaFactor = 1d / getCompartmentManager().getLastStand().getAreaHa();

		BiomassParameters biomassParameters = getCompartmentManager().getCarbonToolSettings().getCurrentBiomassParameters();
		Map<LoggableTree, Collection<WoodPiece>> woodPieceMap = this.getCompartmentManager().getCarbonToolSettings().getTreeLogger().getWoodPieces();
		TreeMap<String, AmountMap<Element>> volumeByLogGrade = new TreeMap<String, AmountMap<Element>>();
		String logCategoryName;
		double volume;

		AmountMap<Element> carrier;
		for (LoggableTree tree : woodPieceMap.keySet()) {
			Collection<WoodPiece> coll = woodPieceMap.get(tree);
			double basicDensity = biomassParameters.getBasicWoodDensityFromThisTree((CarbonToolCompatibleTree) tree, getCompartmentManager());
			for (WoodPiece piece : coll) {
				logCategoryName = piece.getLogCategory().getName();
				volume = piece.getWeightedVolumeM3();
				if (!volumeByLogGrade.containsKey(logCategoryName)) {
					volumeByLogGrade.put(logCategoryName, new AmountMap<Element>());
				}
				carrier = volumeByLogGrade.get(logCategoryName);
				carrier.add(Element.Volume, volume);
				carrier.add(Element.Biomass, volume * basicDensity);
			}
		}
		for (String logName : volumeByLogGrade.keySet()) {
			carrier = volumeByLogGrade.get(logName);
			volumeByLogGrade.put(logName, carrier.multiplyByAScalar(areaFactor));
		}

		return volumeByLogGrade;
	}

	protected Map<Integer, Map<UseClass, AmountMap<Element>>> getWoodProductEvolutionPerHa() {
		double areaFactor = 1d / getCompartmentManager().getLastStand().getAreaHa();

		Map<Integer, Map<UseClass, AmountMap<Element>>> outerMap = getWoodProductEvolution();

		Map<UseClass, AmountMap<Element>> innerMap;
		for (Integer date : outerMap.keySet()) {
			innerMap = outerMap.get(date);
			for (UseClass useClass : innerMap.keySet()) {
				AmountMap<Element> carrier = innerMap.get(useClass);
				innerMap.put(useClass, carrier.multiplyByAScalar(areaFactor));
			}
		}

		return outerMap;
	}


}
