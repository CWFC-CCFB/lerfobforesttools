package lerfob.predictor.mathilde.climate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lerfob.predictor.mathilde.climate.GeographicalCoordinatesGenerator.PlotCoordinates;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.stats.estimates.GaussianEstimate;
import repicea.util.ObjectUtility;

public class MathildeClimatePredictorTest {

	static Map<String, Double> blupMean;
	static Map<String, Double> blupStdErr;
	static Map<String, Double> meanLongitude;
	static Map<String, Double> meanLatitude;
	
	protected static void readBlups() throws Exception {
		CSVReader reader = null;
		try {
			blupMean = new HashMap<String, Double>();
			blupStdErr = new HashMap<String, Double>();
			String filename = ObjectUtility.getRelativePackagePath(MathildeClimatePredictorTest.class) + "dataBaseClimateBlups.csv";
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String experiment = record[0].toString();
				double mean = Double.parseDouble(record[1].toString());
				double variance = Double.parseDouble(record[2].toString());
				blupMean.put(experiment, mean);
				blupStdErr.put(experiment, variance);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	protected static void readMeanPlotCoordinates() throws IOException {
		CSVReader reader = null;
		try {
			meanLongitude = new HashMap<String, Double>();
			meanLatitude = new HashMap<String, Double>();
			String filename = ObjectUtility.getRelativePackagePath(MathildeClimatePredictorTest.class) + "meanPlotCoordinates.csv";
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String department = record[0].toString();
				double longitude = Double.parseDouble(record[1].toString());
				double latitude = Double.parseDouble(record[2].toString());
				meanLongitude.put(department, longitude);
				meanLatitude.put(department, latitude);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	@Test
	public void testDeterministicPredictionsWithReferenceStands() {
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false);
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
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false);
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
	
	@Test
	public void testMeanPlotCoordinates() throws IOException {
		readMeanPlotCoordinates();
		int i = 0;
		for (String department : meanLongitude.keySet()) {
			double expectedLongitude = meanLongitude.get(department);
			double expectedLatitude = meanLatitude.get(department);
			PlotCoordinates coord = GeographicalCoordinatesGenerator.getInstance().getMeanCoordinatesForThisDepartment(department);
			Assert.assertEquals("Comparing mean longitude for department : " + department,
					expectedLongitude, 
					coord.longitude, 
					1E-6);
			Assert.assertEquals("Comparing mean latitude for department : " + department,
					expectedLatitude, 
					coord.latitude, 
					1E-6);
			i++;
		}
		System.out.println("Successful comparison of mean plot coordinates:" + i);
	}
}
