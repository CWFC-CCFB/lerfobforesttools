package lerfob.predictor.frenchgeneralhdrelationship2018;

import repicea.simulation.HierarchicalLevel;

class FrenchHDRelationship2018TreeImpl implements FrenchHDRelationship2018Tree {

	protected static boolean BlupPrediction = false;
	
	private double heightM;
	private double dbhCm;
	private String species;
	private FrenchHDRelationship2018Stand stand;
	private double pred;
	private double gOther;
	
	FrenchHDRelationship2018TreeImpl(double heightM, double dbhCm, double gOther, String species, double pred, FrenchHDRelationship2018Stand stand) {
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = gOther;
		this.species = species;
		this.pred = pred;
		this.stand = stand;
		((FrenchHDRelationship2018StandImpl) this.stand).addTree(this); 
	}
	
	@Override
	public String getSubjectId() {return ((Integer) hashCode()).toString();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	@Override
	public int getMonteCarloRealizationId() {return stand.getMonteCarloRealizationId();}

	@Override
	public double getHeightM() {
		if (BlupPrediction) {
			return heightM;
		} else {
			return -1d;
		}
	}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCmPlus1() {return Math.log(getDbhCm() + 1);}

	@Override
	public double getSquaredLnDbhCmPlus1() {
		double lnDbhCm = getLnDbhCmPlus1();
		return lnDbhCm * lnDbhCm;
	}

	@Override
	public FrenchHd2018Species getFrenchHDTreeSpecies() {
		int index = FrenchHDRelationship2018PredictorTest.speciesList.indexOf(species);
		return FrenchHd2018Species.values()[index];
	}

	protected double getPred() {return pred;}
	protected double getGOther() {return gOther;}
	
	@Override
	public int getErrorTermIndex() {
		return 0;
	}

	@Override
	public Enum<?> getHDRelationshipTreeErrorGroup() {
		return null;
	}

}
