package lerfob.carbonbalancetool.biomassparameters;

import lerfob.carbonbalancetool.AboveGroundBiomassProvider;
import lerfob.carbonbalancetool.AboveGroundCarbonProvider;
import lerfob.carbonbalancetool.AboveGroundVolumeProvider;
import lerfob.carbonbalancetool.BasicWoodDensityProvider;
import lerfob.carbonbalancetool.BelowGroundBiomassProvider;
import lerfob.carbonbalancetool.BelowGroundCarbonProvider;
import lerfob.carbonbalancetool.CarbonContentRatioProvider;

public class GUITest {

	public static class FakeReferent implements AboveGroundBiomassProvider, 
												AboveGroundVolumeProvider, 
												AboveGroundCarbonProvider,
												BelowGroundCarbonProvider,
												BelowGroundBiomassProvider,
												BasicWoodDensityProvider,
												CarbonContentRatioProvider {

		@Override
		public double getAboveGroundBiomassMg() {
			return 0;
		}

		@Override
		public double getBelowGroundBiomassMg() {
			return 0;
		}

//		@Override
//		public double getBelowGroundVolumeM3() {
//			return 0;
//		}

		@Override
		public double getBelowGroundCarbonMg() {
			return 0;
		}

		@Override
		public double getAboveGroundCarbonMg() {
			return 0;
		}

		@Override
		public double getAboveGroundVolumeM3() {
			return 0;
		}

		@Override
		public double getCarbonContentRatio() {
			return 0;
		}

		@Override
		public double getBasicWoodDensity() {
			return 0;
		}
		
	}

	
	public static void main(String[] args) {
		BiomassParameters param = new BiomassParameters();
		param.setReferent(new FakeReferent());
		param.showInterface(null);
//		System.exit(0);
	}
	
	
}
