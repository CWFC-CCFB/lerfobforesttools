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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The CarbonCompartment class handles the calculation of the carbon content for
 * the different compartment (root, aboveground, wood products, etc.)
 * @author Mathieu Fortin - July 2010
 */
@SuppressWarnings("rawtypes")
public class CarbonCompartment implements Comparable {
	
	/**
	 * This enum contains basic information on the carbon compartments.
	 * @author Mathieu Fortin - June 2010
	 */
	public static enum CompartmentInfo implements TextableEnum {
		/**
		 * The compartment that contains the root biomass.
		 */
		Roots(false, "Belowground biomass", "Biomasse souterraine", false),
		/**
		 * The aboveground biomass (bole, branches and twigs).
		 */
		AbGround(false, "Aboveground biomass", "Biomasse a\u00E9rienne", false),
		/**
		 * The dead biomass, i.e. the dead trees and the wood pieces left in the forest
		 */
		DeadBiom(false, "Dead organic matter", "Mati\u00E8re organique morte", false),
		/**
		 * The wood products.
		 */
		Products(false, "Harvested wood products", "Produits bois", false),
		/**
		 * The landfill degradable carbon pool.
		 */
		LfillDeg(false, "Landfill (DOCf)", "D\u00E9charge (DOCf)", false),
		/**
		 * The carbon emissions due to fossil fuel consumption.
		 */
		CarbEmis(true, "Carbon emissions", "Emissions", false),
		/**
		 * The energy substitution.
		 */
		EnerSubs(true, "Energy and material substitution", "Substitution mat\u00E9rielle et \u00E9nerg\u00E9tique", false),
		/**
		 * The landfill non degradable carbon pool.
		 */
		LfillND(true, "Landfill (Non degradable)", "D\u00E9charge (Non d\u00E9gradable)", false),
		/**
		 * The Landfill GHG emissions (CH4)
		 */
		LfillEm(true, "Landfill (Methane emissions)", "D\u00E9charge (Emissions de m\u00E9thane)", false),
		/**
		 * The living biomass, i.e. the aboveground biomass + the roots.
		 */
		TotalBiomass(false, "Forest carbon pool", "Pool de carbone de la for\u00EAt", true),
		/**
		 * The sum of the living biomass and the wood products.
		 */
		TotalProducts(false, "HWP carbon pool", "Pool de carbone des produits du bois", true),
		/**
		 * The net substitution, i.e. the energy substitution less the carbon emissions.
		 */
		NetSubs(true, "Cumulative net flux", "Flux net cumulatif", true);

		private static List<CompartmentInfo> naturalOrder;
		
		private boolean isFlux;
		private boolean resultFromGrouping;

		CompartmentInfo(boolean isFlux, String englishText, String frenchText, boolean resultFromGrouping) {
			this.isFlux = isFlux;
			this.resultFromGrouping = resultFromGrouping;
			setText(englishText, frenchText);
		}
		
		protected boolean isFlux() {
			return isFlux;
		}

		protected boolean isResultFromGrouping() {
			return resultFromGrouping;
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {
			return REpiceaTranslator.getString(this);
		}

		/**
		 * This method returns the natural order of this enum variable.
		 * @return a List of CompartmentInfo instance
		 */
		public static List<CompartmentInfo> getNaturalOrder() {
			if (naturalOrder == null) {
				naturalOrder = new ArrayList<CompartmentInfo>();
				naturalOrder.add(TotalBiomass);
				naturalOrder.add(AbGround);
				naturalOrder.add(Roots);
				naturalOrder.add(DeadBiom);
				naturalOrder.add(TotalProducts);
				naturalOrder.add(Products);
				naturalOrder.add(LfillDeg);
				naturalOrder.add(NetSubs);
				naturalOrder.add(EnerSubs);
				naturalOrder.add(CarbEmis);
				naturalOrder.add(LfillEm);
				naturalOrder.add(LfillND);
			}
			return naturalOrder;
		}
	}

	private CarbonCompartmentManager compartmentManager;
	private CompartmentInfo compartmentInfo;
	private List<CarbonCompartment> fatherCompartments;
	private boolean isMerged;
	private CarbonCompartmentMethodLibrary carbonMethodLibrary;
	private double[] calculatedCarbonArray;
	private double integratedCarbon;

	private Collection<? extends CarbonUnit>[] carbonUnitsCollectionArray;
	

