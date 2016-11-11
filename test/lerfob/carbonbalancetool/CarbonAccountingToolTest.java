package lerfob.carbonbalancetool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import lerfob.carbonbalancetool.CATCompartment.CompartmentInfo;
import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;
import repicea.math.Matrix;
import repicea.util.ObjectUtility;


public class CarbonAccountingToolTest {


	
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
	public void simpleCarbonAccountingTest() {
		String managerFilename = ObjectUtility.getPackagePath(ProductionProcessorManager.class) + "exampleProductionLines.prl";
		final String standID = "StandTest";
		final double areaHa = .04;
		List<CATCompatibleStand> stands = new ArrayList<CATCompatibleStand>();
		CATCompatibleStand stand;
		CATCompatibleTree tree;
		for (int i = 1; i <= 10; i++) {
			stand = new CarbonToolCompatibleStandImpl(standID, areaHa, i*10);
			stands.add(stand);
			for (int j = 1; j <= 10; j++) {
				tree = new CarbonToolCompatibleTreeImpl(stand.getDateYr() * .01, "Fagus");
				((CarbonToolCompatibleStandImpl) stand).addTree(tree);
			}
		}
		
		CarbonAccountingTool tool = new CarbonAccountingTool();
		tool.initializeTool(false, null);
		tool.setStandList(stands);
		try {
			tool.getCarbonToolSettings().getCurrentProductionProcessorManager().load(managerFilename);
			tool.calculateCarbon();
			CATSingleSimulationResult result = tool.getCarbonCompartmentManager().getSimulationSummary();
			Assert.assertTrue(result != null);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unable to calculate carbon!");
		}
	}
	
	
	@Test
	public void matterBalanceAfterHarvest() {
		String managerFilename = ObjectUtility.getPackagePath(ProductionProcessorManager.class) + "exampleProductionLines.prl";
		final String standID = "StandTest";
		final double areaHa = .04;
		List<CATCompatibleStand> stands = new ArrayList<CATCompatibleStand>();
		CATCompatibleStand stand;
		CATCompatibleTree tree;
		for (int i = 1; i <= 10; i++) {
			stand = new CarbonToolCompatibleStandImpl(standID, areaHa, i*10);
			stands.add(stand);
			for (int j = 1; j <= 10; j++) {
				tree = new CarbonToolCompatibleTreeImpl(stand.getDateYr() * .01, "Fagus");
				((CarbonToolCompatibleStandImpl) stand).addTree(tree);
			}
		}
		
		CarbonAccountingTool tool = new CarbonAccountingTool();
		tool.initializeTool(false, null);
		tool.setStandList(stands);
		try {
			tool.getCarbonToolSettings().getCurrentProductionProcessorManager().load(managerFilename);
			tool.calculateCarbon();
			CATSingleSimulationResult result = tool.getCarbonCompartmentManager().getSimulationSummary();
			Assert.assertTrue(result != null);
			Matrix obsBiomass = result.getEvolutionMap().get(CompartmentInfo.TotalBiomass).getMean();
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
				double totalBefore = obsBiomass.m_afData[indexFirstProducts - 1][0];
				double totalAfter = obsBiomass.m_afData[indexFirstProducts][0] + obsProducts.m_afData[indexFirstProducts][0];
				Assert.assertEquals("Testing total biomass before and biomass after harvesting", totalBefore, totalAfter, 1E-8);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unable to calculate carbon!");
		}
	}
	
}
