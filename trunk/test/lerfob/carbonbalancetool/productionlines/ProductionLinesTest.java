package lerfob.carbonbalancetool.productionlines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import lerfob.carbonbalancetool.productionlines.CarbonUnit.BiomassType;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;
import lerfob.carbonbalancetool.productionlines.WoodyDebrisProcessor.WoodyDebrisProcessorID;
import repicea.simulation.processsystem.AmountMap;
import repicea.simulation.processsystem.ProcessUnit;
import repicea.simulation.processsystem.Processor;
import repicea.util.ObjectUtility;

@SuppressWarnings("deprecation")
public class ProductionLinesTest {

	/**
	 * Tests if a production line file can be read successfully.
	 */
	@Test
	public void testProductionLineDeserialisation() {
		try {
			String filename = ObjectUtility.getPackagePath(getClass()) + "oakProductionLines20121112.prl";
			ProductionLineManager wpmm = new ProductionLineManager();
			wpmm.load(filename);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Tests if the amount in is equal to the amount out
	 */
	@Test
	public void testBiomassBalanceInProductionLines() {
		try {
			String filename = ObjectUtility.getPackagePath(getClass()) + "oakProductionLines20121112.prl";
			ProductionLineManager wpmm = new ProductionLineManager();
			wpmm.load(filename);

			double volume = 1d;
			double carbonContent = .5;
			double basicWoodDensity = .5;

			AmountMap<Element> amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);

			for (String productionLine : wpmm.getProductionLineNames()) {
				System.out.println("Testing " + productionLine);
				wpmm.resetCarbonUnitMap();
				wpmm.processWoodPiece(productionLine, 2010, amountMap);
				CarbonUnitList list = new CarbonUnitList();
				for (CarbonUnitStatus type : CarbonUnitStatus.values()) {
					list.addAll(wpmm.getCarbonUnits(type));
				}

				double totalVolume = 0d;
				for (CarbonUnit unit : list) {
					totalVolume += unit.getAmountMap().get(Element.Volume);
				}

				Assert.assertEquals("Test for production line : " + productionLine,
						volume, 
						totalVolume, 
						1E-12);


			}			

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 

	}

	/**
	 * Tests if the amount in is equal to the amount out
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void testBiomassBalanceInProductionLinesWithFormerNewImplementation() {
		try {
			String filename = ObjectUtility.getPackagePath(getClass()) + "oakProductionLines20121112.prl";
			ProductionLineManager wpmm = new ProductionLineManager();
			wpmm.load(filename);

			double volume = 100d;
			double carbonContent = .5;
			double basicWoodDensity = .5;

			AmountMap<Element> amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);
			
			CarbonUnit carbonUnit = new CarbonUnit(2013, "", null, amountMap, "Unknown", BiomassType.Wood);
			
			int index = wpmm.getProductionLineNames().indexOf("Sawing");
			if (index == -1) {
				throw new Exception("This production line does not exist : " + "Sawing");
			} else {
				ProductionLine model = wpmm.getContent().get(index);
				Processor processor = model.getPrimaryProcessor();
				List<ProcessUnit> processUnits = new ArrayList<ProcessUnit>();
				processUnits.add(carbonUnit);
				Collection<ProcessUnit> outputUnits = processor.doProcess(processUnits);
				
				double totalVolume = 0d;
				for (ProcessUnit unit : outputUnits) {
					totalVolume += ((CarbonUnit) unit).getAmountMap().get(Element.Volume);
				}

				Assert.assertEquals("Test for production line : " + "Sawing",
						volume, 
						totalVolume, 
						1E-12);
			}			

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 


	}


	/**
	 * Tests if the amount in is equal to the amount out
	 */
	@Test
	public void testBiomassBalanceInProductionLinesWithNewImplementation() {
		try {
			String filename = ObjectUtility.getRelativePackagePath(ProductionProcessorManager.class) + "library" + ObjectUtility.PathSeparator + "hardwood_simple_en.prl";
			ProductionProcessorManager processorManager = new ProductionProcessorManager();
			processorManager.load(filename);

			double volume = 100d;
			double carbonContent = .5;
			double basicWoodDensity = .5;

			AmountMap<Element> amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);
			Map<BiomassType, AmountMap<Element>> amountMaps = new HashMap<BiomassType, AmountMap<Element>>();
			amountMaps.put(BiomassType.Wood, amountMap);

			List<Processor> processors = processorManager.getPrimaryProcessors();
			int i = processors.size() - 1;
			LogCategoryProcessor sawingProcessor = null;
			while (i >= 0) {
				Processor p = processors.get(i);
				if (p instanceof LogCategoryProcessor) {
					sawingProcessor = (LogCategoryProcessor) p;
					break;
				}
				i--;
			}

//			Collection<CarbonUnit> endProducts = processorManager.processWoodPiece(sawingProcessor.logCategory, 2015, amountMap);
			processorManager.processWoodPiece(sawingProcessor.logCategory, 2015, "", amountMaps, "Unknown");
			Collection<CarbonUnit> endProducts = new ArrayList<CarbonUnit>();
			for (CarbonUnitStatus status : CarbonUnitStatus.values()) {
				endProducts.addAll(processorManager.getCarbonUnits(status));
			}
			double totalVolume = 0d;
			for (CarbonUnit unit : endProducts) {
				totalVolume += unit.getAmountMap().get(Element.Volume);
			}

			Assert.assertEquals("Test for production line : " + "Sawing",
					volume, 
					totalVolume, 
					1E-12);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 

	}

	
	
	private static void testBiomassBalanceInProductionLinesWithNewImplementationAndDebarkingUsingThisFile(String filename) {
		try {
			ProductionProcessorManager processorManager = new ProductionProcessorManager();
			processorManager.load(filename);

			double volume = 100d;
			double carbonContent = .5;
			double basicWoodDensity = .5;
			double barkProportion = 0.11;
			AmountMap<Element> amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume * (1-barkProportion));
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);
			Map<BiomassType, AmountMap<Element>> amountMaps = new HashMap<BiomassType, AmountMap<Element>>();
			amountMaps.put(BiomassType.Wood, amountMap);

			amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume * (barkProportion));
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);
			amountMaps.put(BiomassType.Bark, amountMap);

