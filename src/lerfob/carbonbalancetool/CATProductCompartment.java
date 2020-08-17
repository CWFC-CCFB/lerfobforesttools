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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.CATUtilityMaps.CATSpeciesAmountMap;
import lerfob.carbonbalancetool.CATUtilityMaps.CATUseClassSpeciesAmountMap;
import lerfob.carbonbalancetool.biomassparameters.BiomassParameters;
import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.CarbonUnitList;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnit;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.math.Matrix;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.WoodPiece;
//import lerfob.carbonbalancetool.productionlines.ProductionLineManager.CarbonUnitType;

/**
 * This method takes in charge the evolution of the carbon in the wood products
 * @author Mathieu Fortin - March 2010
 */
public class CATProductCompartment extends CATCompartment {

	
	private double[] heatProductionArray;
	private double totalHeatProduction;
	

	/**
	 * Constructor.
	 * @param compartmentManager the CarbonCompartmentManager instance that drives this compartment
	 * @param compartment a CompartmentInfo enum variable 
	 */
	protected CATProductCompartment(CATCompartmentManager compartmentManager,	CompartmentInfo compartment) {
		super(compartmentManager, compartment);
	}


	private ProductionProcessorManager getProductionProcessorManager() {return getCompartmentManager().getCarbonToolSettings().getCurrentProductionProcessorManager();}
	
	
	@SuppressWarnings("deprecation")
	private Map<CarbonUnitStatus, CATUseClassSpeciesAmountMap> getAmountByUseClass(boolean withRecycling) {
		Map<CarbonUnitStatus, CATUseClassSpeciesAmountMap> outputMap = new HashMap<CarbonUnitStatus, CATUseClassSpeciesAmountMap>();
		if (getCompartmentManager().getCarbonToolSettings().formerImplementation) {
			CarbonUnitList endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct);
			outputMap.put(CarbonUnitStatus.EndUseWoodProduct, summarizeWoodProductsInMap(endUseProductList));
			endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.IndustrialLosses);
			outputMap.put(CarbonUnitStatus.IndustrialLosses, summarizeWoodProductsInMap(endUseProductList));
			if (withRecycling) {
				endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.Recycled);
				outputMap.put(CarbonUnitStatus.Recycled, summarizeWoodProductsInMap(endUseProductList));
				endUseProductList = getCompartmentManager().getCarbonToolSettings().getProductionLines().getCarbonUnits(CarbonUnitStatus.RecycledLosses);
				outputMap.put(CarbonUnitStatus.RecycledLosses, summarizeWoodProductsInMap(endUseProductList));
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

