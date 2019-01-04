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

import lerfob.app.LERFOBJARSVNAppVersion;
import lerfob.carbonbalancetool.CATTask.SetProperRealizationTask;
import lerfob.carbonbalancetool.CATTask.Task;
import lerfob.carbonbalancetool.CATUtility.BiomassParametersName;
import lerfob.carbonbalancetool.CATUtility.ProductionManagerName;
import lerfob.carbonbalancetool.catdiameterbasedtreelogger.CATDiameterBasedTreeLogger;
import lerfob.carbonbalancetool.io.CATExportTool;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings.VariabilitySource;
import lerfob.treelogger.basictreelogger.BasicTreeLogger;
import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogger;
import lerfob.treelogger.douglasfirfcba.DouglasFCBATreeLogger;
import lerfob.treelogger.europeanbeech.EuropeanBeechBasicTreeLogger;
import lerfob.treelogger.maritimepine.MaritimePineBasicTreeLogger;
import lerfob.treelogger.mathilde.MathildeTreeLogger;
import repicea.app.AbstractGenericEngine;
import repicea.app.GenericTask;
import repicea.app.REpiceaJARSVNAppVersion;
import repicea.app.SettingMemory;
import repicea.gui.REpiceaShowableUI;
import repicea.gui.REpiceaShowableUIWithParent;
import repicea.gui.genericwindows.REpiceaLicenseWindow;
import repicea.gui.genericwindows.REpiceaSplashWindow;
import repicea.lang.REpiceaSystem;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.covariateproviders.treelevel.SamplingUnitIDProvider;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.simulation.treelogger.TreeLoggerManager;
import repicea.stats.Distribution;
//import repicea.treelogger.wbirchprodvol.WBirchProdVolTreeLogger;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;

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

	static {
		XmlSerializerChangeMonitor.registerClassNameChange("repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider$SpeciesType", "repicea.simulation.covariateproviders.treelevel.SpeciesTypeProvider$SpeciesType");
		
		TreeLoggerManager.registerTreeLoggerName(BasicTreeLogger.class.getName());
		TreeLoggerManager.registerTreeLoggerName(DiameterBasedTreeLogger.class.getName());
		TreeLoggerManager.registerTreeLoggerName(CATDiameterBasedTreeLogger.class.getName());
		TreeLoggerManager.registerTreeLoggerName(EuropeanBeechBasicTreeLogger.class.getName());
		TreeLoggerManager.registerTreeLoggerName(MaritimePineBasicTreeLogger.class.getName());
//		TreeLoggerManager.registerTreeLoggerName(WBirchProdVolTreeLogger.class.getName());
		
		TreeLoggerManager.registerTreeLoggerName(MathildeTreeLogger.class.getName());
		TreeLoggerManager.registerTreeLoggerName(DouglasFCBATreeLogger.class.getName());
	}

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
	
	protected static final String englishTitle = "Carbon Accounting Tool (CAT)";
	protected static final String frenchTitle = "Outil de comptabilit\u00E9 carbone (CAT)";
	private static final StandComparator StandComparator = new StandComparator();
	protected static boolean hasAlreadyBeenInstanciated = false;
	
	public static enum CATMode {
		/**
		 * Running CAT as a stand alone application with user interface enabled.
		 */
		STANDALONE, 
		/**
		 * Running CAT from another application with user interface enabled.
		 */
		FROM_OTHER_APP, 
		/**
		 * Running CAT in script mode with used interface disabled and clearing of random deviates of the 
		 * sensitivity analysis.
		 */
		SCRIPT;
	}
	
	
	private CATCompartmentManager carbonCompartmentManager;	
	protected boolean finalCutHadToBeCarriedOut;
	private final Map<StatusClass, Map<CATCompatibleStand, Map<String, Collection<CATCompatibleTree>>>> treeCollections;
	private final Map<CATCompatibleTree, CATCompatibleStand> treeRegister;
	
	protected Window parentFrame;
	protected List<CATCompatibleStand> waitingStandList;
	protected transient CATFrame guiInterface;
	protected transient Window owner;
	
	private String biomassParametersFilename;
	private String productionManagerFilename;

	private final CATMode mode;
	private boolean initialized;
	
	/**
	 * Constructor for stand alone application.
	 */
	public CarbonAccountingTool() {
		this(CATMode.STANDALONE);
	}
	

	/**
	 * Generic constructor.
	 * @param mode defines how CAT is to be used. See the CATMode enum variable.
	 */
	protected CarbonAccountingTool(CATMode mode) {
		this.mode = mode;
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

	@Override
	protected void shutdown(int shutdownCode) {
		System.out.println("Shutting down CAT...");
		CATSensitivityAnalysisSettings.getInstance().clear();
		if (mode == CATMode.STANDALONE) {		// only the stand alone mode will shutdown the JVM
			System.exit(shutdownCode);
		}
	}
	
	/**
	 * This method returns the settings of the carbon accounting tool.
	 * @return a CarbonAccountingToolSettings instance
	 */
	protected CATSettings getCarbonToolSettings() {
		return carbonCompartmentManager.getCarbonToolSettings();
	}
	
	
	/**
	 * This method initializes the carbon accounting tool either in script or in GUI mode. This method can
	 * be called only once per instance. 
	 * @param parentFrame the parent frame which can be null
	 */
	public void initializeTool(Window parentFrame) throws Exception {
		if (initialized) {
			throw new Exception("The Carbon Accounting Tool is already initialized!");
		} else {
			this.parentFrame = parentFrame;
			CATSettings carbonToolSettings = new CATSettings(getSettingMemory());
			carbonCompartmentManager = new CATCompartmentManager(carbonToolSettings);
			if (isGuiEnabled()) {
				if (!hasAlreadyBeenInstanciated) {
					String packagePath = ObjectUtility.getRelativePackagePath(CarbonAccountingTool.class);
					String iconPath =  packagePath + "SplashImage.jpg";
					String bottomSplashWindowString = "Build " +  LERFOBJARSVNAppVersion.getInstance().getBuild() + "-" + REpiceaJARSVNAppVersion.getInstance().getBuild();
					new REpiceaSplashWindow(iconPath, 4, parentFrame, bottomSplashWindowString);

					String licensePath = packagePath + "CATLicense_en.txt";
					if (REpiceaTranslator.getCurrentLanguage() == REpiceaTranslator.Language.French) {
						licensePath = packagePath + "CATLicense_fr.txt";
					}

					REpiceaLicenseWindow licenseDlg;
					try {
						licenseDlg = new REpiceaLicenseWindow(parentFrame, licensePath);
						licenseDlg.setVisible(true);
						if (!licenseDlg.isLicenseAccepted()) {
							addTask(new CATTask(Task.SHUT_DOWN, this));
						} else {
							hasAlreadyBeenInstanciated = true;
						}
					} catch (IOException e) {
						e.printStackTrace();
						addTask(new CATTask(Task.SHUT_DOWN, this));
					}
				}
				addTask(new CATTask(Task.SHOW_INTERFACE, this));
			}
			initialized = true;
		}
	}

	/**
	 * This method sets the list of stands from which the carbon balance should be calculated.
	 * @param standList a List of CarbonToolCompatibleStand instance
	 */
	public void setStandList(List<CATCompatibleStand> standList) {
		Collections.sort(standList, StandComparator);
		waitingStandList = standList;
		addTask(new CATTask(Task.SET_STANDLIST, this));
		addTask(new CATTask(Task.UNLOCK_ENGINE, this));
		if (!isGuiEnabled()) {
			try {
				lockEngine();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
	private TreeLoggerCompatibilityCheck getTreeLoggerCompatibilityCheck() {
		Object treeInstance = null;
		for (CATCompatibleStand stand : carbonCompartmentManager.getStandList()) {
			for (StatusClass status : StatusClass.values()) {
				Collection coll = stand.getTrees(status);
				if (coll != null && !coll.isEmpty()) {
					treeInstance =  coll.iterator().next();
				}
			}
		}
		TreeLoggerCompatibilityCheck c = new TreeLoggerCompatibilityCheck(treeInstance);
		return c;
	}
	
	protected void setStandList() {
		finalCutHadToBeCarriedOut = false;
		clearTreeCollections();
		carbonCompartmentManager.init(waitingStandList);
		setReferentForBiomassParameters(carbonCompartmentManager.getStandList());
		getCarbonToolSettings().setTreeLoggerDescriptions(findMatchingTreeLoggers(getTreeLoggerCompatibilityCheck()));
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

	protected Vector<TreeLoggerDescription> findMatchingTreeLoggers(TreeLoggerCompatibilityCheck check) {
		Vector<TreeLoggerDescription> defaultTreeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		if (check != null) {
			List<TreeLoggerDescription> availableCompatibleTreeLoggerDescription = TreeLoggerManager.getInstance().getCompatibleTreeLoggers(check);
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

	/**
	 * This method retrieves the simulation results after calling the calculateCarbon method.
	 * @return a CATSingleSimulationResult instance
	 */
	public CATSingleSimulationResult retrieveSimulationSummary() {
		return getCarbonCompartmentManager().getSimulationSummary();
	}
	
	protected CATCompartmentManager getCarbonCompartmentManager() {return carbonCompartmentManager;}

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
		return mode == CATMode.STANDALONE || mode == CATMode.FROM_OTHER_APP;
	}

	@Override
	protected void unlockEngine() {super.unlockEngine();}
	
	@Override
	protected void lockEngine() throws InterruptedException {super.lockEngine();}

	
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
		return new CATExportTool(getCarbonCompartmentManager().getCarbonToolSettings().getSettingMemory(), 
				getCarbonCompartmentManager().getSimulationSummary());
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

	/**
	 * This method sets the biomass parameters for the next simulation in script mode.
	 * @param filename a String that defines the filename (.bpf) of the biomass parameters
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void setBiomassParameters(String filename) throws InterruptedException {
		this.biomassParametersFilename = filename;
		addTask(new CATTask(Task.SET_BIOMASS_PARMS, this));
		addTask(new CATTask(Task.UNLOCK_ENGINE, this));
		if (!isGuiEnabled()) {
			lockEngine();
		}
	}

	protected void setBiomassParameters() throws IOException {
		getCarbonToolSettings().getCustomizableBiomassParameters().load(biomassParametersFilename);
		getCarbonToolSettings().currentBiomassParameters = BiomassParametersName.customized;
	}
	
	/**
	 * This method sets the production lines for the next simulation in script mode. 
	 * @param filename a String that defines the filename (.prl) of the processor manager
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public void setProductionManager(String filename) throws InterruptedException {
		this.productionManagerFilename = filename;
		addTask(new CATTask(Task.SET_PRODUCTION_MANAGER, this));
		addTask(new CATTask(Task.UNLOCK_ENGINE, this));
		if (!isGuiEnabled()) {
			lockEngine();
		}
	}
	
	protected void setProductionManager() throws IOException {
		getCarbonToolSettings().getCustomizableProductionProcessorManager().load(productionManagerFilename);
		getCarbonToolSettings().currentProcessorManager = ProductionManagerName.customized;
	}
	
	/**
	 * This method sets the different parameters of the sensitivity analysis at the level suggested by the IPCC.
	 * @param source the source of variability
	 * @param type the distribution (either uniform (default) or Gaussian
	 * @param enabled true to enable or false to disable
	 */
	public void setVariabilitySource(VariabilitySource source, Distribution.Type type, boolean enabled) {
		CATSensitivityAnalysisSettings.getInstance().setVariabilitySource(source, type, enabled, source.getSuggestedIPCCValue() * .01);
	}	

	/**
	 * This method sets the different parameters of the sensitivity analysis
	 * @param source the source of variability
	 * @param type the distribution (either uniform (default) or Gaussian
	 * @param enabled true to enable or false to disable
	 * @param multiplier a value between 0.0 and 0.5 (50%)
	 */
	public void setVariabilitySource(VariabilitySource source, Distribution.Type type, boolean enabled, double multiplier) {
		CATSensitivityAnalysisSettings.getInstance().setVariabilitySource(source, type, enabled, multiplier);
	}

	/**
	 * This method returns true if this CAT instance has been initialized or false otherwise
	 * @return a boolean
	 */
	public boolean isInitialized() {return initialized;}
	
	/*
	 * Entry point for CAT in standalone modes
	 */
	public static void main(String[] args) throws Exception {
//		REpiceaTranslator.setCurrentLanguage(Language.French);
//		REpiceaTranslator.setCurrentLanguage(Language.English);
		CarbonAccountingTool tool = new CarbonAccountingTool();
		tool.initializeTool(null);
		Vector<TreeLoggerDescription> treeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		treeLoggerDescriptions.add(new TreeLoggerDescription(BasicTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(CATDiameterBasedTreeLogger.class));
		
		treeLoggerDescriptions.add(new TreeLoggerDescription(MathildeTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(MaritimePineBasicTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(EuropeanBeechBasicTreeLogger.class));
		treeLoggerDescriptions.add(new TreeLoggerDescription(DouglasFCBATreeLogger.class));
		tool.getCarbonToolSettings().setTreeLoggerDescriptions(treeLoggerDescriptions);
	}


	
}
