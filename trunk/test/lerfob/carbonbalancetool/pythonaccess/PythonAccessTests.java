package lerfob.carbonbalancetool.pythonaccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import lerfob.carbonbalancetool.CATBasicWoodDensityProvider.AverageBasicDensity;
import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.CarbonUnitList;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.io.javacsv.CSVReader;
import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.covariateproviders.treelevel.SpeciesNameProvider.SpeciesType;
import repicea.simulation.covariateproviders.treelevel.TreeStatusProvider.StatusClass;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.treelogger.TreeLogger;
import repicea.simulation.treelogger.WoodPiece;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLogger;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerParameters;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLogger;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters;
import repicea.util.ObjectUtility;

public class PythonAccessTests {

	private static Map<?,?> InputMap;
	
	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testCompleteWithBeech() throws Exception {
		String refMapFilename = ObjectUtility.getPackagePath(PythonAccessTests.class) + "referenceBeech.ref";
		
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("beech");
		pap.setAreaHA(0.1);
		
		Map<Integer, Map<String, Double>> resultingMap = pap.processStandList("exampleBeech", getInputMap());
//		XmlSerializer serializer = new XmlSerializer(refMapFilename);
//		serializer.writeObject(resultingMap);
		
		Collection<Integer> yearsWithBiomass = new ArrayList<Integer>();
		yearsWithBiomass.add(2010);
		yearsWithBiomass.add(2011);
		yearsWithBiomass.add(2012);

		XmlDeserializer deserializer = new XmlDeserializer(refMapFilename);
		Map<?,?> refMap = (Map) deserializer.readObject();
		
		Assert.assertEquals(refMap.size(), resultingMap.size());
		int nbValuesCompared = 0;
		for (Integer key : resultingMap.keySet()) {
			Map<String, Double> innerResultingMap = resultingMap.get(key);
			Map<?,?> innerRefMap = (Map) refMap.get(key);
			if (innerRefMap == null) {
				Assert.fail();
			} else {
				Assert.assertEquals(innerRefMap.size(), innerResultingMap.size());
			}
			double totalBiomass = 0;
			for (String key2 : innerResultingMap.keySet()) {
				Double resultingValue = innerResultingMap.get(key2);
				Double refValue = (Double) innerRefMap.get(key2);
				if (refValue == null) {
					Assert.fail();
				} else {
					Assert.assertEquals("Comparing " + key2, refValue, resultingValue, 1E-8);
					nbValuesCompared++;
					if (key2.startsWith("BiomassMgHa") && !key2.contains("STUMPS") && !key2.contains("BRANCHES")) {
						totalBiomass += resultingValue;
					}
				}
			}
			System.out.println("Total biomass in HWP at year " + key + " - " + totalBiomass);
			if (yearsWithBiomass.contains(key)) {
				Assert.assertEquals("Comparing biomass for year " + key, 100, totalBiomass, 1E-8);
			}
		}
		System.out.println("Successfully compared this number of values: " + nbValuesCompared);
	}

	
	@Test
	public void testWithBeechProductionLinesOnly() throws Exception {
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("beech");
		pap.setAreaHA(0.1);
	
		ProductionProcessorManager manager = pap.getCarbonToolSettings().getCurrentProductionProcessorManager();
		AmountMap<Element> amountMap = new AmountMap<Element>();
		amountMap.put(Element.Volume, 100d);
		
		EuropeanBeechBasicTreeLoggerParameters loggerParams = new EuropeanBeechBasicTreeLogger().createDefaultTreeLoggerParameters();
		for (DiameterBasedTreeLogCategory logCategory : loggerParams.getLogCategoryList()) {
			manager.resetCarbonUnitMap();
			manager.processWoodPiece(logCategory, 0, "", amountMap);
			double volume = 0;
			for (CarbonUnitStatus type : CarbonUnitStatus.values()) {
				CarbonUnitList list = manager.getCarbonUnits(type);
				for (CarbonUnit	unit : list) {
					volume += unit.getAmountMap().get(Element.Volume);
				}
			}
			Assert.assertEquals("Comparing " + logCategory.getName(), 100d, volume, 1E-8);
			System.out.println("Comparing " + logCategory.getName() + " expected = " + 100 + " ; actual = " + volume);
		}
	}		


