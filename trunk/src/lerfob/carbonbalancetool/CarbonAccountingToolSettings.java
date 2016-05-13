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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import lerfob.carbonbalancetool.CarbonAccountingToolUtility.BiomassParametersName;
import lerfob.carbonbalancetool.CarbonAccountingToolUtility.BiomassParametersWrapper;
import lerfob.carbonbalancetool.CarbonAccountingToolUtility.CarbonToolSettingsVector;
import lerfob.carbonbalancetool.CarbonAccountingToolUtility.ProductionManagerName;
import lerfob.carbonbalancetool.CarbonAccountingToolUtility.ProductionProcessorManagerWrapper;
import lerfob.carbonbalancetool.biomassparameters.BiomassParameters;
import lerfob.carbonbalancetool.productionlines.ProductionLineManager;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import lerfob.carbonbalancetool.woodpiecedispatcher.WoodPieceDispatcher;
import repicea.app.SettingMemory;
import repicea.gui.permissions.DefaultREpiceaGUIPermission;
import repicea.simulation.Parameterizable;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.TreeLoggerWrapper;
import repicea.treelogger.basictreelogger.BasicTreeLogger;
import repicea.treelogger.basictreelogger.BasicTreeLoggerParameters;
import repicea.util.ExtendedFileFilter;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.TextableEnum;

/**
 * The CarbonAccountingToolSettings class handles the production lines, the wood piece dispatcher
 * and the biomass parameters. 
 * @author Mathieu Fortin - February 2014
 */
@SuppressWarnings("deprecation")
public final class CarbonAccountingToolSettings {

	public static enum AssessmentReport implements TextableEnum {
		Second("Second Assessment Report", "Deuxi\u00E8me rapport d'\u00E9valuation"),
		Fourth("Fourth Assessment Report", "Quatri\u00E8me rapport d'\u00E9valuation"),
		Fifth("Fifth Assessment Report", "Cinqui\u00E8me rapport d'\u00E9valuation");

		AssessmentReport(String englishText, String frenchText) {
			setText(englishText, frenchText);
		}
		
		@Override
		public void setText(String englishText, String frenchText) {
			REpiceaTranslator.setString(this, englishText, frenchText);
		}

		@Override
		public String toString() {return REpiceaTranslator.getString(this);}
	}
	
	public static class GWP {
		
		private final double ch4ToCo2Eq;
		private final double n2oToCo2Eq;
		
		GWP(double ch4ToCo2Eq, double n2oToCo2Eq) {
			this.ch4ToCo2Eq = ch4ToCo2Eq;
			this.n2oToCo2Eq = n2oToCo2Eq;
		}
		
		/**
		 * This method returns the CO2 eq of the CH4 gaz
		 * @return a double
		 */
		public double getCH4Factor() {return ch4ToCo2Eq;}
		
		/**
		 * This method returns the CO2 eq of the N2O gaz
		 * @return a double
		 */
		public double getN2OFactor() {return n2oToCo2Eq;}
	}

	private static Map<AssessmentReport, GWP> GlobalWarmingPotentialMap = new HashMap<AssessmentReport, GWP>();
	static {
		GlobalWarmingPotentialMap.put(AssessmentReport.Second, new GWP(21, 310));
		GlobalWarmingPotentialMap.put(AssessmentReport.Fourth, new GWP(25, 298));
		GlobalWarmingPotentialMap.put(AssessmentReport.Fifth, new GWP(28, 265));
	}

	
	private final ExponentialFunction decayFunction = new ExponentialFunction();
	private final SettingMemory settings;
	protected boolean formerImplementation;

//	public static final double CH4_CO2_EQUIVALENT = 25;	// taken from IPCC(2007, Table 2.14)
//	public static final double N2O_CO2_EQUIVALENT = 298;	// taken from IPCC(2007, Table 2.14)
	public static final double CO2_C_FACTOR = 12d / 44;
	public static final double C_C02_FACTOR = 44d / 12;

	protected static AssessmentReport selectedAR = AssessmentReport.Fifth;
	
	@Deprecated
	private TreeLoggerWrapper treeLoggerWrapper;
	@Deprecated
	private final ProductionLineManager productionLines;
	@Deprecated
	private final WoodPieceDispatcher woodSupply;
	@Deprecated
	private Vector<TreeLoggerDescription> treeLoggerDescriptions;

	protected final CarbonToolSettingsVector<ProductionProcessorManagerWrapper> processorManagers = new CarbonToolSettingsVector<ProductionProcessorManagerWrapper>();
	protected final CarbonToolSettingsVector<BiomassParametersWrapper> biomassParametersVector = new CarbonToolSettingsVector<BiomassParametersWrapper>();

