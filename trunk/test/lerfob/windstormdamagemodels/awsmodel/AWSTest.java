package lerfob.windstormdamagemodels.awsmodel;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lerfob.windstormdamagemodels.awsmodel.AWSTree.AWSTreeSpecies;

import org.junit.Ignore;
import org.junit.Test;



public class AWSTest {

	protected static class AWSModelImpl extends AlbrechtWindStormModel {
		protected AWSModelImpl() {
			super();
		}
		
		@Override
		protected Boolean getResultForThisTree(AWSTree tree) throws Exception {
			return super.getResultForThisTree(tree);
		}
			
	}
	
	
	/**
	 * This test checks if the three stand-level predictions are valid.
	 * @throws Exception
	 */
	@Ignore		// This test has never worked
	@Test
	public void AWSTestAtStandLevel() throws Exception {
		Collection<AWSTestStand> stands = null;
		
		try {
			URL url = this.getClass().getResource(this.getClass().getSimpleName() + ".class");
			File file = new File(url.toURI());
			String pathName = file.getParent();
			String testStands = pathName + File.separator + "AWSTestStandFile.txt";
			stands = AWSTestFileReader.instantiateTestStands(testStands);
		} catch (Exception e) {
			System.out.println("Problem while reading the test stands " + e.getMessage());
			e.printStackTrace();
			throw e;
		}
	
		Map<AWSTreeSpecies, Boolean[]> diagnosticMap = new HashMap<AWSTreeSpecies, Boolean[]>();
		for (AWSTreeSpecies species : AWSTreeSpecies.values()) {
			Boolean[] boolArray = new Boolean[3];
			for (int i = 0; i < boolArray.length; i++) {
				boolArray[i] = new Boolean(true);
			}
			diagnosticMap.put(species, boolArray);
		}
		
		try {
			if (stands != null && !stands.isEmpty()) {
				AlbrechtWindStormModel aWSModel = new AlbrechtWindStormModel();
				for (AWSTestStand stand : stands) {
					aWSModel.getPredictionForThisStand(stand, stand);
					stand.compareStandPredictions(diagnosticMap.get(stand.getDominantSpecies()));
				}
			}
		} catch (Exception e) {
			System.out.println("Problem while running AWS model " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		for (Boolean[] boolArray : diagnosticMap.values()) {
			for (boolean bool : boolArray) {
				try {
					assertEquals(true, bool);
				} catch (AssertionError e) {
					System.out.println("At least one species has a modeling step that does not work properly");
					for (AWSTreeSpecies species : diagnosticMap.keySet()) {
						Boolean[] boolArrayForThisSpecies = diagnosticMap.get(species);
						String responses = "";
						for (int i = 0; i < boolArrayForThisSpecies.length; i++) {
							String booleanString = boolArrayForThisSpecies[i].toString();
							responses += booleanString + " ";
						}
						System.out.println(species.toString() + " " + responses);
					}
					throw e;
				}
			}
		}
	}
	

	@Test
	public void AWSTestAtTreeLevelWithKnownOffset() throws Exception {
		Collection<AWSTestStand> stands = null;

		try {
			URL url = this.getClass().getResource(this.getClass().getSimpleName() + ".class");
			File file = new File(url.toURI());
			String pathName = file.getParent();
			String testTrees = pathName + File.separator + "TreeTest.txt";
			stands = AWSTestFileReader.instantiateTestTrees(testTrees);
		} catch (Exception e) {
			System.out.println("Problem while reading the test stands " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		try {
			if (stands != null && !stands.isEmpty()) {
				AWSModelImpl windModel = new AWSModelImpl();
				for (AWSTestStand stand : stands) {
					windModel.getPredictionForThisStand(stand, stand);
					for (AWSTree tree : stand.getAlbrechtWindStormModelTrees()) {
						windModel.getResultForThisTree(tree);
						double expected = ((AWSTestTree) tree).getPredictedProbabilityFromFile();
						double actual = tree.getProbability();
						assertEquals(expected, actual, 1E-8);
					}
				}
			}
			
		} catch (Exception e) {
			System.out.println("Problem while running AWS model " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

	}
	
	
	/*
	 * This test is expected not to work because the known offset contains a plot random effect
	 * @throws Exception
	 */
	@Ignore
	@Test
	public void AWSTestOffset() throws Exception {
		Collection<AWSTestStand> stands = null;

		try {
			URL url = this.getClass().getResource(this.getClass().getSimpleName() + ".class");
			File file = new File(url.toURI());
			String pathName = file.getParent();
			String testTrees = pathName + File.separator + "TreeTest.txt";
			stands = AWSTestFileReader.instantiateTestTrees(testTrees);
		} catch (Exception e) {
			System.out.println("Problem while reading the test stands " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

		try {
			if (stands != null && !stands.isEmpty()) {
				AWSModelImpl windModel = new AWSModelImpl();
				for (AWSTestStand stand : stands) {
					windModel.getPredictionForThisStand(stand, stand);
					for (AWSTree tree : stand.getAlbrechtWindStormModelTrees()) {
						double probability = stand.getProbabilities()[2];
						double expected = ((AWSTestTree) tree).getPredictedOffsetFromFile();
						double actual = Math.log(probability / (1 - probability));
						assertEquals(expected, actual, 1E-8);
					}
				}
			}
			
		} catch (Exception e) {
			System.out.println("Problem while running AWS model " + e.getMessage());
			e.printStackTrace();
			throw e;
		}

	}

	
	
}