	@Test
	public void testWithPineProductionLinesOnly() throws Exception {
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("pine");
		pap.setAreaHA(0.1);
	
		ProductionProcessorManager manager = pap.getCarbonToolSettings().getCurrentProductionProcessorManager();
		AmountMap<Element> amountMap = new AmountMap<Element>();
		amountMap.put(Element.Volume, 100d);
		
		MaritimePineBasicTreeLoggerParameters loggerParams = new MaritimePineBasicTreeLogger().createDefaultTreeLoggerParameters();
		for (DiameterBasedTreeLogCategory logCategory : loggerParams.getLogCategoryList()) {
			manager.resetCarbonUnitMap();
			manager.processWoodPiece(logCategory, 0, "", amountMap);
			double volume = 0;
			for (CarbonUnitStatus type : CarbonUnitStatus.values()) {
				CarbonUnitList list = manager.getCarbonUnits(type);
				for (CarbonUnit	unit : list) {
					volume += unit.getAmountMap().get(Element.Volume);
				}
			}
			Assert.assertEquals("Comparing " + logCategory.getName(), 100d, volume, 1E-8);
			System.out.println("Comparing " + logCategory.getName() + " expected = " + 100 + " ; actual = " + volume);
		}
	}		


	@SuppressWarnings({ "rawtypes" })
	@Test
	public void testCompleteWithPine() throws Exception {
		String refMapFilename = ObjectUtility.getPackagePath(PythonAccessTests.class) + "referencePine.ref";
		
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("pine");
		pap.setAreaHA(0.1);
		
		Map<Integer, Map<String, Double>> resultingMap = pap.processStandList("examplePine", getInputMap());
//		XmlSerializer serializer = new XmlSerializer(refMapFilename);
//		serializer.writeObject(resultingMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(refMapFilename);
		Map<?,?> refMap = (Map) deserializer.readObject();
		
		Collection<Integer> yearsWithBiomass = new ArrayList<Integer>();
		yearsWithBiomass.add(2010);
		yearsWithBiomass.add(2011);
		yearsWithBiomass.add(2012);
		
		Assert.assertEquals(refMap.size(), resultingMap.size());
		int nbValuesCompared = 0;
		for (Integer key : resultingMap.keySet()) {
			Map<String, Double> innerResultingMap = resultingMap.get(key);
			Map<?,?> innerRefMap = (Map) refMap.get(key);
			if (innerRefMap == null) {
				Assert.fail();
			} else {
				Assert.assertEquals(innerRefMap.size(), innerResultingMap.size());
			}
			double totalBiomass = 0;
			for (String key2 : innerResultingMap.keySet()) {
				Double resultingValue = innerResultingMap.get(key2);
				Double refValue = (Double) innerRefMap.get(key2);
				if (refValue == null) {
					Assert.fail();
				} else {
					Assert.assertEquals("Comparing " + key2, refValue, resultingValue, 1E-8);
					nbValuesCompared++;
					if (key2.startsWith("BiomassMgHa") && !key2.contains("STUMPS") && !key2.contains("BRANCHES")) {
						totalBiomass += resultingValue;
					}
				}
			}
			System.out.println("Total biomass in HWP at year " + key + " - " + totalBiomass);
			if (yearsWithBiomass.contains(key)) {
				Assert.assertEquals("Comparing biomass for year " + key, 100, totalBiomass, 1E-8);
			}
		}
		System.out.println("Successfully compared this number of values: " + nbValuesCompared);
	}


	@Test
	public void testWithPineLoggingOnly() throws Exception {
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("pine");
		pap.setAreaHA(0.1);
	
		TreeLogger<?,?> manager = pap.getCarbonToolSettings().getTreeLogger();
		
		PythonMaritimePineTree tree = new PythonMaritimePineTree(SpeciesType.ConiferousSpecies, 
				AverageBasicDensity.MaritimePine,
				StatusClass.cut,
				1,
				PythonAccessPoint.getAverageDryBiomassByTree(0, .1),
				PythonAccessPoint.getAverageDryBiomassByTree(10d, .1),
				PythonAccessPoint.getAverageDryBiomassByTree(0, .1),
				45,
				7.45);

		Collection<PythonMaritimePineTree> trees = new ArrayList<PythonMaritimePineTree>();
		trees.add(tree);
		manager.init(trees);
		manager.run();
		double volume = 0;
		for (Collection<WoodPiece> woodPieces : manager.getWoodPieces().values()) {
			for (WoodPiece woodPiece : woodPieces) {
				volume += woodPiece.getWeightedVolumeM3();
			}
		}
		double biomass = volume * AverageBasicDensity.MaritimePine.getBasicDensity();
		Assert.assertEquals("Comparing logged biomasses", 1000d, biomass, 1E-8);
	}		

