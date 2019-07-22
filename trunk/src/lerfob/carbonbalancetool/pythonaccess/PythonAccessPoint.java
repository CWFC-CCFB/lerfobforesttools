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
package lerfob.carbonbalancetool.pythonaccess;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lerfob.app.LERFOBJARSVNAppVersion;
import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.CATCompatibleStand;
import lerfob.carbonbalancetool.CATSettings;
import lerfob.carbonbalancetool.CATSettings.CATSpecies;
import lerfob.carbonbalancetool.CATSimulationResult;
import lerfob.carbonbalancetool.CATUtilityMaps.MonteCarloEstimateMap;
import lerfob.carbonbalancetool.CATUtilityMaps.UseClassSpeciesMonteCarloEstimateMap;
import lerfob.carbonbalancetool.CarbonAccountingTool;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import lerfob.carbonbalancetool.sensitivityanalysis.CATSensitivityAnalysisSettings;
import lerfob.treelogger.basictreelogger.BasicTreeLogger;
import lerfob.treelogger.douglasfirfcba.DouglasFCBATreeLogger;
import lerfob.treelogger.europeanbeech.EuropeanBeechBasicTreeLogger;
import lerfob.treelogger.maritimepine.MaritimePineBasicTreeLogger;
import py4j.GatewayServer;
import repicea.app.REpiceaJARSVNAppVersion;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.treelogger.TreeLoggerCompatibilityCheck;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.util.ObjectUtility;

/**
 * This class is the entry point for a coupling with a Python application. Once exported as a .jar with the main function set to this
 * class, the entry point should use the method processStandList(Map myMap).
 * @author Mathieu Fortin - May 2014
 */
public class PythonAccessPoint extends CarbonAccountingTool {

	private static final String LISTEN = "-listen";
	private static final String CALLBACK = "-callback";

	protected CATSpecies speciesForSimulation; 
	protected double areaHa = 1d;
	
	public PythonAccessPoint() throws Exception {
		super(CATMode.SCRIPT);
		initializeTool(null);
		getCarbonToolSettings().setTreeLoggerDescriptions(findMatchingTreeLoggers(null));
	}
	
	/**
	 * This method sets the species for the simulation
	 * @param species a String either "beech" or "pine"
	 * @throws IOException 
	 */
	public void setSpecies(String species) throws Exception {
		CATSpecies speciesCode;
		if (species.toLowerCase().trim().equals("beech")) {
			speciesCode = CATSpecies.FAGUS_SYLVATICA;
		} else if (species.toLowerCase().trim().equals("pine")) {
			speciesCode = CATSpecies.PINUS_PINASTER;
		} else if (species.toLowerCase().trim().equals("douglas")) {
			speciesCode = CATSpecies.PSEUDOTSUGA_MENZIESII;
		} else {
			throw new InvalidParameterException("Only beech and pine are accepted as species!");
		}
		setSpeciesAndSettings(speciesCode);
	}


	/**
	 * This method sets the area of the plot to be processed. By default, this area is set
	 * to 1 ha.
	 * @param areaHA the area (ha)
	 */
	public void setAreaHA(double areaHa) {
		this.areaHa = areaHa;
	}
	
