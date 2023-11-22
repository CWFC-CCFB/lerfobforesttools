package lerfob.predictor.hdrelationships.frenchgeneralhdrelationship2014;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import lerfob.predictor.hdrelationships.FrenchHDRelationshipTree.FrenchHdSpecies;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.math.SymmetricMatrix;
import repicea.stats.distributions.StandardGaussianDistribution;
import repicea.stats.estimates.Estimate;
import repicea.util.ObjectUtility;

public class FrenchHDRelationship2014PredictorTest {

//	static List<String> speciesList = new ArrayList<String>();
	static List<FrenchHDRelationship2014StandImpl> Stands;
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
		Stands = readTrees();
		FrenchHDRelationship2014TreeImpl.BlupPrediction = false;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor();
		int nbTrees = 0;
		for (int i = 0; i < 10000; i++) {
			FrenchHDRelationship2014StandImpl stand = Stands.get(i);
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getPred();
				Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-4);
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}
	
	@Test
	public void validation2BlupsPredictionsAndVariance() throws IOException {
		Stands = readTrees();
		FrenchHDRelationship2014TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor(true);
		int nbBlups = 0;

		FrenchHDRelationship2014StandImpl s = Stands.get(0);
		List<FrenchHDRelationship2014StandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2014StandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);
		
		for (FrenchHDRelationship2014Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
//				if (tree.getFrenchHDTreeSpecies().ordinal() <= 3) {
				if (FrenchHdSpecies.getSpeciesIn2014().indexOf(tree.getFrenchHDTreeSpecies()) <= 3) {
					int index = FrenchHdSpecies.getSpeciesIn2014().indexOf(tree.getFrenchHDTreeSpecies()) + 1;
					predictor.predictHeightM(stand, tree);
					Estimate<Matrix, SymmetricMatrix, ? extends StandardGaussianDistribution> currentBlups = predictor.getBlups(stand, tree);
					double actualBlup = currentBlups.getMean().getValueAt(0, 0);
					double actualVariance = currentBlups.getVariance().getValueAt(0, 0);
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
		Stands = readTrees();
		FrenchHDRelationship2014TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor(false, true, true);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2014StandImpl s = Stands.get(0);
		List<FrenchHDRelationship2014StandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2014StandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2014Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
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
		Stands = readTrees();
		FrenchHDRelationship2014TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor(false, true, false);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2014StandImpl s = Stands.get(0);
		List<FrenchHDRelationship2014StandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2014StandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2014Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
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
		Stands = readTrees();
		FrenchHDRelationship2014TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor(false, false, true);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2014StandImpl s = Stands.get(0);
		List<FrenchHDRelationship2014StandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2014StandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2014Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
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
		Stands = readTrees();
		FrenchHDRelationship2014TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor(false);	// variability of residual error terms only
		int nbTrees = 0;
		
		FrenchHDRelationship2014StandImpl s = Stands.get(0);
		List<FrenchHDRelationship2014StandImpl> retainedPlots = new ArrayList<FrenchHDRelationship2014StandImpl>();
		for (int i = 0; i < 400; i++) {
			retainedPlots.add(s.standList.get(i));
		}
		s.standList.retainAll(retainedPlots);

		for (FrenchHDRelationship2014Stand stand : s.standList) {
			for (Object obj : stand.getTreesForFrenchHDRelationship()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getHeightM();
				Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-6);
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}

	
	private static List<FrenchHDRelationship2014StandImpl> readTrees() {
		String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2014PredictorTest.class) + "testData.csv";
		List<FrenchHDRelationship2014StandImpl> standList = new ArrayList<FrenchHDRelationship2014StandImpl>();
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
			Map<Integer, FrenchHDRelationship2014StandImpl> standMap = new HashMap<Integer, FrenchHDRelationship2014StandImpl>();
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
				if (!standMap.containsKey(idp)) {
					FrenchHDRelationship2014StandImpl stand = new FrenchHDRelationship2014StandImpl(counter++, idp, mqd, pent2, harvestInLastFiveYears, standList);
					standMap.put(idp, stand);
				}
				new FrenchHDRelationship2014TreeImpl(htot, d130, gOther, species, pred, standMap.get(idp));
//				if (!speciesList.contains(species)) {
//					speciesList.add(species);
//					System.out.println(species);
//				}
			}
//			Collections.sort(speciesList);
			standList.addAll(standMap.values());
			Collections.sort(standList);
			return standList;
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unable to read the stands");
			return null;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	

	private static Map<Integer, Map<Integer, Blup>> readBlups() {
		String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2014PredictorTest.class) + "testBlups.csv";
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
