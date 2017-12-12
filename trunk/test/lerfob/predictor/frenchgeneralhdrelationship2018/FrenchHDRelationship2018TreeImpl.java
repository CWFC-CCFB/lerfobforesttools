package lerfob.predictor.frenchgeneralhdrelationship2018;

import repicea.simulation.HierarchicalLevel;

class FrenchHDRelationship2018TreeImpl implements FrenchHDRelationship2018Tree {
	
	protected static boolean BlupPrediction = false;
	
	double heightM;
	private double dbhCm;
	final FrenchHd2018Species species;
	private FrenchHDRelationship2018Stand stand;
	double reference;
	private double gOther;
	boolean knownHeight;
	
	FrenchHDRelationship2018TreeImpl(double heightM, double dbhCm, double gOther, String speciesName, double pred, FrenchHDRelationship2018Stand stand) {
		this.heightM = heightM;
		this.dbhCm = dbhCm;
		this.gOther = gOther;
		this.species = FrenchHDRelationship2018Tree.getFrenchHd2018SpeciesFromThisString(speciesName);
		this.reference = pred;
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
		return species;
	}

	protected double getPred() {return reference;}
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
