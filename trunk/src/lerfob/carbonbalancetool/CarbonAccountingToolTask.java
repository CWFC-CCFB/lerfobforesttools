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

import lerfob.carbonbalancetool.biomassparameters.BiomassParameters;
import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.ProductionLineManager;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import lerfob.nutrientmodel.NutrientConcentrationPredictionModel.Nutrient;
import lerfob.nutrientmodel.NutrientConcentrationProviderObject;
import repicea.app.AbstractGenericTask;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.MemoryWatchDog;
import repicea.util.ObjectUtility;

@SuppressWarnings({ "serial", "deprecation" })
public class CarbonAccountingToolTask extends AbstractGenericTask {
	
	/**
	 * This enum defines the different tasks performed by the InternalSwingWorker class.
	 * @author Mathieu Fortin - December 2010
	 */
	public static enum Task {LOG_AND_BUCK_TREES(true), 
		GENERATE_WOODPRODUCTS(true), 
		ACTUALIZE_CARBON(true), 
		COMPILE_CARBON(true),
		SET_REALIZATION(false),
		SHUT_DOWN(false),
		SET_STANDLIST(false),
		UNLOCK_ENGINE(false),
		SHOW_INTERFACE(false),
		RESET_MANAGER(false), 
		DISPLAY_RESULT(false);
	
		private boolean longTask;
		private static int NumberOfLongTasks = -1;	
		
		Task(boolean longTask) {
			this.longTask = longTask;
		}
	
		protected static int getNumberOfLongTasks() {
			if (NumberOfLongTasks == -1) {
				NumberOfLongTasks = 0;
				for (Task task : Task.values()) {
					if (task.longTask) {
						NumberOfLongTasks++;
					}
				}
			}
			return NumberOfLongTasks;
		}
		
	
	}

	protected static class SetProperRealizationTask extends CarbonAccountingToolTask {

		private final int realizationID;
		
		protected SetProperRealizationTask(LERFoBCarbonAccountingTool caller, int realizationID) {
			super(Task.SET_REALIZATION, caller);
			this.realizationID = realizationID;
		}
		
	}
	
	private Task currentTask;
	
	private LERFoBCarbonAccountingTool caller;
	
	public CarbonAccountingToolTask(Task currentTask, LERFoBCarbonAccountingTool caller) {
		this.currentTask = currentTask;
		this.setName(currentTask.name());
		this.caller = caller;
		if (caller.guiInterface != null) {	// if the interface is enabled then the interface listens to this worker (for the progress bar implementation)
			super.addPropertyChangeListener(caller.guiInterface);
		}
	}

	
	@Override
	protected void doThisJob() throws Exception {
		switch (currentTask) {
		case RESET_MANAGER:
			caller.getCarbonCompartmentManager().resetManager();
			break;
		case SET_REALIZATION:
			caller.getCarbonCompartmentManager().setRealization(((SetProperRealizationTask) this).realizationID);
			break;
		case DISPLAY_RESULT:
			caller.showResult();
			break;
		case SHOW_INTERFACE:
			caller.showInterface();
			break;
		case SET_STANDLIST:
			caller.setStandList();
			break;
		case LOG_AND_BUCK_TREES:
			firePropertyChange("OngoingTask", null, currentTask);
			logAndBuckTrees();
			break;
		case GENERATE_WOODPRODUCTS:
			firePropertyChange("OngoingTask", null, currentTask);
			createEndUseWoodProductsFromWoodPieces();
			break;
		case ACTUALIZE_CARBON:
			firePropertyChange("OngoingTask", null, currentTask);
			actualizeCarbon();
			break;
		case COMPILE_CARBON:
			firePropertyChange("OngoingTask", null, currentTask);
			calculateCarbonInCompartments();
			break;
		case SHUT_DOWN:
			firePropertyChange("Cleaning memory", null, currentTask);
			caller.requestShutdown();
			break;
		case UNLOCK_ENGINE:
			firePropertyChange("Unlocking Engine", null, currentTask);
			caller.unlockEngine();
			break;
		}
	}

	

