package lerfob.predictor.frenchgeneralhdrelationship2014;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.simulation.ParameterLoader;
import repicea.simulation.ParameterMap;
import repicea.util.ObjectUtility;

public class FrenchHDRelationship2014PredictorTest {

	static List<String> speciesList = new ArrayList<String>();
	static Collection<FrenchHDRelationship2014StandImpl> Stands = readTrees();
	static ParameterMap Blups = readBlups();
	

	@Test
	public void validation1FixedEffectPredictions() throws IOException {
		FrenchHDRelationship2014TreeImpl.BlupPrediction = false;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor();
		int nbTrees = 0;
		for (FrenchHDRelationship2014Stand stand : Stands) {
			for (Object obj : stand.getTrees()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
				double actual = predictor.predictHeightM(stand, tree);
				double expected = tree.getPred();
				Assert.assertEquals("Comparting tree in plot " + stand.getSubjectId(), expected, actual, 1E-6);
				nbTrees++;
			}
		}
		System.out.println("Successfully compared " + nbTrees + " trees.");
	}
	
	@Ignore
	@Test
	public void validation2BlupsPredictions() throws IOException {
		FrenchHDRelationship2014TreeImpl.BlupPrediction = true;
		FrenchHDRelationship2014Predictor predictor = new FrenchHDRelationship2014Predictor();
		int nbBlups = 0;
		for (FrenchHDRelationship2014Stand stand : Stands) {
			for (Object obj : stand.getTrees()) {
				FrenchHDRelationship2014TreeImpl tree = (FrenchHDRelationship2014TreeImpl) obj;
				if (tree.getFrenchHDTreeSpecies().ordinal() <= 3) {
					int index = tree.getFrenchHDTreeSpecies().ordinal() + 1;
					predictor.predictHeightM(stand, tree);
					double actualBlup = predictor.getBlups(stand, tree).m_afData[0][0];
					double expectedBlup = Blups.get(index, stand.getSubjectId()).m_afData[0][0];
					Assert.assertEquals("Comparing blups for species = " + tree.getFrenchHDTreeSpecies().name() + " in plot " + stand.getSubjectId(), 
							expectedBlup, 
							actualBlup,
							1E-5);
					nbBlups++;
				}
			}
		}
		System.out.println("Successfully compared " + nbBlups + " blups.");
	}

	private static Collection<FrenchHDRelationship2014StandImpl> readTrees() {
		String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2014PredictorTest.class) + "testData.csv";
		try {
			CSVReader reader = new CSVReader(filename);
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
					FrenchHDRelationship2014StandImpl stand = new FrenchHDRelationship2014StandImpl(counter++, idp, mqd, pent2, harvestInLastFiveYears);
					standMap.put(idp, stand);
				}
				new FrenchHDRelationship2014TreeImpl(htot, d130, gOther, species, pred, standMap.get(idp));
				if (!speciesList.contains(species)) {
					speciesList.add(species);
				}
			}
			Collections.sort(speciesList);
			List<FrenchHDRelationship2014StandImpl> standList = new ArrayList<FrenchHDRelationship2014StandImpl>();
			standList.addAll(standMap.values());
			Collections.sort(standList);
			return standList;
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unable to read the stands");
			return null;
		}
	}
	

	private static ParameterMap readBlups() {
		String filename = ObjectUtility.getPackagePath(FrenchHDRelationship2014PredictorTest.class) + "testBlups.csv";
		try {
			return ParameterLoader.loadVectorFromFile(2, filename);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail("Unable to read the blups");
			return null;
		}
	}

}
