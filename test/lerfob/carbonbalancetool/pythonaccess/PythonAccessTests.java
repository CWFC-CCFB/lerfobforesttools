package lerfob.carbonbalancetool.pythonaccess;

import java.util.Map;

import junit.framework.Assert;
import lerfob.carbonbalancetool.productionlines.CarbonUnit;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.CarbonUnitList;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;

import org.junit.Test;

import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.processsystem.AmountMap;
import repicea.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLogger;
import repicea.treelogger.europeanbeech.EuropeanBeechBasicTreeLoggerParameters;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLogger;
import repicea.treelogger.maritimepine.MaritimePineBasicTreeLoggerParameters;
import repicea.util.ObjectUtility;

public class PythonAccessTests {

	private static Map<?,?> InputMap;
	
	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Test
	public void simpleTestWithBeech() throws Exception {
		String refMapFilename = ObjectUtility.getPackagePath(PythonAccessTests.class) + "referenceBeech.ref";
		
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("beech");
		pap.setAreaHA(0.1);
		
		Map<Integer, Map<String, Double>> resultingMap = pap.processStandList("exampleBeech", getInputMap());
//		XmlSerializer serializer = new XmlSerializer(refMapFilename);
//		serializer.writeObject(resultingMap);
		
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
		}
		System.out.println("Successfully compared this number of values: " + nbValuesCompared);
	}

	
	@SuppressWarnings({"deprecation"})
	@Test
	public void completeTestWithBeechProductionLines() throws Exception {
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("beech");
		pap.setAreaHA(0.1);
	
		ProductionProcessorManager manager = pap.getCarbonToolSettings().getCurrentProductionProcessorManager();
		AmountMap<Element> amountMap = new AmountMap<Element>();
		amountMap.put(Element.Volume, 100d);
		
		EuropeanBeechBasicTreeLoggerParameters loggerParams = new EuropeanBeechBasicTreeLogger().createDefaultTreeLoggerParameters();
		for (DiameterBasedTreeLogCategory logCategory : loggerParams.getLogCategoryList()) {
			manager.resetCarbonUnitMap();
			manager.processWoodPiece(logCategory, 0, amountMap);
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


	@SuppressWarnings({"deprecation"})
	@Test
	public void completeTestWithPineProductionLines() throws Exception {
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("pine");
		pap.setAreaHA(0.1);
	
		ProductionProcessorManager manager = pap.getCarbonToolSettings().getCurrentProductionProcessorManager();
		AmountMap<Element> amountMap = new AmountMap<Element>();
		amountMap.put(Element.Volume, 100d);
		
		MaritimePineBasicTreeLoggerParameters loggerParams = new MaritimePineBasicTreeLogger().createDefaultTreeLoggerParameters();
		for (DiameterBasedTreeLogCategory logCategory : loggerParams.getLogCategoryList()) {
			manager.resetCarbonUnitMap();
			manager.processWoodPiece(logCategory, 0, amountMap);
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


	@SuppressWarnings({ "rawtypes", "deprecation" })
	@Test
	public void testWithPine() throws Exception {
		String refMapFilename = ObjectUtility.getPackagePath(PythonAccessTests.class) + "referencePine.ref";
		
		PythonAccessPoint pap = new PythonAccessPoint();
		pap.setSpecies("pine");
		pap.setAreaHA(0.1);
		
		Map<Integer, Map<String, Double>> resultingMap = pap.processStandList("examplePine", getInputMap());
//		XmlSerializer serializer = new XmlSerializer(refMapFilename);
//		serializer.writeObject(resultingMap);
		
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
		}
		System.out.println("Successfully compared this number of values: " + nbValuesCompared);
	}

	@SuppressWarnings("rawtypes")
	private static Map getInputMap() throws Exception {
		if (InputMap == null) {
//			String filename = ObjectUtility.getPackagePath(PythonAccessTests.class) + "referenceMapPythonAccess.ref";
			String filename = ObjectUtility.getPackagePath(PythonAccessTests.class) + "testMapPythonAccess.ref";
			XmlDeserializer deserializer = new XmlDeserializer(filename);
			InputMap = (Map) deserializer.readObject();
		}
		return InputMap;
	}
}
