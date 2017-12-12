package lerfob.carbonbalancetool.biomassparameters;

import lerfob.carbonbalancetool.CATAboveGroundBiomassProvider;
import lerfob.carbonbalancetool.CATAboveGroundCarbonProvider;
import lerfob.carbonbalancetool.CATAboveGroundVolumeProvider;
import lerfob.carbonbalancetool.CATBasicWoodDensityProvider;
import lerfob.carbonbalancetool.CATBelowGroundBiomassProvider;
import lerfob.carbonbalancetool.CATBelowGroundCarbonProvider;
import lerfob.carbonbalancetool.CATCarbonContentRatioProvider;

public class GUITest {

	public static class FakeReferent implements CATAboveGroundBiomassProvider, 
												CATAboveGroundVolumeProvider, 
												CATAboveGroundCarbonProvider,
												CATBelowGroundCarbonProvider,
												CATBelowGroundBiomassProvider,
												CATBasicWoodDensityProvider,
												CATCarbonContentRatioProvider {

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
		param.showUI(null);
		System.exit(0);
	}
	
	
}
