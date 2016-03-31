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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import lerfob.app.LERFOBJARSVNAppVersion;
import lerfob.carbonbalancetool.BasicWoodDensityProvider.AverageBasicDensity;
import lerfob.carbonbalancetool.CarbonAssessmentToolSimulationResult;
import lerfob.carbonbalancetool.CarbonCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.CarbonToolCompatibleStand;
import lerfob.carbonbalancetool.LERFoBCarbonAccountingTool;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.EndUseWoodProductCarbonUnitFeature.UseClass;
import py4j.GatewayServer;
import repicea.app.REpiceaJARSVNAppVersion;
import repicea.math.Matrix;
import repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider.SpeciesType;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.treelogger.TreeLoggerDescription;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.treelogger.basictreelogger.BasicTreeLogger;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLogger;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLogger;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaSystem;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

/**
 * This class is the entry point for a coupling with a Python application. Once exported as a .jar with the main function set to this
 * class, the entry point should use the method processStandList(Map myMap).
 * @author Mathieu Fortin - May 2014
 */
public class PythonAccessPoint extends LERFoBCarbonAccountingTool {

	protected AverageBasicDensity speciesForSimulation; 
	protected double areaHa = 1d;
	
	public PythonAccessPoint() {
		super();
		initializeTool(false, null);
	}
	
	/**
	 * This method sets the species for the simulation
	 * @param species a String either "beech" or "pine"
	 * @throws IOException 
	 */
	public void setSpecies(String species) throws IOException {
		AverageBasicDensity speciesCode;
		if (species.toLowerCase().trim().equals("beech")) {
			speciesCode = AverageBasicDensity.EuropeanBeech;
		} else if (species.toLowerCase().trim().equals("pine")) {
			speciesCode = AverageBasicDensity.MaritimePine;
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
	protected void setTreeLoggerDescription(Object referent) {
		Vector<TreeLoggerDescription> defaultTreeLoggerDescriptions = new Vector<TreeLoggerDescription>();
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(BasicTreeLogger.class.getName()));
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(MaritimePineBasicTreeLogger.class.getName()));
		defaultTreeLoggerDescriptions.add(new TreeLoggerDescription(EuropeanBeechBasicTreeLogger.class.getName()));
		getCarbonToolSettings().setTreeLoggerDescriptions(defaultTreeLoggerDescriptions);
	}

