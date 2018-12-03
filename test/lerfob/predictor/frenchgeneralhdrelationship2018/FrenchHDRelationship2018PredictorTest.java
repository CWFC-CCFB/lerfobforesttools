package lerfob.predictor.frenchgeneralhdrelationship2018;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.util.ObjectUtility;

public class FrenchHDRelationship2018PredictorTest {

	static List<FrenchHDRelationship2018StandImpl> Stands;
	static List<FrenchHDRelationship2018ExtStandImpl> ExtStands;
	static Map<Integer, Map<Integer, Blup>> Blups = readBlups();
	
	static class Blup {
		final double estimate;
		final double variance;
		Blup(double estimate, double std) {
			this.estimate = estimate;
			this.variance = std * std;
		}
	}
	
	

	@Test
	public void validation1FixedEffectPredictions() throws IOException {
		readTrees();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = false;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		int nbTrees = 0;
		for (FrenchHDRelationship2018Stand stand : ExtStands) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getPred();
				if (expected >= 1.3) {
					Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-4);
					nbTrees++;
				}
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees (should be 566 170 - 10 trees with negative predictions).");
	}

	
	@Test
	public void validation1FixedEffectPredictionsWithClimateGenerator() throws IOException {
		readTrees();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = false;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor();
		int nbTrees = 0;
		for (FrenchHDRelationship2018Stand stand : Stands) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getPred();
				if (expected >= 1.3) {
					Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-4);
					nbTrees++;
				}
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees (should be 566 170 - 10 trees with negative predictions).");
	}

	
	/*
	 * Make sure that blup estimation is correct. The predictor is checked as
	 * well as its variance.
	 */
	@Test
	public void validation2BlupsPredictionsAndVariance() throws IOException {
		readTrees();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor(true);
		int nbBlups = 0;

		FrenchHDRelationship2018ExtStandImpl s = ExtStands.get(0);
		List<FrenchHDRelationship2018ExtStandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2018ExtStandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);
		
		for (FrenchHDRelationship2018Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) obj;
				if (tree.getFrenchHDTreeSpecies().getIndex() <= 4) {
					int index = tree.getFrenchHDTreeSpecies().getIndex();
					predictor.predictHeightM(stand, tree);
					Estimate<? extends StandardGaussianDistribution> currentBlups = predictor.getBlups(stand, tree);
					double actualBlup = currentBlups.getMean().m_afData[0][0];
					double actualVariance = currentBlups.getVariance().m_afData[0][0];
					int convertedIndex = Integer.parseInt(stand.getSubjectId());
					double expectedBlup = Blups.get(index).get(convertedIndex).estimate;
					double expectedVariance = Blups.get(index).get(convertedIndex).variance;
					Assert.assertEquals("Comparing blups for species = " + tree.getFrenchHDTreeSpecies().name() + " in plot " + stand.getSubjectId(), 
							expectedBlup, 
							actualBlup,
							1E-5);
					Assert.assertEquals("Comparing blups variance for species = " + tree.getFrenchHDTreeSpecies().name() + " in plot " + stand.getSubjectId(), 
							expectedVariance, 
							actualVariance,
							1E-5);
					nbBlups++;
				}
			}
		}
		System.out.println("Successfully compared " + nbBlups + " blups.");
	}

	
	@Test
	public void validationErrorTermForKnownHeightWithStochasticSimulationRandomEffectPlusResiduals() throws IOException {
		readTrees();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor(false, true, true);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2018ExtStandImpl s = ExtStands.get(0);
		List<FrenchHDRelationship2018ExtStandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2018ExtStandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2018Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getHeightM();
				Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-6);
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}

	@Test
	public void validationErrorTermForKnownHeightWithStochasticSimulationRandomEffectWithoutResiduals() throws IOException {
		readTrees();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor(false, true, false);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2018ExtStandImpl s = ExtStands.get(0);
		List<FrenchHDRelationship2018ExtStandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2018ExtStandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2018Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getHeightM();
				Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-6);
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}

	@Test
	public void validationErrorTermForKnownHeightWithStochasticSimulationWithoutRandomEffectButWithResiduals() throws IOException {
		readTrees();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor(false, false, true);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2018ExtStandImpl s = ExtStands.get(0);
		List<FrenchHDRelationship2018ExtStandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2018ExtStandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2018Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getHeightM();
				Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-6);
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}
	
	@Test
	public void validationErrorTermForKnownHeightWithStochasticSimulationWithoutRandomEffectNorResiduals() throws IOException {
		readTrees();
		FrenchHDRelationship2018TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2018Predictor predictor = new FrenchHDRelationship2018Predictor(false);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2018ExtStandImpl s = ExtStands.get(0);
		List<FrenchHDRelationship2018ExtStandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2018ExtStandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2018Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2018TreeImpl tree = (FrenchHDRelationship2018TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getHeightM();
				Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-6);
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}

	
	static void readTrees() {
		String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2018PredictorTest.class) + "testData.csv";
		List<FrenchHDRelationship2018StandImpl> standList = new ArrayList<FrenchHDRelationship2018StandImpl>();
		List<FrenchHDRelationship2018ExtStandImpl> extStandList = new ArrayList<FrenchHDRelationship2018ExtStandImpl>();

		CSVReader reader = null;
		try {
			reader = new CSVReader(filename);
			Object[] record;
			int idp;
			String species;
			double pent2;
			double gOther;
			double d130;
			double pred;
			double mqd;
			double htot;
			double harvestInLastFiveYears;
			double meanTemp_3, meanTemp_4, meanTemp_5, meanTemp_6, meanTemp_7, meanTemp_8, meanTemp_9;
			double meanPrec_3, meanPrec_4, meanPrec_5, meanPrec_6, meanPrec_7, meanPrec_8, meanPrec_9;
			double weight;
			double xCoord;
			double yCoord;
			Map<Integer, FrenchHDRelationship2018StandImpl> standMap = new HashMap<Integer, FrenchHDRelationship2018StandImpl>();
			Map<Integer, FrenchHDRelationship2018ExtStandImpl> extStandMap = new HashMap<Integer, FrenchHDRelationship2018ExtStandImpl>();
			int counter = 0;
			while ((record = reader.nextRecord()) != null) {
				idp = Integer.parseInt(record[0].toString());
				species = record[1].toString();
				pent2 = Double.parseDouble(record[2].toString());
				gOther = Double.parseDouble(record[3].toString());
				d130 = Double.parseDouble(record[4].toString());
				pred = Double.parseDouble(record[5].toString());
				mqd = Double.parseDouble(record[6].toString());
				htot = Double.parseDouble(record[7].toString());
				harvestInLastFiveYears = Double.parseDouble(record[8].toString()); 
				meanTemp_3 = Double.parseDouble(record[9].toString()); 
				meanTemp_4 = Double.parseDouble(record[10].toString()); 
				meanTemp_5 = Double.parseDouble(record[11].toString()); 
				meanTemp_6 = Double.parseDouble(record[12].toString()); 
				meanTemp_7 = Double.parseDouble(record[13].toString()); 
				meanTemp_8 = Double.parseDouble(record[14].toString()); 
				meanTemp_9 = Double.parseDouble(record[15].toString()); 
				meanPrec_3 = Double.parseDouble(record[16].toString()); 
				meanPrec_4 = Double.parseDouble(record[17].toString()); 
				meanPrec_5 = Double.parseDouble(record[18].toString()); 
				meanPrec_6 = Double.parseDouble(record[19].toString()); 
				meanPrec_7 = Double.parseDouble(record[20].toString()); 
				meanPrec_8 = Double.parseDouble(record[21].toString()); 
				meanPrec_9 = Double.parseDouble(record[22].toString()); 
				weight = Double.parseDouble(record[23].toString());
				xCoord = Double.parseDouble(record[24].toString());
				yCoord = Double.parseDouble(record[25].toString());
				double meanPrec = meanPrec_3 + meanPrec_4 + meanPrec_5 + meanPrec_6 + meanPrec_7 + meanPrec_8 + meanPrec_9; 
				double meanTemp = (meanTemp_3 + meanTemp_4 + meanTemp_5 + meanTemp_6 + meanTemp_7 + meanTemp_8 + meanTemp_9) / 7d;
				if (!standMap.containsKey(idp)) {
					int count = counter++;
					FrenchHDRelationship2018StandImpl stand = new FrenchHDRelationship2018StandImpl(count, idp, mqd, pent2, harvestInLastFiveYears, xCoord, yCoord, standList);
					standMap.put(idp, stand);
					//						FrenchHDRelationship2018StandImpl stand = new FrenchHDRelationship2018StandImpl(counter++, idp, mqd, pent2, harvestInLastFiveYears, meanTemp, meanPrec, standList);
					FrenchHDRelationship2018ExtStandImpl extStand = new FrenchHDRelationship2018ExtStandImpl(count, idp, mqd, pent2, harvestInLastFiveYears, meanTemp, meanPrec, extStandList);
					extStandMap.put(idp, extStand);
				}
				new FrenchHDRelationship2018TreeImpl(htot, d130, gOther, species, weight, pred, standMap.get(idp));
				new FrenchHDRelationship2018TreeImpl(htot, d130, gOther, species, weight, pred, extStandMap.get(idp));
			}
			standList.addAll(standMap.values());
			Collections.sort(standList);
			Stands = standList;
			extStandList.addAll(extStandMap.values());
			Collections.sort(extStandList);
			ExtStands = extStandList;
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unable to read the stands");
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	

	private static Map<Integer, Map<Integer, Blup>> readBlups() {
		String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2018PredictorTest.class) + "testBlups.csv";
		CSVReader reader = null; 
		Map<Integer, Map<Integer, Blup>> blupMap = new HashMap<Integer, Map<Integer,Blup>>();
		try {
			reader = new CSVReader(filename);
			Object[] record;
			while ((record = reader.nextRecord()) != null) {
				int index = Integer.parseInt(record[0].toString());
				int plotId = Integer.parseInt(record[1].toString());
				double estimate = Double.parseDouble(record[2].toString());
				double std = Double.parseDouble(record[3].toString());
				if (!blupMap.containsKey(index)) {
					blupMap.put(index, new HashMap<Integer, Blup>());
				}
				Map<Integer, Blup> innerMap = blupMap.get(index);
				innerMap.put(plotId, new Blup(estimate,std));
			}
			
			return blupMap;
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unable to read the blups");
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

}
