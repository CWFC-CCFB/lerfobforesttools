package lerfob.windstormdamagemodels.bockwinddamagemodel;

import org.junit.Test;



/**
 * Implementation of JUnit tests for Bock et al.'s wind damage model.
 * @author Mathieu Fortin - October 2010
 */
public class BWDTest {

	@Test
	public void BWDTestAtStandLevel() throws Exception {
		BWDTestStand stand = new BWDTestStand(40, false, false, true);
		
		BWDModel model = new BWDModel();
		
		double proportion = model.getProportionOfDamagedTrees(stand, 130);
		
		System.out.println("Predicted proportion = " + proportion);

		BWDTestTree tree;
		double probability;
		for (int height = 15; height <= 30; height += 5) {
			for (int dbh = 25; dbh <=40; dbh += 5) {
				for (double crownLength = height * .1; crownLength <= height * .5; crownLength += height * .2) {
					for (double crownRadius = 4; crownRadius <= 8; crownRadius += 2) {
						for (int soil = 40; soil <= 80; soil += 20) {
							tree = new BWDTestTree(height, dbh, soil, crownLength, crownRadius);
							probability = model.getProbabilityOfDamageForThisTree(tree);
							System.out.println("Height = " + height + "; Dbh = " + dbh + "; CrownLength = " + crownLength + "; Soil = " + soil + "; CrownRadius =" + crownRadius + "; Probability = " + probability);
						}
					}
				}
			}
		}
	}

}
