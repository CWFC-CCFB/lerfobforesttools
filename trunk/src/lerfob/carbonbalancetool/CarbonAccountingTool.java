/*
 * This file is part of the lerfob-foresttools library.
 *
 * Copyright (C) 2010-2014 Mathieu Fortin for LERFOB AgroParisTech/INRA, 
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

import java.awt.Container;
import java.awt.Window;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.SwingUtilities;

import lerfob.carbonbalancetool.CATTask.SetProperRealizationTask;
import lerfob.carbonbalancetool.CATTask.Task;
import lerfob.carbonbalancetool.CATUtility.BiomassParametersName;
import lerfob.carbonbalancetool.CATUtility.ProductionManagerName;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings;
import lerfob.treelogger.douglasfirfcba.DouglasFCBATreeLogger;
import lerfob.treelogger.mathilde.MathildeTreeLogger;
import repicea.app.AbstractGenericEngine;
import repicea.app.GenericTask;
import repicea.app.SettingMemory;
import repicea.gui.REpiceaShowableUI;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.gui.genericwindows.GeneralLicenseWindow;
import repicea.gui.genericwindows.GenericSplashWindow;
import repicea.simulation.covariateproviders.treelevel.SamplingUnitIDProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.simulation.treelogger.TreeLoggerManager;
import repicea.treelogger.basictreelogger.BasicTreeLogger;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLogger;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLogger;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

/**
 * LERFoBCarbonAccountingTool is the class that implements a tool for the calculation of the carbon balance in a series
 * of CarbonToolCompatibleStand instances (LERFoB-CAT). 
 * 
 * ARCHITECTURE : The LERFoBCarbonAccountingTool has a GUI interface and it is in charge of calculating the carbon for all the compartments. It contains a CarbonCompartmentManager instance,
 * which contains in turn a collection of CarbonCompartment objects that can provide their carbon content on their own.
 * 
 * @author Mathieu Fortin (INRA) - January 2010
 */
public class CarbonAccountingTool extends AbstractGenericEngine implements REpiceaShowableUIWithParent, REpiceaShowableUI {
	
	private static class StandComparator implements Comparator<CATCompatibleStand> {

		@Override
		public int compare(CATCompatibleStand arg0, CATCompatibleStand arg1) {
			if (arg0.getDateYr() < arg1.getDateYr()) {
				return -1;
			} else if (arg0.getDateYr() == arg1.getDateYr()) {
				if (arg0.isInterventionResult()) {
					return 1;
				} else if (arg1.isInterventionResult()) {
					return -1;
				} else {
					return 0;
				}
			} else {
				return 1;
			}
		}
	}
	
	protected static final String englishTitle = "LERFoB Carbon Accounting Tool (LERFoB-CAT)";
	protected static final String frenchTitle = "Outil de comptabilit\u00E9 du carbone LERFoB (LERFoB-CAT)";
	private static final StandComparator StandComparator = new StandComparator();
	protected static boolean hasAlreadyBeenInstanciated = false;
	
	private CATCompartmentManager carbonCompartmentManager;	
	protected boolean finalCutHadToBeCarriedOut;
	private final Map<StatusClass, Map<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>>> treeCollections;
	private final Map<CATCompatibleTree, CATCompatibleStand> treeRegister;
	
	protected Window parentFrame;
	protected List<CATCompatibleStand> waitingStandList;
	protected transient CATFrame guiInterface;
	protected transient Window owner;
	

