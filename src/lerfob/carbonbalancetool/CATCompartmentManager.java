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
import java.util.HashMap;
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
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings;
import repicea.simulation.HierarchicalLevel;
import repicea.simulation.MonteCarloSimulationCompliantObject;
import repicea.simulation.covariateproviders.plotlevel.StochasticInformationProvider;
import repicea.simulation.covariateproviders.treelevel.SamplingUnitIDProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;

@SuppressWarnings("deprecation")
public class CATCompartmentManager implements MonteCarloSimulationCompliantObject {

	private static int NumberOfExtraYrs = 80;	// number of years after the final cut

	private final Map<StatusClass, Map<CATCompatibleStand, Map<String, Map<String, Collection<CATCompatibleTree>>>>> treeCollections;
	private final Map<CATCompatibleTree, CATCompatibleStand> treeRegister;
	private final List<String> speciesList;

	private List<CATCompatibleStand> currentStands;	
	private List<CATCompatibleStand> stands;
	private CATSettings carbonAccountingToolSettings;		// reference to the extractor settings
	
	private Map<CompartmentInfo, CATCompartment> carbonCompartments;
	private int rotationLength;
	private boolean isEvenAged;
	private CATTimeTable timeTable;

	private boolean isSimulationValid;
//	protected boolean isStochastic;
	private int currentRealization;
//	protected int nRealizations;
	private int nbSimulations = 0;

	
	protected CATSingleSimulationResult summary;
	
	
	/**
	 * Constructor for this class
	 * @param settings a CATSettings instance
	 */
	public CATCompartmentManager(CATSettings settings) {
		treeCollections = new HashMap<StatusClass, Map<CATCompatibleStand, Map<String, Map<String, Collection<CATCompatibleTree>>>>>();
		treeRegister = new HashMap<CATCompatibleTree, CATCompatibleStand>();
		speciesList = new ArrayList<String>();
		
		this.carbonAccountingToolSettings = settings;
		this.carbonCompartments = new TreeMap<CompartmentInfo, CATCompartment>();	// TreeMap to make sure the merge compartments are not called before the regular compartment
		isSimulationValid = false;
		initializeCompartments();
	}
		
	protected void registerTree(StatusClass statusClass, CATCompatibleStand stand, CATCompatibleTree tree) {
		if (!treeCollections.containsKey(statusClass)) {
			treeCollections.put(statusClass, new HashMap<CATCompatibleStand, Map<String, Map<String, Collection<CATCompatibleTree>>>>());
		}
		Map<CATCompatibleStand, Map<String, Map<String, Collection<CATCompatibleTree>>>> innerMap = treeCollections.get(statusClass);
		if (!innerMap.containsKey(stand)) {
			innerMap.put(stand, new HashMap<String, Map<String, Collection<CATCompatibleTree>>>());
		}
		
		Map<String, Map<String, Collection<CATCompatibleTree>>> innerInnerMap = innerMap.get(stand);
		
		String samplingUnitID;
		if (tree instanceof SamplingUnitIDProvider) {
			samplingUnitID = ((SamplingUnitIDProvider) tree).getSamplingUnitID(); 
		} else {
			samplingUnitID = "";
		}
		if (!innerInnerMap.containsKey(samplingUnitID)) {
			innerInnerMap.put(samplingUnitID, new HashMap<String, Collection<CATCompatibleTree>>());
		}
		
		Map<String, Collection<CATCompatibleTree>> mostInsideMap = innerInnerMap.get(samplingUnitID);
		if (!mostInsideMap.containsKey(tree.getSpeciesName())) {
			mostInsideMap.put(tree.getSpeciesName(), new ArrayList<CATCompatibleTree>());
		}
		
		Collection<CATCompatibleTree> trees = mostInsideMap.get(tree.getSpeciesName());
		trees.add(tree);
		treeRegister.put(tree, stand);
	}

	
	protected int getDateIndexForThisTree(CATCompatibleTree tree) {
		if (treeRegister.containsKey(tree)) {
			CATCompatibleStand stand = treeRegister.get(tree);
			return getStandList().indexOf(stand);
		} else {
			return -1;
		}
	}

	protected Map<CATCompatibleStand, Map<String, Map<String, Collection<CATCompatibleTree>>>> getTrees(StatusClass statusClass) {
		if (treeCollections.containsKey(statusClass)) {
			return treeCollections.get(statusClass);
		} else {
			return new HashMap<CATCompatibleStand, Map<String, Map<String, Collection<CATCompatibleTree>>>>();
		}
	}

	private void clearTreeCollections() {
		treeCollections.clear();
		treeRegister.clear();
		speciesList.clear();
	}

	protected List<String> getSpeciesList() {
		return speciesList;
	}

	protected void registerTreeSpecies(CATCompatibleTree tree) {
		if (!speciesList.contains(tree.getSpeciesName())) {
			speciesList.add(tree.getSpeciesName());
		}
	}

	protected void setSimulationValid(boolean isSimulationValid) {
		this.isSimulationValid = isSimulationValid;
	}
	
