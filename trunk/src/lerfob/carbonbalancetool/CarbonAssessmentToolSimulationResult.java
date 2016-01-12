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

import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import repicea.stats.estimates.MonteCarloEstimate;

/**
 * The CarbonAssessmentToolSimulationResult interface ensures the object can provide the results of a carbon simulation.
 * @author Mathieu Fortin - February 2014
 */
public interface CarbonAssessmentToolSimulationResult {

	
	/**
	 * This method returns the "rotation-averaged" carbon stocks in each compartment.
	 * @return a Map with CompartmentInfo and Double as keys and values
	 */
	public Map<CompartmentInfo, MonteCarloEstimate> getBudgetMap();
	
	/**
	 * This method return the identifier of the stand.
	 * @return a String
	 */
	public String getStandID();
	
	/**
	 * This method returns an array with the date or age of the carbon simulation.
	 * @return an array of integers
	 */
	public Integer[] getTimeScale();
	
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
	public Map<CarbonUnitStatus, Map<UseClass, Map<Element, MonteCarloEstimate>>> getHWPPerHaByUseClass();
	
	
	/**
	 * This method returns the volume and biomass by log grade categories.
	 * @return a Map of String and VolumeBiomassCarrier
	 */
	public Map<String, Map<Element, MonteCarloEstimate>> getLogGradePerHa();
	
	/**
	 * This method returns the HWPs by use class at the different steps of the simulation.
	 * @return a Map of integer and maps.
	 */
	public Map<Integer, Map<UseClass, Map<Element, MonteCarloEstimate>>> getProductEvolutionPerHa();
	

}