	protected int currentProcessorManagerIndex = 0;
	protected int currentBiomassParametersIndex = 0; 
	
	protected ProductionProcessorManager customizedManager;
	
	/**
	 * Constructor.
	 */
	public CarbonAccountingToolSettings(SettingMemory settings) {			
		this.settings = settings;
		readProcessorManagers();
		readBiomassParametersVector();
//		biomassParameters = new BiomassParameters();
		productionLines = new ProductionLineManager();
		treeLoggerWrapper = new TreeLoggerWrapper();
		woodSupply = new WoodPieceDispatcher(treeLoggerWrapper, productionLines);
		treeLoggerDescriptions = new Vector<TreeLoggerDescription>();
	}
	

	/**
	 * This method returns the Global Warming Potential according
	 * to the selected assessment report.
	 * @return a GWP instance
	 */
	public static GWP getGlobalWarmingPotential() {
		return GlobalWarmingPotentialMap.get(selectedAR);
	}
	
	protected static void setAssessmentReportForGWP(AssessmentReport aR) {
		selectedAR = aR;
	}
	
	private void readBiomassParametersVector() {
		BiomassParameters biomassParameters;
		String relativePathname = ObjectUtility.getRelativePackagePath(BiomassParameters.class) + "library" + ObjectUtility.PathSeparator;
		for (BiomassParametersName biomassParameterNames : BiomassParametersName.values()) {
			if (biomassParameterNames == BiomassParametersName.customized) {
				biomassParameters = new BiomassParameters();
				biomassParametersVector.add(new BiomassParametersWrapper(biomassParameterNames, biomassParameters));
			} else {
				try {
					biomassParameters = new BiomassParameters(new DefaultREpiceaGUIPermission(false));
					String filename = relativePathname + biomassParameterNames.name().toLowerCase() + ((ExtendedFileFilter) biomassParameters.getFileFilter()).getExtension(); 
					biomassParameters.load(filename);
					biomassParametersVector.add(new BiomassParametersWrapper(biomassParameterNames, biomassParameters));
				} catch (Exception e) {}
			}
		}
	}

	private void readProcessorManagers() {
		ProductionProcessorManager productionProcessorManager;
		String relativePathname = ObjectUtility.getRelativePackagePath(ProductionProcessorManager.class) + "library" + ObjectUtility.PathSeparator;
		for (ProductionManagerName processorManagerName : ProductionManagerName.values()) {
			if (processorManagerName == ProductionManagerName.customized) {
				productionProcessorManager = new ProductionProcessorManager();
				processorManagers.add(new ProductionProcessorManagerWrapper(processorManagerName, productionProcessorManager));
				customizedManager = processorManagers.get(processorManagers.size() - 1).manager;
			} else {
				try {
					productionProcessorManager = new ProductionProcessorManager(new DefaultREpiceaGUIPermission(false));
					String filename = relativePathname + processorManagerName.name().toLowerCase() + "_" + REpiceaTranslator.getCurrentLanguage().getCode() + ((ExtendedFileFilter) productionProcessorManager.getFileFilter()).getExtension(); 
					productionProcessorManager.load(filename);
					processorManagers.add(new ProductionProcessorManagerWrapper(processorManagerName, productionProcessorManager));
				} catch (Exception e) {
					System.out.println("Unable to read processor manager : " + processorManagerName.name());
				}
			}
		}
	}

	/**
	 * This method returns the customizable production manager.
	 * @return a ProductionProcessorManager instance
	 */
	public ProductionProcessorManager getCustomizableProductionProcessorManager() {return customizedManager;}
	
	/**
	 * This method returns the currently selected ProductionProcessorManager instance.
	 * @return a ProductionProcessorManager instance
	 */
	public ProductionProcessorManager getCurrentProductionProcessorManager() {
		return processorManagers.get(currentProcessorManagerIndex).manager;
	}

	
	/**
	 * This method returns the currently selected BiomassParameters instance.
	 * @return a BiomassParameters instance
	 */
	public BiomassParameters getCurrentBiomassParameters() {
		return biomassParametersVector.get(currentBiomassParametersIndex).manager;
	}

	/**
	 * This method sets the possible tree logger options.
	 * @param treeLoggerDescriptions a Vector of TreeLoggerDescription instances
	 */
	public void setTreeLoggerDescriptions(Vector<TreeLoggerDescription> treeLoggerDescriptions) {
		ProductionProcessorManager manager = (ProductionProcessorManager) processorManagers.getFirstInstanceWithThisName(ProductionManagerName.customized);
		if (manager != null) {
			manager.setAvailableTreeLoggers(treeLoggerDescriptions);
		}
	}

