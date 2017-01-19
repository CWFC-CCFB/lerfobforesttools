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
import java.util.List;

import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnit;
import lerfob.carbonbalancetool.productionlines.LandfillCarbonUnit;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

/**
 * This class contains functions to compute either the integral of the carbon compartment
 * or the evolution of carbon within this compartment.
 * @author Mathieu Fortin - July 2010
 */
public class CATCompartmentCompileLibrary {
		
	/**
	 * This method implements the functions for calculating and integrating the carbon over
	 * the forecast period.
	 * @param carbonCompartment = a carbon compartment (CarbonCompartment object)
	 */
	@SuppressWarnings("unchecked")
	public void selectCalculatorFunction(CATCompartment carbonCompartment) throws Exception {
		Collection<? extends CarbonUnit> carbonUnits;
		CATCompartmentManager manager = carbonCompartment.getCompartmentManager();
		CATTimeTable timeScale = manager.getTimeTable();
		double[] carbon = new double[timeScale.size()];
		double integratedCarbon = 0d;
		int revolutionPeriod = manager.getRotationLength();
		CATExponentialFunction decayFunction;
		
		List<CATCompatibleStand> stands = manager.getStandList();

		switch (carbonCompartment.getCompartmentID()) {
		
		case Roots:
			
			for (int i = 0; i < timeScale.size(); i++) {
				double carbonContent = 0d;
				if (i < stands.size()) {
					CATCompatibleStand stand = stands.get(i);
					carbonContent = manager.getCarbonToolSettings().getCurrentBiomassParameters().getBelowGroundCarbonMg(stand.getTrees(StatusClass.alive), manager);
				}
				carbonCompartment.setCarbonIntoArray(i, carbonContent);
			}
			
			carbonCompartment.setIntegratedCarbon(integrateCarbonOverHorizon(carbonCompartment) / revolutionPeriod);
			
			break;
			
		case AbGround:
			
			for (int i = 0; i < timeScale.size(); i++) {
				double carbonContent = 0d;
				if (i < stands.size()) {
					CATCompatibleStand stand = stands.get(i);
					carbonContent = manager.getCarbonToolSettings().getCurrentBiomassParameters().getAboveGroundCarbonMg(stand.getTrees(StatusClass.alive), manager);
				}
				carbonCompartment.setCarbonIntoArray(i, carbonContent);
			}			
			
			carbonCompartment.setIntegratedCarbon(integrateCarbonOverHorizon(carbonCompartment) / revolutionPeriod);

			break;
			
		// STOCK COMPARTMENTS
		case DeadBiom:
		case Products:
		case LfillDeg:
			
			decayFunction = manager.getCarbonToolSettings().getDecayFunction();
			
			for (int i = 0; i < timeScale.size(); i++) {
				carbonUnits = carbonCompartment.getCarbonUnitsArray()[i];
				if (carbonUnits != null && !carbonUnits.isEmpty()) {
					for (CarbonUnit carbonUnit : carbonUnits) {
						double[] actualizedCarbon = carbonUnit.getCurrentCarbonArray();	
						for (int j = 0; j < timeScale.size(); j++) {
							carbon[j] += actualizedCarbon[j];
						}
						integratedCarbon += carbonUnit.getIntegratedCarbon(decayFunction, manager);
					}
				}
			}
			
			for (int i = 0; i < timeScale.size(); i++) {
				carbonCompartment.setCarbonIntoArray(i, carbon[i]);
			}
			
			if (isEvenAged(carbonCompartment)) {
				carbonCompartment.setIntegratedCarbon(integratedCarbon / revolutionPeriod);
			} else {
				carbonCompartment.setIntegratedCarbon(integrateCarbonOverHorizon(carbonCompartment) / revolutionPeriod);
			}
		
			break;

			
		case CarbEmis:
			
			carbon = new double[timeScale.size()];
			double scalingFactor = CATSettings.CO2_C_FACTOR;
			if (manager.getCarbonToolSettings().formerImplementation) {
				scalingFactor = 1d;
			};
			
			// evolution
			for (int i = 0; i < timeScale.size(); i++) {
				carbonUnits = carbonCompartment.getCarbonUnitsArray()[i];
				if (carbonUnits != null && !carbonUnits.isEmpty()) {
					for (CarbonUnit carbonUnit : carbonUnits) {
						carbon[i] += carbonUnit.getTotalNonRenewableCarbonEmissionsMgCO2Eq() * scalingFactor;
					}
				}
				integratedCarbon += carbon[i];
				
			}

			for (int i = 0; i < timeScale.size(); i++) {
				if (i > 0) {
					carbon[i] += carbon[i - 1];
				}
			}

			for (int i = 0; i < timeScale.size(); i++) {
				carbonCompartment.setCarbonIntoArray(i, carbon[i]);
			}
			
			carbonCompartment.setIntegratedCarbon(integratedCarbon / revolutionPeriod);
			
			break;

		case EnerSubs:
			
			decayFunction = manager.getCarbonToolSettings().getDecayFunction();

			for (int i = 0; i < timeScale.size(); i++) {
				carbonUnits = carbonCompartment.getCarbonUnitsArray()[i];
				if (carbonUnits != null && !carbonUnits.isEmpty()) {
					for (CarbonUnit carbonUnit : carbonUnits) {
						EndUseWoodProductCarbonUnit endProduct = (EndUseWoodProductCarbonUnit) carbonUnit;
						
						double[] substitutedCarbon = endProduct.getCurrentCarbonSubstitution(manager);	
						for (int j = 0; j < timeScale.size(); j++) {
							carbon[j] += substitutedCarbon[j];
						}
						integratedCarbon += endProduct.getTotalCarbonSubstitution(manager);
					}
				}
			}
			
			for (int i = 0; i < timeScale.size(); i++) {
				if (i > 0) {
					carbon[i] += carbon[i - 1];
				}
			}

			for (int i = 0; i < timeScale.size(); i++) {
				carbonCompartment.setCarbonIntoArray(i, carbon[i]);
			}
			
			carbonCompartment.setIntegratedCarbon(integratedCarbon / revolutionPeriod);

			break;

		case LfillND:
			
			for (int i = 0; i < timeScale.size(); i++) {
				carbonUnits = carbonCompartment.getCarbonUnitsArray()[i];
				if (carbonUnits != null && !carbonUnits.isEmpty()) {
					for (CarbonUnit carbonUnit : carbonUnits) {
						LandfillCarbonUnit landfillCarbonProduct = (LandfillCarbonUnit) carbonUnit;
						integratedCarbon += landfillCarbonProduct.getInitialCarbon();
					}
				}

				carbonCompartment.setCarbonIntoArray(i, integratedCarbon);
			}
			carbonCompartment.setIntegratedCarbon(integratedCarbon / revolutionPeriod);
			break;
			
		case LfillEm:
			decayFunction = manager.getCarbonToolSettings().getDecayFunction();
			
			for (int i = 0; i < timeScale.size(); i++) {
				carbonUnits = carbonCompartment.getCarbonUnitsArray()[i];
				if (carbonUnits != null && !carbonUnits.isEmpty()) {
					for (CarbonUnit carbonUnit : carbonUnits) {
						LandfillCarbonUnit landfillCarbonProduct = (LandfillCarbonUnit) carbonUnit;

						double[] currentEmission = landfillCarbonProduct.getCarbonEquivalentMethaneEmissionsArray();	
						for (int j = 0; j < timeScale.size(); j++) {
							carbon[j] += currentEmission[j];
						}
						integratedCarbon += landfillCarbonProduct.getTotalNonRenewableCarbonEmissions();
					}
				}
			}
			
			for (int i = 0; i < timeScale.size(); i++) {
				if (i > 0) {
					carbon[i] += carbon[i - 1];
				}
			}

			for (int i = 0; i < timeScale.size(); i++) {
				carbonCompartment.setCarbonIntoArray(i, carbon[i]);
			}
			
			carbonCompartment.setIntegratedCarbon(integratedCarbon / revolutionPeriod);
			break;
			
		case LivingBiomass:
		case TotalProducts:
		case NetSubs:
			carbonCompartment.mergeWithFatherCompartments();
			break;
		}
		
	
	}
	
