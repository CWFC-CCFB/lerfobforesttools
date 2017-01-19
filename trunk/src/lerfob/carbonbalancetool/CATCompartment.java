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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import repicea.math.Matrix;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The CarbonCompartment class handles the calculation of the carbon content for
 * the different compartment (root, aboveground, wood products, etc.)
 * @author Mathieu Fortin - July 2010
 */
@SuppressWarnings("rawtypes")
public class CATCompartment implements Comparable {
	
	/**
	 * This enum contains basic information on the carbon compartments.
	 * @author Mathieu Fortin - June 2010
	 */
	public static enum CompartmentInfo implements TextableEnum {
		/**
		 * The compartment that contains the root biomass.
		 */
		Roots(false, "Belowground biomass", "Biomasse souterraine", false, new Color(0,100,0)),
		/**
		 * The aboveground biomass (bole, branches and twigs).
		 */
		AbGround(false, "Aboveground biomass", "Biomasse a\u00E9rienne", false, new Color(0,150,0)),
		/**
		 * The dead biomass, i.e. the dead trees and the wood pieces left in the forest
		 */
		DeadBiom(false, "Dead organic matter", "Mati\u00E8re organique morte", true, new Color(0,50,0)),
		/**
		 * The wood products.
		 */
		Products(false, "Harvested wood products", "Produits bois", false, new Color(0,0,150)),
		/**
		 * The landfill degradable carbon pool.
		 */
		LfillDeg(false, "Landfill (DOCf)", "D\u00E9charge (DOCf)", false, new Color(0,0,100)),
		/**
		 * The carbon emissions due to fossil fuel consumption.
		 */
		CarbEmis(true, "Carbon emissions", "Emissions", false, new Color(150,100,100)),
		/**
		 * The energy substitution.
		 */
		EnerSubs(true, "Energy and material substitution", "Substitution mat\u00E9rielle et \u00E9nerg\u00E9tique", false, new Color(0,100,100)),
		/**
		 * The landfill non degradable carbon pool.
		 */
		LfillND(true, "Landfill (Non degradable)", "D\u00E9charge (Non d\u00E9gradable)", false, new Color(0,150,150)),
		/**
		 * The Landfill GHG emissions (CH4)
		 */
		LfillEm(true, "Landfill (Methane emissions)", "D\u00E9charge (Emissions de m\u00E9thane)", false, new Color(150,150,150)),
		/**
		 * The libing biomass, i.e. the aboveground biomass + the belowground biomass.
		 */
		LivingBiomass(false, "Living biomass", "Biomass vivante", true, new Color(0,200,0)),
		/**
		 * The sum of the living biomass and the wood products.
		 */
		TotalProducts(false, "HWP carbon pool", "Pool de carbone des produits du bois", true, new Color(0,0,200)),
		/**
		 * The net substitution, i.e. the energy substitution less the carbon emissions.
		 */
		NetSubs(true, "Cumulative net flux", "Flux net cumulatif", true, new Color(200,0,0));

		private static List<CompartmentInfo> naturalOrder;
		
		private boolean isFlux;
		private boolean resultFromGrouping;
		private Color color;

		CompartmentInfo(boolean isFlux, String englishText, String frenchText, boolean resultFromGrouping, Color color) {
			this.isFlux = isFlux;
			this.resultFromGrouping = resultFromGrouping;
			this.color = color;
			setText(englishText, frenchText);
		}
		
		protected boolean isFlux() {return isFlux;}

		protected boolean isPrimaryCompartment() {return resultFromGrouping;}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}
		
		@Override
		public String toString() {return REpiceaTranslator.getString(this);}

