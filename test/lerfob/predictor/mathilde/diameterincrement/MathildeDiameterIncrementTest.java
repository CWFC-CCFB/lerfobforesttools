package lerfob.predictor.mathilde.diameterincrement;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import lerfob.predictor.mathilde.MathildeTree;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.stats.distributions.EmpiricalDistribution;
import repicea.util.ObjectUtility;

public class MathildeDiameterIncrementTest {

	private static List<MathildeTree> trees;
	
	
	private static void readTrees() throws IOException {
		if (trees == null) {
			String path = ObjectUtility.getRelativePackagePath(MathildeDiameterIncrementTest.class);
			String testFilename = path + "growthDataTest.csv";
			trees = new ArrayList<MathildeTree>();
			CSVReader reader = new CSVReader(testFilename);
			Object[] record;
			double diam0;
			double bal22;
			double bal42;
			int grEss;
			double st;
			int upcomingCut;
			double deltaT;
			double logPred;
			double tIntervalVeg6;
			MathildeTree tree;

			while ((record = reader.nextRecord()) != null) {
				logPred = Double.parseDouble(record[0].toString());
				diam0 = Double.parseDouble(record[1].toString());
				bal22 = Double.parseDouble(record[3].toString());
				bal42 = Double.parseDouble(record[4].toString());
				st = Double.parseDouble(record[6].toString());
				grEss = Integer.parseInt(record[7].toString());
				deltaT = Double.parseDouble(record[8].toString());
				upcomingCut = Integer.parseInt(record[9].toString());
				tIntervalVeg6 = Double.parseDouble(record[10].toString());
				
				tree = new MathildeDiameterIncrementTreeImpl(diam0, 
						bal22,
						bal42,
						grEss, 
						st, 
						upcomingCut, 
						deltaT, 
						logPred,
						tIntervalVeg6);
				trees.add(tree);
			}
			reader.close();
		}
	}
	
	@Test
	public void testInDeterministicMode() throws IOException {
		readTrees();
		
		MathildeDiameterIncrementPredictor predictor = new MathildeDiameterIncrementPredictor(false);
		
		MathildeDiameterIncrementStand stand;
		int nbTrees = 0;
		for (MathildeTree tree : trees) {
			stand = ((MathildeDiameterIncrementTreeImpl) tree).getStand();
			double actual = predictor.getFixedEffectOnlyPrediction(stand, tree);
			double expected = ((MathildeDiameterIncrementTreeImpl) tree).getPred();
			assertEquals(expected, actual, 1E-10);
			nbTrees++;
		}
		System.out.println("Trees succesfully compared : " + nbTrees);
	}
	
	@Test
	public void testInDeterministicModeWithBackTransformationCorrection() throws IOException {
		readTrees();
		
		MathildeDiameterIncrementPredictor predictor = new MathildeDiameterIncrementPredictor(false);
		
		MathildeDiameterIncrementStand stand;
		int nbTrees = 0;
		for (MathildeTree tree : trees) {
			stand = ((MathildeDiameterIncrementTreeImpl) tree).getStand();
			double actual = predictor.predictGrowth(stand, tree);
			double expected = ((MathildeDiameterIncrementTreeImpl) tree).getBacktransformedPred(predictor.subModules.get(0).errorTotalVariance);
			if (expected < 0) {
				expected = 0;
			}
			assertEquals(expected, actual, 1E-4);
			nbTrees++;
		}
		System.out.println("Trees succesfully compared : " + nbTrees);
	}
	
	@Test
	public void testInStochasticModeWithoutParameterVariability() throws IOException {
		int nbReal = 50000;
		readTrees();
		
		MathildeDiameterIncrementPredictor predictor = new MathildeDiameterIncrementPredictor(false, true, true);
		
		MathildeTree tree = trees.get(0);
		MathildeDiameterIncrementStand stand = ((MathildeDiameterIncrementTreeImpl) tree).getStand();
		EmpiricalDistribution dist = new EmpiricalDistribution();
		Matrix result;
		for (int i = 0; i < nbReal; i++) {
//			tree.setMonteCarloRealizationId(i);
			((MathildeDiameterIncrementStandImpl) stand).setMonteCarloRealizationId(i);
			result = new Matrix(1,1);
			result.setValueAt(0, 0, predictor.predictGrowth(stand, tree)); 
			dist.addRealization(result);
		}
		double meanDeterministic = ((MathildeDiameterIncrementTreeImpl) tree).getBacktransformedPred(predictor.subModules.get(0).errorTotalVariance);
		double meanStochastic = dist.getMean().getValueAt(0, 0);
		
		assertEquals(meanDeterministic, meanStochastic, 0.02);
		
	}
	
	@Test
	public void testInStochasticModeWithParameterVariability() throws IOException {
		int nbReal = 50000;
		readTrees();
		
		MathildeDiameterIncrementPredictor predictor = new MathildeDiameterIncrementPredictor(true);
		
		MathildeTree tree = trees.get(0);
		MathildeDiameterIncrementStand stand = ((MathildeDiameterIncrementTreeImpl) tree).getStand();
		EmpiricalDistribution dist = new EmpiricalDistribution();
		Matrix result;
		for (int i = 0; i < nbReal; i++) {
			((MathildeDiameterIncrementStandImpl) stand).setMonteCarloRealizationId(i);
			result = new Matrix(1,1);
			result.setValueAt(0, 0, predictor.predictGrowth(stand, tree)); 
			dist.addRealization(result);
		}
		double meanDeterministic = ((MathildeDiameterIncrementTreeImpl) tree).getBacktransformedPred(predictor.subModules.get(0).errorTotalVariance);
		double meanStochastic = dist.getMean().getValueAt(0, 0);
		
		assertEquals(meanDeterministic, meanStochastic, 0.05);			// subject to failure since the variability in the parameter estimates increases the error variance

	}
	
	
	
}
