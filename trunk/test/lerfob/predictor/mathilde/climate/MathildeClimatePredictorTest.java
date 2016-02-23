package lerfob.predictor.mathilde.climate;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

public class MathildeClimatePredictorTest {

	static Map<String, Double> blupMean;
	static Map<String, Double> blupStdErr;

	protected static void readBlups() throws Exception {
		blupMean = new HashMap<String, Double>();
		blupStdErr = new HashMap<String, Double>();
		String filename = ObjectUtility.getRelativePackagePath(MathildeClimatePredictorTest.class) + "dataBaseClimateBlups.csv";
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		while ((record = reader.nextRecord()) != null) {
			String experiment = record[0].toString();
			double mean = Double.parseDouble(record[1].toString());
			double variance = Double.parseDouble(record[2].toString());
			blupMean.put(experiment, mean);
			blupStdErr.put(experiment, variance);
		}
	}
	
	
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
	public void testBlups() throws Exception {
		readBlups();
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false, false, false);
		int nbStands = 0;
		for (MathildeClimateStand s : MathildeClimatePredictor.getReferenceStands()) {
			MathildeClimateStandImpl stand = (MathildeClimateStandImpl) s;
			climatePredictor.getMeanTemperatureForGrowthInterval(stand);
			GaussianEstimate blup = climatePredictor.getBlupsForThisSubject(stand);
			String experiment = stand.getSubjectId();
			
			double actualMean = blup.getMean().m_afData[0][0]; 
			double expectedMean = blupMean.get(experiment);
			Assert.assertEquals("Comparing blup mean for stand : " + stand.name + stand.dateYr,
					expectedMean, 
					actualMean, 
					1E-6);

			double actualStdErr = Math.sqrt(blup.getVariance().m_afData[0][0]); 
			double expectedStdErr = blupStdErr.get(experiment);
			Assert.assertEquals("Comparing blup stdErr for stand : " + stand.name + stand.dateYr,
					expectedStdErr, 
					actualStdErr, 
					1E-6);

			nbStands++;
		}
		System.out.println("MathildeClimatePredictorTest, Number of stands successfully tested : " + nbStands);
	}
}
