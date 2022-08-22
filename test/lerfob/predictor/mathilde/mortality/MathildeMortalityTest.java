package lerfob.predictor.mathilde.mortality;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import lerfob.predictor.mathilde.MathildeTree;
import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.stats.distributions.EmpiricalDistribution;
import repicea.stats.integral.AbstractGaussQuadrature.NumberOfPoints;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.util.ObjectUtility;

public class MathildeMortalityTest {
	
	private static List<MathildeTree> treesOriginalModel;
	private static List<MathildeTree> treesImprovedModel;
	
	
	private static void readTreesOriginalModel() throws IOException {
		if (treesOriginalModel == null) {
			String path = ObjectUtility.getRelativePackagePath(MathildeMortalityTest.class);
			String testFilename = path + "dataBaseMortalityPredictions.csv";
			treesOriginalModel = new ArrayList<MathildeTree>();
			CSVReader reader = new CSVReader(testFilename);
			Object[] record;
			double diam0;
			double bal22;
			double bal42;
			int grEss;
			int upcomingCut;
			double deltaT;
			double pred;
			int upcomingDrought;
			int upcomingWindstorm;
			MathildeTree tree;

			try {
				while ((record = reader.nextRecord()) != null) {
					pred = Double.parseDouble(record[14].toString());
					bal22 = Double.parseDouble(record[5].toString());
					bal42 = Double.parseDouble(record[6].toString());
					grEss = Integer.parseInt(record[8].toString());
					diam0 = Double.parseDouble(record[4].toString());
					int year0 = Integer.parseInt(record[3].toString());
					int year1 = Integer.parseInt(record[7].toString());
					deltaT = year1 - year0;
					upcomingCut = Integer.parseInt(record[9].toString());
					upcomingDrought = Integer.parseInt(record[11].toString());
					upcomingWindstorm = Integer.parseInt(record[10].toString());
					tree = new MathildeMortalityTreeImpl(pred, 
							diam0, 
							grEss, 
							bal22, 
							bal42, 
							deltaT, 
							upcomingCut, 
							upcomingDrought,
							upcomingWindstorm);
					treesOriginalModel.add(tree);
				}
			} catch (NumberFormatException e) {
				System.out.println("Unable to read record " + (treesOriginalModel.size() + 1));
			} finally {
				reader.close();
			}
		}
	}
	
	private static void readTreesImprovedModel() throws IOException {
		if (treesImprovedModel == null) {
			String path = ObjectUtility.getRelativePackagePath(MathildeMortalityTest.class);
			String testFilename = path + "dataBaseMortality2Predictions.csv";
			treesImprovedModel = new ArrayList<MathildeTree>();
			CSVReader reader = new CSVReader(testFilename);
			Object[] record;
			double diam0;
			double bal22;
			double bal42;
			int grEss;
			int upcomingCut;
			double deltaT;
			double pred;
			int upcomingDrought;
			int upcomingWindstorm;
			MathildeTree tree;

			try {
				while ((record = reader.nextRecord()) != null) {
					pred = Double.parseDouble(record[14].toString());
					bal22 = Double.parseDouble(record[5].toString());
					bal42 = Double.parseDouble(record[6].toString());
					grEss = Integer.parseInt(record[8].toString());
					diam0 = Double.parseDouble(record[4].toString());
					int year0 = Integer.parseInt(record[3].toString());
					int year1 = Integer.parseInt(record[7].toString());
					deltaT = year1 - year0;
					upcomingCut = Integer.parseInt(record[9].toString());
					upcomingDrought = Integer.parseInt(record[11].toString());
					upcomingWindstorm = Integer.parseInt(record[10].toString());
					tree = new MathildeMortalityTreeImpl(pred, 
							diam0, 
							grEss, 
							bal22, 
							bal42, 
							deltaT, 
							upcomingCut, 
							upcomingDrought,
							upcomingWindstorm);
					treesImprovedModel.add(tree);
				}
			} catch (NumberFormatException e) {
				System.out.println("Unable to read record " + (treesImprovedModel.size() + 1));
			} finally {
				reader.close();
			}
		}
	}

	@Test
	public void testInDeterministicModeOriginalModel() throws IOException {
		readTreesOriginalModel();
		
		MathildeMortalityPredictor predictor = new MathildeMortalityPredictor(false);
		predictor.ghq = new GaussHermiteQuadrature(NumberOfPoints.N5);
		
		int nbTrees = 0;
		MathildeMortalityStand stand;
		for (MathildeTree tree : treesOriginalModel) {
			stand = ((MathildeMortalityTreeImpl) tree).getStand();
			double actual = predictor.predictEventProbability(stand, tree);
			double expected = ((MathildeMortalityTreeImpl) tree).getPred();
			double delta = 1E-5;
//			if (Math.abs(actual - expected) > delta) {
//				int u = 0;
//			}
			assertEquals(expected, actual, delta);
			nbTrees++;
		}
		System.out.println(nbTrees + " out of " + treesOriginalModel.size() + " trees have been successfully tested!");
	}
	
