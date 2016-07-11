package lerfob.carbonbalancetool.pythonaccess;

import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializer;
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
