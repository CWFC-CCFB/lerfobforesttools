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

import repicea.simulation.processsystem.AmountMap;


public class LandfillCarbonUnit extends CarbonUnit {
	
	
	/**
	 * Constructor
	 * @param endProduct a EndProduct instance that originates from the landfill market flux.
	 */
	protected LandfillCarbonUnit(int dateIndex, 
							LandfillCarbonUnitFeature landfillCarbonUnitFeature,
							AmountMap<Element> amountMap,
							CarbonUnitStatus landfillStatus) {
		super(dateIndex, landfillCarbonUnitFeature, amountMap);
		addStatus(landfillStatus);
	}
	
	@Override
	protected LandfillCarbonUnitFeature getCarbonUnitFeature() {
		return (LandfillCarbonUnitFeature) super.getCarbonUnitFeature();
	}

	/**
	 * This method returns the CH4 carbon equivalent emissions through out the lifetime of the landfill product. 
	 * NOTE: if the array of released carbon is null this method returns null
	 * @return an array of double based on the time scale (in tC eq.)
	 */
	public double[] getCarbonEquivalentMethaneEmissionsArray() {
		double[] releasedCarbonArray = getReleasedCarbonArray();
		if (releasedCarbonArray != null) {
			double[] carbonEquivalentMethaneEmissionsArray = new double[releasedCarbonArray.length];
			for (int i = 0; i < releasedCarbonArray.length; i++) {
				carbonEquivalentMethaneEmissionsArray[i] = getCH4EmissionsForAParticularAmountOfCarbon(releasedCarbonArray[i]);
			}
			return carbonEquivalentMethaneEmissionsArray;
		} else {
			return null;
		}
	}
	
	/**
	 * This method returns the total CH4 carbon equivalent emissions through out the lifetime of the landfill product.
	 * @return a double (in tC eq.)
	 */
	public double getTotalNonRenewableCarbonEmissions() {
		return getCH4EmissionsForAParticularAmountOfCarbon(getInitialCarbon());
	}
	
	private double getCH4EmissionsForAParticularAmountOfCarbon(double carbon) {
		final double CH4_part = .5;
		final double CH4_CO2_conversion = 16d / 44;		// the relation between the different GHG is in term of weight. More methane is needed to get the same weight. 
		
		return - carbon * CH4_part * CH4_CO2_conversion * (LifeCycleAnalysis.CH4_CO2_EQUIVALENT - 1);		// -1 to account for the carbon that is already lost by the piece of wood (as if it were CO2)
	}
	
}
