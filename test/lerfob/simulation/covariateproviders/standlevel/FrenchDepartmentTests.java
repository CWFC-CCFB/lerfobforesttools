package lerfob.simulation.covariateproviders.standlevel;

import org.junit.Assert;
import org.junit.Test;

import lerfob.simulation.covariateproviders.standlevel.FrenchDepartmentProvider.FrenchDepartment;

public class FrenchDepartmentTests {

	@Test
	public void testGroupingFormerRegionsForNewRegions() {
		for (FrenchDepartment dep : FrenchDepartment.values()) {
			Assert.assertTrue(dep.getNewRegion() == dep.newRegion);
		}
	}
	
	
}