		/**
		 * This method returns the natural order of this enum variable.
		 * @return a List of CompartmentInfo instance
		 */
		public static List<CompartmentInfo> getNaturalOrder() {
			if (naturalOrder == null) {
				naturalOrder = new ArrayList<CompartmentInfo>();
				naturalOrder.add(LivingBiomass);
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
		
		/**
		 * This method returns the color of the compartment for rendering.
		 * @return a Color instance
		 */
		public Color getColor() {return color;}
	}

	private CATCompartmentManager compartmentManager;
	private CompartmentInfo compartmentInfo;
	private List<CATCompartment> fatherCompartments;
	private boolean isMerged;
	private CATCompartmentCompileLibrary carbonMethodLibrary;
	private double[] calculatedCarbonArray;
	private double integratedCarbon;

	private Collection<? extends CarbonUnit>[] carbonUnitsCollectionArray;
	

	/**
	 * General constructor for a CarbonCompartment object.
	 * @param compartmentManager = the compartment manager that handles this compartment
	 * @param compartmentInfo = the compartment info of this compartment
	 */
	protected CATCompartment(CATCompartmentManager compartmentManager, CompartmentInfo compartmentInfo) {
		this.compartmentManager = compartmentManager;
		this.compartmentInfo = compartmentInfo;
		this.fatherCompartments = new ArrayList<CATCompartment>();
		this.carbonMethodLibrary = new CATCompartmentCompileLibrary();
	}
	
	/* 
	 * Accessors
	 */
	public CompartmentInfo getCompartmentID() {return compartmentInfo;}
	public CATCompartmentManager getCompartmentManager() {return compartmentManager;}
	protected void addFatherCompartment(CATCompartment carbonCompartment) {fatherCompartments.add(carbonCompartment);}
	
	protected double[] getCalculatedCarbonArray() {return calculatedCarbonArray;}
	protected double getIntegratedCarbon() {return integratedCarbon;}
	protected void setIntegratedCarbon(double integratedCarbon) {this.integratedCarbon = integratedCarbon;}
	protected void setCarbonUnitsArray(Collection<? extends CarbonUnit>[] carbonUnitsCollectionArray) {this.carbonUnitsCollectionArray = carbonUnitsCollectionArray;}
	protected Collection<? extends CarbonUnit>[] getCarbonUnitsArray() {return carbonUnitsCollectionArray;}
	
	/**
	 * This method initializes the carbon map array.
	 */
	protected void resetCarbon() {
		calculatedCarbonArray = new double[getTimeTable().size()];
		integratedCarbon = 0d;
		isMerged = false;
	}
	
	/**
	 * This method return a Map object whose keys are the steps and values are
	 * the carbon mass.
	 * @param plotAreaHa the area of the plot in ha
	 * @return a Matrix instance
	 */
	protected Matrix getCarbonEvolution(double plotAreaHa) {
		double areaFactor = 1d / plotAreaHa;
		
		Matrix value = new Matrix(getTimeTable().size(),1);
		
		for (int i = 0; i < value.m_iRows; i++) {
			value.m_afData[i][0] = calculatedCarbonArray[i] * areaFactor;
		}
		
		return value;
	}
	
	
	/**
	 * This method returns a random variable that contains the integrated carbon.
	 * @param plotArea the plot area in ha (double) 
	 * @return a double 
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
			for (CATCompartment comp : this.fatherCompartments) {
				comp.mergeWithFatherCompartments();			// recursive method to ensure that compartment with father compartments have been merged
				
				// for the calculated carbon
				for (int i = 0; i < getTimeTable().size(); i++) {
					calculatedCarbonArray[i] += comp.getCalculatedCarbonArray()[i];
				}
				
				// for the integrated carbon
				this.integratedCarbon += comp.getIntegratedCarbon(); 
			}
			isMerged = true;
		}
	}
	
	public List<CATCompartment> getFatherCompartment() {return fatherCompartments;} 
	
	
	/**
	 * This method calculates and integrates the carbon for this compartment by calling the 
	 * method library and using the appropriate method.
	 */
	public void calculateAndIntegrateCarbon() throws Exception {
		carbonMethodLibrary.selectCalculatorFunction(this);
	}
	
	
	protected CATTimeTable getTimeTable() {return getCompartmentManager().getTimeTable();}
	
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
		CATCompartment thatComp = (CATCompartment) o;
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
	
	
//	public static void main(String[] args) {
//		JDialog dlg = new JDialog();
//		dlg.setModal(true);
//		dlg.setLayout(new GridLayout(6,6));
//		for (int i = 0; i < 6; i++) {
//			for (int j = 0; j < 6; j++) {
//				JButton button = new JButton("Allo_" + i + "_" + j);
//				button.setBackground(new Color(i * 50, 100, 100));
//				dlg.add(button);
//			}
//		}
//		dlg.setSize(new Dimension(100,100));
//		dlg.setVisible(true);
//		System.exit(0);
//	}
}