	@Test
	public void testWithBeechLoggingOnly() throws Exception {
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("beech");
		pap.setAreaHA(0.1);
	
		TreeLogger<?,?> manager = pap.getCarbonToolSettings().getTreeLogger();
		
		PythonEuropeanBeechTree tree = new PythonEuropeanBeechTree(SpeciesType.BroadleavedSpecies, 
				AverageBasicDensity.EuropeanBeech,
				StatusClass.cut,
				1,
				PythonAccessPoint.getAverageDryBiomassByTree(0, .1),
				PythonAccessPoint.getAverageDryBiomassByTree(10d, .1),
				PythonAccessPoint.getAverageDryBiomassByTree(0, .1),
				45,
				7.45);

		Collection<PythonEuropeanBeechTree> trees = new ArrayList<PythonEuropeanBeechTree>();
		trees.add(tree);
		manager.init(trees);
		manager.run();
		double volume = 0;
		for (Collection<WoodPiece> woodPieces : manager.getWoodPieces().values()) {
			for (WoodPiece woodPiece : woodPieces) {
				volume += woodPiece.getWeightedVolumeM3();
			}
		}
		double biomass = volume * AverageBasicDensity.EuropeanBeech.getBasicDensity();
		Assert.assertEquals("Comparing logged biomasses", 1000d, biomass, 1E-8);
	}		

	@SuppressWarnings("rawtypes")
	private static Map getInputMap() throws Exception {
		if (InputMap == null) {
			String filename = ObjectUtility.getPackagePath(PythonAccessTests.class) + "testMapPythonAccess.ref";
			XmlDeserializer deserializer = new XmlDeserializer(filename);
			InputMap = (Map) deserializer.readObject();
		}
		return InputMap;
	}
	

	@Test
	public void testWithRealDataFromGoPlusBeech() throws Exception {
		final String keyName = "RECOLTE";
		final String shortFilename = "Hetre_SKH810000-8025_new.csv";
		String refMapFilename = ObjectUtility.getPackagePath(PythonAccessTests.class) + shortFilename;

		CSVReader reader = null;
		List<String> harvestFieldnames = new ArrayList<String>();
		List<String> resultFieldnames = new ArrayList<String>();
		
		Map<Integer, Map<String, Map<String, Double>>> inputMap = new HashMap<Integer, Map<String, Map<String, Double>>>();
		
		try {
			reader = new CSVReader(refMapFilename);
			for (int i = 0; i < reader.getFieldCount(); i++) {
				String fieldName = reader.getHeader().getField(i).getName();
				if (fieldName.contains(keyName)) {
					harvestFieldnames.add(fieldName);
				} else  if (fieldName.contains("MgHa")) {
					resultFieldnames.add(fieldName);
				}
			}
			int nbTreesAtYearMinus1 = Integer.MIN_VALUE;
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				Map<String, Map<String, Double>> innerMap = new HashMap<String, Map<String, Double>>();
				int year = Integer.parseInt(record[0].toString());
				inputMap.put(year, innerMap);
				int nbTrees = Integer.parseInt(record[record.length - 1].toString());
				int diffTrees = 0;
				boolean isHarvested = false;
				if (nbTreesAtYearMinus1 != Integer.MIN_VALUE && nbTreesAtYearMinus1 > nbTrees) {
					diffTrees = nbTreesAtYearMinus1 - nbTrees;
					innerMap.put(keyName, new HashMap<String, Double>());
					isHarvested = true;
					innerMap.get(keyName).put("NbTrees", (double) diffTrees);
				}
				nbTreesAtYearMinus1 = nbTrees;

				if (isHarvested) {
					for (int i = 0; i < harvestFieldnames.size(); i ++) {
						String oldFieldname = harvestFieldnames.get(i);
						double value = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(oldFieldname)].toString());
						String newFieldname = oldFieldname.substring(oldFieldname.indexOf(keyName) + keyName.length() + 1);
						innerMap.get(keyName).put(newFieldname, value);
					}
				}
				
				innerMap.put("RESULTS", new HashMap<String, Double>());
				for (int i = 0; i < resultFieldnames.size(); i++) {
					String oldFieldname = resultFieldnames.get(i);
					String valueString = record[reader.getHeader().getIndexOfThisField(oldFieldname)].toString();
					if (valueString.isEmpty()) {
						valueString = "0";
					}
					if (oldFieldname.equals("CeqEmissionTransMgHa")) {
						oldFieldname = "CEqEmissionTransMgHa";
					}
					double value = Double.parseDouble(valueString);
					innerMap.get("RESULTS").put(oldFieldname, value);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("beech");
		
		pap.setAreaHA(0.1);
		
		Map<Integer, Map<String, Double>> resultingMap = pap.processStandList(shortFilename, inputMap);

		Assert.assertEquals("Testing map sizes", inputMap.size(), resultingMap.size());

		for (Integer year : inputMap.keySet()) {
			if (!resultingMap.containsKey(year)) {
				Assert.fail("The resulting map does not contain year " + year);
			} else {
				Map<String, Double> innerRefMap = inputMap.get(year).get("RESULTS");
				Map<String, Double> innerActualMap = resultingMap.get(year);
				Assert.assertEquals("Testing inner map sizes", innerRefMap.size(), innerActualMap.size());
				for (String key : innerRefMap.keySet()) {
					if (!innerActualMap.containsKey(key)) {
						Assert.fail("The inner resulting map does not contain key " + key);
					} else {
						double refValue = innerRefMap.get(key);
						double actualValue = innerActualMap.get(key);
						Assert.assertEquals("Testing value for key " + key, refValue, actualValue, 1E-8);
					}
				}
			}
		}
		
		System.out.println("Successfully compared file : " + shortFilename);
	}

	
	
