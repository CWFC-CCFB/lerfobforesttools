/*
 * This file is part of the lerfob-forestools library.
 *
 * Copyright (C) 2010-2012 Mathieu Fortin for LERFOB INRA/AgroParisTech, 
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
package lerfob.treelogger.diameterbasedtreelogger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import repicea.serial.xml.XmlDeserializer;
import repicea.simulation.treelogger.TreeLoggerParameters;
import repicea.simulation.treelogger.WoodPiece;
import repicea.util.ObjectUtility;
import repicea.util.REpiceaTranslator;
import repicea.util.REpiceaTranslator.Language;

public class DiameterBasedTreeLoggerTest {

	private static Language languageBefore;
	
	@BeforeClass
	public static void doThisBefore() {
		languageBefore = REpiceaTranslator.getCurrentLanguage();
		REpiceaTranslator.setCurrentLanguage(Language.English);
	}
	
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testForLargeTree() throws Exception {
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "testLargeTree.xml";
		DiameterBasedTreeLogger logger = new DiameterBasedTreeLogger();
		logger.setTreeLoggerParameters(logger.createDefaultTreeLoggerParameters());
		
		DiameterBasedLoggableTreeImpl tree = new DiameterBasedLoggableTreeImpl(60d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedTotalVolumeM3());
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
		
		DiameterBasedLoggableTreeImpl tree = new DiameterBasedLoggableTreeImpl(30d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedTotalVolumeM3());
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
		
		DiameterBasedLoggableTreeImpl tree = new DiameterBasedLoggableTreeImpl(15d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedTotalVolumeM3());
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
		
		
		DiameterBasedLoggableTreeImpl tree = new DiameterBasedLoggableTreeImpl(15d);
		logger.logThisTree(tree);

		Collection<WoodPiece> woodPieces = logger.getWoodPieces().get(tree);
		Map<String, Double> obsMap = new HashMap<String, Double>();
		for (WoodPiece wp : woodPieces) {
			String logCatName = wp.getLogCategory().getName();
			if (!obsMap.containsKey(logCatName)) {
				obsMap.put(logCatName, 0d);
			}
			obsMap.put(logCatName, obsMap.get(logCatName) + wp.getWeightedTotalVolumeM3());
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

	@AfterClass
	public static void doThatAfter() {
		REpiceaTranslator.setCurrentLanguage(languageBefore);
	}

}
