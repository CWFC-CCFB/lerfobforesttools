package lerfob.predictor.mathilde.climate;

import org.junit.Assert;
import org.junit.Test;

public class MathildeClimatePredictorTest {

	@Test
	public void testDeterministicPredictionsWithReferenceStands() {
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false, false, false);
		int nbStands = 0;
		for (MathildeClimateStand s : MathildeClimatePredictor.getReferenceStands()) {
			MathildeClimateStandImpl stand = (MathildeClimateStandImpl) s;
			double actualPrediction = climatePredictor.getFixedEffectPrediction(stand);
			double expectedPrediction = stand.getPrediction();
			Assert.assertEquals("Comparing predictions for stand : " + stand.name + stand.dateYr,
					expectedPrediction, 
					actualPrediction, 
					1E-6);
			nbStands++;
		}
		System.out.println("MathildeClimatePredictorTest, Number of stands successfully tested : " + nbStands);
	}
	
	
	
	@Test
	public void testBlups() {
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false, false, false);
		int nbStands = 0;
		for (MathildeClimateStand s : MathildeClimatePredictor.getReferenceStands()) {
			MathildeClimateStandImpl stand = (MathildeClimateStandImpl) s;
			double actualPrediction = climatePredictor.getMeanTemperatureForGrowthInterval(stand);
			double expectedPrediction = stand.getPrediction();
			Assert.assertEquals("Comparing predictions for stand : " + stand.name + stand.dateYr,
					expectedPrediction, 
					actualPrediction, 
					1E-6);
			nbStands++;
		}
		System.out.println("MathildeClimatePredictorTest, Number of stands successfully tested : " + nbStands);
	}
}
