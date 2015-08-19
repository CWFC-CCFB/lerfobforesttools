package lerfob.predictor.frenchgeneralhdrelationship2014;

import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

class FrenchHDRelationship2014TreeImpl implements FrenchHDRelationship2014Tree {

	protected static boolean BlupPrediction = false;
	
	private double heightM;
	private double dbhCm;
	private String species;
	private FrenchHDRelationship2014Stand stand;
	private double pred;
	private double gOther;
	
	FrenchHDRelationship2014TreeImpl(double heightM, double dbhCm, double gOther, String species, double pred, FrenchHDRelationship2014Stand stand) {
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = gOther;
		this.species = species;
		this.pred = pred;
		this.stand = stand;
		((FrenchHDRelationship2014StandImpl) this.stand).addTree(this); 
	}
	
	@Override
	public int getSubjectId() {return stand.getMonteCarloRealizationId();}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.Tree;}

	@Override
	public void setMonteCarloRealizationId(int i) {stand.setMonteCarloRealizationId(i);}

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
	public FrenchHdSpecies getFrenchHDTreeSpecies() {
		int index = FrenchHDRelationship2014PredictorTest.speciesList.indexOf(species);
		return FrenchHdSpecies.values()[index];
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
