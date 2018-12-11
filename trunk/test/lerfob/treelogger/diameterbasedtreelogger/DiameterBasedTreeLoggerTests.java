package lerfob.treelogger.diameterbasedtreelogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.ObjectUtility;

public class DiameterBasedTreeLoggerTests {

	
	@SuppressWarnings("rawtypes")
	@Test
	public void testForLargeTree() throws Exception {
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "testLargeTree.xml";
		DiameterBasedTreeLogger logger = new DiameterBasedTreeLogger();
		logger.setTreeLoggerParameters(logger.createDefaultTreeLoggerParameters());
		
		DiameterBasedLoggableTree tree = new DiameterBasedLoggableTreeImpl(60d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedVolumeM3());
		}
		
		double volume = 0d;
		for (double vol : obsMap.values()) {
			volume += vol;
		}
		Assert.assertEquals("Test volume balance for a large oak", 1d, volume, 1E-8);

		//		UNCOMMENT TO UPDATE THE RESULTS OF THE TEST
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(obsMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(refFilename);
		Map refMap = (Map) deserializer.readObject();
		
		for (String key : obsMap.keySet()) {
			double expected = (Double) refMap.get(key);
			double actual = obsMap.get(key);
			Assert.assertEquals("Testing " + key, expected, actual, 1E-8);
		}
	}

	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testForMediumTree() throws Exception {
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "testMediumTree.xml";
		DiameterBasedTreeLogger logger = new DiameterBasedTreeLogger();
		logger.setTreeLoggerParameters(logger.createDefaultTreeLoggerParameters());
		
		DiameterBasedLoggableTree tree = new DiameterBasedLoggableTreeImpl(30d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedVolumeM3());
		}
		
		double volume = 0d;
		for (double vol : obsMap.values()) {
			volume += vol;
		}
		Assert.assertEquals("Test volume balance for a large oak", 1d, volume, 1E-8);

		//		UNCOMMENT TO UPDATE THE RESULTS OF THE TEST
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(obsMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(refFilename);
		Map refMap = (Map) deserializer.readObject();
		
		for (String key : obsMap.keySet()) {
			double expected = (Double) refMap.get(key);
			double actual = obsMap.get(key);
			Assert.assertEquals("Testing " + key, expected, actual, 1E-8);
		}
	}

	
	@SuppressWarnings("rawtypes")
	@Test
	public void testForSmallTree() throws Exception {
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "testSmallTree.xml";
		DiameterBasedTreeLogger logger = new DiameterBasedTreeLogger();
		logger.setTreeLoggerParameters(logger.createDefaultTreeLoggerParameters());
		
		DiameterBasedLoggableTree tree = new DiameterBasedLoggableTreeImpl(15d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedVolumeM3());
		}
		
		double volume = 0d;
		for (double vol : obsMap.values()) {
			volume += vol;
		}
		Assert.assertEquals("Test volume balance for a large oak", 1d, volume, 1E-8);

		//		UNCOMMENT TO UPDATE THE RESULTS OF THE TEST
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(obsMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(refFilename);
		Map refMap = (Map) deserializer.readObject();
		
		for (String key : obsMap.keySet()) {
			double expected = (Double) refMap.get(key);
			double actual = obsMap.get(key);
			Assert.assertEquals("Testing " + key, expected, actual, 1E-8);
		}
	}

	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testForSmallTreeWithSerialization() throws Exception {
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "testSmallTree.xml";
		String loggerFilename = ObjectUtility.getPackagePath(getClass()) + "dbTreeLogger.xml";
		DiameterBasedTreeLogger logger = new DiameterBasedTreeLogger();
//		UNCOMMENT THIS PART TO SAVE A NEW TreeLoggerParameters instance
//		logger.setTreeLoggerParameters(logger.createDefaultTreeLoggerParameters());
//		logger.getTreeLoggerParameters().save(loggerFilename);
		DiameterBasedTreeLoggerParameters params = new DiameterBasedTreeLoggerParameters();
		params.getSpeciesLogCategories(TreeLoggerParameters.ANY_SPECIES).clear();
		params.load(loggerFilename);
		logger.setTreeLoggerParameters(params);
		
		
		DiameterBasedLoggableTree tree = new DiameterBasedLoggableTreeImpl(15d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedVolumeM3());
		}
		
		double volume = 0d;
		for (double vol : obsMap.values()) {
			volume += vol;
		}
		Assert.assertEquals("Test volume balance for a large oak", 1d, volume, 1E-8);

		//		UNCOMMENT TO UPDATE THE RESULTS OF THE TEST
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(obsMap);
		
		XmlDeserializer deserializer = new XmlDeserializer(refFilename);
		Map refMap = (Map) deserializer.readObject();
		
		for (String key : obsMap.keySet()) {
			double expected = (Double) refMap.get(key);
			double actual = obsMap.get(key);
			Assert.assertEquals("Testing " + key, expected, actual, 1E-8);
		}
	}

}