	@Override
	protected Vector<TreeLoggerDescription> findMatchingTreeLoggers(TreeLoggerCompatibilityCheck referent) {
		Vector<TreeLoggerDescription> defaultTreeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(BasicTreeLogger.class));
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(MaritimePineBasicTreeLogger.class));
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(EuropeanBeechBasicTreeLogger.class));
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(DouglasFCBATreeLogger.class));
		return defaultTreeLoggerDescriptions;
	}

	private void setSpeciesAndSettings(CATSpecies speciesCode) throws Exception {
		if (!speciesCode.equals(speciesForSimulation)) {
			speciesForSimulation = speciesCode;
			String filename;
			if (speciesForSimulation.equals(CATSpecies.PINUS_PINASTER)) {
				filename = ObjectUtility.getRelativePackagePath(getClass()) + "maritimepine.prl";
			} else if (speciesForSimulation.equals(CATSpecies.PSEUDOTSUGA_MENZIESII)) {
				filename = ObjectUtility.getRelativePackagePath(getClass()) + "Douglas_20170703_P_EOL.prl";
			} else {
				filename = ObjectUtility.getRelativePackagePath(getClass()) + "europeanbeech.prl";;
			}
			System.out.println("Loading settings : " + filename);
			setProductionManager(filename);
		}
		String biomassFilename;
		if (speciesForSimulation.equals(CATSpecies.PSEUDOTSUGA_MENZIESII)) {
			biomassFilename = ObjectUtility.getRelativePackagePath(getClass()) + "biomassParametersDouglasFir.bpf";
		} else {
			biomassFilename = ObjectUtility.getRelativePackagePath(getClass()) + "biomassParametersBeechPine.bpf";
		}
		setBiomassParameters(biomassFilename);
	}

	/*
	 * For extended visibility only (non-Javadoc)
	 * @see lerfob.carbonbalancetool.CarbonAccountingTool#getCarbonToolSettings()
	 */
	@Override
	protected CATSettings getCarbonToolSettings() {
		return super.getCarbonToolSettings();
	}
	
	@Override
	protected void shutdown(int shutdownCode) {
		System.out.println("Shutting down CAT...");
		CATSensitivityAnalysisSettings.getInstance().clear();
		System.exit(shutdownCode);
	}

	@SuppressWarnings("rawtypes")
	private double convertStringToDouble(Map innerMap, String key) {
		if (innerMap.containsKey(key)) {
			return Double.parseDouble(innerMap.get(key).toString());
		} else {
			return 0d;
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<Integer, Map<String, Double>> processStandList(String standID, Map inputMap) throws Exception {

		final String keyFirstInnerMap = "RECOLTE";
		List<CATCompatibleStand> standList = new ArrayList<CATCompatibleStand>();
		PythonCarbonToolCompatibleStand stand;
		PythonCarbonToolCompatibleTree tree;
		
		List<Integer> years = new ArrayList<Integer>();
		years.addAll(inputMap.keySet());
		Collections.sort(years);

		for (Integer dateYr : years) {
			Map innerMap = (Map) ((Map) inputMap.get(dateYr)).get(keyFirstInnerMap);
			
			stand = new PythonCarbonToolCompatibleStand(speciesForSimulation.name(), areaHa, standID, dateYr);
			standList.add(stand);
			
			if (innerMap != null) {
				double nbTreesHa = Double.parseDouble(innerMap.get("NbTrees").toString());
				double mqd = Double.parseDouble(innerMap.get("DBHmy").toString());
				double weightCrownKg_M2 = convertStringToDouble(innerMap, "Wcrown");		// TODO FP this is a patch while waiting for Christophe's reply
//				double weightCrownKg_M2 = Double.parseDouble(innerMap.get("Wcrown").toString());
				double weightTrunkKg_M2 = Double.parseDouble(innerMap.get("Wtrunk").toString());
				double dbhStandardDeviation = Double.parseDouble(innerMap.get("DBHect").toString());
				double weightRootsKg_M2 = Double.parseDouble(innerMap.get("Wroots").toString());
				boolean isProcessable = weightCrownKg_M2 >= 0d && weightTrunkKg_M2 >= 0d && weightRootsKg_M2 >= 0d && mqd > 0;
				if (isProcessable) {
					double nbTrees = nbTreesHa * stand.getAreaHa();
					
					if (speciesForSimulation == CATSpecies.PINUS_PINASTER) {
						tree = new PythonMaritimePineTree(StatusClass.cut,
								nbTrees,
								getAverageDryBiomassByTree(weightRootsKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightTrunkKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightCrownKg_M2, nbTreesHa),
								mqd,
								dbhStandardDeviation);
						stand.addTree(StatusClass.cut, tree);
					} else if (speciesForSimulation == CATSpecies.FAGUS_SYLVATICA) {
						tree = new PythonEuropeanBeechTree(StatusClass.cut,
								nbTrees,
								getAverageDryBiomassByTree(weightRootsKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightTrunkKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightCrownKg_M2, nbTreesHa),
								mqd,
								dbhStandardDeviation);
						stand.addTree(StatusClass.cut, tree);
					} else {
						tree = new PythonDouglasFirTree(StatusClass.cut,
								nbTrees,
								getAverageDryBiomassByTree(weightRootsKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightTrunkKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightCrownKg_M2, nbTreesHa),
								mqd,
								dbhStandardDeviation);
						stand.addTree(StatusClass.cut, tree);
					}
				}
			}
		}
		
		setStandList(standList);
		calculateCarbon();
		CATSimulationResult simulationResult = retrieveSimulationSummary();
		Map<Integer, UseClassSpeciesMonteCarloEstimateMap> productEvolutionMap = simulationResult.getProductEvolutionPerHa();
		
		Matrix carbonInHWP = simulationResult.getEvolutionMap().get(CompartmentInfo.TotalProducts).getMean();
		
		Matrix permanentSeqInLandfill = simulationResult.getEvolutionMap().get(CompartmentInfo.LfillND).getMean();
		Matrix landfillCarbonDegradable = simulationResult.getEvolutionMap().get(CompartmentInfo.LfillDeg).getMean();
		Matrix emissionDueToTransformation = simulationResult.getEvolutionMap().get(CompartmentInfo.CarbEmis).getMean();
		
		Map<Integer, Map<String, Double>> outputMap = new HashMap<Integer, Map<String, Double>>();
		
		for (Integer year : years) {
			if (!outputMap.containsKey(year)) {
				outputMap.put(year, new HashMap<String, Double>());
			}
			Map<String, Double> innerOutputMap1 = outputMap.get(year);
			UseClassSpeciesMonteCarloEstimateMap innerInputMap2 = productEvolutionMap.get(year);
			for (UseClass useClass : UseClass.values()) {
				if (useClass != UseClass.EXTRACTIVE) {
					String key = "BiomassMgHa" + useClass.name().toUpperCase();
					if (innerInputMap2 != null && innerInputMap2.containsKey(useClass)) {
						MonteCarloEstimateMap amountMap = innerInputMap2.get(useClass).getSumAcrossSpecies();
						innerOutputMap1.put(key, amountMap.get(Element.Biomass).getMean().m_afData[0][0]);
					} else {
						innerOutputMap1.put(key, 0d);
					}
				}
			}
			innerOutputMap1.put("CurrentCarbonHWPMgHa", carbonInHWP.m_afData[years.indexOf(year)][0]);
			innerOutputMap1.put("LandfillCarbonNDMgHa", permanentSeqInLandfill.m_afData[years.indexOf(year)][0]);
			innerOutputMap1.put("LandfillCarbonDegMgHa", landfillCarbonDegradable.m_afData[years.indexOf(year)][0]);
			innerOutputMap1.put("CEqEmissionTransMgHa", emissionDueToTransformation.m_afData[years.indexOf(year)][0]);
			
		}
		System.out.println("Stand " + standID + " processed...");
		return outputMap;
	}
	
	protected static double getAverageDryBiomassByTree(double kgM2, double nbTreesHa) {
		return kgM2 * 10d / nbTreesHa;		// 10: 0.001 from kg to Mg times 10000 m2/ha
	}

	@Override
	protected void respondToWindowClosing() {}	// Do nothing to avoid the automatic shut down

	/**
	 * This method returns the version of the lerfob-cat application.
	 * @return a String
	 */
	public String getRevision() {
		String lerfobRevision = LERFOBJARSVNAppVersion.getInstance().getName() + "; " + LERFOBJARSVNAppVersion.getInstance().getRevision();
		return lerfobRevision;
	}
	
	
	/**
	 * Start method for connection to Python.
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("Running on repicea " + REpiceaJARSVNAppVersion.getInstance().getRevision());
		System.out.println("Running on lerfob-foresttools " + LERFOBJARSVNAppVersion.getInstance().getRevision());
		List<String> argumentList = Arrays.asList(args);
		Integer listeningPort = null;
		Integer callbackPort = null;

		if (argumentList.contains(LISTEN) && argumentList.contains(CALLBACK)) {
			int indexListen = argumentList.indexOf(LISTEN) + 1;
			int indexCallback = argumentList.indexOf(CALLBACK) + 1;
			if (indexListen < argumentList.size() && indexCallback < argumentList.size()) {
				String newListeningPort = argumentList.get(indexListen);
				try {
					listeningPort = Integer.parseInt(newListeningPort);
					String newCallbackPort = argumentList.get(indexCallback);
					try {
						if (newCallbackPort.equals(newListeningPort)) {
							throw new InvalidParameterException("The callback and the listing ports should be different!");
						}
						callbackPort = Integer.parseInt(newCallbackPort);
					} catch (NumberFormatException e) {
						System.out.println("Unable to set callback port to " + newCallbackPort +".");
						System.out.println("The listening and callback ports will be set to default values 25333 and 25334 respectively!");
					} catch (InvalidParameterException e) {
						System.out.println(e.getMessage());
						System.out.println("The listening and callback ports will be set to default values 25333 and 25334 respectively!");
					}
				} catch (NumberFormatException e) {
					System.out.println("Unable to listen port " + newListeningPort +".");
					System.out.println("The listening and callback ports will be set to default values 25333 and 25334 respectively!");
				}
			}
		} 
		
		PythonAccessPoint pap = new PythonAccessPoint();
		
		GatewayServer gatewayServer;
		String portMessage;
		if (listeningPort != null && callbackPort != null) {
			portMessage = "ports " + listeningPort.toString() + " and " + callbackPort.toString() + "...";
			gatewayServer = new GatewayServer(pap, listeningPort, callbackPort, 0, 0, null);
		} else {
			portMessage = "default ports 25333 and 25334...";
			gatewayServer = new GatewayServer(pap); // default port
		}
		gatewayServer.start();
		System.out.println("Gateway Server started on " + portMessage);
	}
	
}