	@SuppressWarnings("deprecation")
	private Map<Integer, CATUseClassSpeciesAmountMap> getWoodProductEvolution() {
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

	
	private CATUseClassSpeciesAmountMap summarizeWoodProductsInMap(CarbonUnitList carbonUnits) {
		List<String> speciesList = getCompartmentManager().getSpeciesList();
		CATUseClassSpeciesAmountMap outputMap = new CATUseClassSpeciesAmountMap(speciesList);
		if (carbonUnits != null && !carbonUnits.isEmpty()) {
			for (UseClass useClass : UseClass.values()) {
				CarbonUnitList subList = carbonUnits.filterList(EndUseWoodProductCarbonUnit.class, "getUseClass", useClass);
				CATSpeciesAmountMap oMap = CATUtilityMaps.convertToSpeciesMap(subList, speciesList);
				if (!oMap.isEmpty()) {
					outputMap.put(useClass, oMap);
				}
			}
		}
		return outputMap;
	}

	private Map<Integer, CATUseClassSpeciesAmountMap> summarizeWoodProductEvolution(CarbonUnitList carbonUnits) {
		List<String> speciesList = getCompartmentManager().getSpeciesList();
		CATTimeTable timeScale = getCompartmentManager().getTimeTable();
		Map<Integer, CATUseClassSpeciesAmountMap> outputMap = new HashMap<Integer, CATUseClassSpeciesAmountMap>();
		if (carbonUnits != null && !carbonUnits.isEmpty()) {
			for (Integer date : timeScale) {
				CarbonUnitList subList;
				if (getCompartmentManager().getCarbonToolSettings().formerImplementation) {
					subList = new CarbonUnitList();
					for (CarbonUnit carbonUnit : carbonUnits) {
						if (getCompartmentManager().getTimeTable().get(carbonUnit.getIndexInTimeScale()) == date) {
							subList.add(carbonUnit);
						}
					}
				} else {
					subList	= carbonUnits.filterList(CarbonUnit.class, "getCreationDate", date);
				}
				for (UseClass useClass : UseClass.values()) {
					CarbonUnitList subSubList = subList.filterList(EndUseWoodProductCarbonUnit.class, "getUseClass", useClass);
					CATSpeciesAmountMap oMap = CATUtilityMaps.convertToSpeciesMap(subSubList, speciesList);
					if (!oMap.isEmpty()) {
						if (!outputMap.containsKey(date)) {
							outputMap.put(date, new CATUseClassSpeciesAmountMap(speciesList));
						}
						CATUseClassSpeciesAmountMap innerMap = outputMap.get(date);
						innerMap.put(useClass, oMap);
					}
				}
			}
		}
		return outputMap;
	}


	
	
	
	

	/**
	 * This method returns the proportion of use class.
	 * @param withRecycling a boolean that takes the value true if recycled products are to be included
	 * @return a TreeMap instance
	 */
	protected TreeMap<UseClass, Map<String, Double>> getProductProportions(boolean withRecycling, Element element) {
		TreeMap<UseClass, Map<String, Double>> outputMap = new TreeMap<UseClass, Map<String, Double>>();  // UseClass / SpeciesName
		Map<CarbonUnitStatus, CATUseClassSpeciesAmountMap> tmpMap = getAmountByUseClass(withRecycling);

		CATUseClassSpeciesAmountMap oMap;
		if (tmpMap.containsKey(CarbonUnitStatus.Recycled)) {
			oMap = tmpMap.get(CarbonUnitStatus.EndUseWoodProduct).mergeWith(tmpMap.get(CarbonUnitStatus.Recycled));
		} else {
			oMap = tmpMap.get(CarbonUnitStatus.EndUseWoodProduct);
		}

		if (!oMap.isEmpty()) {
			AmountMap<Element> sum = oMap.getSum();
			for (UseClass useClass : oMap.keySet()) {
				if (!outputMap.containsKey(useClass)) {
					outputMap.put(useClass, new TreeMap<String, Double>());
				}
				CATSpeciesAmountMap innerMap = oMap.get(useClass);
				for (String speciesName : innerMap.keySet()) {
					outputMap.get(useClass).put(speciesName, innerMap.get(speciesName).get(element) / sum.get(element));
				}
			}
		}
		return outputMap;
	}

	/**
	 * This method returns the list of the different log grades and their associated volumes and biomasses.
	 * @return a TreeMap instance
	 */
	protected TreeMap<String, CATSpeciesAmountMap> getVolumeByLogGradePerHa() {
		List<String> speciesList = getCompartmentManager().getSpeciesList();
		double areaFactor = 1d / getCompartmentManager().getLastStand().getAreaHa();

		BiomassParameters biomassParameters = getCompartmentManager().getCarbonToolSettings().getCurrentBiomassParameters();
		TreeLogger<?,?> treeLogger = getCompartmentManager().getCarbonToolSettings().getTreeLogger();
		Map<LoggableTree, Collection<WoodPiece>> woodPieceMap = treeLogger.getWoodPieces();
		
		TreeMap<String, CATSpeciesAmountMap> outputMap = new TreeMap<String, CATSpeciesAmountMap>();
		for (String logCategory : treeLogger.getTreeLoggerParameters().getLogCategoryNames()) {
			outputMap.put(logCategory, new CATSpeciesAmountMap(speciesList));
		}
		
		String logCategoryName;
		double volume;

		AmountMap<Element> carrier;
		for (LoggableTree tree : woodPieceMap.keySet()) {
			String speciesName = tree.getSpeciesName();
			Collection<WoodPiece> coll = woodPieceMap.get(tree);
			double basicDensity = biomassParameters.getBasicWoodDensityFromThisTree((CATCompatibleTree) tree, getCompartmentManager());
			for (WoodPiece piece : coll) {
				logCategoryName = piece.getLogCategory().getName();
				volume = piece.getWeightedUnderbarkVolumeM3();
//				if (!outputMap.containsKey(logCategoryName)) {
//					outputMap.put(logCategoryName, new CATSpeciesAmountMap(speciesList));
//				}
				CATSpeciesAmountMap speciesAmountMap = outputMap.get(logCategoryName);
//				if (!speciesAmountMap.containsKey(speciesName)) {
//					speciesAmountMap.put(speciesName, new AmountMap<Element>());
//				}
				
				carrier = speciesAmountMap.get(speciesName);
				carrier.add(Element.Volume, volume);
				carrier.add(Element.Biomass, volume * basicDensity);
			}
		}
		for (CATSpeciesAmountMap speciesAmountMap : outputMap.values()) {
			for (String speciesName : speciesAmountMap.keySet()) {
				speciesAmountMap.put(speciesName, speciesAmountMap.get(speciesName).multiplyByAScalar(areaFactor));
			}
		}

		return outputMap;
	}

	/**
	 * This method returns the amount of nutrients per hectare (kg/ha) for each use class.
	 * @return a Map instance
	 */
	protected Map<CarbonUnitStatus, CATUseClassSpeciesAmountMap> getHWPContentByUseClassPerHa(boolean withRecycling) {
		double areaFactor = 1d / getCompartmentManager().getLastStand().getAreaHa();
		Map<CarbonUnitStatus, CATUseClassSpeciesAmountMap> outputMap = getAmountByUseClass(withRecycling);
//		Map<CarbonUnitStatus, CATUseClassSpeciesAmountMap> scaledMap = AmountMap.scaleMap(outputMap, areaFactor);
		AmountMap.scaleMap(outputMap, areaFactor);
		return outputMap;
	}

	protected Map<Integer, CATUseClassSpeciesAmountMap> getWoodProductEvolutionPerHa() {
		double areaFactor = 1d / getCompartmentManager().getLastStand().getAreaHa();
		Map<Integer, CATUseClassSpeciesAmountMap> outerMap = getWoodProductEvolution();
//		Map<Integer, CATUseClassSpeciesAmountMap> scaledMap = AmountMap.scaleMap(outerMap, areaFactor);
		AmountMap.scaleMap(outerMap, areaFactor);
		return outerMap;
	}

	protected void setHeatProductionArray(double[] heatProductionArray) {
		this.heatProductionArray = heatProductionArray;
	}
	
	protected void setTotalHeatProduction(double totalHeatProduction) {
		this.totalHeatProduction = totalHeatProduction;
	}

	protected Matrix getHeatProductionEvolutionMgWhHa(double plotAreaHa) {
		Matrix mat = new Matrix(heatProductionArray);
		return mat.scalarMultiply(1d / plotAreaHa);
	}
	
	protected Matrix getTotalHeatProductionMgWhHa(double plotAreaHa) {
		Matrix mat = new Matrix(1,1);
		mat.m_afData[0][0] = totalHeatProduction * 1d / plotAreaHa;
		return mat;
	}
	
}