	/**
	 * General constructor for a CarbonCompartment object.
	 * @param compartmentManager = the compartment manager that handles this compartment
	 * @param compartmentInfo = the compartment info of this compartment
	 */
	protected CarbonCompartment(CarbonCompartmentManager compartmentManager, CompartmentInfo compartmentInfo) {
		this.compartmentManager = compartmentManager;
		this.compartmentInfo = compartmentInfo;
		this.fatherCompartments = new ArrayList<CarbonCompartment>();
		this.carbonMethodLibrary = new CarbonCompartmentMethodLibrary();
	}
	
	/* 
	 * Accessors
	 */
	public CompartmentInfo getCompartmentID() {return compartmentInfo;}
	public CarbonCompartmentManager getCompartmentManager() {return compartmentManager;}
	protected void addFatherCompartment(CarbonCompartment carbonCompartment) {fatherCompartments.add(carbonCompartment);}
	
	protected double[] getCalculatedCarbonArray() {return calculatedCarbonArray;}
	protected double getIntegratedCarbon() {return integratedCarbon;}
	protected void setIntegratedCarbon(double integratedCarbon) {this.integratedCarbon = integratedCarbon;}
	protected void setCarbonUnitsArray(Collection<? extends CarbonUnit>[] carbonUnitsCollectionArray) {this.carbonUnitsCollectionArray = carbonUnitsCollectionArray;}
	protected Collection<? extends CarbonUnit>[] getCarbonUnitsArray() {return carbonUnitsCollectionArray;}
	
	/**
	 * This method initializes the carbon map array.
	 */
	protected void resetCarbon() {
		calculatedCarbonArray = new double[getTimeScale().length];
		integratedCarbon = 0d;
		isMerged = false;
	}
	
	/**
	 * This method return a Map object whose keys are the steps and values are
	 * the carbon mass.
	 */
	public Double[] getCarbonEvolution(double plotAreaHa) {
		double areaFactor = 1d / plotAreaHa;
		
		Double[] outputArray = new Double[getTimeScale().length];
		
		
		for (int i = 0; i < getTimeScale().length; i++) {
			outputArray[i] = calculatedCarbonArray[i] * areaFactor;
		}
		
		return outputArray;
	}
	
	
	/**
	 * This method returns a random variable that contains the integrated carbon.
	 * @param plotArea the plot area in ha (double) 
	 * @return a double instance
	 */
	public double getIntegratedCarbon(double plotAreaHa) {
		double areaFactor = 1d / plotAreaHa;
		return integratedCarbon * areaFactor;
	}
	
	protected void setCarbonIntoArray(int indexDate, double d) {
		calculatedCarbonArray[indexDate] = d;
	}
	
	/**
	 * This method merges the carbon values of the father compartments into this compartment.
	 */
	protected void mergeWithFatherCompartments() {
		if (!this.fatherCompartments.isEmpty() && !isMerged) {
			for (CarbonCompartment comp : this.fatherCompartments) {
				comp.mergeWithFatherCompartments();			// recursive method to ensure that compartment with father compartments have been merged
				
				// for the calculated carbon
				for (int i = 0; i < getTimeScale().length; i++) {
					calculatedCarbonArray[i] += comp.getCalculatedCarbonArray()[i];
				}
				
				// for the integrated carbon
				this.integratedCarbon += comp.getIntegratedCarbon(); 
			}
			isMerged = true;
		}
	}
	
	public List<CarbonCompartment> getFatherCompartment() {return fatherCompartments;} 
	
	
	/**
	 * This method calculates and integrates the carbon for this compartment by calling the 
	 * method library and using the appropriate method.
	 */
	public void calculateAndIntegrateCarbon() throws Exception {
		carbonMethodLibrary.selectCalculatorFunction(this);
	}
	
	
	protected Integer[] getTimeScale() {return getCompartmentManager().getTimeScale();}
	
	protected static Vector<CompartmentInfo> getCompartmentInfoMap(boolean isFlux) {
		Vector<CompartmentInfo> outputSet = new Vector<CompartmentInfo>();
		for (CompartmentInfo compartmentInfo : CompartmentInfo.values()) {
			if (compartmentInfo.isFlux() == isFlux) {
				outputSet.add(compartmentInfo);
			}
		}
		return outputSet;
	}

	@Override
	public int compareTo (Object o) {
		CarbonCompartment thatComp = (CarbonCompartment) o;
		int thatOrder = CompartmentInfo.getNaturalOrder().indexOf(thatComp.getCompartmentID());
		int thisOrder = CompartmentInfo.getNaturalOrder ().indexOf(getCompartmentID());
		if (thisOrder < thatOrder) {
			return -1;
		} else if (thisOrder == thatOrder) {
			return 0;
		} else {
			return 1;
		}
	}
}