	public void init(List<CATCompatibleStand> stands) {
		this.stands = stands;
		this.currentStands = null;
		if (stands != null) {
			CATCompatibleStand lastStand = stands.get(stands.size() - 1);
			isEvenAged = lastStand.canBeRunInInfiniteSequence();
			boolean isStochastic = false;
			int nRealizations = 1;
			if (lastStand instanceof StochasticInformationProvider) {
				StochasticInformationProvider<?> stochProv = (StochasticInformationProvider<?>) lastStand;
				List<Integer> monteCarloIds = stochProv.getRealizationIds();
				if (stochProv.isStochastic() && stochProv.getRealization(monteCarloIds.get(0)) instanceof CATCompatibleStand) {
					isStochastic = true;
					nRealizations = monteCarloIds.size();
				}
			}
			CATSensitivityAnalysisSettings.getInstance().setModelStochastic(isStochastic);
			CATSensitivityAnalysisSettings.getInstance().setNumberOfMonteCarloRealizations(nRealizations);
			int nbExtraYears = 0;
			int initialAgeYr = -999;
			if (isEvenAged) {
				rotationLength = lastStand.getAgeYr();
				initialAgeYr = stands.get(0).getAgeYr();
				nbExtraYears = NumberOfExtraYrs;
			} else {
				rotationLength = lastStand.getDateYr() - stands.get(0).getDateYr();
			}
				
			int averageTimeStep = retrieveAverageTimeStep(stands);
			if (averageTimeStep == 0) {
				averageTimeStep = 5;	// default value in case there is a single step
			}
			

			// TODO change to an annual CATTimeTable instance
//			timeTable = new CATTimeTable(stands, initialAgeYr, nbExtraYears);
			timeTable = new CATTimeTable(stands, initialAgeYr, nbExtraYears, averageTimeStep);
//			timeTable = new CATTimeTable(lastStand.getDateYr(), initialAgeYr);
//			
//			int size = stands.size() + nbExtraYears / averageTimeStep;
//			for (int i = 0; i < size; i++) {
//				if (i < stands.size()) {
//					timeTable.add(stands.get(i).getDateYr());
//				} else  {
//					timeTable.add(timeTable.get(i - 1) + averageTimeStep);
//				}
//			}

		}
	}
	
	protected void resetManager() {
		clearTreeCollections();
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
	 * @return a CATTimeTable instance
	 */
	public CATTimeTable getTimeTable() {return timeTable;}
	
	public CATSettings getCarbonToolSettings() {return carbonAccountingToolSettings;}

	@SuppressWarnings({ "unchecked"})
	public void resetCompartmentsAndSetCarbonUnitCollections() {
		if (getCarbonToolSettings().isVerboseEnabled()) {
			System.out.println("Resetting compartment...");
		}
		CarbonUnitList joinEndUseProductRecyclageList = new CarbonUnitList();
		CarbonUnitList leftInForestList;
		CarbonUnitList degradableLandfillList;
		CarbonUnitList nonDegradableLandfillList;
		if 	(!getCarbonToolSettings().formerImplementation) {
			ProductionProcessorManager productionLineManager = getCarbonToolSettings().getCurrentProductionProcessorManager();
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct));
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.Recycled));
			leftInForestList = productionLineManager.getCarbonUnits(CarbonUnitStatus.DeadWood);
			degradableLandfillList = productionLineManager.getCarbonUnits(CarbonUnitStatus.LandFillDegradable);
			nonDegradableLandfillList = productionLineManager.getCarbonUnits(CarbonUnitStatus.LandFillNonDegradable);
		} else {
			ProductionLineManager productionLineManager = getCarbonToolSettings().getProductionLines();
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.EndUseWoodProduct));
			joinEndUseProductRecyclageList.addAll(productionLineManager.getCarbonUnits(CarbonUnitStatus.Recycled));
			leftInForestList = productionLineManager.getCarbonUnits(CarbonUnitStatus.DeadWood);
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
		carbonCompartments.get(CompartmentInfo.WComb).setCarbonUnitsArray(endUseWoodProductCarbonUnits);

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
			case WComb:
				carbonCompartments.put(compartmentInfo, new CATProductCompartment(this, compartmentInfo));
				break;
			case LivingBiomass:
				CATCompartment standing = new CATCompartment(this, compartmentInfo);
				standing.addFatherCompartment(carbonCompartments.get(CompartmentInfo.AbGround));
				standing.addFatherCompartment(carbonCompartments.get(CompartmentInfo.Roots));
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
	protected CATSingleSimulationResult getSimulationSummary() {
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
		if (CATSensitivityAnalysisSettings.getInstance().isModelStochastic()) {
			currentStands = new ArrayList<CATCompatibleStand>();
			for (CATCompatibleStand stand : stands) {
				currentRealization = realizationID;
				List<Integer> monteCarloIds = ((StochasticInformationProvider<? extends CATCompatibleStand>) stand).getRealizationIds();
				currentStands.add(((StochasticInformationProvider<? extends CATCompatibleStand>) stand).getRealization(monteCarloIds.get(realizationID)));
			}
		} else {
			currentRealization = realizationID;
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


