/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.carbonbalancetool.productionlines;

import java.util.ArrayList;
import java.util.List;

import lerfob.carbonbalancetool.CATCompartmentManager;
//import lerfob.carbonbalancetool.CATCompartmentManager;
import lerfob.carbonbalancetool.CATDecayFunction;
import lerfob.carbonbalancetool.CATExponentialFunction;
import lerfob.carbonbalancetool.CATTimeTable;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.processsystem.ProcessUnit;


/**
 * A CarbonUnit instance is a piece of carbon.
 * @author Mathieu Fortin - November 2010
 */
public class CarbonUnit extends ProcessUnit<Element> {

	
	public static enum CarbonUnitStatus {
		EndUseWoodProduct, 
		LandFillDegradable,
		LandFillNonDegradable,
		Recycled, 
		/** Can be either harvest residues or dead wood. */
		DeadWood, 
		IndustrialLosses,
		RecycledLosses;
	};

	
	public static enum Element {
		Volume,
		Biomass,
		/** Carbon */
		C,	
		/** Nitrogen */
		N,
		/** Sulfur */
		S, 
		/** Phosphorus */
		P, 
		/** Potassium */
		K,
		/** Emissions CO2 eq. related to the production and transport and others **/
		EmissionsCO2Eq;
		
		private static List<Element> nutrientList;
		
		public static List<Element> getNutrients() {
			if (nutrientList == null) {
				nutrientList = new ArrayList<Element>();
				nutrientList.add(N);
				nutrientList.add(S);
				nutrientList.add(P);
				nutrientList.add(K);
				nutrientList.add(C);
			}
			return nutrientList;
		}
		
		
	}
	
	private CATTimeTable timeTable;
	private final int dateIndex;
	protected final String samplingUnitID;
	private final List<CarbonUnitStatus> status; 
	
	/**
	 * Initial carbon in this product (Mg)
	 */
	private double[] currentCarbonArray;
	
	private CarbonUnitFeature carbonUnitFeature;
	private boolean actualized;
	private final String speciesName;

	/**
	 * General constructor.
	 * @param dateIndex the creation date index of the time scale
	 * @param samplingUnitID the id of the sample unit
	 * @param carbonUnitFeature a CarbonUnitFeature instance
	 * @param initialAmounts a map that contains the amount of each element to be processed
	 * @param species a CATSpecies enum
	 */
	protected CarbonUnit(int dateIndex, 
			String samplingUnitID, 
			CarbonUnitFeature carbonUnitFeature, 
			AmountMap<Element> initialAmounts,
			String speciesName) {
		super(initialAmounts);
		this.dateIndex = dateIndex;
		this.carbonUnitFeature = carbonUnitFeature;
		this.samplingUnitID = samplingUnitID;
		this.speciesName = speciesName;
		status = new ArrayList<CarbonUnitStatus>();
		actualized = false;
	}


	@Override
	protected void addProcessUnit(ProcessUnit<Element> unit) {
		super.addProcessUnit(unit);
	}

	
	protected boolean isActualized() {return actualized;}
	
	protected String getSpeciesName() {return speciesName;}

	/**
	 * This method returns the creation date of the product
	 * @return an integer
	 */
	protected int getCreationDate() {return timeTable.get(dateIndex);}
	
	protected void setTimeTable(CATTimeTable timeTable) {this.timeTable = timeTable;}
	protected CATTimeTable getTimeTable() {return timeTable;}
	protected CarbonUnitFeature getCarbonUnitFeature() {return carbonUnitFeature;}
	/**
	 * This method returns the carbon (tC) at the creation date. NOTE: For the landfill carbon, only
	 * the degradable organic carbon is considered.
	 * @return a double 
	 */
	public double getInitialCarbon() {return getAmountMap().get(Element.C);}

	/**
	 * This method returns an array that contains the current carbon (tC) or null if the carbon unit has not been actualized.
	 * @return an array of double
	 */
	public double[] getCurrentCarbonArray() {
		if (isActualized()) {
			return currentCarbonArray;
		} else {
			return null;
		}
	}

