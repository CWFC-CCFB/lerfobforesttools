package lerfob.predictor.mathilde.recruitment;

import lerfob.predictor.mathilde.MathildeTree;
import repicea.simulation.HierarchicalLevel;

class MathildeTreeImpl implements MathildeTree {

	private final MathildeTreeSpecies thisSpecies;
	private final MathildeRecruitmentStand stand;
	private final double[] predictions;
	
	MathildeTreeImpl(MathildeRecruitmentStand stand, MathildeTreeSpecies species, double[] predictions) throws Exception {
		thisSpecies = species;
		this.stand = stand;
		((MathildeRecruitmentStandImpl) this.stand).treeList.add(this);
		this.predictions = predictions;
	}
	
	@Override
	public double getDbhCm() {return 0;}

	@Override
	public double getLnDbhCm() {return 0;}

	@Override
	public String getSubjectId() {return null;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.TREE;}

	@Override
	public int getMonteCarloRealizationId() {
		return 0;
	}

	@Override
	public MathildeTreeSpecies getMathildeTreeSpecies() {return thisSpecies;}


	MathildeRecruitmentStand getStand() {return stand;}
	
	double[] getPredictions() {return predictions;}

	/*
	 * Useless here (non-Javadoc)
	 * @see lerfob.predictor.mathilde.MathildeTree#getBasalAreaLargerThanSubjectM2Ha(lerfob.predictor.mathilde.MathildeTreeSpeciesProvider.MathildeTreeSpecies)
	 */
	@Override
	public double getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies species) {
		return 0;
	}
}
