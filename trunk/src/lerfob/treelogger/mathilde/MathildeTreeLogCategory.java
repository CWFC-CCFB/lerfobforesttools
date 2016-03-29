package lerfob.treelogger.mathilde;

import lerfob.predictor.mathilde.MathildeTree.MathildeTreeSpecies;
import repicea.simulation.treelogger.TreeLogCategory;
import repicea.simulation.treelogger.WoodPiece;

@SuppressWarnings("serial")
public class MathildeTreeLogCategory extends TreeLogCategory {

	protected final double minimumDbhCm;
	private transient MathildeTreeLogCategoryPanel guiInterface;
	
	protected MathildeTreeLogCategory(MathildeTreeSpecies species, String name, double minimumDiameter) {
		super(name);
		setSpecies(species.name());
		this.minimumDbhCm = minimumDiameter;
	}
	
	
	@Override
	public MathildeTreeLogCategoryPanel getGuiInterface() {
		if (guiInterface == null) {
			guiInterface = new MathildeTreeLogCategoryPanel(this);
		}
		return guiInterface;
	}

	@Override
	public double getYieldFromThisPiece(WoodPiece piece) throws Exception {return 1d;}

}
