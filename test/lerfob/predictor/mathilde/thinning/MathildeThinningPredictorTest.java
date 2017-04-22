package lerfob.predictor.mathilde.thinning;

import java.util.ArrayList;
import java.util.List;

import lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies;

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
			int recordId = 1;
			try {
				CSVReader reader = new CSVReader(filenamePath);
				Object[] record;
				
				while((record = reader.nextRecord()) != null) {
					String standId = record[1].toString();
					String treeId = record[2].toString();
					double basalAreaM2Ha = Double.parseDouble(record[5].toString());
					double mqdCm = Double.parseDouble(record[6].toString());
					int dateYr = Integer.parseInt(record[7].toString());
					int speciesCode = Integer.parseInt(record[9].toString());
					double dbhCm = Double.parseDouble(record[10].toString());
					int alreadyCut = Integer.parseInt(record[11].toString());
					int cutPlot = Integer.parseInt(record[13].toString());
					double timeSinceLastCut;
					if (alreadyCut == 1) {
						timeSinceLastCut = Double.parseDouble(record[18].toString());
					} else {
						timeSinceLastCut = Double.POSITIVE_INFINITY;
					}
					int excludedGroup = Integer.parseInt(record[24].toString());
					double linearPlotPred = Double.parseDouble(record[25].toString());
					double linearTreePred = Double.parseDouble(record[26].toString());
					double pred = Double.parseDouble(record[27].toString());
					MathildeTreeSpecies species = MathildeTreeSpecies.getSpecies(speciesCode);
					MathildeThinningStandImpl stand = new MathildeThinningStandImpl(standId, mqdCm, basalAreaM2Ha, dateYr, timeSinceLastCut, excludedGroup, linearPlotPred);
					MathildeThinningTreeImpl tree = new MathildeThinningTreeImpl(treeId, dbhCm, species, cutPlot, linearTreePred, pred, stand);
					Trees.add(tree);
					recordId++;
				}
			} catch (Exception e) {
				System.out.println("Unable to read tree in MathildeThinningPredictorTest class at line : " + recordId);
			}
		}
	}
	
	@Test
	public void PlotPredictionsTest() {
		ReadTrees();
		MathildeStandThinningPredictor standPredictor = new MathildeStandThinningPredictor(false);
		int nbTested = 0;
		for (MathildeThinningTreeImpl tree : Trees) {
			MathildeThinningStandImpl stand = tree.getStand();
			double standProb = standPredictor.predictEventProbability(stand, null, stand.getExcludedGroup());
			double actualLinearPred = Math.log(standProb/(1-standProb));
//			if (Math.abs(stand.getLinearPlotPred() - actualLinearPred) > 1E-8) {
//				int u = 0;
//			}
			Assert.assertEquals(stand.getLinearPlotPred(), actualLinearPred, 1E-8);
			nbTested++;
		}
		System.out.println("Number of plot predictions successfully tested: " + nbTested);
	}
	
	@Test
	public void TreePredictionsFixedEffectsOnlyTest() {
		ReadTrees();
		MathildeTreeThinningPredictor treePredictor = new MathildeTreeThinningPredictor(false);
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
	
	@Test
	public void completeMarginalPredictionsTest() {
		ReadTrees();
		MathildeStandThinningPredictor standPredictor = new MathildeStandThinningPredictor(false);
		MathildeTreeThinningPredictor treePredictor = new MathildeTreeThinningPredictor(false);
		int nbTested = 0;
		for (MathildeThinningTreeImpl tree : Trees) {
//			if (nbTested == 176423) {
//				int u = 0;
//			}
			MathildeThinningStandImpl stand = tree.getStand();
			double standPred = standPredictor.predictEventProbability(stand, tree, stand.getExcludedGroup());
			double treePred = treePredictor.predictEventProbability(stand, tree, tree.getExcludedGroup());
			double marginalPred = standPred * treePred;
			Assert.assertEquals("Testing obs " + ++nbTested, tree.getPrediction(), marginalPred, 1E-6);
		}
		System.out.println("Number of tree predictions successfully tested: " + nbTested);
	}
	
	
	

}