	/**
	 * This method calculates the average carbon by integrating the carbon contained in 
	 * this product over its lifetime.
	 * @return the integrated carbon in tC (double)
	 */
	public double getIntegratedCarbon(CATExponentialFunction decayFunction, MonteCarloSimulationCompliantObject subject) {
		decayFunction.setParameterValue(0, getCarbonUnitFeature().getAverageLifetime(subject));
		return getInitialCarbon() * decayFunction.getInfiniteIntegral(); //	0d : unnecessary parameter
	}



	/**
	 * This method actualizes the carbon content of this carbon unit.
	 * @param decayFunction the decay function 
	 * @param timeScale an array of integers that indicates the dates
	 * @throws Exception
	 */
	protected void actualizeCarbon(CATCompartmentManager compartmentManager) throws Exception {
		CATDecayFunction decayFunction = compartmentManager.getCarbonToolSettings().getDecayFunction();
		CATTimeTable timeScale = compartmentManager.getTimeTable();
		setTimeTable(timeScale);
		currentCarbonArray = new double[timeScale.size()];

		double lambdaValue = getCarbonUnitFeature().getAverageLifetime(compartmentManager);
		double currentCarbon = getInitialCarbon();

		double formerCarbon;
		double factor;
		int date;
		double formerDate;
		
		for (int i = dateIndex; i < timeScale.size(); i++) {
			date = timeScale.get(i);
			if (date > getCreationDate() && currentCarbon > ProductionProcessorManager.VERY_SMALL) {
				formerDate = timeScale.get(i - 1);
				decayFunction.setParameterValue(0, lambdaValue);
				decayFunction.setVariableValue(0, date - formerDate);
				factor = decayFunction.getValue();	// last parameter is unnecessary			
				formerCarbon = currentCarbonArray[i - 1];
				currentCarbon =  formerCarbon * factor;
				currentCarbonArray[i] = currentCarbon;
			} else if (date == getCreationDate()) {
				currentCarbonArray[i] = getInitialCarbon();
			}
		}
		actualized = true;
	}
	
//	protected boolean isFromIntervention() {
//		return status.size() == 1 && CarbonUnitStatus.getPotentialInterventionResultStatus().contains(status.get(0));
//	}

	/**
	 * This method returns the released carbon along in time given the product has been actualized. Otherwise it returns null.
	 * @return an array of double that contains the released carbon (tC)
	 */
	public double[] getReleasedCarbonArray() {
		if (isActualized()) {
			double[] releasedCarbonArray = new double[currentCarbonArray.length];
			int date;
			for (int i = 1; i < currentCarbonArray.length; i++) {
				date = getTimeTable().get(i);
				if (date > getCreationDate()) {
					releasedCarbonArray[i] = currentCarbonArray[i - 1] - currentCarbonArray[i];
				}
			}
			return releasedCarbonArray;
		} else {
			return null;
		}
	}


	/**
	 * A carbon unit object is considered to be equal if it has the same creation date and the same carbon unit features.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof CarbonUnit) {
			CarbonUnit otherUnit = (CarbonUnit) obj;
			if (carbonUnitFeature.equals(otherUnit.carbonUnitFeature)) {
				if (dateIndex == otherUnit.dateIndex) {
					if (status.equals(otherUnit.status)) {
						if (samplingUnitID.equals(otherUnit.samplingUnitID)) {
							return true;
						}
					}
				}
			}
		} 
		return false;
	}

	
	
	@Override
	public String toString() {
		return "Code : " + this.hashCode() 
				+ "; Volume = " + this.getAmountMap().get(Element.Volume) 
				+ "; Carbon : " + getInitialCarbon();
	}
	
	protected CarbonUnitStatus getLastStatus() {
		return status.get(status.size() - 1);
	}
	
	protected void addStatus(CarbonUnitStatus currentStatus) {status.add(currentStatus);}
	
	/**
	 * This method returns the emissions in Mg of CO2 Eq.
	 * @return a double
	 */
	public double getTotalNonRenewableCarbonEmissionsMgCO2Eq() {
		Double emissions = getAmountMap().get(Element.EmissionsCO2Eq);
		if (emissions == null) {
			return 0d;
		} else {
			return - emissions;
		}
	}
	
	/** 
	 * This method returns the index on the time scale at which the product has been created.
	 * @return an Integer
	 */
	public int getIndexInTimeScale() {return dateIndex;}
	
	
}
