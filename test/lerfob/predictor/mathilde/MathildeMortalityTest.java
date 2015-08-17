package lerfob.predictor.mathilde;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import repicea.io.javacsv.CSVReader;
import repicea.math.Matrix;
import repicea.stats.distributions.NonparametricDistribution;
import repicea.stats.integral.GaussHermiteQuadrature;
import repicea.stats.integral.GaussQuadrature.NumberOfPoints;
import repicea.util.ObjectUtility;

public class MathildeMortalityTest {
	
	private static List<MathildeTree> trees;
	
	
	private static void readTrees() throws IOException {
		if (trees == null) {
			String path = ObjectUtility.getRelativePackagePath(MathildeMortalityTest.class);
			String testFilename = path + "dataBaseMortalityPredictions.csv";
			trees = new ArrayList<MathildeTree>();
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
					trees.add(tree);
				}
			} catch (NumberFormatException e) {
				System.out.println("Unable to read record " + (trees.size() + 1));
			}
		}
	}
	
	@Test
	public void testInDeterministicMode() throws IOException {
		readTrees();
		
		MathildeMortalityPredictor predictor = new MathildeMortalityPredictor(false, false, false);
		predictor.ghq = new GaussHermiteQuadrature(NumberOfPoints.N5);
		
		int nbTrees = 0;
		MathildeMortalityStand stand;
		for (MathildeTree tree : trees) {
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
		System.out.println(nbTrees + " out of " + trees.size() + " trees have been successfully tested!");
	}
	
	@Test
	public void testInStochasticModeWithRandomEffectOnly() throws IOException {
		int nbReal = 100000;
		readTrees();

		List<MathildeTree> firstTenTreesWithWindstorm = new ArrayList<MathildeTree>();
		int nbTrees = 0;
		for (MathildeTree tree : trees) {
			if (((MathildeMortalityTreeImpl) tree).getStand().isAWindstormGoingToOccur()) {
				firstTenTreesWithWindstorm.add(tree);
				nbTrees++;
				if (nbTrees == 10) {
					break;
				}
			}
		}
		
		
		MathildeMortalityPredictor stochasticPredictor = new MathildeMortalityPredictor(false, true, false);
		MathildeMortalityPredictor deterministicPredictor = new MathildeMortalityPredictor(false, false, false);
		
		for (MathildeTree tree : firstTenTreesWithWindstorm) {
			MathildeMortalityStand stand = ((MathildeMortalityTreeImpl) tree).getStand();
			NonparametricDistribution dist = new NonparametricDistribution();
			Matrix result;
			for (int i = 0; i < nbReal; i++) {
				stand.setMonteCarloRealizationId(i);
				result = new Matrix(1,1);
				result.m_afData[0][0] = stochasticPredictor.predictEventProbability(stand, tree);
				dist.addRealization(result);
			}

			double meanDeterministic = deterministicPredictor.predictEventProbability(stand, tree);
			double meanStochastic = dist.getMean().m_afData[0][0];

			System.out.println("Deterministic : " + meanDeterministic + "; Stochastic : " + meanStochastic);			
			assertEquals(meanDeterministic, meanStochastic, 0.009);
		}
	}
	
	
}
