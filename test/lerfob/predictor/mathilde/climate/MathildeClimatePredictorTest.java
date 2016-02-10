package lerfob.predictor.mathilde.climate;

import org.junit.Assert;
import org.junit.Test;

public class MathildeClimatePredictorTest {

	@Test
	public void testDeterministicPredictionsWithReferenceStands() {
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false, false, false);
		for (MathildeClimateStandImpl stand : MathildeClimatePredictor.getReferenceStands()) {
			double actualPrediction = climatePredictor.getMeanTemperatureForGrowthInterval(stand);
			double expectedPrediction = stand.getPrediction();
			Assert.assertEquals("Comparing predictions for stand : " + stand.name + stand.dateYr,
					expectedPrediction, 
					actualPrediction, 
					1E-6);
		}
	}
}