	private boolean isEvenAged(CATCompartment carbonCompartment) {
		return carbonCompartment.getCompartmentManager().isEvenAged();
	}
	
	/**
	 * This method computes a numerical approximation of the integral based on the trapezoidal rule.
	 * @param carbonCompartment = a carbon compartment (CarbonCompartment object)
	 * @return the total carbon (double)
	 */
	private double integrateCarbonOverHorizon(CATCompartment carbonCompartment) {
		CATTimeTable timeScale = carbonCompartment.getCompartmentManager().getTimeTable();
		boolean isEvenAged = isEvenAged(carbonCompartment);
		double previousValue;
		double currentValue;
		double totalCarbon = 0d;
		int currentDate;
		int previousDate;
		
		for (int i = 1; i < timeScale.size(); i++) {
			
			if (i == 1 &&  isEvenAged) {
				currentDate = timeScale.get(i - 1);
				currentValue = carbonCompartment.getCalculatedCarbonArray()[i - 1];
				totalCarbon += calculateCarbonForThisPeriod(0, currentDate, 0, currentValue);
			}
			
			currentDate = timeScale.get(i);
			currentValue = carbonCompartment.getCalculatedCarbonArray()[i];

			previousDate = timeScale.get(i - 1);
			previousValue = carbonCompartment.getCalculatedCarbonArray()[i - 1];
		
			totalCarbon += calculateCarbonForThisPeriod(previousDate, 
					currentDate, 
					previousValue, 
					currentValue);
		}
		
		return totalCarbon;
	}
	
	private double calculateCarbonForThisPeriod(int date0, int date1, double value0, double value1) {
		if (date1 - date0 > 0) {
			return (value1 + value0) * .5 * (date1 - date0);
		} else {
			return 0d;
		}
	}
	
}