	@Test
	public void testWithRealDataFromGoPlusPine() throws Exception {
		final String keyName = "RECOLTE";
		final String shortFilename = "PinMaritime_PCS300000-7345_new.csv";
		String refMapFilename = ObjectUtility.getPackagePath(PythonAccessTests.class) + shortFilename;

		CSVReader reader = null;
		List<String> harvestFieldnames = new ArrayList<String>();
		List<String> resultFieldnames = new ArrayList<String>();
		
		Map<Integer, Map<String, Map<String, Double>>> inputMap = new HashMap<Integer, Map<String, Map<String, Double>>>();
		
		try {
			reader = new CSVReader(refMapFilename);
			for (int i = 0; i < reader.getFieldCount(); i++) {
				String fieldName = reader.getHeader().getField(i).getName();
				if (fieldName.contains(keyName)) {
					harvestFieldnames.add(fieldName);
				} else  if (fieldName.contains("MgHa")) {
					resultFieldnames.add(fieldName);
				}
			}
			int nbTreesAtYearMinus1 = Integer.MIN_VALUE;
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				Map<String, Map<String, Double>> innerMap = new HashMap<String, Map<String, Double>>();
				int year = Integer.parseInt(record[0].toString());
				inputMap.put(year, innerMap);
				int nbTrees = Integer.parseInt(record[record.length - 1].toString());
				int diffTrees = 0;
				boolean isHarvested = false;
				if (nbTreesAtYearMinus1 != Integer.MIN_VALUE && nbTreesAtYearMinus1 > nbTrees) {
					diffTrees = nbTreesAtYearMinus1 - nbTrees;
					innerMap.put(keyName, new HashMap<String, Double>());
					isHarvested = true;
					innerMap.get(keyName).put("NbTrees", (double) diffTrees);
				}
				nbTreesAtYearMinus1 = nbTrees;

				if (isHarvested) {
					for (int i = 0; i < harvestFieldnames.size(); i ++) {
						String oldFieldname = harvestFieldnames.get(i);
						double value = Double.parseDouble(record[reader.getHeader().getIndexOfThisField(oldFieldname)].toString());
						String newFieldname = oldFieldname.substring(oldFieldname.indexOf(keyName) + keyName.length() + 1);
						innerMap.get(keyName).put(newFieldname, value);
					}
				}
				
				innerMap.put("RESULTS", new HashMap<String, Double>());
				for (int i = 0; i < resultFieldnames.size(); i++) {
					String oldFieldname = resultFieldnames.get(i);
					String valueString = record[reader.getHeader().getIndexOfThisField(oldFieldname)].toString();
					if (valueString.isEmpty()) {
						valueString = "0";
					}
					if (oldFieldname.equals("CeqEmissionTransMgHa")) {
						oldFieldname = "CEqEmissionTransMgHa";
					}
					double value = Double.parseDouble(valueString);
					innerMap.get("RESULTS").put(oldFieldname, value);
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("pine");
		
		pap.setAreaHA(0.1);
		
		Map<Integer, Map<String, Double>> resultingMap = pap.processStandList(shortFilename, inputMap);

		Assert.assertEquals("Testing map sizes", inputMap.size(), resultingMap.size());

		for (Integer year : inputMap.keySet()) {
			if (!resultingMap.containsKey(year)) {
				Assert.fail("The resulting map does not contain year " + year);
			} else {
				Map<String, Double> innerRefMap = inputMap.get(year).get("RESULTS");
				Map<String, Double> innerActualMap = resultingMap.get(year);
				Assert.assertEquals("Testing inner map sizes", innerRefMap.size(), innerActualMap.size());
				for (String key : innerRefMap.keySet()) {
					if (!innerActualMap.containsKey(key)) {
						Assert.fail("The inner resulting map does not contain key " + key);
					} else {
						double refValue = innerRefMap.get(key);
						double actualValue = innerActualMap.get(key);
						Assert.assertEquals("Testing value for key " + key, refValue, actualValue, 1E-8);
					}
				}
			}
		}
		
		System.out.println("Successfully compared file : " + shortFilename);
	}

	
}