	protected void setReferentForBiomassParameters(Object referent) {
		BiomassParameters manager = (BiomassParameters) biomassParametersVector.getFirstInstanceWithThisName(BiomassParametersName.customized);
		if (manager != null) {
			manager.setReferent(referent);
		}
	}
	
	
	
	/**
	 * This method returns the list of possible tree loggers.
	 * @return a Vector of TreeLoggerDescription instances
	 */
	@Deprecated
	protected Vector<TreeLoggerDescription> getTreeLoggerDescriptions() {
		return treeLoggerDescriptions;
	}


	/**
	 * This method returns true if the settings are valid.
	 * @return a boolean
	 */
	public boolean isValid() {
		if (formerImplementation) { 			// former implementation
			if (treeLoggerWrapper.getTreeLogger() != null) {
				if (woodSupply.isValid()) {
					return true;
				}
			}
			return false;
		} else {
			return getCurrentProductionProcessorManager().isValid();
		}
	}


	public ExponentialFunction getDecayFunction() {return decayFunction;}
	

	private void loadFromParameter(Parameterizable params, String filename) {
		try {
			params.loadFromFile(filename);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}


	protected SettingMemory getSettingMemory() {return settings;}


	/**
	 * This method sets the tree logger instance.
	 * @param selectedItem a String (default) or a TreeLoggerDescription instance
	 */
	@SuppressWarnings("rawtypes")
	@Deprecated
	public void setHarvestVolumeMethod(Object selectedItem) {
		formerImplementation = true;
		TreeLogger<?,?> treeLogger = null;
		if (selectedItem instanceof TreeLoggerDescription) {
			treeLogger = ((TreeLoggerDescription) selectedItem).instantiateTreeLogger(true);
			treeLoggerWrapper.setTreeLogger(treeLogger);
		} else if (selectedItem.toString().equals(CarbonAccountingToolDialog.MessageID.Default.toString())) {
			treeLogger = new BasicTreeLogger();
			TreeLoggerParameters params = new BasicTreeLoggerParameters();
			((BasicTreeLogger) treeLogger).setTreeLoggerParameters((BasicTreeLoggerParameters) params);
			treeLoggerWrapper.setTreeLogger(treeLogger);
		} else {
			loadFromParameter(treeLoggerWrapper, selectedItem.toString());
		}
	}

	/**
	 * This method sets the wood supply.  
	 * @param ws the filename without the path (which is assumed to be the workspace)
	 */
	@Deprecated
	public void setWoodSupply(Object selectedItem) {
		formerImplementation = true;
		if (selectedItem.equals(CarbonAccountingToolDialog.MessageID.Default.toString())) {
			woodSupply.reset();
		} else {
			loadFromParameter(woodSupply, selectedItem.toString());
		}
	}
	
	/**
	 * This method sets the production lines.  
	 * @param selectedItem the filename without the path (which is assumed to be the workspace)
	 */
	@Deprecated
	public void setProductionLines(Object selectedItem) {
		formerImplementation = true;
		if (selectedItem.equals(CarbonAccountingToolDialog.MessageID.Default.toString())) {
			productionLines.setToDefaultValue();
		} else {
			loadFromParameter(productionLines, selectedItem.toString());
		}
	}

	/**
	 * This method should be called in script mode.	
	 * @param treeLogger
	 * @param productionLinesFilename the path to the production lines file
	 * @param woodDispatcherFilename the path to the wood dispatcher file 
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	@Deprecated
	public void initialize(TreeLogger treeLogger, String productionLinesFilename, String woodDispatcherFilename) throws IOException {
		formerImplementation = true;
		treeLoggerWrapper.setTreeLogger(treeLogger);
		productionLines.load(productionLinesFilename);
		woodSupply.load(woodDispatcherFilename);
	}
	
	/**
	 * This method returns the TreeLogger instance of the settings.
	 * @return a TreeLogger instance
	 */
	public TreeLogger<?,?> getTreeLogger() {
		if (formerImplementation) {
			return treeLoggerWrapper.getTreeLogger();
		} else {
			return getCurrentProductionProcessorManager().getSelectedTreeLogger();
		}
	}

	/**
	 * This method returns the production lines.
	 * @return a ProductionLineManager instance
	 */
	@Deprecated
	protected ProductionLineManager getProductionLines() {return productionLines;}

	/**
	 * This method returns the wood piece dispatcher.
	 * @return a WoodPieceDispatcher instance
	 */
	@Deprecated
	protected WoodPieceDispatcher getWoodSupplySetup() {return woodSupply;}

}
