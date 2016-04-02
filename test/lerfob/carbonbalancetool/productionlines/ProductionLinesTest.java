package lerfob.carbonbalancetool.productionlines;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.CarbonUnitStatus;
import lerfob.carbonbalancetool.productionlines.CarbonUnit.Element;

import org.junit.Test;

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

			CarbonUnit carbonUnit = new CarbonUnit(2013, null, amountMap);
			
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
			String filename = ObjectUtility.getPackagePath(getClass()) + File.separator 
					+ "library" + File.separator 
					+ "hardwood_simple_en.prl";
			ProductionProcessorManager processorManager = new ProductionProcessorManager();
			processorManager.load(filename);

			double volume = 100d;
			double carbonContent = .5;
			double basicWoodDensity = .5;

			AmountMap<Element> amountMap = new AmountMap<Element>();
			amountMap.put(Element.Volume, volume);
			amountMap.put(Element.Biomass, volume * basicWoodDensity);
			amountMap.put(Element.C, volume * basicWoodDensity * carbonContent);

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

			Collection<CarbonUnit> endProducts = processorManager.processWoodPiece(sawingProcessor.logCategory, 2015, amountMap);
				
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
			Collection<CarbonUnit> endProducts = processorManager.processWoodPiece(sawingProcessor.logCategory, 2015, amountMap);
				
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
