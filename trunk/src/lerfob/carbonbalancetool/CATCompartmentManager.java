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
import java.util.Map;
import java.util.TreeMap;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnitList;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnit;
import lerfob.carbonbalancetool.productionlines.ProductionLineManager;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.standlevel.StochasticInformationProvider;

@SuppressWarnings("deprecation")
public class CATCompartmentManager implements MonteCarloSimulationCompliantObject {

	private static int NumberOfExtraYrs = 80;	// number of years after the final cut

	private List<CATCompatibleStand> currentStands;	
	private List<CATCompatibleStand> stands;
	private CATSettings carbonAccountingToolSettings;		// reference to the extractor settings
	
	private Map<CompartmentInfo, CATCompartment> carbonCompartments;
	private int rotationLength;
	private boolean isEvenAged;
	private CATTimeTable timeTable;
//	private Integer[] timeScale;

	private boolean isSimulationValid;
	protected boolean isStochastic;
	private int currentRealization;
	protected int nRealizations;
	private int nbSimulations = 0;

	
	protected CATSingleSimulationResult summary;
	
	
	/**
	 * Constructor for this class
	 * @param tool a CarbonAccountingTool instance
	 * @throws Exception
	 */
	public CATCompartmentManager(CATSettings settings) {
		this.carbonAccountingToolSettings = settings;
		this.carbonCompartments = new TreeMap<CompartmentInfo, CATCompartment>();	// TreeMap to make sure the merge compartments are not called before the regular compartment
		isSimulationValid = false;
		initializeCompartments();
	}
		
	protected void setSimulationValid(boolean isSimulationValid) {
		this.isSimulationValid = isSimulationValid;
	}
	
	public void init(List<CATCompatibleStand> stands) {
		this.stands = stands;
		this.currentStands = null;
		if (stands != null) {
			CATCompatibleStand lastStand = stands.get(stands.size() - 1);
			isEvenAged = lastStand instanceof CATCompatibleEvenAgedStand;
			isStochastic = false;
			nRealizations = 1;
			if (lastStand instanceof StochasticInformationProvider) {
				StochasticInformationProvider<?> stochProv = (StochasticInformationProvider<?>) lastStand;
				if (stochProv.isStochastic() && stochProv.getRealization(0) instanceof CATCompatibleStand) {
					isStochastic = true;
					nRealizations = stochProv.getNumberRealizations();
				}
			}
			int nbExtraYears = 0;
			if (isEvenAged) {
				rotationLength = ((CATCompatibleEvenAgedStand) lastStand).getAgeYr();
				nbExtraYears = NumberOfExtraYrs;
			} else {
				rotationLength = lastStand.getDateYr() - stands.get(0).getDateYr();
			}
				
			int averageTimeStep = retrieveAverageTimeStep(stands);
			if (averageTimeStep == 0) {
				averageTimeStep = 5;	// default value in case there is a single step
			}
//			timeScale = new Integer[stands.size() + nbExtraYears / averageTimeStep];
//
//			for (int i = 0; i < timeScale.length; i++) {
//				if (i < stands.size()) {
//					if (isEvenAged) {
//						timeScale[i] = ((CarbonToolCompatibleEvenAgedStand) stands.get(i)).getAgeYr();
//					} else {
//						timeScale[i] = stands.get(i).getDateYr();
//					}
//				} else  {
//					timeScale[i] = timeScale[i - 1] + averageTimeStep;
//				}
//			}
			timeTable = new CATTimeTable(lastStand.getDateYr());
			
			int size = stands.size() + nbExtraYears / averageTimeStep;
			for (int i = 0; i < size; i++) {
				if (i < stands.size()) {
					if (isEvenAged) {
						timeTable.add(((CATCompatibleEvenAgedStand) stands.get(i)).getAgeYr());
					} else {
						timeTable.add(stands.get(i).getDateYr());
					}
				} else  {
					timeTable.add(timeTable.get(i - 1) + averageTimeStep);
				}
			}

		}
	}
	
	protected void resetManager() {
		resetCompartments();
		if (getCarbonToolSettings().formerImplementation) {
			ProductionLineManager productionLines = carbonAccountingToolSettings.getProductionLines();
			productionLines.resetCarbonUnitMap();
		} else {
			getCarbonToolSettings().getCurrentProductionProcessorManager().resetCarbonUnitMap();
		}
	}

	/**
	 * This method provides the duration of the time step
	 * @param steps a Vector of Step instances
	 * @return an integer 
	 */
	private int retrieveAverageTimeStep(List<CATCompatibleStand> stands) {
		double averageTimeStep = 0;		// default time step
		int nbHits = 0;
		int date;
		int formerDate;
		for (int i = 1; i < stands.size(); i++) {
			date = stands.get(i).getDateYr();
			formerDate = stands.get(i-1).getDateYr();
			if (date - formerDate > 0) {
				averageTimeStep += date - formerDate;
				nbHits++;
			}
		}
		return (int) Math.round(averageTimeStep / nbHits);
	}
	
