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
import repicea.util.ObjectUtility;

public class MathildeRecruitmentTest {

	@SuppressWarnings("resource")
	@Test
	public void numbersOfRecruitsTest() throws Exception {
		List<MathildeTreeImpl> trees = new ArrayList<MathildeTreeImpl>();
		Map<String, MathildeRecruitmentStand> standMap = new HashMap<String, MathildeRecruitmentStand>();
		String filename = ObjectUtility.getPackagePath(getClass()) + "zinb_pred.csv";
		Object[] record;
		CSVReader reader = new CSVReader(filename);
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
			((MathildeRecruitmentStandImpl) stand).setSpecies(thisSpecies, gThisSpecies);
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
			MathildeTreeImpl tree = new MathildeTreeImpl(stand, thisSpecies, predictions);
			trees.add(tree);
		}
		reader.close();
		
		MathildeRecruitmentNumberPredictor pred = new MathildeRecruitmentNumberPredictor(false);

		int nbTreesTested = 0;
		for (MathildeTreeImpl tree : trees) {
			Matrix predictions = pred.getMarginalPredictionsForThisStandAndSpecies(tree.getStand(), tree.getMathildeTreeSpecies(), true);
			double[] expectedValues = tree.getPredictions();
			Assert.assertEquals("Testing the number of predicted values", predictions.m_iCols, expectedValues.length);
			for (int j = 0; j < predictions.m_iCols; j++) {
				double expected = expectedValues[j];
				double actual = predictions.m_afData[0][j];
				Assert.assertEquals(expected, actual, 2E-7);
			}
			nbTreesTested++;
		}
		System.out.println("Prediction of number of recruits successfully tested on " + nbTreesTested + " trees.");
	}
	
	
	
	
	
}