	/**
	 * Task no 1 : log the trees and buck them into wood pieces
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void logAndBuckTrees() throws Exception {
		CarbonCompartmentManager manager = caller.getCarbonCompartmentManager();
		manager.setSimulationValid(false);
		
		CarbonToolCompatibleStand lastStand = manager.getLastStand(); 

		// performing a final cut if has not been done and only if the stand implements the CarbonToolCompatibleEvenAgedStand interface
		if (lastStand instanceof CarbonToolCompatibleEvenAgedStand && !lastStand.getTrees(StatusClass.alive).isEmpty()) {
			List<CarbonToolCompatibleStand> stands = manager.getStandList();
			CarbonToolCompatibleStand stand = ((CarbonToolCompatibleEvenAgedStand) lastStand).getHarvestedStand();
			stands.add(stand);
			manager.init(stands);				
			caller.setFinalCutHadToBeCarriedOut(true);
		} 					

		// retrieve the loggable trees
		Collection<CarbonToolCompatibleTree> retrievedTreesFromStep;
		caller.clearTreeCollections();
		for (CarbonToolCompatibleStand stand : manager.getStandList()) {
			for (StatusClass statusClass : StatusClass.values()) {
				if (statusClass != StatusClass.alive) {
					retrievedTreesFromStep = stand.getTrees(statusClass);
					if (!retrievedTreesFromStep.isEmpty()) {
						for (CarbonToolCompatibleTree t : retrievedTreesFromStep) {
							caller.registerTree(statusClass, stand, t);
						}
					} 
				}
			}
		}

		TreeLogger logger = caller.getCarbonToolSettings().getTreeLogger();
		if (!caller.getTrees(StatusClass.cut).isEmpty()) {
			if (caller.guiInterface != null) {
				logger.addTreeLoggerListener(caller.getGuiInterface()); 
			}
			logger.init(convertMapIntoCollectionOfLoggableTrees());
			logger.run();
			if (caller.guiInterface != null) {
				logger.removeTreeLoggerListener(caller.getGuiInterface()); 
			}			
			setProgress((int) (100 * (double) 1 / Task.getNumberOfLongTasks()));
		} else {
			logger.getWoodPieces().clear();
		}
	}
	

	private ProductionProcessorManager getProcessorManager() {return caller.getCarbonToolSettings().getCurrentProductionProcessorManager();}
	
	/**
	 * Task no 2 : process the logs into end use wood products
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes"})
	private void createEndUseWoodProductsFromWoodPieces() throws Exception {
		BiomassParameters biomassParameters = caller.getCarbonCompartmentManager().getCarbonToolSettings().getCurrentBiomassParameters();
		if (!caller.getCarbonToolSettings().formerImplementation) {
			getProcessorManager().resetCarbonUnitMap();
			if (!caller.getCarbonToolSettings().getTreeLogger().getWoodPieces().isEmpty()) {
				int numberOfTreesProcessed = 0;
				double progressFactor = (double) 100 / caller.getCarbonToolSettings().getTreeLogger().getWoodPieces().size() / Task.values().length;
				TreeLogger treeLogger = caller.getCarbonToolSettings().getTreeLogger();
				outerLoop:
					for (LoggableTree t : (Collection<LoggableTree>) treeLogger.getWoodPieces().keySet()) {

						MemoryWatchDog.checkAvailableMemory();		// memory check before going further on

						CarbonToolCompatibleTree tree = (CarbonToolCompatibleTree) t;
						double carbonContentRatio = biomassParameters.getCarbonContentFromThisTree(tree);
						double basicWoodDensity = biomassParameters.getBasicWoodDensityFromThisTree(tree);

						Collection<WoodPiece> woodPieces = (Collection<WoodPiece>) treeLogger.getWoodPieces().get(t);
						double totalWoodPieceCarbon = 0d;
						for (WoodPiece woodPiece : woodPieces) {
							if (isCancelled) {
								break outerLoop;
							}
							
							double[] nutrientConcentrations = null;

							if (woodPiece instanceof NutrientConcentrationProviderObject) {
								nutrientConcentrations = ((NutrientConcentrationProviderObject) woodPiece).getAllNutrientConcentrationsFromThisObject();
							}

							double[] nutrientAmounts = ObjectUtility.multiplyArrayByScalar(nutrientConcentrations, woodPiece.getWeightedVolumeM3() * basicWoodDensity);	// the amounts are expressed here in kg

							AmountMap<Element> amountMap = new AmountMap<Element>();
							double volume = woodPiece.getWeightedVolumeM3();
							double biomass = volume * basicWoodDensity;
							double carbon = biomass * carbonContentRatio;
							amountMap.put(Element.Volume, volume);
							amountMap.put(Element.Biomass, biomass);
							amountMap.put(Element.C, carbon);

							if (nutrientAmounts != null) {
								amountMap.put(Element.N, nutrientAmounts[Nutrient.N.ordinal()]);
								amountMap.put(Element.S, nutrientAmounts[Nutrient.S.ordinal()]);
								amountMap.put(Element.P, nutrientAmounts[Nutrient.P.ordinal()]);
								amountMap.put(Element.K, nutrientAmounts[Nutrient.K.ordinal()]);
							}

							Collection<CarbonUnit> carbonUnits = getProcessorManager().processWoodPiece(woodPiece.getLogCategory(), caller.getDateIndexForThisTree(tree), amountMap);		
							totalWoodPieceCarbon += getCarbonFromCarbonUnitList(carbonUnits);
						}
						
						double totalAboveGroundCarbon = biomassParameters.getAboveGroundCarbonMg(tree);
						double unconsideredAboveGroundCarbon = totalAboveGroundCarbon - totalWoodPieceCarbon; 				// the difference between the carbon in the wood piece and the total aboveground carbon is the part that is left in the forest
						if (unconsideredAboveGroundCarbon > 0) {		// those branches are sent to the fine woody debris
							double biomass = unconsideredAboveGroundCarbon / carbonContentRatio;
							double volume = biomass / basicWoodDensity;
							AmountMap<Element> amountMap = new AmountMap<Element>();						// No calculation for nutrients left in the forest here
							amountMap.put(Element.Volume, volume);
							amountMap.put(Element.Biomass, biomass);
							amountMap.put(Element.C, unconsideredAboveGroundCarbon);
							
							getProcessorManager().processFineWoodyDebris(caller.getDateIndexForThisTree(tree), amountMap);
						}
						numberOfTreesProcessed++;
						setProgress((int) (numberOfTreesProcessed * progressFactor + (double) (currentTask.ordinal()) * 100 / Task.getNumberOfLongTasks()));
					}
			}
			createCoarseWoodyDebris(caller.getTrees(StatusClass.cut), false);
			createCoarseWoodyDebris(caller.getTrees(StatusClass.dead), true);
			createCoarseWoodyDebris(caller.getTrees(StatusClass.windfall), true);
			
			createFineWoodyDebris(caller.getTrees(StatusClass.dead));
			createFineWoodyDebris(caller.getTrees(StatusClass.windfall));
		} else {
			ProductionLineManager productionLineManager = caller.getCarbonToolSettings().getProductionLines();
			productionLineManager.resetCarbonUnitMap();							// reinitialize the land fill carbon unit and left in forest carbon unit collections
			Map<String, Double> dispatchMap;
			if (!caller.getCarbonToolSettings().getTreeLogger().getWoodPieces().isEmpty()) {
				int numberOfTreesProcessed = 0;
				double progressFactor = (double) 100 / caller.getCarbonToolSettings().getTreeLogger().getWoodPieces().size() / Task.values().length;
				TreeLogger treeLogger = caller.getCarbonToolSettings().getTreeLogger();
				outerLoop:
					for (LoggableTree t : (Collection<LoggableTree>) treeLogger.getWoodPieces().keySet()) {

						MemoryWatchDog.checkAvailableMemory();		// memory check before going further on

						CarbonToolCompatibleTree tree = (CarbonToolCompatibleTree) t;
						double carbonContentRatio = biomassParameters.getCarbonContentFromThisTree(tree);
						double basicWoodDensity = biomassParameters.getBasicWoodDensityFromThisTree(tree);

						Collection<WoodPiece> woodPieces = (Collection<WoodPiece>) treeLogger.getWoodPieces().get(t);
						double totalWoodPieceCarbon = 0d;
						for (WoodPiece woodPiece : woodPieces) {
							if (isCancelled) {
								break outerLoop;
							}
							dispatchMap = caller.getCarbonToolSettings().getWoodSupplySetup().dispatchThisWoodPiece(woodPiece.getLogCategory().getName());
							double proportion;

							double[] nutrientConcentrations = null;

							if (woodPiece instanceof NutrientConcentrationProviderObject) {
								nutrientConcentrations = ((NutrientConcentrationProviderObject) woodPiece).getAllNutrientConcentrationsFromThisObject();
							}

							for (String productionLineName : dispatchMap.keySet()) {
								proportion = dispatchMap.get(productionLineName);
								double carbonOfTheFutureWoodProduct = proportion * woodPiece.getWeightedVolumeM3() * basicWoodDensity * carbonContentRatio;
								if (carbonOfTheFutureWoodProduct > ProductionProcessorManager.VERY_SMALL) {
									totalWoodPieceCarbon += carbonOfTheFutureWoodProduct;
								}

								double[] nutrientAmounts = ObjectUtility.multiplyArrayByScalar(nutrientConcentrations, proportion * woodPiece.getWeightedVolumeM3() * basicWoodDensity);	// the amounts are expressed here in kg

								AmountMap<Element> amountMap = new AmountMap<Element>();
								double volume = proportion * woodPiece.getWeightedVolumeM3();
								double biomass = volume * basicWoodDensity;
								double carbon = biomass * carbonContentRatio;
								amountMap.put(Element.Volume, volume);
								amountMap.put(Element.Biomass, biomass);
								amountMap.put(Element.C, carbon);

								if (nutrientAmounts != null) {
									amountMap.put(Element.N, nutrientAmounts[Nutrient.N.ordinal()]);
									amountMap.put(Element.S, nutrientAmounts[Nutrient.S.ordinal()]);
									amountMap.put(Element.P, nutrientAmounts[Nutrient.P.ordinal()]);
									amountMap.put(Element.K, nutrientAmounts[Nutrient.K.ordinal()]);
								}

								productionLineManager.processWoodPiece(productionLineName, caller.getDateForThisTree(tree), amountMap);		
							}
						}
						double totalAboveGroundCarbon = biomassParameters.getAboveGroundCarbonMg(tree);
						double unconsideredAboveGroundCarbon = totalAboveGroundCarbon - totalWoodPieceCarbon; 				// the difference between the carbon in the wood piece and the total aboveground carbon is the part that is left in the forest
						if (unconsideredAboveGroundCarbon > 0) {
							double biomass = unconsideredAboveGroundCarbon / carbonContentRatio;
							double volume = biomass / basicWoodDensity;
							AmountMap<Element> amountMap = new AmountMap<Element>();						// No calculation for nutrients left in the forest here
							amountMap.put(Element.Volume, volume);
							amountMap.put(Element.Biomass, biomass);
							amountMap.put(Element.C, unconsideredAboveGroundCarbon);
							productionLineManager.leftThisPieceInTheForest(caller.getDateForThisTree(tree), amountMap);		
						}
						numberOfTreesProcessed++;
						setProgress((int) (numberOfTreesProcessed * progressFactor + (double) (currentTask.ordinal()) * 100 / Task.getNumberOfLongTasks()));
					}

			}


			// The belowground biomass of the logged trees must be considered as left in the forest
			if (!caller.getTrees(StatusClass.cut).isEmpty()) {
				for (CarbonToolCompatibleStand stand : caller.getTrees(StatusClass.cut).keySet()) {
					if (isCancelled) {
						break;
					}
					int creationDate = stand.getDateYr();
					Collection<CarbonToolCompatibleTree> trees = caller.getTrees(StatusClass.cut).get(stand);
					double volume = biomassParameters.getBelowGroundVolumeM3(trees);
					double biomass = biomassParameters.getBelowGroundBiomassMg(trees);
					double carbonContent = biomassParameters.getBelowGroundCarbonMg(trees);

					AmountMap<Element> amountMap = new AmountMap<Element>(); 				// No calculation for nutrients left in the forest here
					amountMap.put(Element.Volume, volume);
					amountMap.put(Element.Biomass, biomass);	
					amountMap.put(Element.C, carbonContent);	

					productionLineManager.leftThisPieceInTheForest(creationDate, amountMap);
				}
			}
		}
	}

	private void createCoarseWoodyDebris(Map<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>> treeMap, boolean includeCommercialPart) {
		BiomassParameters biomassParameters = caller.getCarbonCompartmentManager().getCarbonToolSettings().getCurrentBiomassParameters();
		for (CarbonToolCompatibleStand stand : treeMap.keySet()) {
			if (isCancelled) {
				break;
			}
			int dateIndex = caller.getCarbonCompartmentManager().getStandList().indexOf(stand);
			Collection<CarbonToolCompatibleTree> trees = treeMap.get(stand);
			double volume = biomassParameters.getBelowGroundVolumeM3(trees);
			double biomass = biomassParameters.getBelowGroundBiomassMg(trees);
			double carbonContent = biomassParameters.getBelowGroundCarbonMg(trees);
			
			if (includeCommercialPart) {
				volume += biomassParameters.getCommercialVolumeM3(trees);
				biomass += biomassParameters.getCommercialBiomassMg(trees);
				carbonContent += biomassParameters.getCommercialCarbonMg(trees);
			}
			
			AmountMap<Element> amountMap = new AmountMap<Element>(); 				// No calculation for nutrients left in the forest here
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, biomass);	
			amountMap.put(Element.C, carbonContent);	

			getProcessorManager().processCoarseWoodyDebris(dateIndex, amountMap);
		}
	}
	
	
	private void createFineWoodyDebris(Map<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>> treeMap) {
		BiomassParameters biomassParameters = caller.getCarbonCompartmentManager().getCarbonToolSettings().getCurrentBiomassParameters();
		for (CarbonToolCompatibleStand stand : treeMap.keySet()) {
			if (isCancelled) {
				break;
			}
			int dateIndex = caller.getCarbonCompartmentManager().getStandList().indexOf(stand);
			Collection<CarbonToolCompatibleTree> trees = treeMap.get(stand);
			double volume = biomassParameters.getAboveGroundVolumeM3(trees) - biomassParameters.getCommercialVolumeM3(trees);
			double biomass = biomassParameters.getAboveGroundBiomassMg(trees) - biomassParameters.getCommercialBiomassMg(trees);
			double carbonContent = biomassParameters.getAboveGroundCarbonMg(trees) - biomassParameters.getCommercialCarbonMg(trees);

			AmountMap<Element> amountMap = new AmountMap<Element>(); 				// No calculation for nutrients left in the forest here
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, biomass);	
			amountMap.put(Element.C, carbonContent);	

			getProcessorManager().processCoarseWoodyDebris(dateIndex, amountMap);
		}
	}

	
	/**
	 * Task no 3 : actualize the carbon units through time
	 * @throws Exception
	 */
	private void actualizeCarbon() throws Exception {
		setProgress((int) ((double) (currentTask.ordinal()) * 100d / Task.values().length));
		if (!caller.getCarbonToolSettings().formerImplementation) {
			getProcessorManager().actualizeCarbonUnits(caller.getCarbonCompartmentManager());
		} else {
			ProductionLineManager marketManager = caller.getCarbonToolSettings().getProductionLines();
			marketManager.actualizeCarbonUnits(caller.getCarbonCompartmentManager());
		}
		setProgress((int) ((double) (currentTask.ordinal() + 1) * 100d / Task.getNumberOfLongTasks()));
	}
	
	
	/**
	 * Task 4 : calculate the carbon in the different compartments.
	 * @throws Exception
	 */
	private void calculateCarbonInCompartments() throws Exception {
		CarbonCompartmentManager manager = caller.getCarbonCompartmentManager();
		manager.resetCompartmentsAndSetCarbonUnitCollections();
		
		double progressFactor = (double) 100d / manager.getCompartments().size() / Task.values().length;
		int compIter = 0;
		for (CarbonCompartment carbonCompartment : manager.getCompartments().values()) {
			if (isCancelled) {
				break;
			}
			carbonCompartment.calculateAndIntegrateCarbon();
			compIter++;
			setProgress((int) (compIter * progressFactor + (double) (currentTask.ordinal() * 100 / Task.getNumberOfLongTasks())));
		}
		manager.setSimulationValid(true);
		manager.storeResults();
	}

	private Collection<CarbonToolCompatibleTree> convertMapIntoCollectionOfLoggableTrees() {
		Collection<CarbonToolCompatibleTree> loggableTreesCollection = new ArrayList<CarbonToolCompatibleTree>();
		for (Collection<CarbonToolCompatibleTree> oColl : caller.getTrees(StatusClass.cut).values()) {
			loggableTreesCollection.addAll(oColl);
		}
		return loggableTreesCollection;
	}
	

	private double getCarbonFromCarbonUnitList(Collection<CarbonUnit> carbonUnits) {
		double sum = 0;
		for (CarbonUnit unit : carbonUnits) {
			sum += unit.getInitialCarbon();
		}
		return sum;
	}
	

	
}
	