	/**
	 * This method returns the TimeScale instance the simulation has been run with.
	 * @return
	 */
	public CATTimeTable getTimeTable() {return timeTable;}
	
	public CATSettings getCarbonToolSettings() {return carbonAccountingToolSettings;}

	@SuppressWarnings({ "unchecked"})
	public void resetCompartmentsAndSetCarbonUnitCollections() {
		CarbonUnitList joinEndUseProductRecyclageList = new CarbonUnitList();
		CarbonUnitList leftInForestList;
		CarbonUnitList degradableLandfillList;
		CarbonUnitList nonDegradableLandfillList;
		if 	(!getCarbonToolSettings().formerImplementation) {
			ProductionProcessorManager productionLineManager = getCarbonToolSettings().getCurrentProductionProcessorManager();
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct));
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.Recycled));
			leftInForestList = productionLineManager.getCarbonUnits(CarbonUnitStatus.HarvestResidues);
			degradableLandfillList = productionLineManager.getCarbonUnits(CarbonUnitStatus.LandFillDegradable);
			nonDegradableLandfillList = productionLineManager.getCarbonUnits(CarbonUnitStatus.LandFillNonDegradable);
		} else {
			ProductionLineManager productionLineManager = getCarbonToolSettings().getProductionLines();
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct));
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.Recycled));
			leftInForestList = productionLineManager.getCarbonUnits(CarbonUnitStatus.HarvestResidues);
			degradableLandfillList = productionLineManager.getCarbonUnits(CarbonUnitStatus.LandFillDegradable);
			nonDegradableLandfillList = productionLineManager.getCarbonUnits(CarbonUnitStatus.LandFillNonDegradable);
			
		}
		Collection<EndUseWoodProductCarbonUnit>[] endUseWoodProductCarbonUnits = (Collection<EndUseWoodProductCarbonUnit>[]) formatCarbonUnits(getTimeTable(), joinEndUseProductRecyclageList);
		Collection<CarbonUnit>[] leftInForestCarbonUnits = formatCarbonUnits(getTimeTable(), leftInForestList);
		Collection<CarbonUnit>[] degradableLandfillCarbonUnits = formatCarbonUnits(getTimeTable(), degradableLandfillList);
		Collection<CarbonUnit>[] nonDegradableLandfillCarbonUnits = formatCarbonUnits(getTimeTable(), nonDegradableLandfillList);


		resetCompartments();

		carbonCompartments.get(CompartmentInfo.CarbEmis).setCarbonUnitsArray(endUseWoodProductCarbonUnits);
		carbonCompartments.get(CompartmentInfo.EnerSubs).setCarbonUnitsArray(endUseWoodProductCarbonUnits);
		carbonCompartments.get(CompartmentInfo.Products).setCarbonUnitsArray(endUseWoodProductCarbonUnits);

		// dead biomass
		carbonCompartments.get(CompartmentInfo.DeadBiom).setCarbonUnitsArray(leftInForestCarbonUnits);
	
		// landfill wood products
		carbonCompartments.get(CompartmentInfo.LfillDeg).setCarbonUnitsArray(degradableLandfillCarbonUnits);
		carbonCompartments.get(CompartmentInfo.LfillEm).setCarbonUnitsArray(degradableLandfillCarbonUnits);
		carbonCompartments.get(CompartmentInfo.LfillND).setCarbonUnitsArray(nonDegradableLandfillCarbonUnits);
	}
	
	
	/**
	 * This method is called just before calculating the carbon in the compartments. It deletes all the carbon values in the different compartments.
	 */
	private void resetCompartments() {
		for (CATCompartment compartment : this.carbonCompartments.values()) {
			compartment.resetCarbon();
		}
	}
	

	/**
	 * This method initializes the different carbon compartments.
	 * @throws Exception
	 */
	private void initializeCompartments() {
		for (CompartmentInfo compartmentInfo : CompartmentInfo.values()) {
			switch (compartmentInfo) {
			case Roots:
			case AbGround:
			case DeadBiom:
			case LfillDeg:
			case LfillND:
			case LfillEm:
				carbonCompartments.put(compartmentInfo,	new CATCompartment(this, compartmentInfo));
				break;
			case CarbEmis:
			case Products:
			case EnerSubs:
				carbonCompartments.put(compartmentInfo, new CATProductCompartment(this, compartmentInfo));
				break;
			case TotalBiomass:
				CATCompartment standing = new CATCompartment(this, compartmentInfo);
				standing.addFatherCompartment(carbonCompartments.get(CompartmentInfo.AbGround));
				standing.addFatherCompartment(carbonCompartments.get(CompartmentInfo.Roots));
				standing.addFatherCompartment(carbonCompartments.get(CompartmentInfo.DeadBiom));
				this.carbonCompartments.put(compartmentInfo, standing);
				break;
			case TotalProducts:
				CATCompartment overallNet = new CATCompartment(this, compartmentInfo);
				overallNet.addFatherCompartment(carbonCompartments.get(CompartmentInfo.Products));
				overallNet.addFatherCompartment(carbonCompartments.get(CompartmentInfo.LfillDeg));
				this.carbonCompartments.put(compartmentInfo, overallNet);
				break;
			case NetSubs:
				CATCompartment substitutionNet = new CATCompartment(this, compartmentInfo);
				substitutionNet.addFatherCompartment(carbonCompartments.get(CompartmentInfo.EnerSubs));
				substitutionNet.addFatherCompartment(carbonCompartments.get(CompartmentInfo.CarbEmis));
				substitutionNet.addFatherCompartment(carbonCompartments.get(CompartmentInfo.LfillEm));
				substitutionNet.addFatherCompartment(carbonCompartments.get(CompartmentInfo.LfillND));
				this.carbonCompartments.put(compartmentInfo, substitutionNet);
				break;
			}
		}

	}
	

	public Map<CompartmentInfo, CATCompartment> getCompartments() {return this.carbonCompartments;}

	/**
	 * This method returns the last stand from the list of stands.
	 * @return a CarbonToolCompatibleStand or null if the list is null or empty
	 */
	public CATCompatibleStand getLastStand() {
		if (getStandList() != null && !getStandList().isEmpty()) {
			return getStandList().get(getStandList().size() - 1);
		} else {
			return null;
		}
	}
	
	protected List<CATCompatibleStand> getStandList() {
		if (currentStands != null) {
			return currentStands;
		} else {
			return stands;
		}
	}

	
	
	/**
	 * This method returns the rotation length in year.
	 * @return an integer
	 */
	public int getRotationLength() {return rotationLength;}


	/**
	 * This method format the end products into an array of collection of end products. The array has the time scale as index.
	 * @param timeScale a TimeScale instance
	 * @param endProductsCollections a Collection of Collections of EndProduct instances
	 * @return an array of Collections of EndProduct instances
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection[] formatCarbonUnits(CATTimeTable timeScale, Collection<? extends CarbonUnit> carbonProducts) {
		Collection<CarbonUnit>[] outputArray = new Collection[timeScale.size()];
		for (int i = 0; i < outputArray.length; i++) {
			outputArray[i] = new ArrayList<CarbonUnit>();
		}
		
		if (carbonProducts!= null && !carbonProducts.isEmpty()) {
			for (CarbonUnit carbonUnit : carbonProducts) {
				outputArray[carbonUnit.getIndexInTimeScale()].add(carbonUnit);
			}
		}
	
		return outputArray;
	}

	/**
	 * This method returns a summary of simulation.
	 * @return a CarbonAccountingToolExportSummary instance if the simulation has been carried out or null otherwise
	 */
	public CATSingleSimulationResult getSimulationSummary() {
		if (isSimulationValid) {
			if (summary == null) {
				summary = new CATSingleSimulationResult("Sim " + ++nbSimulations, this);
			}
			return summary; 
		} else {
			return null;
		}
	}

	protected void storeResults() {
		getSimulationSummary().updateResult(this);
	}
		
	protected boolean isEvenAged() {return isEvenAged;}

	@SuppressWarnings("unchecked")
	protected void setRealization(int realizationID) {
		if (isStochastic && nRealizations > 1) {
			currentStands = new ArrayList<CATCompatibleStand>();
			for (CATCompatibleStand stand : stands) {
				currentRealization = realizationID;
				currentStands.add(((StochasticInformationProvider<? extends CATCompatibleStand>) stand).getRealization(realizationID));
			}
		} else {
			currentRealization = 1;
			currentStands = stands;
		}
	}

	@Override
	public int getMonteCarloRealizationId() {
		return currentRealization;
	}
	
	/*
	 * Useless for sensitivity analysis (non-Javadoc)
	 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getSubjectId()
	 */
	@Override
	public String getSubjectId() {return null;}

	/*
	 * Useless for sensitivity analysis (non-Javadoc)
	 * @see repicea.simulation.MonteCarloSimulationCompliantObject#getHierarchicalLevel()
	 */
	@Override
	public HierarchicalLevel getHierarchicalLevel() {return null;}


	
}


