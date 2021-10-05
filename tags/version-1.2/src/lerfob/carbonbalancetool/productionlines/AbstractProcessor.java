/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2015 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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

import java.awt.Point;
import java.util.Collection;
import java.util.List;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.processsystem.ProcessUnit;
import repicea.simulation.processsystem.Processor;

@SuppressWarnings("serial")
public class AbstractProcessor extends Processor {

	protected double functionUnitBiomass; // in Mg
	
	protected double emissionsByFunctionalUnit; // in Mg

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<ProcessUnit> doProcess(List<ProcessUnit> inputUnits) {
		for (ProcessUnit processUnit : inputUnits) {
			AbstractProcessor.updateProcessEmissions(processUnit.getAmountMap(), functionUnitBiomass, emissionsByFunctionalUnit);
		}
		return super.doProcess(inputUnits);
	}

	
	protected static void updateProcessEmissions(AmountMap<CarbonUnit.Element> amountMap, double functionalUnitBiomassMg, double emissionsMgCO2ByFunctionalUnit) {
		Double biomassMg = (Double) amountMap.get(Element.Biomass);
		if (biomassMg != null && functionalUnitBiomassMg > 0) {
			double fonctionalUnits = biomassMg / functionalUnitBiomassMg;
			double emissions = fonctionalUnits * emissionsMgCO2ByFunctionalUnit;
			amountMap.add(Element.EmissionsCO2Eq, emissions);
		}
	}
	
	/*
	 * For extended visibility in the package.
	 */
	@Override
	protected Point getOriginalLocation() {
		return super.getOriginalLocation();
	}

	
}
