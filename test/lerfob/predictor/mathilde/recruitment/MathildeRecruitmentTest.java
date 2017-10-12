package lerfob.predictor.mathilde.recruitment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.stats.estimates.MonteCarloEstimate;
import repicea.util.ObjectUtility;

public class MathildeRecruitmentTest {

	private static List<MathildeTreeImpl> TREES;
	private static Map<String, MathildeRecruitmentStandImpl> STAND_MAP;


	private static List<MathildeTreeImpl> SetTreeListForDbhTests() throws Exception {
		Map<String, MathildeRecruitmentStand> standMap = new HashMap<String, MathildeRecruitmentStand>();
		List<MathildeTreeImpl> trees = new ArrayList<MathildeTreeImpl>();
		String filename = ObjectUtility.getPackagePath(MathildeRecruitmentTest.class) + "recruitDbh_pred.csv";
		Object[] record;
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			MathildeTreeSpecies thisSpecies;
			while ((record = reader.nextRecord()) != null) {
				String idp = record[0].toString();
				String species = record[1].toString();
				if (species.equals("Beech")) {
					thisSpecies = MathildeTreeSpecies.FAGUS;
				} else  if (species.equals("Hornbeam")) {
					thisSpecies = MathildeTreeSpecies.CARPINUS;
				} else  if (species.equals("Oak")) {
					thisSpecies = MathildeTreeSpecies.QUERCUS;
				} else  if (species.equals("Others")) {
					thisSpecies = MathildeTreeSpecies.OTHERS;
				} else {
					throw new Exception("Unrecognized species " + species);
				}
				double basalAreaM2Ha = Double.parseDouble(record[2].toString());
				if (!standMap.containsKey(idp)) {
					standMap.put(idp, new MathildeRecruitmentStandImpl(idp, basalAreaM2Ha));
				}
				MathildeRecruitmentStand stand = standMap.get(idp);
				double[] predictions = new double[1];
				predictions[0] = Double.parseDouble(record[3].toString());
				MathildeTreeImpl tree = new MathildeTreeImpl(stand, thisSpecies, predictions);
				trees.add(tree);
			}
			return trees;
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	private static Map<String, MathildeRecruitmentStandImpl> getStandMap() throws Exception {
		Map<String, MathildeRecruitmentStandImpl> standMap = new HashMap<String, MathildeRecruitmentStandImpl>();
		String filename = ObjectUtility.getPackagePath(MathildeRecruitmentTest.class) + "zinb_pred.csv";
		Object[] record;
		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			MathildeTreeSpecies thisSpecies;
			while ((record = reader.nextRecord()) != null) {
				String idp = record[0].toString();
				String species = record[1].toString();
				if (species.equals("Beech")) {
					thisSpecies = MathildeTreeSpecies.FAGUS;
				} else  if (species.equals("Hornbeam")) {
					thisSpecies = MathildeTreeSpecies.CARPINUS;
				} else  if (species.equals("Oak")) {
					thisSpecies = MathildeTreeSpecies.QUERCUS;
				} else  if (species.equals("Others")) {
					thisSpecies = MathildeTreeSpecies.OTHERS;
				} else {
					throw new Exception("Unrecognized species " + species);
				}
				double basalAreaM2Ha = Double.parseDouble(record[3].toString());
				if (!standMap.containsKey(idp)) {
					standMap.put(idp, new MathildeRecruitmentStandImpl(idp, basalAreaM2Ha));
				}
				MathildeRecruitmentStand stand = standMap.get(idp);
				double gThisSpecies = Double.parseDouble(record[4].toString());
				((MathildeRecruitmentStandImpl) stand).setBasalAreaM2HaOfThisSpecies(thisSpecies, gThisSpecies);
				double[] predictions = new double[15];
				predictions[0] = Double.parseDouble(record[5].toString());
				predictions[1] = Double.parseDouble(record[6].toString());
				predictions[2] = Double.parseDouble(record[7].toString());
				predictions[3] = Double.parseDouble(record[8].toString());
				predictions[4] = Double.parseDouble(record[9].toString());
				predictions[5] = Double.parseDouble(record[10].toString());
				predictions[6] = Double.parseDouble(record[11].toString());
				predictions[7] = Double.parseDouble(record[12].toString());
				predictions[8] = Double.parseDouble(record[13].toString());
				predictions[9] = Double.parseDouble(record[14].toString());
				predictions[10] = Double.parseDouble(record[15].toString());
				predictions[11] = Double.parseDouble(record[16].toString());
				predictions[12] = Double.parseDouble(record[17].toString());
				predictions[13] = Double.parseDouble(record[18].toString());
				predictions[14] = Double.parseDouble(record[19].toString());
				new MathildeTreeImpl(stand, thisSpecies, predictions);
			}
			return standMap;
		} catch (Exception e) {
			throw e;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	@Test
	public void numbersOfRecruitsTest() throws Exception {
		if (STAND_MAP == null) {
			STAND_MAP = getStandMap();
		}

		MathildeRecruitmentNumberPredictor pred = new MathildeRecruitmentNumberPredictor(false);

		int nbTreesTested = 0;
		for (MathildeRecruitmentStandImpl s : STAND_MAP.values()) {
			for (MathildeTreeImpl tree : s.treeList) {
				Matrix predictions = pred.getMarginalPredictionsForThisStandAndSpecies(tree.getStand(), tree.getMathildeTreeSpecies(), 15, true);
				double[] expectedValues = tree.getPredictions();
				Assert.assertEquals("Testing the number of predicted values", predictions.m_iCols, expectedValues.length);
				for (int j = 0; j < predictions.m_iCols; j++) {
					double expected = expectedValues[j];
					double actual = predictions.m_afData[0][j];
					Assert.assertEquals(expected, actual, 2E-7);
				}
				nbTreesTested++;
			}
		}
		System.out.println("Prediction of number of recruits successfully tested on " + nbTreesTested + " trees.");
	}


	@Test
	public void meanNumbersOfRecruitsTest() throws Exception {
		if (STAND_MAP == null) {
			STAND_MAP = getStandMap();
		}

		MathildeRecruitmentNumberPredictor pred = new MathildeRecruitmentNumberPredictor(false);

		int nbTreesTested = 0;
		for (MathildeRecruitmentStandImpl s : STAND_MAP.values()) {
			for (MathildeTreeImpl tree : s.treeList) {
				Matrix predictions = pred.getMarginalPredictionsForThisStandAndSpecies(tree.getStand(), tree.getMathildeTreeSpecies(), 50, true);
				double actualMean = 0;
				for (int j = 0; j < predictions.m_iCols; j++) {
					actualMean += j * predictions.m_afData[0][j];
				}
				double expectedMean = pred.predictNumberOfRecruits(tree.getStand()).m_afData[tree.getMathildeTreeSpecies().ordinal()][0];
				Assert.assertEquals(expectedMean, actualMean, 1E-5);
				nbTreesTested++;
			}
		}
		System.out.println("Prediction of number of recruits successfully tested on " + nbTreesTested + " trees.");
	}

	@Test
	public void recruitDiameterTest() throws Exception {
		if (TREES == null) {
			TREES = MathildeRecruitmentTest.SetTreeListForDbhTests();
		}

		MathildeRecruitDbhPredictor pred = new MathildeRecruitDbhPredictor(false);

		int nbTreesTested = 0;
		for (MathildeTreeImpl tree : TREES) {
			double actualValue = pred.predictRecruitDiameterWithOffset(tree.getStand(), tree);
			double expectedValues = tree.getPredictions()[0];
			Assert.assertEquals(expectedValues, actualValue, 2E-7);
			nbTreesTested++;
		}
		System.out.println("Prediction of recruit diameter successfully tested on " + nbTreesTested + " trees.");
	}

	@Test
	public void recruitDiameterStochasticTest() throws Exception {
		if (TREES == null) {
			TREES = MathildeRecruitmentTest.SetTreeListForDbhTests();
		}

		MathildeRecruitDbhPredictor pred = new MathildeRecruitDbhPredictor(false, true);	// only residual variability enabled

		for (int k = 0; k < 10; k++) {
			MathildeTreeImpl tree = TREES.get(k);

			MonteCarloEstimate estimate = new MonteCarloEstimate();
			Matrix realization; 
			for (int i = 0; i < 100000; i++) {
				realization = new Matrix(1,1);
				realization.m_afData[0][0] = pred.predictRecruitDiameterWithOffset(tree.getStand(), tree);
				estimate.addRealization(realization);
			}
			
			double actualMean = estimate.getMean().m_afData[0][0];
			double expectedMean = tree.getPredictions()[0];
			System.out.println("Prediction of stochastic recruit diameter (mean), expected = " + expectedMean + " vs actual = " + actualMean);
			Assert.assertEquals(expectedMean, actualMean, 1E-2);
			double actualVariance = estimate.getVariance().m_afData[0][0];
			double expectedVariance = expectedMean * expectedMean / MathildeRecruitDbhPredictor.Dispersion;
			System.out.println("Prediction of stochastic recruit diameter (variance), expected = " + expectedVariance + " vs actual = " + actualVariance);
			Assert.assertEquals(expectedVariance, actualVariance, 1E-2);
			
		}
		
		System.out.println("Prediction of stochastic recruit diameter successfully tested.");
	}

	/*
	 * This test makes sure that the copula predictions are on average equivalent to those of the margins.
	 */
	@Test
	public void recruitDiameterCopulaTest() throws Exception {
		if (STAND_MAP == null) {
			STAND_MAP = getStandMap();
		}

		MathildeRecruitmentStandImpl stand = STAND_MAP.get("19");
		MathildeRecruitmentNumberPredictor pred = new MathildeRecruitmentNumberPredictor(false, true);	// only residual variability enabled

		int nbRealizations = 100000;
		double realizationFactor = 1d/nbRealizations;
		Matrix averageFrequencies = new Matrix(4,15);
		for (int k = 0; k < nbRealizations; k++) {
			Matrix predictedFrequencies = pred.predictNumberOfRecruits(stand);
			for (int i = 0; i < predictedFrequencies.m_iRows; i++) {
				averageFrequencies.m_afData[i][(int) predictedFrequencies.m_afData[i][0]] += realizationFactor;  
			}
		}
		for (MathildeTreeImpl tree : stand.treeList) {
			int rowIndex = tree.getMathildeTreeSpecies().ordinal();
			Matrix averageFrequenciesForThisSpecies = averageFrequencies.getSubMatrix(rowIndex, rowIndex, 0, averageFrequencies.m_iCols - 1);
			double[] predictions = tree.getPredictions();
			Assert.assertEquals("Testing the number of predicted values", averageFrequenciesForThisSpecies.m_iCols, predictions.length);
			for (int j = 0; j < averageFrequenciesForThisSpecies.m_iCols; j++) {
				double expected = predictions[j];
				double actual = averageFrequenciesForThisSpecies.m_afData[0][j];
				Assert.assertEquals(expected, actual, 5E-3);
			}
		}
		System.out.println("Prediction of copula successfully tested on 4 trees.");
	}


}
