package lerfob.treelogger.douglasfirfcba;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.simulation.treelogger.WoodPiece;

public class DouglasFCBATreeLoggerTests {

	
	
	@Test
	public void testDouglasFirLargeSawlogBalance() {
		DouglasFCBATreeLogger logger = new DouglasFCBATreeLogger();
		logger.setTreeLoggerParameters(logger.createDefaultTreeLoggerParameters());

		List<DouglasFCBALoggableTree> trees = new ArrayList<DouglasFCBALoggableTree>();
		for (int i = 1; i < 15; i++) {
			trees.clear();
			trees.add(new DouglasFCBALoggableTreeImpl(i * 5));
			logger.init(trees);
			logger.run();
			double volume = 0d;
			for (WoodPiece woodPiece : logger.getWoodPieces().get(trees.get(0))) {
				volume += woodPiece.getWeightedTotalVolumeM3();
			}
			Assert.assertEquals("Test volume balance for a collection of douglas trees", 1d, volume, 1E-8);
		}
	}
}
