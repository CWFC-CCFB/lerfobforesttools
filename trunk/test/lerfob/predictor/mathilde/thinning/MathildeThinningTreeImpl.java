package lerfob.predictor.mathilde.thinning;

import repicea.simulation.HierarchicalLevel;
import lerfob.predictor.mathilde.MathildeTree;

class MathildeThinningTreeImpl implements MathildeTree {

	private final double dbhCm;
	private final String id;
	private final MathildeTreeSpecies species;
	private final double predicted;
	private final MathildeThinningStandImpl stand;
	private final double linearTreePred; 
	private final int cutPlot;
	
	MathildeThinningTreeImpl(String id, double dbhCm, MathildeTreeSpecies species, int cutPlot, double linearTreePred, double predicted, MathildeThinningStandImpl stand) {
		this.id = id;
		this.dbhCm = dbhCm;
		this.species = species;
		this.cutPlot = cutPlot;
		this.predicted = predicted;
		this.stand = stand;
		this.linearTreePred = linearTreePred;
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
	protected int getExcludedGroup() {return getStand().getExcludedGroup();}
	protected MathildeThinningStandImpl getStand() {return stand;}
	protected double getPrediction() {return predicted;}
	protected double getLinearTreePred() {return linearTreePred;}
	protected boolean isCutPlot() {return cutPlot == 1;}
}
