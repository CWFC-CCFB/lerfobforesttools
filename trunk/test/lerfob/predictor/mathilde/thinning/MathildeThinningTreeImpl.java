package lerfob.predictor.mathilde.thinning;

import repicea.simulation.HierarchicalLevel;
import lerfob.predictor.mathilde.MathildeTree;

class MathildeThinningTreeImpl implements MathildeTree {

	private final double dbhCm;
	private final String id;
	private final MathildeTreeSpecies species;
	
	MathildeThinningTreeImpl(String id, double dbhCm, int speciesCode) {
		this.id = id;
		this.dbhCm = dbhCm;
		species = MathildeTreeSpecies.getSpecies(speciesCode);
	}
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getLnDbhCm() {return Math.log(dbhCm);}

	@Override
	public String getSubjectId() {return id;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public MathildeTreeSpecies getMathildeTreeSpecies() {return species;}

	@Override
	public double getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies species) {
		return 0;
	}

}
