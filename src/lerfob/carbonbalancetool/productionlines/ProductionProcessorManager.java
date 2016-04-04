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
package lerfob.carbonbalancetool.productionlines;

import java.awt.Container;
import java.awt.Window;
import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.filechooser.FileFilter;

import lerfob.carbonbalancetool.CarbonCompartmentManager;
import lerfob.carbonbalancetool.DecayFunction;
import lerfob.carbonbalancetool.ExponentialFunction;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.WoodyDebrisProcessor.WoodyDebrisProcessorID;
import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.processsystem.ProcessUnit;
import repicea.simulation.processsystem.Processor;
import repicea.simulation.processsystem.SystemManager;
import repicea.simulation.processsystem.SystemManagerDialog;
import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.treelogger.basictreelogger.BasicTreeLogger;
import repicea.util.ExtendedFileFilter;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

/**
 * The ProductionProcessorManager class is an implementation of SystemManager for production
 * line design in a context of carbon balance assessment.
 * @author Mathieu Fortin - May 2014
 */
public class ProductionProcessorManager extends SystemManager implements Memorizable {

	static {
		XmlSerializerChangeMonitor.registerClassNameChange("lerfob.carbonbalancetool.defaulttreelogger.CATDefaultLogCategory", "repicea.treelogger.basictreelogger.BasicLogCategory");
		XmlSerializerChangeMonitor.registerClassNameChange("lerfob.carbonbalancetool.defaulttreelogger.CATDefaultTreeLogger", "repicea.treelogger.basictreelogger.BasicTreeLogger");
		XmlSerializerChangeMonitor.registerClassNameChange("lerfob.carbonbalancetool.defaulttreelogger.CATDefaultTreeLoggerParameters", "repicea.treelogger.basictreelogger.BasicTreeLoggerParameters");
		XmlSerializerChangeMonitor.registerClassNameChange("lerfob.carbonbalancetool.defaulttreelogger.CATDefaultTreeLoggerParametersDialog", "repicea.treelogger.basictreelogger.BasicTreeLoggerParametersDialog");
		XmlSerializerChangeMonitor.registerClassNameChange("lerfob.carbonbalancetool.defaulttreelogger.CATWoodPiece", "repicea.treelogger.basictreelogger.BasicTreeLoggerWoodPiece");
	}
	
	/**
	 * This class is the file filter for loading and saving production lines.
	 * @author Mathieu Fortin - May 2014
	 */
	private static class ProductionLineFileFilter extends FileFilter implements ExtendedFileFilter {

		private String extension = ".prl";
		
		@Override
		public boolean accept(File f) {
			if (f.isDirectory() || f.getAbsolutePath().toLowerCase().trim().endsWith(extension)) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public String getDescription() {
			return ProductionProcessorManagerDialog.MessageID.ProductionLineFileExtension.toString();
		}

		@Override
		public String getExtension() {return extension;}
	}

	
	protected static List<LeftHandSideProcessor> DefaultLeftHandSideProcessors;
	
	protected static final ProductionLineFileFilter MyFileFilter = new ProductionLineFileFilter();
	
	/**
	 * The VERY_SMALL value serves as threshold when dealing with small quantities. Below the threshold the quantity is not considered at all. 
	 */
	public static final double VERY_SMALL = 1E-12;

	protected static enum EnhancedMode {CreateEndOfLifeLinkLine}
	
	private transient final Vector<TreeLoggerParameters<?>> availableTreeLoggerParameters;
	
	@SuppressWarnings("rawtypes")
	private TreeLoggerParameters selectedTreeLoggerParameters;
	
	@SuppressWarnings("rawtypes")
	private transient TreeLogger treeLogger;

	protected final ArrayList<LeftHandSideProcessor> logCategoryProcessors;
	
	private transient Map<TreeLogCategory, LogCategoryProcessor> logCategoryProcessorIndices = new HashMap<TreeLogCategory, LogCategoryProcessor>();
	
	private transient CarbonUnitMap<CarbonUnitStatus> carbonUnitMap;
	
	private DecayFunction decayFunction;
	
	
	/**
	 * Constructor.
	 */
	public ProductionProcessorManager(DefaultREpiceaGUIPermission defaultPermission) {
		super(defaultPermission);
		logCategoryProcessors = new ArrayList<LeftHandSideProcessor>();
		availableTreeLoggerParameters = new Vector<TreeLoggerParameters<?>>();

		Vector<TreeLoggerDescription> defaultTreeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(BasicTreeLogger.class));
//		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(MaritimePineBasicTreeLogger.class.getName()));
//		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(EuropeanBeechBasicTreeLogger.class.getName()));
//		try {
//			Class<?> petroTreeLoggerClass = ClassLoader.getSystemClassLoader().loadClass("quebecmrnfutility.treelogger.petrotreelogger.PetroTreeLogger");
//			defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(petroTreeLoggerClass.getName()));
//		} catch (ClassNotFoundException e) {}
//		
//		try {
//			Class<?> sybilleTreeLoggerClass = ClassLoader.getSystemClassLoader().loadClass("quebecmrnfutility.treelogger.sybille.SybilleTreeLogger");
//			defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(sybilleTreeLoggerClass.getName()));
//		} catch (ClassNotFoundException e) {}
		
