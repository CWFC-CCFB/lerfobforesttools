package lerfob.carbonbalancetool;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.CATSettings.CATSpecies;
import lerfob.carbonbalancetool.CATUtility.ProductionManagerName;
import lerfob.carbonbalancetool.CarbonAccountingTool.CATMode;
import lerfob.carbonbalancetool.io.CATGrowthSimulationRecordReader;
import lerfob.carbonbalancetool.io.CATYieldTableRecordReader;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.io.tools.ImportFieldManager;
import repicea.math.Matrix;
import repicea.serial.xml.XmlDeserializer;
import repicea.serial.xml.XmlSerializerChangeMonitor;
import repicea.stats.distributions.utility.GaussianUtility;
import repicea.stats.estimates.Estimate;
import repicea.util.ObjectUtility;



public class CarbonAccountingToolTest {

	static {
		XmlSerializerChangeMonitor.registerClassNameChange("repicea.stats.distributions.NonparametricDistribution", "repicea.stats.distributions.EmpiricalDistribution");
	}
	
	
	
	private final static NumberFormat FORMATTER = NumberFormat.getInstance();

	@Test
	public void deserializationTest() {
		String filename = ObjectUtility.getPackagePath(ProductionProcessorManager.class) + "exampleProductionLines.prl";
		ProductionProcessorManager ppm = new ProductionProcessorManager();
		try {
			ppm.load(filename);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Error while loading");
		}
		Assert.assertTrue(ppm.isValid());
	}


	@Test
	public void simpleCarbonAccountingTest() throws Exception {
		String managerFilename = ObjectUtility.getPackagePath(ProductionProcessorManager.class) + "exampleProductionLines.prl";
		final String standID = "StandTest";
		final double areaHa = .04;
		List<CATCompatibleStand> stands = new ArrayList<CATCompatibleStand>();
		CATCompatibleStand stand;
		CATCompatibleTree tree;
		for (int i = 1; i <= 10; i++) {
			stand = new CarbonToolCompatibleStandImpl("beech", standID, areaHa, i*10);
			stands.add(stand);
			for (int j = 1; j <= 10; j++) {
				tree = new CarbonToolCompatibleTreeImpl(stand.getDateYr() * .01, "Fagus sylvatica");
				((CarbonToolCompatibleStandImpl) stand).addTree(tree);
			}
		}
		
		CarbonAccountingTool tool = new CarbonAccountingTool(CATMode.SCRIPT);
		tool.initializeTool(null);
		tool.setStandList(stands);
		try {
			tool.getCarbonToolSettings().getCurrentProductionProcessorManager().load(managerFilename);
			tool.calculateCarbon();
			CATSingleSimulationResult result = tool.getCarbonCompartmentManager().getSimulationSummary();
			Assert.assertTrue(result != null && result.isValid());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unable to calculate carbon!");
		}
		tool.requestShutdown();
	}
	

