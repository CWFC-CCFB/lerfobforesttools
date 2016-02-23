/*
 * This file is part of the lerfob-forestools library.
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

import lerfob.carbonbalancetool.CarbonAccountingToolTask.SetProperRealizationTask;
import lerfob.carbonbalancetool.CarbonAccountingToolTask.Task;
import repicea.app.AbstractGenericEngine;
import repicea.app.SettingMemory;
import repicea.gui.ShowableObject;
import repicea.gui.ShowableObjectWithParent;
import repicea.gui.genericwindows.GeneralLicenseWindow;
import repicea.gui.genericwindows.GenericSplashWindow;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.treelogger.basictreelogger.BasicTreeLogger;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaSystem;
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
public class LERFoBCarbonAccountingTool extends AbstractGenericEngine implements ShowableObjectWithParent, ShowableObject {
	
	private static class StandComparator implements Comparator<CarbonToolCompatibleStand> {

		@Override
		public int compare(CarbonToolCompatibleStand arg0, CarbonToolCompatibleStand arg1) {
			if (arg0.getDateYr() < arg1.getDateYr()) {
				return -1;
			} else if (arg0.getDateYr() == arg1.getDateYr()) {
				return 0;
			} else {
				return 1;
			}
		}
		
	}
	
	
	
	protected static final String englishTitle = "LERFoB Carbon Accounting Tool (LERFoB-CAT)";
	protected static final String frenchTitle = "Outil d'\u00E9valuation du carbone LERFoB (LERFoB-CAT)";
	private static final StandComparator StandComparator = new StandComparator();
	protected static boolean hasAlreadyBeenInstanciated = false;
	
	private CarbonCompartmentManager carbonCompartmentManager;	
	protected boolean finalCutHadToBeCarriedOut;
	private final Map<StatusClass, Map<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>>> treeCollections;
	private final Map<CarbonToolCompatibleTree, CarbonToolCompatibleStand> treeRegister;
	
	protected Window parentFrame;
	protected List<CarbonToolCompatibleStand> waitingStandList;
	protected transient CarbonAccountingToolDialog guiInterface;
	protected transient Window owner;
	

	/**
	 * General constructor.
	 */
	public LERFoBCarbonAccountingTool() {
		setSettingMemory(new SettingMemory(REpiceaSystem.getJavaIOTmpDir() + "settingsCarbonTool.ser"));
		
		finalCutHadToBeCarriedOut = false;
		treeCollections = new HashMap<StatusClass, Map<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>>>();
		treeRegister = new HashMap<CarbonToolCompatibleTree, CarbonToolCompatibleStand>();
		
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
	public CarbonAccountingToolSettings getCarbonToolSettings() {
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
		CarbonAccountingToolSettings carbonToolSettings = new CarbonAccountingToolSettings(getSettingMemory());
		carbonCompartmentManager = new CarbonCompartmentManager(carbonToolSettings);
		
		if (isGuiEnabled) {
			if (!hasAlreadyBeenInstanciated) {
				String packagePath = ObjectUtility.getRelativePackagePath(LERFoBCarbonAccountingTool.class);
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
			addTask(new CarbonAccountingToolTask(Task.SHOW_INTERFACE, this));
		}
		
		return true;
	}

	/**
	 * This method sets the list of stands from which the carbon balance should be calculated.
	 * @param standList a List of CarbonToolCompatibleStand instance
	 */
	public void setStandList(List<CarbonToolCompatibleStand> standList) {
		Collections.sort(standList, StandComparator);
		waitingStandList = standList;
		addTask(new CarbonAccountingToolTask(Task.SET_STANDLIST, this));
	}
	
	protected void registerTree(StatusClass statusClass, CarbonToolCompatibleStand stand, CarbonToolCompatibleTree tree) {
		if (!treeCollections.containsKey(statusClass)) {
			treeCollections.put(statusClass, new HashMap<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>>());
		}
		Map<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>> innerMap = treeCollections.get(statusClass);
		if (!innerMap.containsKey(stand)) {
			innerMap.put(stand, new ArrayList<CarbonToolCompatibleTree>());
		}
		Collection<CarbonToolCompatibleTree> trees = innerMap.get(stand);
		trees.add(tree);
		treeRegister.put(tree, stand);
	}
	
	protected Map<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>> getHarvestedTrees() {
		if (treeCollections.containsKey(StatusClass.cut)) {
			return treeCollections.get(StatusClass.cut);
		} else {
			return new HashMap<CarbonToolCompatibleStand, Collection<CarbonToolCompatibleTree>>();
		}
	}
	
	protected int getDateForThisTree(CarbonToolCompatibleTree tree) {
		if (treeRegister.containsKey(tree)) {
			return treeRegister.get(tree).getDateYr();
		} else {
			return -1;
		}
	}
	
	protected void clearTreeCollections() {
		treeCollections.clear();
		treeRegister.clear();
	}
	
	protected void setStandList() {
		finalCutHadToBeCarriedOut = false;
		clearTreeCollections();
		carbonCompartmentManager.init(waitingStandList);
		setReferentForBiomassParameters(carbonCompartmentManager.getStandList());
		setTreeLoggerDescription();
		if (isGuiEnabled()) {
			Runnable doRun = new Runnable() {
				@Override
				public void run() {
					getGuiInterface().majorProgressBar.setMinimum(0);
					getGuiInterface().majorProgressBar.setMaximum(getCarbonCompartmentManager().nRealizations);
					getGuiInterface().refreshInterface();
				}
			};
			SwingUtilities.invokeLater(doRun);
		}

	}
	
	@SuppressWarnings("unchecked")
	private void setReferentForBiomassParameters(List<CarbonToolCompatibleStand> stands) {
		Object referent = null;
		if (stands != null && !stands.isEmpty()) {
			for (CarbonToolCompatibleStand stand : stands) {
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
					if (obj instanceof CarbonToolCompatibleTree) {
						referent = obj;
						break;
					}
				}
			}
		}
		getCarbonToolSettings().setReferentForBiomassParameters(referent);
	}

	protected void setTreeLoggerDescription() {
		Vector<TreeLoggerDescription> defaultTreeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(BasicTreeLogger.class.getName()));
		getCarbonToolSettings().setTreeLoggerDescriptions(defaultTreeLoggerDescriptions);
	}
	
	@Override
	protected void firstTasksToDo() {}

	@Override
	protected void decideWhatToDo(String taskName, Exception failureReason) {
		super.decideWhatToDo(taskName, failureReason);
		unlockEngine();
	}

	
	public CarbonCompartmentManager getCarbonCompartmentManager() {return carbonCompartmentManager;}

	/**
	 * This method launches the calculation of the different carbon compartments.
	 * @throws InterruptedException if the engine lock is interrupted
	 * @throws InvalidParameterException if the CarbonToolSettings instance is invalid
	 */
	public void calculateCarbon() throws InvalidParameterException, InterruptedException {
		if (!carbonCompartmentManager.getCarbonToolSettings().isValid()) {
			throw new InvalidParameterException("The settings of the carbon accounting tool are invalid. Please check!");
		} else {
			int nbReals = carbonCompartmentManager.nRealizations;
			if (nbReals < 1) {
				nbReals = 1;
			}
			carbonCompartmentManager.summary = null; // reset the summary before going on
			for (int i = 0; i < nbReals; i++) {
				addTask(new CarbonAccountingToolTask(Task.RESET_MANAGER, this));
				addTask(new SetProperRealizationTask(this, i));
				addTask(new CarbonAccountingToolTask(Task.LOG_AND_BUCK_TREES, this));
				addTask(new CarbonAccountingToolTask(Task.GENERATE_WOODPRODUCTS, this));
				addTask(new CarbonAccountingToolTask(Task.ACTUALIZE_CARBON, this));
				addTask(new CarbonAccountingToolTask(Task.COMPILE_CARBON, this));
			} 
			addTask(new CarbonAccountingToolTask(Task.UNLOCK_ENGINE, this));
			if (!isGuiEnabled()) {
				lockEngine();
			} else {
				addTask(new CarbonAccountingToolTask(Task.DISPLAY_RESULT, this));
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
	public CarbonAccountingToolExport createExportTool() throws Exception {
		return new CarbonAccountingToolExport(getCarbonCompartmentManager().getCarbonToolSettings (), getCarbonCompartmentManager().getSimulationSummary());
	}
	
	/**
	 * By default, closing the gui shuts the engine down. This method must be 
	 * overriden with empty content to disable the automatic shut down.
	 */
	protected void respondToWindowClosing() {
		addTask(new CarbonAccountingToolTask(Task.SHUT_DOWN, this));
	}
	
	@Override
	public CarbonAccountingToolDialog getGuiInterface(Container parent) {
		if (owner == null && parent != null && parent instanceof Window) {
			owner = (Window) parent;
		}
		if (guiInterface == null) {
			guiInterface = new CarbonAccountingToolDialog(this, null);
		}
		return guiInterface;
	}	

	@Override
	public void showInterface(Window parent) {
		getGuiInterface(parent).setVisible(true);
	}

	@Override
	public CarbonAccountingToolDialog getGuiInterface() {return getGuiInterface(null);}

	@Override
	public void showInterface() {
		if (owner != null) {
			showInterface(owner);
		} else {
			getGuiInterface().setVisible(true);
		}
		
	}

	protected void showResult() {
		if (isGuiEnabled()) {
			Runnable job = new Runnable() {
				@Override
				public void run() {
					getGuiInterface().displayResult();
				}
			};
			SwingUtilities.invokeLater(job);
		}
	}

	
	
	public static void main(String[] args) {
		LERFoBCarbonAccountingTool tool = new LERFoBCarbonAccountingTool();
		tool.initializeTool(true, null);
//		System.exit(0);
	}


	
}
