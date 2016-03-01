package lerfob.predictor.mathilde.thinning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.util.ObjectUtility;

public class MathildeThinningPredictorTest {

	private static List<MathildeThinningTreeImpl> Trees;
	
	
	
	private static void ReadTrees() {
		if (Trees == null) {
			Trees = new ArrayList<MathildeThinningTreeImpl>();
			
			String filenamePath = ObjectUtility.getRelativePackagePath(MathildeThinningPredictorTest.class) + "dataBaseThinningGlobalPredictions.csv";
			
			try {
				CSVReader reader = new CSVReader(filenamePath);
				Object[] record;
				
				while((record = reader.nextRecord()) != null) {
					String standId = record[1].toString();
					String treeId = record[2].toString();
					int cutPlot = Integer.parseInt(record[10].toString());
					int speciesCode = Integer.parseInt(record[5].toString());
					int dateYr = Integer.parseInt(record[6].toString());
					double dbhCm = Double.parseDouble(record[8].toString());
					double mqdCm = Double.parseDouble(record[13].toString());
					int excludedGroup = Integer.parseInt(record[22].toString());
					double linearPlotPred = Double.parseDouble(record[23].toString());
					double linearTreePred = Double.parseDouble(record[24].toString());
					double pred = Double.parseDouble(record[25].toString());
					int timeSinceLastCut = Integer.parseInt(record[16].toString());
					MathildeThinningStandImpl stand = new MathildeThinningStandImpl(standId, mqdCm, dateYr, timeSinceLastCut, excludedGroup, linearPlotPred);
					MathildeThinningTreeImpl tree = new MathildeThinningTreeImpl(treeId, dbhCm, speciesCode, cutPlot, linearTreePred, pred, stand);
					Trees.add(tree);
				}
			} catch (IOException e) {
				System.out.println("Unable to read trees in MathildeThinningPredictorTest class!");
			}
		}
	}
	
	@Test
	public void PlotPredictionsTest() {
		ReadTrees();
		MathildeStandThinningPredictor standPredictor = new MathildeStandThinningPredictor(false, false);
		int nbTested = 0;
		for (MathildeThinningTreeImpl tree : Trees) {
			MathildeThinningStandImpl stand = tree.getStand();
			double standProb = standPredictor.predictEventProbability(stand, null, stand.getExcludedGroup());
			double actualLinearPred = Math.log(standProb/(1-standProb));
			Assert.assertEquals(stand.getLinearPlotPred(), actualLinearPred, 1E-8);
			nbTested++;
		}
		System.out.println("Number of plot predictions successfully tested: " + nbTested);
	}
	
	@Test
	public void TreePredictionsFixedEffectsOnlyTest() {
		ReadTrees();
		MathildeTreeThinningPredictor treePredictor = new MathildeTreeThinningPredictor(false, false, false);
		int nbTested = 0;
		for (MathildeThinningTreeImpl tree : Trees) {
			if (tree.isCutPlot()) {
				MathildeThinningStandImpl stand = tree.getStand();
				MathildeThinningSubModule subModule = treePredictor.getSubModule(tree.getExcludedGroup());
				Matrix beta = subModule.getParameters(stand);
				double actualLinearPred = treePredictor.getFixedEffectOnlyPrediction(beta, stand, tree);
//				if (Math.abs(actualLinearPred - tree.getLinearTreePred()) > 1E-8) {
//					int u = 0;
//				}
				Assert.assertEquals(tree.getLinearTreePred(), actualLinearPred, 1E-5);
				nbTested++;
			}
		}
		System.out.println("Number of tree predictions successfully tested: " + nbTested);
	}

}
