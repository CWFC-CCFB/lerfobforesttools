package lerfob.predictor.frenchcommercialvolume2014;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.util.ObjectUtility;

public class FrenchCommercialVolume2014PredictorTest {

	@Test
	public void testAgainstSASPredictions() throws IOException {
		FrenchCommercialVolume2014Predictor predictor = new FrenchCommercialVolume2014Predictor(false, false);
		List<FrenchCommercialVolume2014TreeImpl> trees = readTrees();
		int nbTrees = 0;
		for (FrenchCommercialVolume2014TreeImpl tree : trees) {
			double actual = predictor.predictTreeCommercialVolumeDm3(tree);
			double expected = tree.getPred();
			Assert.assertEquals("Testing tree " + tree.getSubjectId(), expected, actual, 1E-5);
			nbTrees++;
		}
		System.out.println("Nb of trees successfully tested " + nbTrees);
	}
	
	private List<FrenchCommercialVolume2014TreeImpl> readTrees() throws IOException {
		List<FrenchCommercialVolume2014TreeImpl> trees = new ArrayList<FrenchCommercialVolume2014TreeImpl>();
		String filename = ObjectUtility.getRelativePackagePath(getClass()) + "0_refTest.csv";
		
		CSVReader reader = new CSVReader(filename);
		Object[] record;
		double dbhCm;
		double heightM;
		String speciesName;
		double pred;
		FrenchCommercialVolume2014TreeImpl tree;
		int id = 0;
		while((record = reader.nextRecord()) != null) {
			dbhCm = Double.parseDouble(record[1].toString());
			heightM = Double.parseDouble(record[2].toString());
			speciesName = record[0].toString();
			pred = Double.parseDouble(record[3].toString());
			
			tree = new FrenchCommercialVolume2014TreeImpl(id, dbhCm, heightM, speciesName, pred);
			trees.add(tree);
			id++;
		}
		reader.close();
		return trees;
	}
	
	
	
}