	/**
	 * General constructor.
	 */
	public CarbonAccountingTool() {
		setSettingMemory(new SettingMemory(REpiceaSystem.getJavaIOTmpDir() + "settingsCarbonTool.ser"));
		
		finalCutHadToBeCarriedOut = false;
		treeCollections = new HashMap<StatusClass, Map<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>>>();
		treeRegister = new HashMap<CATCompatibleTree, CATCompatibleStand>();
		
		Runnable toBeRun = new Runnable () {
			@Override
			public void run () {
				try {
					startApplication();
				} catch (Exception e) {
					throw new RuntimeException("The CarbonCalcultor engine has failed!");
				} 
			}
		};
		
		new Thread(toBeRun, "CarbonAccountingTool").start();
	}
	
	
	/**
	 * This method returns the settings of the carbon accounting tool.
	 * @return a CarbonAccountingToolSettings instance
	 */
	public CATSettings getCarbonToolSettings() {
		return carbonCompartmentManager.getCarbonToolSettings();
	}
	
	
	/**
 	 * This method initialises the carbon accounting tool either in script or in GUI mode.
	 * @param isGuiEnabled a boolean (true to enable the GUI or false otherwise)
	 * @param parentFrame the parent frame which can be null
	 * @return true if the tool was properly initialised or false otherwise
	 */
	public boolean initializeTool(boolean isGuiEnabled, Window parentFrame) {
		
		this.parentFrame = parentFrame;
		CATSettings carbonToolSettings = new CATSettings(getSettingMemory());
		carbonCompartmentManager = new CATCompartmentManager(carbonToolSettings);
		
		if (isGuiEnabled) {
			if (!hasAlreadyBeenInstanciated) {
				String packagePath = ObjectUtility.getRelativePackagePath(CarbonAccountingTool.class);
				String iconPath =  packagePath + "SplashImage.jpg";
				new GenericSplashWindow(iconPath, 4, parentFrame);

				String licensePath = packagePath + "LGPLLicense_en.html";
				if (REpiceaTranslator.getCurrentLanguage() == REpiceaTranslator.Language.French) {
					licensePath = packagePath + "LGPLLicense_fr.html";
				}
				
				GeneralLicenseWindow licenseDlg;
				try {
					licenseDlg = new GeneralLicenseWindow(parentFrame, licensePath);
					licenseDlg.setVisible(true);
					if (!licenseDlg.isLicenseAccepted()) {
						return false;
					} else {
						hasAlreadyBeenInstanciated = true;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}
			addTask(new CATTask(Task.SHOW_INTERFACE, this));
		}
		
		return true;
	}

	/**
	 * This method sets the list of stands from which the carbon balance should be calculated.
	 * @param standList a List of CarbonToolCompatibleStand instance
	 */
	public void setStandList(List<CATCompatibleStand> standList) {
		Collections.sort(standList, StandComparator);
		waitingStandList = standList;
		addTask(new CATTask(Task.SET_STANDLIST, this));
	}
	
	protected void registerTree(StatusClass statusClass, CATCompatibleStand stand, CATCompatibleTree tree) {
		if (!treeCollections.containsKey(statusClass)) {
			treeCollections.put(statusClass, new HashMap<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>>());
		}
		Map<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>> innerMap = treeCollections.get(statusClass);
		if (!innerMap.containsKey(stand)) {
//			innerMap.put(stand, new ArrayList<CATCompatibleTree>());
			innerMap.put(stand, new HashMap<String, Collection<CATCompatibleTree>>());
		}
		
		Map<String, Collection<CATCompatibleTree>> innerInnerMap = innerMap.get(stand);
		
		String samplingUnitID;
		if (tree instanceof SamplingUnitIDProvider) {
			samplingUnitID = ((SamplingUnitIDProvider) tree).getSamplingUnitID(); 
		} else {
			samplingUnitID = "";
		}
		if (!innerInnerMap.containsKey(samplingUnitID)) {
			innerInnerMap.put(samplingUnitID, new ArrayList<CATCompatibleTree>());
		}
		Collection<CATCompatibleTree> trees = innerInnerMap.get(samplingUnitID);
		trees.add(tree);
		treeRegister.put(tree, stand);
	}
	
	protected Map<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>> getTrees(StatusClass statusClass) {
		if (treeCollections.containsKey(statusClass)) {
			return treeCollections.get(statusClass);
		} else {
			return new HashMap<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>>();
		}
	}
	
	protected int getDateIndexForThisTree(CATCompatibleTree tree) {
		if (treeRegister.containsKey(tree)) {
			CATCompatibleStand stand = treeRegister.get(tree);
			return getCarbonCompartmentManager().getStandList().indexOf(stand);
		} else {
			return -1;
		}
	}
	
//	@Deprecated
//	protected int getDateForThisTree(CarbonToolCompatibleTree tree) {
//		if (treeRegister.containsKey(tree)) {
//			return treeRegister.get(tree).getDateYr();
//		} else {
//			return -1;
//		}
//	}

	protected void clearTreeCollections() {
		treeCollections.clear();
		treeRegister.clear();
	}

	@SuppressWarnings("rawtypes")
	private Object getAReferentTree() {
		for (CATCompatibleStand stand : carbonCompartmentManager.getStandList()) {
			for (StatusClass status : StatusClass.values()) {
				Collection coll = stand.getTrees(status);
				if (coll != null && !coll.isEmpty()) {
					return coll.iterator().next();
				}
			}
		}
		return null;
	}
	
	protected void setStandList() {
		finalCutHadToBeCarriedOut = false;
		clearTreeCollections();
		carbonCompartmentManager.init(waitingStandList);
		setReferentForBiomassParameters(carbonCompartmentManager.getStandList());
		getCarbonToolSettings().setTreeLoggerDescriptions(findMatchingTreeLoggers(getAReferentTree()));
		if (isGuiEnabled()) {
			Runnable doRun = new Runnable() {
				@Override
				public void run() {
					getUI().setCalculateCarbonButtonsEnabled(true);
					getUI().redefineProgressBar();
				}
			};
			SwingUtilities.invokeLater(doRun);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setReferentForBiomassParameters(List<CATCompatibleStand> stands) {
		Object referent = null;
		if (stands != null && !stands.isEmpty()) {
			for (CATCompatibleStand stand : stands) {
				referent = stand;
				Collection<?> coll = null;
				for (StatusClass statusClass : StatusClass.values()) {
					if (coll == null) {
						coll = stand.getTrees(statusClass);
					} else {
						coll.addAll(stand.getTrees(statusClass));
					}
				}
				if (coll != null && !coll.isEmpty()) {
					Object obj = coll.iterator().next();
					if (obj instanceof CATCompatibleTree) {
						referent = obj;
						break;
					}
				}
			}
		}
		getCarbonToolSettings().setReferentForBiomassParameters(referent);
	}

	protected Vector<TreeLoggerDescription> findMatchingTreeLoggers(Object referent) {
		Vector<TreeLoggerDescription> defaultTreeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		if (referent != null) {
			List<TreeLoggerDescription> availableCompatibleTreeLoggerDescription = TreeLoggerManager.getInstance().getCompatibleTreeLoggers(referent);
			defaultTreeLoggerDescriptions.addAll(availableCompatibleTreeLoggerDescription);
		} else {
			defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(BasicTreeLogger.class));
		}
		return defaultTreeLoggerDescriptions;
	}
	
	@Override
	protected void firstTasksToDo() {}

	@Override
	protected void decideWhatToDoInCaseOfFailure(GenericTask task) {
		super.decideWhatToDoInCaseOfFailure(task);
		unlockEngine();
		if (isGuiEnabled()) {
			getUI().setSimulationRunning(false);
		}
	}

	
	public CATCompartmentManager getCarbonCompartmentManager() {return carbonCompartmentManager;}

	/**
	 * This method launches the calculation of the different carbon compartments.
	 * @throws InterruptedException if the engine lock is interrupted
	 * @throws InvalidParameterException if the CarbonToolSettings instance is invalid
	 */
	public void calculateCarbon() throws InvalidParameterException, InterruptedException {
		if (!carbonCompartmentManager.getCarbonToolSettings().isValid()) {
			throw new InvalidParameterException("The settings of the carbon accounting tool are invalid. Please check!");
		} else {
			int nbReals = CATSensitivityAnalysisSettings.getInstance().getNumberOfMonteCarloRealizations();
			if (nbReals < 1) {
				nbReals = 1;
			}
			carbonCompartmentManager.summary = null; // reset the summary before going on
			for (int i = 0; i < nbReals; i++) {
				addTask(new CATTask(Task.RESET_MANAGER, this));
				addTask(new SetProperRealizationTask(this, i));
				addTask(new CATTask(Task.LOG_AND_BUCK_TREES, this));
				addTask(new CATTask(Task.GENERATE_WOODPRODUCTS, this));
				addTask(new CATTask(Task.ACTUALIZE_CARBON, this));
				addTask(new CATTask(Task.COMPILE_CARBON, this));
			} 
			addTask(new CATTask(Task.UNLOCK_ENGINE, this));
			if (isGuiEnabled()) {
				addTask(new CATTask(Task.DISPLAY_RESULT, this));
			} else {
				lockEngine();
			}
		}
	}

	
	protected boolean isGuiEnabled() {
		return guiInterface != null  && guiInterface.isVisible();
	}

	@Override
	protected void unlockEngine() {super.unlockEngine();}
	

	
	protected void setFinalCutHadToBeCarriedOut(boolean finalCutHadToBeCarriedOut) {
		this.finalCutHadToBeCarriedOut = finalCutHadToBeCarriedOut;
	}

	/**
	 * This method returns an export tool adapted to the carbon accounting tool. In script mode, the export
	 * tool can then be used to export the data in dbf file format.
	 * @return a CarbonAccountingToolExport instance
	 * @throws Exception 
	 */
	public CATExportTool createExportTool() throws Exception {
		return new CATExportTool(getCarbonCompartmentManager().getCarbonToolSettings(), getCarbonCompartmentManager().getSimulationSummary());
	}
	
	/**
	 * By default, closing the gui shuts the engine down. This method must be 
	 * overriden with empty content to disable the automatic shut down.
	 */
	protected void respondToWindowClosing() {
		addTask(new CATTask(Task.SHUT_DOWN, this));
	}
	
	@Override
	public CATFrame getUI(Container parent) {
		if (owner == null && parent != null && parent instanceof Window) {
			owner = (Window) parent;
		}
		if (guiInterface == null) {
			guiInterface = new CATFrame(this, null);
		}
		return guiInterface;
	}	

	@Override
	public void showUI(Window parent) {
		getUI(parent).setVisible(true);
	}

	@Override
	public CATFrame getUI() {return getUI(null);}

	@Override
	public void showUI() {
		if (owner != null) {
			showUI(owner);
		} else {
			getUI().setVisible(true);
		}
		
	}

	protected void showResult() {
		if (isGuiEnabled()) {
			Runnable job = new Runnable() {
				@Override
				public void run() {
					getUI().displayResult();
				}
			};
			SwingUtilities.invokeLater(job);
		}
	}

	@Override
	public boolean isVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}

	protected void setCurrentBiomassParameters(BiomassParametersName name) {
		getCarbonToolSettings().currentBiomassParameters = name;
	}
	
	protected void setCurrentProductionManager(ProductionManagerName name) {
		getCarbonToolSettings().currentProcessorManager = name;
	}
	
	/*
	 * Entry point for FCBA in GESFOR project
	 */
	public static void main(String[] args) {
//		REpiceaTranslator.setCurrentLanguage(Language.French);
		REpiceaTranslator.setCurrentLanguage(Language.English);
		CarbonAccountingTool tool = new CarbonAccountingTool();
		if (!tool.initializeTool(true, null)) {
			System.exit(0);
		}
		Vector<TreeLoggerDescription> treeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		treeLoggerDescriptions.add(new TreeLoggerDescription(BasicTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(MathildeTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(MaritimePineBasicTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(EuropeanBeechBasicTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(DouglasFCBATreeLogger.class));
		tool.getCarbonToolSettings().setTreeLoggerDescriptions(treeLoggerDescriptions);
	}


	
}