		setAvailableTreeLoggers(defaultTreeLoggerDescriptions);
	}

	public ProductionProcessorManager() {
		this(new DefaultREpiceaGUIPermission(true));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TreeLogger getSelectedTreeLogger() {
		TreeLoggerParameters<?> parms = getSelectedTreeLoggerParameters();
		if (treeLogger == null || !parms.getTreeLoggerDescription().getTreeLoggerClass().equals(treeLogger.getClass())) {
			treeLogger = parms.createTreeLoggerInstance();
		} else if (!treeLogger.getTreeLoggerParameters().equals(parms)) {
			treeLogger.setTreeLoggerParameters(parms);
		}
		return treeLogger;
	}

	
	public void resetCarbonUnitMap() {
		logCategoryProcessorIndices.clear();
		getCarbonUnitMap().clear();
	}
	
	protected TreeLoggerParameters<?> getSelectedTreeLoggerParameters() {return selectedTreeLoggerParameters;}

	protected void setSelectedTreeLogger(TreeLoggerParameters<?> treeLoggerParameters) {
		if (!isCompatibleWithAvailableTreeLoggerParameters(treeLoggerParameters)) {
			throw new InvalidParameterException("The TreeLoggerDescription instance is not compatible!");
		} else {
			selectedTreeLoggerParameters = treeLoggerParameters;
			actualizeTreeLoggerParameters();
		}
	}
	
	private boolean isCompatibleWithAvailableTreeLoggerParameters(TreeLoggerParameters<?> treeLoggerParameters) {
		boolean matchFound = false;
		int index = -1;
		for (index = 0; index < availableTreeLoggerParameters.size(); index++) {
			if (availableTreeLoggerParameters.get(index).getClass().equals(treeLoggerParameters.getClass())) {
				matchFound = true;
				break;
			}
		}
		if (matchFound) {
			availableTreeLoggerParameters.remove(index);
			availableTreeLoggerParameters.add(index, treeLoggerParameters);
			treeLoggerParameters.setReadWritePermissionGranted(getGUIPermission());
		}
		return matchFound;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void actualizeTreeLoggerParameters() {
		// TODO clean that up
		List<LeftHandSideProcessor> formerProcessorList = new ArrayList<LeftHandSideProcessor>();
		formerProcessorList.addAll(logCategoryProcessors);
		List<LeftHandSideProcessor> newProcessorList = new ArrayList<LeftHandSideProcessor>();
		newProcessorList.addAll(getDefaultLeftHandSideProcessors());
		for (Object species : selectedTreeLoggerParameters.getLogCategories().keySet()) {
			List<TreeLogCategory> innerList = (List) selectedTreeLoggerParameters.getLogCategories().get(species);
 			for (TreeLogCategory logCategory : innerList) {
 				newProcessorList.add(new LogCategoryProcessor(logCategory));
			}
		}
		formerProcessorList.removeAll(newProcessorList);
//		System.out.println("Removing " + formerProcessorList.toString());
		for (Processor processor : formerProcessorList) {
			logCategoryProcessors.remove(processor);
			removeObject(processor);
		}
		newProcessorList.removeAll(logCategoryProcessors);
//		System.out.println("Adding " + newProcessorList.toString());
		for (LeftHandSideProcessor  processor : newProcessorList) {
			logCategoryProcessors.add(processor);
			registerObject(processor);
		}
	}
	
	private Collection<? extends LeftHandSideProcessor> getDefaultLeftHandSideProcessors() {
		if (DefaultLeftHandSideProcessors == null) {
			DefaultLeftHandSideProcessors = new ArrayList<LeftHandSideProcessor>();
			DefaultLeftHandSideProcessors.add(new WoodyDebrisProcessor(WoodyDebrisProcessorID.FineWoodyDebris));
			DefaultLeftHandSideProcessors.add(new WoodyDebrisProcessor(WoodyDebrisProcessorID.CommercialWoodyDebris));
			DefaultLeftHandSideProcessors.add(new WoodyDebrisProcessor(WoodyDebrisProcessorID.CoarseWoodyDebris));
		}
		return DefaultLeftHandSideProcessors;
	}

	protected boolean isGuiVisible() {
		return guiInterface != null && guiInterface.isVisible();
	}
	

	protected Vector<TreeLoggerParameters<?>> getAvailableTreeLoggerParameters() {return availableTreeLoggerParameters;}

	@Override
	public FileFilter getFileFilter() {return MyFileFilter;}
	
	
	@Override
	public SystemManagerDialog getGuiInterface(Container parent) {
		if (guiInterface == null) {
			guiInterface = new ProductionProcessorManagerDialog((Window) parent, this);
		}
		return guiInterface;
	}

	@Override
	public MemorizerPackage getMemorizerPackage() {
		MemorizerPackage mp = super.getMemorizerPackage();
		mp.add(logCategoryProcessors);
		mp.add(selectedTreeLoggerParameters);
		return mp;
	}

	@SuppressWarnings({"unchecked","rawtypes"})
	@Override
	public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
		super.unpackMemorizerPackage(wasMemorized);
		ArrayList<LogCategoryProcessor> lcp = (ArrayList) wasMemorized.remove(0);
		logCategoryProcessors.clear();
		logCategoryProcessors.addAll(lcp);
		TreeLoggerParameters tlp = (TreeLoggerParameters) wasMemorized.remove(0);
		setSelectedTreeLogger(tlp);
		this.actualizeTreeLoggerParameters();
	}

	/**
	 * This method sets the list of tree logger descriptions.
	 * @param vector a Vector instance containing TreeLoggerdescription objects
	 */
	public void setAvailableTreeLoggers(Vector<TreeLoggerDescription> vector) {
		availableTreeLoggerParameters.clear();
		if (vector == null) {
			vector = new Vector<TreeLoggerDescription>();
		}
		if (vector.isEmpty()) {
			vector.add(new TreeLoggerDescription(BasicTreeLogger.class.getName()));
		}
		for (TreeLoggerDescription description : vector) {
			TreeLoggerParameters<?> treeLoggerParameters = description.instantiateTreeLogger(true).createDefaultTreeLoggerParameters();
			treeLoggerParameters.setReadWritePermissionGranted(getGUIPermission());
			availableTreeLoggerParameters.add(treeLoggerParameters);
			
		}
		if (selectedTreeLoggerParameters == null || !availableTreeLoggerParameters.contains(selectedTreeLoggerParameters)) {
			setSelectedTreeLogger(availableTreeLoggerParameters.get(0));	// default selection here
		}
	}

	/**
	 * This method returns true if the ProductionProcessorManager instance is valid or false otherwise. To be 
	 * valid, all the LogCategoryProcessor must have at least one sub processor. Moreover, all the processors must be
	 * valid, i.e. the sum of the fluxes to the sub processors must be equal to 100%.
	 * @return a boolean
	 */
	public boolean isValid() {
		for (Processor logCategoryProcessor : logCategoryProcessors) {
			if (!logCategoryProcessor.hasSubProcessors()) {
				return false;
			}
		}
		for (Processor processor : getList()) {
			if (!processor.isValid()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * The main method for this class. The different kinds of produced carbon units are stored in the carbon unit map, which is accessible
	 * through the getCarbonUnits(CarbonUnitType) method.
	 * @param treeLogCategory a TreeLogCategory instance
	 * @param dateIndex the index of the date in the time scale
	 * @param amountMap a Map which contains the amounts of the different elements
	 */
	public Collection<CarbonUnit> processWoodPiece(TreeLogCategory treeLogCategory,	int dateIndex, AmountMap<Element> amountMap) {
		Processor processor = findLeftHandSideProcessor(treeLogCategory);
		return processAmountMap(processor, dateIndex, amountMap);
	}

	/**
	 * This method calculates the carbon units in the woody debris. 
	 * @param dateIndex the index of the date in the time scale
	 * @param amountMap a Map which contains the amounts of the different elements
	 * @param type a WoodyDebrisProcessorID enum variable
	 */
	public void processWoodyDebris(int dateIndex, AmountMap<Element> amountMap, WoodyDebrisProcessorID type) {
		Processor processor = findWoodyDebrisProcessor(type);
		processAmountMap(processor, dateIndex, amountMap);
	}
	
//	/**
//	 * This method calculates the carbon units in the coarse woody debris. 
//	 * @param dateIndex the index of the date in the time scale
//	 * @param amountMap a Map which contains the amounts of the different elements
//	 */
//	public void processCoarseWoodyDebris(int dateIndex, AmountMap<Element> amountMap) {
//		Processor processor = findWoodyDebrisProcessor(WoodyDebrisProcessorID.CoarseWoodyDebris);
//		processAmountMap(processor, dateIndex, amountMap);
//	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Collection<CarbonUnit> processAmountMap(Processor processor, int dateIndex, AmountMap<Element> amountMap) {
		List<ProcessUnit> inputUnits = new ArrayList<ProcessUnit>();
		inputUnits.add(new CarbonUnit(dateIndex, null, amountMap));
		Collection<CarbonUnit> processedUnits = (Collection) processor.doProcess(inputUnits);
		getCarbonUnitMap().add(processedUnits);
		return processedUnits;
	}
	
	/**
	 * This method actualizes the different carbon units. It proceeds in the following order : </br>
	 * &nbsp	1- the carbon units left in the forest</br>
	 * &nbsp	2- the carbon units in the wood products</br>
	 * &nbsp	3- the carbon units at the landfill site</br>
	 * &nbsp	4- the carbon units recycled from the disposed wood products</br>
	 * @param timeScale is an Array of integer that indicates the years of actualization
	 * @throws Exception
	 */
	public void actualizeCarbonUnits(CarbonCompartmentManager compartmentManager) throws Exception {
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.HarvestResidues, compartmentManager);
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.EndUseWoodProduct, compartmentManager);
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.LandFillDegradable, compartmentManager);
		actualizeCarbonUnitsOfThisType(CarbonUnitStatus.Recycled, compartmentManager);
	}

