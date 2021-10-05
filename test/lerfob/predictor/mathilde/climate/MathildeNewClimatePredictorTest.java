package lerfob.predictor.mathilde.climate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import lerfob.predictor.mathilde.climate.GeographicalCoordinatesGenerator.PlotCoordinates;
import lerfob.simulation.covariateproviders.plotlevel.FrenchDepartmentProvider.FrenchDepartment;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.simulation.climate.REpiceaClimateVariableMap.ClimateVariable;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class MathildeNewClimatePredictorTest {
	static Map<String, Double> blupMean;
	static Map<String, Double> blupStdErr;
	static Map<FrenchDepartment, Double> meanLongitude;
	static Map<FrenchDepartment, Double> meanLatitude;
	
	protected static void readBlups() throws Exception {
		CSVReader reader = null;
		try {
			blupMean = new HashMap<String, Double>();
			blupStdErr = new HashMap<String, Double>();
			String filename = ObjectUtility.getRelativePackagePath(MathildeNewClimatePredictorTest.class) + "dataBaseNewClimateBlups.csv";
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
			meanLongitude = new HashMap<FrenchDepartment, Double>();
			meanLatitude = new HashMap<FrenchDepartment, Double>();
			String filename = ObjectUtility.getRelativePackagePath(MathildeNewClimatePredictorTest.class) + "meanPlotCoordinates.csv";
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				String departmentCode = record[0].toString();
				FrenchDepartment department = FrenchDepartment.getDepartment(departmentCode);
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
		for (MathildeClimatePlot s : MathildeClimatePredictor.getReferenceStands()) {
			MathildeClimatePlotImpl stand = (MathildeClimatePlotImpl) s;
			double actualPrediction = climatePredictor.getFixedEffectPrediction(stand);
			double actualVariance = climatePredictor.getFixedEffectPredictionVariance(stand);
			double expectedPrediction = stand.getPrediction();
			double expectedVariance = stand.getPredictionVariance();
			Assert.assertEquals("Comparing predictions for stand : " + stand.name + stand.dateYr,
					expectedPrediction, 
					actualPrediction, 
					1E-6);
			Assert.assertEquals("Comparing prediction variance for stand : " + stand.name + stand.dateYr,
					expectedVariance, 
					actualVariance, 
					1E-6);
			// MF2019-06-27 The comparison of scaled residual is hindered by the fact that matrix G is complex. It works
			// only for the first residual.
//			double residual = stand.meanAnnualTempAbove6C - expectedPrediction; // should include the blup here
//			double residualVariance = climatePredictor.getResidualVariance(stand);
//			double actualScaledResidual = residual / Math.sqrt(residualVariance);
//			double expectedScaledResidual = stand.getScaledResidual();
//			Assert.assertEquals("Comparing scaled residual for stand : " + stand.name + stand.dateYr,
//					expectedScaledResidual, 
//					actualScaledResidual, 
//					1E-6);
			nbStands++;
		}
		System.out.println("MathildeClimatePredictorTest, Number of stands successfully tested : " + nbStands);
	}
	
	
	
	@Test
	public void testBlups() throws Exception {
		readBlups();
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false);
		int nbStands = 0;
		for (MathildeClimatePlot s : MathildeClimatePredictor.getReferenceStands()) {
			MathildeClimatePlotImpl stand = (MathildeClimatePlotImpl) s;
			climatePredictor.getClimateVariables(stand);
			Estimate<? extends StandardGaussianDistribution> blup = climatePredictor.getBlupsForThisSubject(stand);
			String experiment = stand.getSubjectId();
			
			double actualMean = blup.getMean().getValueAt(0, 0); 
			double expectedMean = blupMean.get(experiment);
			Assert.assertEquals("Comparing blup mean for stand : " + stand.name + stand.dateYr,
					expectedMean, 
					actualMean, 
					1E-6);

			double actualStdErr = Math.sqrt(blup.getVariance().getValueAt(0, 0)); 
			double expectedStdErr = blupStdErr.get(experiment);
			Assert.assertEquals("Comparing blup stdErr for stand : " + stand.name + stand.dateYr,
					expectedStdErr, 
					actualStdErr, 
					4E-4);

			nbStands++;
		}
		System.out.println("MathildeClimatePredictorTest, Number of stands successfully tested : " + nbStands);
	}

	
	@Test
	public void testBlupsInStochasticMode() throws Exception {
		readBlups();
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(true);
		int nbRealizations = 50000;
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		MathildeClimatePlot s = MathildeClimatePredictor.getReferenceStands().get(10);
		
		for (int i = 0; i < nbRealizations; i++) {
			for (MathildeClimatePlot stand : MathildeClimatePredictor.getReferenceStands()) {
				((MathildeClimatePlotImpl) stand).realization = i;
			}
			climatePredictor.getClimateVariables(s);
			estimate.addRealization(climatePredictor.getRandomEffects(s));
		}
		
		double expectedMean = climatePredictor.getBlupsForThisSubject(s).getMean().getValueAt(0, 0);
		double actualMean = estimate.getMean().getValueAt(0, 0);
		double expectedVariance = climatePredictor.getBlupsForThisSubject(s).getVariance().getValueAt(0, 0);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Comparing blup means",	expectedMean, actualMean, 5E-3);
		Assert.assertEquals("Comparing blup variances",	expectedVariance, actualVariance, 2E-3);
		System.out.println("MathildeClimatePredictorTest, Stochastic simulation of blups successfully tested!");
	}

	@Test
	public void testResidualErrorInStochasticMode() throws Exception {
		MathildeClimatePredictor climatePredictor = new MathildeClimatePredictor(false, false, true); // only residual variability enabled
		int nbRealizations = 50000;
		MonteCarloEstimate estimate = new MonteCarloEstimate();
		MathildeClimatePlot s = MathildeClimatePredictor.getReferenceStands().get(10);
		
		Matrix real;
		for (int i = 0; i < nbRealizations; i++) {
			((MathildeClimatePlotImpl) s).realization = i;
			real = new Matrix(1,1);
			real.setValueAt(0, 0, climatePredictor.getClimateVariables(s).get(ClimateVariable.MeanGrowingSeasonTempC));
			estimate.addRealization(real);
		}
		
		double expectedVariance = climatePredictor.getResidualVariance((MathildeClimatePlotImpl) s);
		double actualVariance = estimate.getVariance().getValueAt(0, 0);
		Assert.assertEquals("Comparing residual variances",	expectedVariance, actualVariance, 1E-3);
		System.out.println("MathildeClimatePredictorTest, Stochastic simulation of residual variance successfully tested!");
	}
	
	@Test
	public void testMeanPlotCoordinates() throws IOException {
		readMeanPlotCoordinates();
		int i = 0;
		for (FrenchDepartment department : meanLongitude.keySet()) {
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
