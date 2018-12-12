package lerfob.carbonbalancetool.catdiameterbasedtreelogger;

import lerfob.treelogger.diameterbasedtreelogger.DiameterBasedTreeLogCategory;

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


}