	@Test
	public void testInStochasticModeWithRandomEffectOnlyOriginalModel() throws IOException {
		int nbReal = 100000;
		readTreesOriginalModel();

		List<MathildeTree> firstTenTreesWithWindstorm = new ArrayList<MathildeTree>();
		int nbTrees = 0;
		for (MathildeTree tree : treesOriginalModel) {
			if (((MathildeMortalityTreeImpl) tree).getStand().isAWindstormGoingToOccur()) {
				firstTenTreesWithWindstorm.add(tree);
				nbTrees++;
				if (nbTrees == 10) {
					break;
				}
			}
		}
		
		
		MathildeMortalityPredictor stochasticPredictor = new MathildeMortalityPredictor(false, true, false);
		MathildeMortalityPredictor deterministicPredictor = new MathildeMortalityPredictor(false);
		
		for (MathildeTree tree : firstTenTreesWithWindstorm) {
			MathildeMortalityStand stand = ((MathildeMortalityTreeImpl) tree).getStand();
			EmpiricalDistribution dist = new EmpiricalDistribution();
			Matrix result;
			for (int i = 0; i < nbReal; i++) {
				((MathildeMortalityStandImpl) stand).setMonteCarloRealizationId(i);
				result = new Matrix(1,1);
				result.setValueAt(0, 0, stochasticPredictor.predictEventProbability(stand, tree));
				dist.addRealization(result);
			}

			double meanDeterministic = deterministicPredictor.predictEventProbability(stand, tree);
			double meanStochastic = dist.getMean().getValueAt(0, 0);

			System.out.println("Deterministic : " + meanDeterministic + "; Stochastic : " + meanStochastic);			
			assertEquals(meanDeterministic, meanStochastic, 0.01);
		}
	}
	
	@Test
	public void testInDeterministicModeImprovedModel() throws IOException {
		readTreesImprovedModel();
		
		MathildeImprovedMortalityPredictor predictor = new MathildeImprovedMortalityPredictor(false);
		predictor.ghq = new GaussHermiteQuadrature(NumberOfPoints.N5);
		
		int nbTrees = 0;
		MathildeMortalityStand stand;
		for (MathildeTree tree : treesImprovedModel) {
			stand = ((MathildeMortalityTreeImpl) tree).getStand();
			double actual = predictor.predictEventProbability(stand, tree);
			double expected = ((MathildeMortalityTreeImpl) tree).getPred();
			double delta = 1E-5;
//			if (Math.abs(actual - expected) > delta) {
//				int u = 0;
//			}
			assertEquals(expected, actual, delta);
			nbTrees++;
		}
		System.out.println(nbTrees + " out of " + treesImprovedModel.size() + " trees have been successfully tested!");
	}

	
	
	@Test
	public void testInStochasticModeWithRandomEffectOnlyImprovedModel() throws IOException {
		int nbReal = 100000;
		readTreesImprovedModel();

		List<MathildeTree> firstTenTreesWithWindstorm = new ArrayList<MathildeTree>();
		int nbTrees = 0;
		for (MathildeTree tree : treesImprovedModel) {
			if (((MathildeMortalityTreeImpl) tree).getStand().isAWindstormGoingToOccur()) {
				firstTenTreesWithWindstorm.add(tree);
				nbTrees++;
				if (nbTrees == 10) {
					break;
				}
			}
		}
		
		
		MathildeImprovedMortalityPredictor stochasticPredictor = new MathildeImprovedMortalityPredictor(false, true, false);
		MathildeImprovedMortalityPredictor deterministicPredictor = new MathildeImprovedMortalityPredictor(false);
		
		for (MathildeTree tree : firstTenTreesWithWindstorm) {
			MathildeMortalityStand stand = ((MathildeMortalityTreeImpl) tree).getStand();
			EmpiricalDistribution dist = new EmpiricalDistribution();
			Matrix result;
			for (int i = 0; i < nbReal; i++) {
				((MathildeMortalityStandImpl) stand).setMonteCarloRealizationId(i);
				result = new Matrix(1,1);
				result.setValueAt(0, 0, stochasticPredictor.predictEventProbability(stand, tree));
				dist.addRealization(result);
			}

			double meanDeterministic = deterministicPredictor.predictEventProbability(stand, tree);
			double meanStochastic = dist.getMean().getValueAt(0, 0);

			System.out.println("Deterministic : " + meanDeterministic + "; Stochastic : " + meanStochastic);			
			assertEquals(meanDeterministic, meanStochastic, 0.012);
		}
	}

}