	@Test
	public void matterBalanceAfterHarvest() throws Exception {
		String managerFilename = ObjectUtility.getPackagePath(ProductionProcessorManager.class) + "exampleProductionLines.prl";
		final String standID = "StandTest";
		final double areaHa = .04;
		List<CATCompatibleStand> stands = new ArrayList<CATCompatibleStand>();
		CATCompatibleStand stand;
		CATCompatibleTree tree;
		for (int i = 1; i <= 10; i++) {
			stand = new CarbonToolCompatibleStandImpl("beech", standID, areaHa, i*10);
			stands.add(stand);
			for (int j = 1; j <= 10; j++) {
				tree = new CarbonToolCompatibleTreeImpl(stand.getDateYr() * .01, "Fagus sylvatica");
				((CarbonToolCompatibleStandImpl) stand).addTree(tree);
			}
		}
		
		CarbonAccountingTool tool = new CarbonAccountingTool(CATMode.SCRIPT);
		tool.initializeTool(null);
		tool.setStandList(stands);
		try {
			tool.getCarbonToolSettings().getCurrentProductionProcessorManager().load(managerFilename);
			tool.calculateCarbon();
			CATSingleSimulationResult result = tool.getCarbonCompartmentManager().getSimulationSummary();
			Assert.assertTrue(result != null && result.isValid());
			Matrix obsLivingBiomass = result.getEvolutionMap().get(CompartmentInfo.LivingBiomass).getMean();
			Matrix obsDOM = result.getEvolutionMap().get(CompartmentInfo.DeadBiom).getMean();
			Matrix obsProducts = result.getEvolutionMap().get(CompartmentInfo.TotalProducts).getMean();
			int indexFirstProducts = -1;
			for (int i = 0; i < obsProducts.m_iRows; i++) {
				if (obsProducts.m_afData[i][0] > 0d) {
					indexFirstProducts = i;
					break;
				}
			}
			if (indexFirstProducts == -1) {
				Assert.fail("Cannot find the first occurrence of HWP!");
			} else {
				double totalBefore = obsLivingBiomass.add(obsDOM).m_afData[indexFirstProducts - 1][0];
				double totalAfter = obsLivingBiomass.add(obsDOM).m_afData[indexFirstProducts][0] + obsProducts.m_afData[indexFirstProducts][0];
				Assert.assertEquals("Testing total biomass before and biomass after harvesting", totalBefore, totalAfter, 1E-8);
			}
			tool.requestShutdown();
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unable to calculate carbon!");
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testWithYieldTable() throws Exception {
		String filename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTable.csv";
		String ifeFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTable.ife";
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTableReference.xml";
		CarbonAccountingTool cat = new CarbonAccountingTool(CATMode.SCRIPT);
		cat.initializeTool(null);
		CATYieldTableRecordReader recordReader = new CATYieldTableRecordReader(CATSpecies.ABIES);
		ImportFieldManager ifm = ImportFieldManager.createImportFieldManager(ifeFilename, filename);
		recordReader.initInScriptMode(ifm);
		recordReader.readAllRecords();
		cat.setStandList(recordReader.getStandList());
		cat.calculateCarbon();
		CATSingleSimulationResult result = cat.getCarbonCompartmentManager().getSimulationSummary();
		Map<CompartmentInfo, Estimate<?>> obsMap = result.getBudgetMap();
		
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(obsMap);

		XmlDeserializer deserializer = new XmlDeserializer(refFilename);
		Map<CompartmentInfo, Estimate<?>> refMap = (Map) deserializer.readObject();
		int nbCompartmentChecked = 0;
		Assert.assertTrue("Testing the size of the map", refMap.size() == obsMap.size());
		for (CompartmentInfo key : refMap.keySet()) {
			double expected = refMap.get(key).getMean().m_afData[0][0];
			double observed = obsMap.get(key).getMean().m_afData[0][0];
			Assert.assertEquals("Testing compartment " + key.name(), expected, observed, 1E-8);
			nbCompartmentChecked++;
		}
		System.out.println("Successfully tested this number of compartments " + nbCompartmentChecked);
		cat.requestShutdown();
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testWithSimulationResults() throws Exception {
		String filename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "MathildeTreeExport.csv";
		String ifeFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "MathildeTreeExport.ife";
		String speciesMatchFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "speciesCorrespondanceForSimulationData.xml";
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "MathildeTreeExportReference.xml";
		CarbonAccountingTool cat = new CarbonAccountingTool(CATMode.SCRIPT);
		cat.initializeTool(null);
		CATGrowthSimulationRecordReader recordReader = new CATGrowthSimulationRecordReader();
		recordReader.getSelector().load(speciesMatchFilename);
		ImportFieldManager ifm = ImportFieldManager.createImportFieldManager(ifeFilename, filename);
		recordReader.initInScriptMode(ifm);
		recordReader.readAllRecords();
		cat.setStandList(recordReader.getStandList());
		cat.calculateCarbon();
		CATSingleSimulationResult result = cat.getCarbonCompartmentManager().getSimulationSummary();
		Map<CompartmentInfo, Estimate<?>> obsMap = result.getBudgetMap();
		
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(obsMap);

		XmlDeserializer deserializer = new XmlDeserializer(refFilename);
		Map<CompartmentInfo, Estimate<?>> refMap = (Map) deserializer.readObject();
		int nbCompartmentChecked = 0;
		Assert.assertTrue("Testing the size of the map", refMap.size() == obsMap.size());
		for (CompartmentInfo key : refMap.keySet()) {
			double expected = refMap.get(key).getMean().m_afData[0][0];
			double observed = obsMap.get(key).getMean().m_afData[0][0];
			Assert.assertEquals("Testing compartment " + key.name(), expected, observed, 1E-8);
			nbCompartmentChecked++;
		}
		System.out.println("Successfully tested this number of compartments " + nbCompartmentChecked);
	}

	@Test
	public void testMemoryLeakage() throws Exception {
		int nbSimulations = 10;
		String filename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "MathildeTreeExport.csv";
		String ifeFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "MathildeTreeExport.ife";
		String speciesMatchFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "speciesCorrespondanceForSimulationData.xml";
		Matrix matX = new Matrix(nbSimulations-1, 2);
		Matrix matY = new Matrix(nbSimulations-1, 1);
		for (int i = 0; i < nbSimulations; i++) {
			CarbonAccountingTool cat = new CarbonAccountingTool(CATMode.SCRIPT);
			cat.initializeTool(null);
			CATGrowthSimulationRecordReader recordReader = new CATGrowthSimulationRecordReader();
			recordReader.getSelector().load(speciesMatchFilename);
			ImportFieldManager ifm = ImportFieldManager.createImportFieldManager(ifeFilename, filename);
			recordReader.initInScriptMode(ifm);
			recordReader.readAllRecords();
			cat.setStandList(recordReader.getStandList());
			cat.calculateCarbon();
			cat.requestShutdown();
			System.gc();
			double currentUsedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 1E-6;
			if (i > 0) { // the first simulation is out of the scope since the field stand is empty
				matX.m_afData[i-1][0] = 1;
				matX.m_afData[i-1][1] = i;
				matY.m_afData[i-1][0] = currentUsedMemory;
			}
			FORMATTER.setMinimumFractionDigits(2);
			System.out.println("Memory load after run " + i + " = " + FORMATTER.format(currentUsedMemory));
		}
		Matrix betaEstimate = matX.transpose().multiply(matX).getInverseMatrix().multiply(matX.transpose())
				.multiply(matY);
		Matrix res = matY.subtract(matX.multiply(betaEstimate));
		double sigma2 = res.transpose().multiply(res).m_afData[0][0] / (res.m_iRows - betaEstimate.m_iRows);
		Matrix omegaMatrix = matX.transpose().multiply(matX).getInverseMatrix().scalarMultiply(sigma2);
		double slopeParameterEstimate = betaEstimate.m_afData[betaEstimate.m_iRows - 1][0];
		double standardError = Math.sqrt(omegaMatrix.m_afData[omegaMatrix.m_iRows - 1][omegaMatrix.m_iRows - 1]);
		double prob = 1 - GaussianUtility.getCumulativeProbability(Math.abs(slopeParameterEstimate / standardError));
		System.out.println("Slope parameter estimate = " + slopeParameterEstimate);
		System.out.println("Pr > z = " + prob);
		Assert.assertTrue(slopeParameterEstimate < 1E-2 || prob > 0.025);		// 1E-2 implies 1Kb memory occupation
	}
	
	@Test
	public void testComparisonHalflifeVsAverageLifetime() throws Exception {
		String filename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTable.csv";
		String ifeFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTable.ife";
		String averageLifetimeFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "SingleProcessorWithAverageLifetime.prl";
		String halflifeFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "SingleProcessorWithHalflife.prl";
		CarbonAccountingTool cat = new CarbonAccountingTool(CATMode.SCRIPT);
		cat.initializeTool(null);
		CATYieldTableRecordReader recordReader = new CATYieldTableRecordReader(CATSpecies.ABIES);
		ImportFieldManager ifm = ImportFieldManager.createImportFieldManager(ifeFilename, filename);
		recordReader.initInScriptMode(ifm);
		recordReader.readAllRecords();
		cat.setStandList(recordReader.getStandList());
		
		cat.getCarbonToolSettings().getCustomizableProductionProcessorManager().load(averageLifetimeFilename);
		cat.getCarbonToolSettings().currentProcessorManager = ProductionManagerName.customized;
		cat.calculateCarbon();
		CATSingleSimulationResult resultAverageLifetime = cat.getCarbonCompartmentManager().getSimulationSummary();
		Map<CompartmentInfo, Estimate<?>> obsMapAverageLifetime = resultAverageLifetime.getBudgetMap();
		
		cat.getCarbonToolSettings().getCustomizableProductionProcessorManager().load(halflifeFilename);
		cat.calculateCarbon();
		CATSingleSimulationResult resultHalflife = cat.getCarbonCompartmentManager().getSimulationSummary();
		Map<CompartmentInfo, Estimate<?>> obsMapHalflife = resultHalflife.getBudgetMap();
		
		
		Assert.assertTrue("Testing the size of the map", obsMapAverageLifetime.size() == obsMapHalflife.size());
		int nbCompartmentChecked = 0;
		for (CompartmentInfo key : obsMapAverageLifetime.keySet()) {
			double expected = obsMapAverageLifetime.get(key).getMean().m_afData[0][0];
			double observed = obsMapHalflife.get(key).getMean().m_afData[0][0];
			Assert.assertEquals("Testing compartment " + key.name(), expected, observed, 1E-8);
			nbCompartmentChecked++;
		}
		System.out.println("Successfully tested this number of compartments " + nbCompartmentChecked);
		cat.requestShutdown();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void testWithYieldTableAndIPCCConfiguration() throws Exception {
		String filename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTable.csv";
		String ifeFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTable.ife";
		String refFilename = ObjectUtility.getPackagePath(getClass()) + "io" + File.separator + "ExampleYieldTableWithIPCCReference.xml";
		String prlFilename = ObjectUtility.getPackagePath(getClass()) + "productionlines" + File.separator + "library" + File.separator + "ipcc2014_en.prl";
		CarbonAccountingTool cat = new CarbonAccountingTool(CATMode.SCRIPT);
		cat.initializeTool(null);
		CATYieldTableRecordReader recordReader = new CATYieldTableRecordReader(CATSpecies.ABIES);
		ImportFieldManager ifm = ImportFieldManager.createImportFieldManager(ifeFilename, filename);
		recordReader.initInScriptMode(ifm);
		recordReader.readAllRecords();
		cat.setStandList(recordReader.getStandList());
		cat.setProductionManager(prlFilename);
		cat.calculateCarbon();
		CATSingleSimulationResult result = cat.getCarbonCompartmentManager().getSimulationSummary();
		Map<CompartmentInfo, Estimate<?>> obsMap = result.getBudgetMap();
		
//		XmlSerializer serializer = new XmlSerializer(refFilename);
//		serializer.writeObject(obsMap);

		XmlDeserializer deserializer = new XmlDeserializer(refFilename);
		Map<CompartmentInfo, Estimate<?>> refMap = (Map) deserializer.readObject();
		int nbCompartmentChecked = 0;
//		Assert.assertTrue("Testing the size of the map", refMap.size() == obsMap.size());
		for (CompartmentInfo key : refMap.keySet()) {
			double expected = refMap.get(key).getMean().m_afData[0][0];
			double observed = obsMap.get(key).getMean().m_afData[0][0];
			Assert.assertEquals("Testing compartment " + key.name(), expected, observed, 1E-8);
			nbCompartmentChecked++;
		}
		System.out.println("Successfully tested this number of compartments " + nbCompartmentChecked);
		cat.requestShutdown();
	}

}
