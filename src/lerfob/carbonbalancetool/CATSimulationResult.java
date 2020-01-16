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

import java.util.Map;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.CATUtilityMaps.SpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.CATUtilityMaps.UseClassSpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.MonteCarloEstimate;

/**
 * The CarbonAssessmentToolSimulationResult interface ensures the object can provide the results of a carbon simulation.
 * @author Mathieu Fortin - February 2014
 */
public interface CATSimulationResult {

	/**
	 * Returns false if the simulation went wrong at some place.
	 * @return a boolean
	 */
	public boolean isValid();
	
	/**
	 * This method returns the "rotation-averaged" carbon stocks in each compartment.
	 * @return a Map with CompartmentInfo and Double as keys and values
	 */
	public Map<CompartmentInfo, Estimate<?>> getBudgetMap();
	
	/**
	 * This method return the identifier of the stand.
	 * @return a String
	 */
	public String getStandID();
	
	/**
	 * This method returns an array with the date or age of the carbon simulation.
	 * @return a TimeScale instance
	 */
	public CATTimeTable getTimeTable();
	
	/**
	 * This method returns either the rotation length (to the final cut) or the projection length in case of uneven-aged stand.
	 * @return an integer
	 */
	public int getRotationLength();
	
	/**
	 * This method returns the carbon stock evolution in each compartment.
	 * @return a Map with CompartmentInfo and array of Doubles as keys and values 
	 */
	public Map<CompartmentInfo, MonteCarloEstimate> getEvolutionMap();
	
	/**
	 * This method returns the amount of harvested wood products (HWPs) by use class and type (recycled, residues, etc...)
	 * @return a Map with CarbonUnitType and Maps as keys and values
	 */
	public Map<CarbonUnitStatus, UseClassSpeciesMonteCarloEstimateMap> getHWPPerHaByUseClass();

	/**
	 * This method returns total volume and biomass by log grade categories over the simulation period or rotation.
	 * @return a Map instance
	 */
	public Map<String, SpeciesMonteCarloEstimateMap> getLogGradePerHa();
	
	/**
	 * This method returns the HWPs by use class at the different steps of the simulation.
	 * @return a Map of integer and maps.
	 */
	public Map<Integer, UseClassSpeciesMonteCarloEstimateMap> getProductEvolutionPerHa();
	
	/**
	 * This method returns the result id for this instance. For example, it could be "sim1".
	 * @return a String
	 */
	public String getResultId();

	/**
	 * This method returns the summary of the HWP with or without recycling.
	 * @param includeRecycling a boolean
	 * @return a Map of Useclass and Map instances
	 */
	public UseClassSpeciesMonteCarloEstimateMap getHWPSummaryPerHa(boolean includeRecycling);

	/**
	 * This method returns the nature of the CarbonToolCompatibleStand stand. The summary of even-aged stands can be
	 * based on infinite sequence.  
	 * @return a boolean
	 */
	public boolean isEvenAged();

	/**
	 * This method returns the heat production (KWh) for one ha.
	 * @return a MonteCarlo estimate
	 */
	public MonteCarloEstimate getHeatProductionEvolutionKWhPerHa();
}
