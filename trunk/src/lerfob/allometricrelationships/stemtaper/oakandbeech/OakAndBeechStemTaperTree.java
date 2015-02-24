package lerfob.allometricrelationships.stemtaper.oakandbeech;

import repicea.simulation.covariateproviders.treelevel.CrownBaseHeightProvider;
import repicea.simulation.covariateproviders.treelevel.DbhCmProvider;
import repicea.simulation.covariateproviders.treelevel.HeightMProvider;
import repicea.simulation.stemtaper.StemTaperModel.BasicStemTaperTree;

public interface OakAndBeechStemTaperTree extends BasicStemTaperTree,
												CrownBaseHeightProvider, 
												DbhCmProvider, 
												HeightMProvider {
	
	public enum Species {Oak, Beech};
	
	/**
	 * This method returns the species of the OakAndBeechStemTaperTree instance.
	 * @return a Species enum
	 */
	public Species getSpecies();
	

}