	private void actualizeCarbonUnitsOfThisType(CarbonUnitStatus type, CarbonCompartmentManager compartmentManager) throws Exception {
		try {
			for (CarbonUnit carbonUnit : getCarbonUnits(type)) {
				carbonUnit.actualizeCarbon(compartmentManager);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("An exception occurred while actualizing carbon units of type " + type.name() + " : " + e.getMessage());
		}
	}

	protected DecayFunction getDecayFunction() {
		if (decayFunction == null) {
			decayFunction = new ExponentialFunction();
		}
		return decayFunction; 
	}
	
	protected CarbonUnitMap<CarbonUnitStatus> getCarbonUnitMap() {
		if (carbonUnitMap == null) {
			carbonUnitMap = new CarbonUnitMap<CarbonUnitStatus>(CarbonUnitStatus.EndUseWoodProduct);
		}
		return carbonUnitMap;
	}
	
	private WoodyDebrisProcessor findWoodyDebrisProcessor(WoodyDebrisProcessorID processorID) {
		for (LeftHandSideProcessor processor : logCategoryProcessors) {
			if (processor instanceof WoodyDebrisProcessor) {
				if (((WoodyDebrisProcessor) processor).wdpID == processorID) {
					return ((WoodyDebrisProcessor) processor);
				}
			}
		}
		if (processorID == WoodyDebrisProcessorID.CommercialWoodyDebris) {
			return findWoodyDebrisProcessor(WoodyDebrisProcessorID.CoarseWoodyDebris);		// in case commercial does not exist
		} 
		return null;
	}
	
	private LeftHandSideProcessor findLeftHandSideProcessor(TreeLogCategory logCategory) {
		if (!logCategoryProcessorIndices.containsKey(logCategory)) {
			for (LeftHandSideProcessor processor : logCategoryProcessors) {
				if (processor instanceof LogCategoryProcessor) {
					if (((LogCategoryProcessor) processor).logCategory.equals(logCategory)) {
						logCategoryProcessorIndices.put(logCategory, (LogCategoryProcessor) processor);
						break;
					}
				}
			}
		}

		LeftHandSideProcessor outputProcessor = logCategoryProcessorIndices.get(logCategory);
		if (outputProcessor == null) {
			throw new InvalidParameterException("The log category is not recognized by the ProductionProcessorManager instance");
		} else {
			return outputProcessor;
		}
	}
	
	/**
	 * This method returns the CarbonUnitList instance that match the type of carbon.
	 * @param type a CarbonUnitType enum (EndUseWoodProduct, Landfille, Recycled, LeftInForest)
	 * @return a CarbonUnitList instance
	 */
	public CarbonUnitList getCarbonUnits(CarbonUnitStatus type) {
		return getCarbonUnitMap().get(type);
	}

	@Override
	public void reset() {
		super.reset();
		logCategoryProcessors.clear();
		actualizeTreeLoggerParameters();
	}
	

	public static void main(String[] args) {
		REpiceaTranslator.setCurrentLanguage(Language.French);
//		ProductionProcessorManager ppm = new ProductionProcessorManager(new DefaultREpiceaGUIPermission(false));
		ProductionProcessorManager ppm = new ProductionProcessorManager();
		String filename = ObjectUtility.getPackagePath(ppm.getClass()) 
				+ File.separator + "library"
				+ File.separator + "hardwood_recycling_fr.prl";
		try {
			ppm.load(filename);
			ppm.showInterface(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

}
