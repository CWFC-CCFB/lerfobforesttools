package lerfob.carbonbalancetool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lerfob.carbonbalancetool.productionlines.ProductionProcessorManager;

import org.junit.Assert;
import org.junit.Test;

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
		List<CarbonToolCompatibleStand> stands = new ArrayList<CarbonToolCompatibleStand>();
		CarbonToolCompatibleStand stand;
		CarbonToolCompatibleTree tree;
		for (int i = 1; i <= 10; i++) {
			stand = new CarbonToolCompatibleStandImpl(standID, areaHa, i*10);
			stands.add(stand);
			for (int j = 1; j <= 10; j++) {
				tree = new CarbonToolCompatibleTreeImpl(stand.getDateYr() * .01, "Fagus");
				((CarbonToolCompatibleStandImpl) stand).addTree(tree);
			}
		}
		
		LERFoBCarbonAccountingTool tool = new LERFoBCarbonAccountingTool();
		tool.initializeTool(false, null);
		tool.setStandList(stands);
		try {
			tool.getCarbonToolSettings().getCurrentProductionProcessorManager().load(managerFilename);
			tool.calculateCarbon();
			CarbonAssessmentToolSingleSimulationResult result = tool.getCarbonCompartmentManager().getSimulationSummary();
			Assert.assertTrue(result != null);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail("Unable to calculate carbon!");
		}
	}
}
