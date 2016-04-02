package lerfob.treelogger.mathilde;

import org.junit.Assert;
import org.junit.Test;

import repicea.simulation.treelogger.WoodPiece;

public class MathildeTreeLoggerTests {

	
	
	@Test
	public void testQuercusLargeSawlogBalance() {
		MathildeTreeLogger logger = new MathildeTreeLogger();
		logger.setTreeLoggerParameters(logger.createDefaultTreeLoggerParameters());
		
		MathildeLoggableTree tree = new MathildeLoggableTreeImpl();
		logger.logThisTree(tree);
		double volume = 0d;
		for (WoodPiece woodPiece : logger.getWoodPieces().get(tree)) {
			volume += woodPiece.getWeightedVolumeM3();
		}
		Assert.assertEquals("Test volume balance for a large oak", 1d, volume, 1E-8);
	}
}
