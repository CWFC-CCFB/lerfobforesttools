package lerfob.carbonbalancetool.catdiameterbasedtreelogger;

import java.util.List;

import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;
import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedWoodPiece;
import repicea.simulation.treelogger.LoggableTree;

@SuppressWarnings("serial")
public class CATDiameterBasedTreeLogCategory extends DiameterBasedTreeLogCategory {

	private transient CATDiameterBasedTreeLogCategoryPanel guiInterface;

	public CATDiameterBasedTreeLogCategory(Enum<?> logGrade, Enum<?> species, double minimumDbhCm,
			double conversionFactor, double downgradingFactor, boolean isFromStump,
			DiameterBasedTreeLogCategory subCategory) {
		super(logGrade, species.name(), minimumDbhCm, conversionFactor, downgradingFactor, isFromStump, subCategory);
	}
	
	@Override
	public CATDiameterBasedTreeLogCategoryPanel getUI() {
		if (guiInterface == null) {
			guiInterface = new CATDiameterBasedTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	/*
	 * Just for extended visibility (non-Javadoc)
	 * @see lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory#extractFromTree(repicea.simulation.treelogger.LoggableTree, java.lang.Object[])
	 */
	@Override
	protected List<DiameterBasedWoodPiece> extractFromTree(LoggableTree tree, Object... parms) {
		return super.extractFromTree(tree, parms);
	}

}