	private void setSpeciesAndSettings(AverageBasicDensity speciesCode) throws IOException {
		if (!speciesCode.equals(speciesForSimulation)) {
			speciesForSimulation = speciesCode;
			String filename;
			if (speciesForSimulation.equals(AverageBasicDensity.MaritimePine)) {
				filename = ObjectUtility.getRelativePackagePath(getClass()) + "maritimepine.prl";
			} else {
				filename = ObjectUtility.getRelativePackagePath(getClass()) + "europeanbeech.prl";;
			}
			System.out.println("Loading settings : " + filename);
			getCarbonToolSettings().getCurrentProductionProcessorManager().load(filename);
		}
	}

	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map<Integer, Map<String, Double>> processStandList(String standID, Map inputMap) throws Exception {
		final String keyFirstInnerMap = "RECOLTE";
		List<CarbonToolCompatibleStand> standList = new ArrayList<CarbonToolCompatibleStand>();
		PythonCarbonToolCompatibleStand stand;
		PythonCarbonToolCompatibleTree tree;
		SpeciesType type;
		if (speciesForSimulation == AverageBasicDensity.EuropeanBeech) {
			type = SpeciesType.BroadleavedSpecies;
		} else  if (speciesForSimulation == AverageBasicDensity.MaritimePine) {
			type = SpeciesType.ConiferousSpecies;
		} else {
			throw new InvalidParameterException("The species has not been properly set!");
		}
		
		List<Integer> years = new ArrayList<Integer>();
		years.addAll(inputMap.keySet());
		Collections.sort(years);

		for (Integer dateYr : years) {
			Map innerMap = (Map) ((Map) inputMap.get(dateYr)).get(keyFirstInnerMap);
			
			stand = new PythonCarbonToolCompatibleStand(areaHa, standID, dateYr);
			standList.add(stand);
			
			if (innerMap != null) {
				double nbTreesHa = Double.parseDouble(innerMap.get("NbTrees").toString());
				double mqd = Double.parseDouble(innerMap.get("DBHmy").toString());
				double weightCrownKg_M2 = Double.parseDouble(innerMap.get("Wcrown").toString());
				double weightTrunkKg_M2 = Double.parseDouble(innerMap.get("Wtrunk").toString());
				double dbhStandardDeviation = Double.parseDouble(innerMap.get("DBHect").toString());
				double weightRootsKg_M2 = Double.parseDouble(innerMap.get("Wroots").toString());
				boolean isProcessable = weightCrownKg_M2 >= 0d && weightTrunkKg_M2 >= 0d && weightRootsKg_M2 >= 0d && mqd > 0;
				if (isProcessable) {
					double nbTrees = nbTreesHa * stand.getAreaHa();
					
					if (speciesForSimulation == AverageBasicDensity.MaritimePine) {
						tree = new PythonMaritimePineTree(type, 
								speciesForSimulation,
								StatusClass.cut,
								mqd,
								dbhStandardDeviation,
								nbTrees,
								getAverageDryBiomassByTree(weightRootsKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightTrunkKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightCrownKg_M2, nbTreesHa));
						stand.addTree(StatusClass.cut, tree);
					} else {
						tree = new PythonEuropeanBeechTree(type, 
								speciesForSimulation,
								StatusClass.cut,
								mqd,
								dbhStandardDeviation,
								nbTrees,
								getAverageDryBiomassByTree(weightRootsKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightTrunkKg_M2, nbTreesHa),
								getAverageDryBiomassByTree(weightCrownKg_M2, nbTreesHa));
						stand.addTree(StatusClass.cut, tree);
					}
				}
			}
		}
		
		setStandList(standList);
		calculateCarbon();
		CarbonAssessmentToolSimulationResult simulationResult = getCarbonCompartmentManager().getSimulationSummary();
		Map<Integer, Map<UseClass, Map<Element, MonteCarloEstimate>>> productEvolutionMap = simulationResult.getProductEvolutionPerHa();
		
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
			Map<UseClass, Map<Element, MonteCarloEstimate>> innerInputMap2 = productEvolutionMap.get(year);
			for (UseClass useClass : UseClass.values()) {
				String key = "BiomassMgHa" + useClass.name().toUpperCase();
				if (innerInputMap2 != null && innerInputMap2.containsKey(useClass)) {
					Map<Element, MonteCarloEstimate> amountMap = innerInputMap2.get(useClass);
					innerOutputMap1.put(key, amountMap.get(Element.Biomass).getMean().m_afData[0][0]);
				} else {
					innerOutputMap1.put(key, 0d);
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
	
	private double getAverageDryBiomassByTree(double kgM2, double nbTreesHa) {
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
	 */
	public static void main(String[] args) {
		String repiceaRevision = REpiceaJARSVNAppVersion.getInstance().getName() + "; " + REpiceaJARSVNAppVersion.getInstance().getRevision();
		System.out.println(repiceaRevision);
		String lerfobRevision = LERFOBJARSVNAppVersion.getInstance().getName() + "; " + LERFOBJARSVNAppVersion.getInstance().getRevision();
		System.out.println(lerfobRevision);
		String inputString = "";
		for (String str : args) {
			inputString = inputString + str + "; ";
		}
		System.out.println("Parameters received:" + inputString);
		REpiceaSystem.setLanguageFromMain(args, Language.English);
		System.out.println("Language set to: " + REpiceaTranslator.getCurrentLanguage().name());
		PythonAccessPoint pap = new PythonAccessPoint();
//		pap.showInterface();
		GatewayServer gatewayServer = new GatewayServer(pap);
		gatewayServer.start();
		System.out.println("Gateway Server Started");
	}
	
}
