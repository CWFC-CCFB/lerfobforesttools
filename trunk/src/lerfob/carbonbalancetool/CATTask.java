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

import lerfob.carbonbalancetool.CATCompatibleStand.Management;
import lerfob.carbonbalancetool.biomassparameters.BiomassParameters;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.ProductionLineManager;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import lerfob.carbonbalancetool.productionlines.WoodyDebrisProcessor.WoodyDebrisProcessorID;
import repicea.app.AbstractGenericTask;
import repicea.lang.MemoryWatchDog;
import repicea.simulation.covariateproviders.treelevel.SamplingUnitIDProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.treelogger.LoggableTree;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings({ "serial", "deprecation" })
public class CATTask extends AbstractGenericTask {
	
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
		DISPLAY_RESULT(false),
		SET_BIOMASS_PARMS(false),
		SET_PRODUCTION_MANAGER(false);
	
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

	protected static class SetProperRealizationTask extends CATTask {

		private final int realizationID;
		
		protected SetProperRealizationTask(CarbonAccountingTool caller, int realizationID) {
			super(Task.SET_REALIZATION, caller);
			this.realizationID = realizationID;
		}
		
	}
	
	private Task currentTask;
	
	private CarbonAccountingTool caller;
	
	public CATTask(Task currentTask, CarbonAccountingTool caller) {
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
			caller.showUI();
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
		case SET_BIOMASS_PARMS:
			firePropertyChange("Setting biomass parameters", null, currentTask);
			caller.setBiomassParameters();
			break;
		case SET_PRODUCTION_MANAGER:
			firePropertyChange("Setting production manager", null, currentTask);
			caller.setProductionManager();
			break;
		}
	}

	

	/**
	 * Task no 1 : log the trees and buck them into wood pieces
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void logAndBuckTrees() throws Exception {
		CATCompartmentManager manager = caller.getCarbonCompartmentManager();
		if (manager.getCarbonToolSettings().isVerboseEnabled()) {
			System.out.println("Bucking harvested trees into wood pieces...");
		}

		manager.setSimulationValid(false);
		
		CATCompatibleStand lastStand = manager.getLastStand(); 

		// performing a final cut if has not been done and only if the stand implements the CarbonToolCompatibleEvenAgedStand interface
//		if (lastStand instanceof CATCompatibleEvenAgedStand && !lastStand.getTrees(StatusClass.alive).isEmpty()) {
		if (lastStand.getManagement() == Management.EvenAged && !lastStand.getTrees(StatusClass.alive).isEmpty()) {
			List<CATCompatibleStand> stands = manager.getStandList();
//			CATCompatibleStand stand = ((CATCompatibleEvenAgedStand) lastStand).getHarvestedStand();
			CATCompatibleStand stand = lastStand.getHarvestedStand();
			stands.add(stand);
			manager.init(stands);				
			caller.setFinalCutHadToBeCarriedOut(true);
		} 					

		// retrieve the loggable trees
		Collection<CATCompatibleTree> retrievedTreesFromStep;
		caller.clearTreeCollections();
		for (CATCompatibleStand stand : manager.getStandList()) {
			for (StatusClass statusClass : StatusClass.values()) {
				if (statusClass != StatusClass.alive) {
					retrievedTreesFromStep = stand.getTrees(statusClass);
					if (!retrievedTreesFromStep.isEmpty()) {
						for (CATCompatibleTree t : retrievedTreesFromStep) {
							caller.registerTree(statusClass, stand, t);
						}
					} 
				}
			}
		}

		TreeLogger logger = caller.getCarbonToolSettings().getTreeLogger();
		if (!caller.getTrees(StatusClass.cut).isEmpty()) {
			if (caller.guiInterface != null) {
				logger.addTreeLoggerListener(caller.getUI()); 
			}
			logger.init(convertMapIntoCollectionOfLoggableTrees());
			logger.run();
			if (caller.guiInterface != null) {
				logger.removeTreeLoggerListener(caller.getUI()); 
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
		CATCompartmentManager manager = caller.getCarbonCompartmentManager();
		if (manager.getCarbonToolSettings().isVerboseEnabled()) {
			System.out.println("Creating HWP from wood pieces...");
		}
		BiomassParameters biomassParameters = manager.getCarbonToolSettings().getCurrentBiomassParameters();
		if (!caller.getCarbonToolSettings().formerImplementation) {
			getProcessorManager().resetCarbonUnitMap();
			if (!caller.getCarbonToolSettings().getTreeLogger().getWoodPieces().isEmpty()) {
				int numberOfTreesProcessed = 0;
				double progressFactor = (double) 100 / caller.getCarbonToolSettings().getTreeLogger().getWoodPieces().size() / Task.values().length;
				TreeLogger treeLogger = caller.getCarbonToolSettings().getTreeLogger();
				outerLoop:
					for (LoggableTree t : (Collection<LoggableTree>) treeLogger.getWoodPieces().keySet()) {

						String samplingUnitID;
						if (t instanceof SamplingUnitIDProvider) {
							samplingUnitID = ((SamplingUnitIDProvider) t).getSamplingUnitID();
						} else {
							samplingUnitID = "";
						}
						MemoryWatchDog.checkAvailableMemory();		// memory check before going further on

						CATCompatibleTree tree = (CATCompatibleTree) t;
						double carbonContentRatio = biomassParameters.getCarbonContentFromThisTree(tree, manager);
						double basicWoodDensityMgM3 = biomassParameters.getBasicWoodDensityFromThisTree(tree, manager);

						Collection<WoodPiece> woodPieces = (Collection<WoodPiece>) treeLogger.getWoodPieces().get(t);
						double totalAboveGroundWoodPieceVolume = 0d;
						double totalBelowGroundWoodPieceVolume = 0d;
						for (WoodPiece woodPiece : woodPieces) {
							if (isCancelled) {
								break outerLoop;
							}

							if (woodPiece.getLogCategory().isFromStump()) {
								totalBelowGroundWoodPieceVolume += woodPiece.getWeightedVolumeM3();
							} else {
								totalAboveGroundWoodPieceVolume += woodPiece.getWeightedVolumeM3();
							}
							
							AmountMap<Element> nutrientConcentrations = null;

							if (woodPiece instanceof CATAdditionalElementsProvider) {
								nutrientConcentrations = ((CATAdditionalElementsProvider) woodPiece).getAdditionalElementConcentrations();
							}

							AmountMap<Element> amountMap = new AmountMap<Element>();
							double volumeM3 = woodPiece.getWeightedVolumeM3();
							double biomassMg = volumeM3 * basicWoodDensityMgM3;
							double carbonMg = biomassMg * carbonContentRatio;
							amountMap.put(Element.Volume, volumeM3);
							amountMap.put(Element.Biomass, biomassMg);
							amountMap.put(Element.C, carbonMg);

							if (nutrientConcentrations != null) {
								AmountMap nutrientAmounts = nutrientConcentrations.multiplyByAScalar(woodPiece.getWeightedVolumeM3() * basicWoodDensityMgM3);	// the amounts are expressed here in kg
								cleanAmountMapOfAdditionalElementsBeforeMerging(nutrientAmounts);	// To make sure volume biomass and carbon will not be double counted
								amountMap.putAll(nutrientAmounts);
//								amountMap.put(Element.N, nutrientAmounts[Nutrient.N.ordinal()]);
//								amountMap.put(Element.S, nutrientAmounts[Nutrient.S.ordinal()]);
//								amountMap.put(Element.P, nutrientAmounts[Nutrient.P.ordinal()]);
//								amountMap.put(Element.K, nutrientAmounts[Nutrient.K.ordinal()]);
							}

							getProcessorManager().processWoodPiece(woodPiece.getLogCategory(), caller.getDateIndexForThisTree(tree), samplingUnitID, amountMap);		
						}
						
						double totalAboveGroundVolumeM3 = biomassParameters.getAboveGroundVolumeM3(tree, manager);
						double unconsideredAboveGroundVolumeM3 = totalAboveGroundVolumeM3 - totalAboveGroundWoodPieceVolume;
						processUnaccountedVolume(unconsideredAboveGroundVolumeM3, 
								basicWoodDensityMgM3, 
								carbonContentRatio, 
								caller.getDateIndexForThisTree(tree), 
								samplingUnitID,
								WoodyDebrisProcessorID.FineWoodyDebris);
						double totalBelowGroundVolume = biomassParameters.getBelowGroundVolumeM3(tree, manager);
						double unconsideredBelowGroundVolume = totalBelowGroundVolume - totalBelowGroundWoodPieceVolume;
						processUnaccountedVolume(unconsideredBelowGroundVolume, 
								basicWoodDensityMgM3, 
								carbonContentRatio, 
								caller.getDateIndexForThisTree(tree), 
								samplingUnitID,
								WoodyDebrisProcessorID.CoarseWoodyDebris);
						numberOfTreesProcessed++;
						setProgress((int) (numberOfTreesProcessed * progressFactor + (double) (currentTask.ordinal()) * 100 / Task.getNumberOfLongTasks()));
					}
			}
			createWoodyDebris(caller.getTrees(StatusClass.dead), WoodyDebrisProcessorID.CoarseWoodyDebris);
			createWoodyDebris(caller.getTrees(StatusClass.dead), WoodyDebrisProcessorID.CommercialWoodyDebris);
			createWoodyDebris(caller.getTrees(StatusClass.dead), WoodyDebrisProcessorID.FineWoodyDebris);
			createWoodyDebris(caller.getTrees(StatusClass.windfall), WoodyDebrisProcessorID.CoarseWoodyDebris);
			createWoodyDebris(caller.getTrees(StatusClass.windfall), WoodyDebrisProcessorID.CommercialWoodyDebris);
			createWoodyDebris(caller.getTrees(StatusClass.windfall), WoodyDebrisProcessorID.FineWoodyDebris);
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

						CATCompatibleTree tree = (CATCompatibleTree) t;
						double carbonContentRatio = biomassParameters.getCarbonContentFromThisTree(tree, manager);
						double basicWoodDensity = biomassParameters.getBasicWoodDensityFromThisTree(tree, manager);

						Collection<WoodPiece> woodPieces = (Collection<WoodPiece>) treeLogger.getWoodPieces().get(t);
						double totalWoodPieceCarbon = 0d;
						for (WoodPiece woodPiece : woodPieces) {
							if (isCancelled) {
								break outerLoop;
							}
							dispatchMap = caller.getCarbonToolSettings().getWoodSupplySetup().dispatchThisWoodPiece(woodPiece.getLogCategory().getName());
							double proportion;

							AmountMap<Element> nutrientConcentrations = null;

							if (woodPiece instanceof CATAdditionalElementsProvider) {
								nutrientConcentrations = ((CATAdditionalElementsProvider) woodPiece).getAdditionalElementConcentrations();
							}

							for (String productionLineName : dispatchMap.keySet()) {
								proportion = dispatchMap.get(productionLineName);
								double carbonOfTheFutureWoodProduct = proportion * woodPiece.getWeightedVolumeM3() * basicWoodDensity * carbonContentRatio;
								if (carbonOfTheFutureWoodProduct > ProductionProcessorManager.VERY_SMALL) {
									totalWoodPieceCarbon += carbonOfTheFutureWoodProduct;
								}


								AmountMap<Element> amountMap = new AmountMap<Element>();
								double volume = proportion * woodPiece.getWeightedVolumeM3();
								double biomass = volume * basicWoodDensity;
								double carbon = biomass * carbonContentRatio;
								amountMap.put(Element.Volume, volume);
								amountMap.put(Element.Biomass, biomass);
								amountMap.put(Element.C, carbon);

								if (nutrientConcentrations != null) {
									AmountMap<Element> nutrientAmounts = nutrientConcentrations.multiplyByAScalar(proportion * woodPiece.getWeightedVolumeM3() * basicWoodDensity);	// the amounts are expressed here in kg
									cleanAmountMapOfAdditionalElementsBeforeMerging(nutrientAmounts);	// To make sure volume biomass and carbon will not be double counted
									amountMap.putAll(nutrientAmounts);
//									amountMap.put(Element.N, nutrientAmounts[Nutrient.N.ordinal()]);
//									amountMap.put(Element.S, nutrientAmounts[Nutrient.S.ordinal()]);
//									amountMap.put(Element.P, nutrientAmounts[Nutrient.P.ordinal()]);
//									amountMap.put(Element.K, nutrientAmounts[Nutrient.K.ordinal()]);
								}

								productionLineManager.processWoodPiece(productionLineName, caller.getDateIndexForThisTree(tree), amountMap);		
							}
						}
						double totalAboveGroundCarbon = biomassParameters.getAboveGroundCarbonMg(tree, manager);
						double unconsideredAboveGroundCarbon = totalAboveGroundCarbon - totalWoodPieceCarbon; 				// the difference between the carbon in the wood piece and the total aboveground carbon is the part that is left in the forest
						if (unconsideredAboveGroundCarbon > 0) {
							double biomass = unconsideredAboveGroundCarbon / carbonContentRatio;
							double volume = biomass / basicWoodDensity;
							AmountMap<Element> amountMap = new AmountMap<Element>();						// No calculation for nutrients left in the forest here
							amountMap.put(Element.Volume, volume);
							amountMap.put(Element.Biomass, biomass);
							amountMap.put(Element.C, unconsideredAboveGroundCarbon);
							productionLineManager.leftThisPieceInTheForest(caller.getDateIndexForThisTree(tree), amountMap);		
						}
						numberOfTreesProcessed++;
						setProgress((int) (numberOfTreesProcessed * progressFactor + (double) (currentTask.ordinal()) * 100 / Task.getNumberOfLongTasks()));
					}

			}


			// The belowground biomass of the logged trees must be considered as left in the forest
			if (!caller.getTrees(StatusClass.cut).isEmpty()) {
				for (CATCompatibleStand stand : caller.getTrees(StatusClass.cut).keySet()) {
					if (isCancelled) {
						break;
					}
					int dateIndex = caller.getCarbonCompartmentManager().getStandList().indexOf(stand);
					Collection<CATCompatibleTree> trees = new ArrayList<CATCompatibleTree>();
					for (Collection<CATCompatibleTree> coll : caller.getTrees(StatusClass.cut).get(stand).values()) {
						trees.addAll(coll);
					}
					double volume = biomassParameters.getBelowGroundVolumeM3(trees, manager);
					double biomass = biomassParameters.getBelowGroundBiomassMg(trees, manager);
					double carbonContent = biomassParameters.getBelowGroundCarbonMg(trees, manager);

					AmountMap<Element> amountMap = new AmountMap<Element>(); 				// No calculation for nutrients left in the forest here
					amountMap.put(Element.Volume, volume);
					amountMap.put(Element.Biomass, biomass);	
					amountMap.put(Element.C, carbonContent);	

					productionLineManager.leftThisPieceInTheForest(dateIndex, amountMap);
				}
			}
		}
	}

	
	private void cleanAmountMapOfAdditionalElementsBeforeMerging(AmountMap<Element> additionalElement) {
		additionalElement.remove(Element.Volume);
		additionalElement.remove(Element.Biomass);
		additionalElement.remove(Element.C);
	}
	
	private void processUnaccountedVolume(double volume, double basicWoodDensity, double carbonContentRatio, int dateIndex, String samplingUnitID, WoodyDebrisProcessorID type) {
		if (volume > 0) {
			double biomass = volume * basicWoodDensity;
			double carbon = biomass * carbonContentRatio;
			AmountMap<Element> amountMap = new AmountMap<Element>();						// No calculation for nutrients left in the forest here
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, biomass);
			amountMap.put(Element.C, carbon);
			
			getProcessorManager().processWoodyDebris(dateIndex, samplingUnitID, amountMap, type);
		}
	}
	
	
	
	private void createWoodyDebris(Map<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>> treeMap, WoodyDebrisProcessorID type) {
		CATCompartmentManager manager = caller.getCarbonCompartmentManager();
		BiomassParameters biomassParameters = manager.getCarbonToolSettings().getCurrentBiomassParameters();
		for (CATCompatibleStand stand : treeMap.keySet()) {
			if (isCancelled) {
				break;
			}
			int dateIndex = caller.getCarbonCompartmentManager().getStandList().indexOf(stand);
			Map<String, Collection<CATCompatibleTree>> oMap = treeMap.get(stand);
			for (String samplingUnitID : oMap.keySet()) {
				Collection<CATCompatibleTree> trees = oMap.get(samplingUnitID);
				double volume = 0d;
				double biomass = 0d;
				double carbonContent = 0d;
				switch(type) {
				case FineWoodyDebris:
					volume = biomassParameters.getAboveGroundVolumeM3(trees, manager) - biomassParameters.getCommercialVolumeM3(trees);
					biomass = biomassParameters.getAboveGroundBiomassMg(trees, manager) - biomassParameters.getCommercialBiomassMg(trees, manager);
					carbonContent = biomassParameters.getAboveGroundCarbonMg(trees, manager) - biomassParameters.getCommercialCarbonMg(trees, manager);
					break;
				case CommercialWoodyDebris:
					volume = biomassParameters.getCommercialVolumeM3(trees);
					biomass = biomassParameters.getCommercialBiomassMg(trees, manager);
					carbonContent = biomassParameters.getCommercialCarbonMg(trees, manager);
					break;
				case CoarseWoodyDebris:
					volume = biomassParameters.getBelowGroundVolumeM3(trees, manager);
					biomass = biomassParameters.getBelowGroundBiomassMg(trees, manager);
					carbonContent = biomassParameters.getBelowGroundCarbonMg(trees, manager);
					break;
				}
				AmountMap<Element> amountMap = new AmountMap<Element>(); 				// No calculation for nutrients left in the forest here
				amountMap.put(Element.Volume, volume);
				amountMap.put(Element.Biomass, biomass);	
				amountMap.put(Element.C, carbonContent);	

				getProcessorManager().processWoodyDebris(dateIndex, samplingUnitID, amountMap, type);
				
			}
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
		CATCompartmentManager manager = caller.getCarbonCompartmentManager();
		if (manager.getCarbonToolSettings().isVerboseEnabled()) {
			System.out.println("Calculating carbon in the different compartments...");
		}
		manager.resetCompartmentsAndSetCarbonUnitCollections();
		
		double progressFactor = (double) 100d / manager.getCompartments().size() / Task.values().length;
		int compIter = 0;
		for (CATCompartment carbonCompartment : manager.getCompartments().values()) {
			if (isCancelled) {
				break;
			}
			carbonCompartment.calculateAndIntegrateCarbon();
			compIter++;
			setProgress((int) (compIter * progressFactor + (double) (currentTask.ordinal() * 100 / Task.getNumberOfLongTasks())));
			if (manager.getCarbonToolSettings().isVerboseEnabled()) {
				System.out.println("Integrated carbon in compartment " + carbonCompartment.getCompartmentID().name() + " = " + carbonCompartment.getIntegratedCarbon());
			}
		}
		manager.setSimulationValid(true);
		manager.storeResults();
	}

	private Collection<CATCompatibleTree> convertMapIntoCollectionOfLoggableTrees() {
		Collection<CATCompatibleTree> loggableTreesCollection = new ArrayList<CATCompatibleTree>();
		for (Map<String, Collection<CATCompatibleTree>> oMap : caller.getTrees(StatusClass.cut).values()) {
			for (Collection<CATCompatibleTree> oColl : oMap.values()) {
				loggableTreesCollection.addAll(oColl);
			}
		}
		return loggableTreesCollection;
	}
	

//	private double getCarbonFromCarbonUnitList(Collection<CarbonUnit> carbonUnits) {
//		double sum = 0;
//		for (CarbonUnit unit : carbonUnits) {
//			sum += unit.getInitialCarbon();
//		}
//		return sum;
//	}
	

	
}
	