			List<Processor> processors = processorManager.getPrimaryProcessors();
			List<LogCategoryProcessor> logCategoryProcessors = (List) processors.stream()
					.filter(l -> l instanceof LogCategoryProcessor)	// we keep only the entries with values set to true
					.collect(Collectors.toList());

			for (LogCategoryProcessor logCategoryProcessor : logCategoryProcessors) {
				processorManager.resetCarbonUnitMap();
				processorManager.validate();
				processorManager.processWoodPiece(logCategoryProcessor.logCategory, 2015, "", amountMaps, "Unknown");			
				Collection<CarbonUnit> endProducts = new ArrayList<CarbonUnit>();
				for (CarbonUnitStatus status : CarbonUnitStatus.values()) {
					endProducts.addAll(processorManager.getCarbonUnits(status));
				}
				double totalVolumeWood = 0d;
				double totalVolumeBark = 0d;
				for (CarbonUnit unit : endProducts) {
					if (unit.getBiomassType() == BiomassType.Wood) {
						totalVolumeWood += unit.getAmountMap().get(Element.Volume);
					} else {
						totalVolumeBark += unit.getAmountMap().get(Element.Volume);
					}
				}

				Assert.assertEquals("Test for production line : " + "Sawing",
						volume * (1 - barkProportion), 
						totalVolumeWood, 
						1E-12);
				Assert.assertEquals("Test for production line : " + "Sawing",
						volume * (barkProportion), 
						totalVolumeBark, 
						1E-12);
			}

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 

	}
	
	
	/**
	 * Tests if the amount in is equal to the amount out
	 */
	@Test
	public void testBiomassBalanceInProductionLinesWithNewImplementationAndDebarking() {
		String filename = ObjectUtility.getPackagePath(getClass()) + "testHardwoodSimpleWithDebarking.prl";
		testBiomassBalanceInProductionLinesWithNewImplementationAndDebarkingUsingThisFile(filename);
	}

	/**
	 * Testing Quebec industrial sector 
	 */
	@Test
	public void testBiomassBalanceInProductionLinesOfQuebecIndustrialSector() {
		String filename = ObjectUtility.getPackagePath(getClass()) + "QuebecIndustrialSectorReference.prl";
		testBiomassBalanceInProductionLinesWithNewImplementationAndDebarkingUsingThisFile(filename);
	}

	
	/**
	 * Tests if the amount in is equal to the amount out
	 */
	@Test
	public void testBiomassBalanceInWoodyDebris() {
		try {
			String filename = ObjectUtility.getPackagePath(getClass()) + "testHardwoodSimpleWithDebarking.prl";
			ProductionProcessorManager processorManager = new ProductionProcessorManager();
			processorManager.load(filename);

			double volume = 100d;
			double carbonContent = .5;
			double basicWoodDensity = .5;
			double barkProportion = 0.11;
			AmountMap<Element> amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume * (1-barkProportion));
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);
			Map<BiomassType, AmountMap<Element>> amountMaps = new HashMap<BiomassType, AmountMap<Element>>();
			amountMaps.put(BiomassType.Wood, amountMap);

			amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume * (barkProportion));
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);
			amountMaps.put(BiomassType.Bark, amountMap);

			processorManager.processWoodyDebris(2015, "", amountMaps, "Unknown", WoodyDebrisProcessorID.CommercialWoodyDebris);
			Collection<CarbonUnit> endProducts = new ArrayList<CarbonUnit>();
			for (CarbonUnitStatus status : CarbonUnitStatus.values()) {
				endProducts.addAll(processorManager.getCarbonUnits(status));
			}
			double totalVolumeWood = 0d;
			double totalVolumeBark = 0d;
			for (CarbonUnit unit : endProducts) {
				if (unit.getBiomassType() == BiomassType.Wood) {
					totalVolumeWood += unit.getAmountMap().get(Element.Volume);
				} else {
					totalVolumeBark += unit.getAmountMap().get(Element.Volume);
				}
			}

			Assert.assertEquals("Test for production line : " + "Sawing",
					volume * (1 - barkProportion), 
					totalVolumeWood, 
					1E-12);
			Assert.assertEquals("Test for production line : " + "Sawing",
					volume * (barkProportion), 
					totalVolumeBark, 
					1E-12);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 
	}

	/**
	 * Tests if the amount in is equal to the amount out
	 */
	@Test
	public void testCumulativeEmissionsWithValueForEachProcess() {
		try {
			String filename = ObjectUtility.getPackagePath(getClass()) + File.separator
					+ "testHardwood_simple_enWithEmissions.prl";
			ProductionProcessorManager processorManager = new ProductionProcessorManager();
			processorManager.load(filename);

			double volume = 100d;
			double carbonContent = .5;
			double basicWoodDensity = .5;

			AmountMap<Element> amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);
			Map<BiomassType, AmountMap<Element>> amountMaps = new HashMap<BiomassType, AmountMap<Element>>();
			amountMaps.put(BiomassType.Wood, amountMap);
			
			List<Processor> processors = processorManager.getPrimaryProcessors();
			int i = processors.size() - 1;
			LogCategoryProcessor sawingProcessor = null;
			while (i >= 0) {
				Processor p = processors.get(i);
				if (p instanceof LogCategoryProcessor) {
					sawingProcessor = (LogCategoryProcessor) p;
					break;
				}
				i--;
			}
//			Collection<CarbonUnit> endProducts = processorManager.processWoodPiece(sawingProcessor.logCategory, 2015, amountMap);
			processorManager.processWoodPiece(sawingProcessor.logCategory, 2015, "", amountMaps, "Unknown");
				
			Collection<CarbonUnit> endProducts = new ArrayList<CarbonUnit>();
			for (CarbonUnitStatus status : CarbonUnitStatus.values()) {
				endProducts.addAll(processorManager.getCarbonUnits(status));
			}

			double actualEmissions = 0d;
			for (CarbonUnit unit : endProducts) {
				actualEmissions += unit.getAmountMap().get(Element.EmissionsCO2Eq);
			}

			double expectedEmissions = 3.5;
			Assert.assertEquals("Test for production line : " + "Sawing",
					expectedEmissions, 
					actualEmissions, 
					1E-12);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		} 

	}

	
	
}
