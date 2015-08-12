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

import lerfob.carbonbalancetool.CarbonCompartmentManager;
import lerfob.carbonbalancetool.DecayFunction;
import lerfob.carbonbalancetool.DecayFunction.ParameterID;
import lerfob.carbonbalancetool.DecayFunction.VariableID;
import lerfob.carbonbalancetool.ExponentialFunction;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
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
		LeftInForest, 
		IndustrialLosses,
		RecycledLosses};

	
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
		K;
		
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
	
	private Integer[] timeScale;
	private int creationDate;
	private boolean isActualized; 
	private final List<CarbonUnitStatus> status; 
	
	/**
	 * Initial carbon in this product (Mg)
	 */
	private double[] currentCarbonArray;
	
	private CarbonUnitFeature carbonUnitFeature;

	/**
	 * General constructor.
	 * @param creationDate the creation date (year)
	 * @param carbonUnitFeature a CarbonUnitFeature instance
	 * @param initialAmounts a map that contains the amount of each element to be processed
	 */
	protected CarbonUnit(int creationDate, 
			CarbonUnitFeature carbonUnitFeature,
			AmountMap<Element> initialAmounts) {
		super(initialAmounts);
		this.creationDate = creationDate;
		this.carbonUnitFeature = carbonUnitFeature;
		status = new ArrayList<CarbonUnitStatus>();
	}


	@Override
	protected void addProcessUnit(ProcessUnit<Element> unit) {
		super.addProcessUnit(unit);
	}

	
	protected boolean isActualized() {return isActualized;}
	protected void setActualized(boolean isActualized) {this.isActualized = isActualized;}

	/**
	 * This method returns the creation date of the product
	 * @return an integer
	 */
	public int getCreationDate() {return creationDate;}
	
	protected void setTimeScale(Integer[] timeScale) {this.timeScale = timeScale;}
	protected Integer[] getTimeScale() {return timeScale;}
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
	public double getIntegratedCarbon(ExponentialFunction decayFunction) {
		decayFunction.setParameterValue(ParameterID.Lambda, getCarbonUnitFeature().getAverageLifetime());
		return getInitialCarbon() * decayFunction.getInfiniteIntegral(); //	0d : unnecessary parameter
	}



	/**
	 * This method actualizes the carbon content of this carbon unit.
	 * @param decayFunction the decay function 
	 * @param timeScale an array of integers that indicates the dates
	 * @throws Exception
	 */
	protected void actualizeCarbon(CarbonCompartmentManager compartmentManager) throws Exception {
		DecayFunction decayFunction = compartmentManager.getCarbonToolSettings().getDecayFunction();
		Integer[] timeScale = compartmentManager.getTimeScale();
		setTimeScale(timeScale);
		currentCarbonArray = new double[timeScale.length];

		double lambdaValue = getCarbonUnitFeature().getAverageLifetime();
		double currentCarbon = getInitialCarbon();

		double formerCarbon;
		double factor;
		int date;
		double formerDate;

		for (int i = 0; i < timeScale.length; i++) {
			date = timeScale[i];
			if (date > getCreationDate() && currentCarbon > ProductionProcessorManager.VERY_SMALL) {
				formerDate = timeScale[i - 1];
				decayFunction.setParameterValue(ParameterID.Lambda, lambdaValue);
				decayFunction.setVariableValue(VariableID.X, date - formerDate);
				factor = decayFunction.getValue();	// last parameter is unnecessary			
				formerCarbon = currentCarbonArray[i - 1];
				currentCarbon =  formerCarbon * factor;
				currentCarbonArray[i] = currentCarbon;
			} else {
				if (date == getCreationDate()) {						
					if (i == timeScale.length - 1 || timeScale[i + 1] > getCreationDate()) {	// we want to have the second step with its date equal to the creation date to ensure this is an intervention result
						currentCarbonArray[i] = getInitialCarbon();
					}
				} 
			}
		}
		setActualized(true);
	}
	

	/**
	 * This method returns the released carbon along in time given the product has been actualized. Otherwise it returns null.
	 * @return an array of double that contains the released carbon (tC)
	 */
	public double[] getReleasedCarbonArray() {
		if (isActualized()) {
			double[] releasedCarbonArray = new double[currentCarbonArray.length];
			int date;
			for (int i = 1; i < currentCarbonArray.length; i++) {
				date = getTimeScale()[i];
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
				if (creationDate == otherUnit.creationDate) {
					if (status.equals(otherUnit.status)) {
						return true;
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
	
	protected CarbonUnitStatus getLastStatus() {return status.get(status.size() - 1);}
	protected void addStatus(CarbonUnitStatus currentStatus) {status.add(currentStatus);}
}