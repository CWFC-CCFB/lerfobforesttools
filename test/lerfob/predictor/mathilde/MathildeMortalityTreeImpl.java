package lerfob.predictor.mathilde;

import lerfob.predictor.mathilde.MathildeTree;
import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

public final class MathildeMortalityTreeImpl implements MathildeTree {

	
	private double dbhCm;
	private MathildeTreeSpecies species;
	private double bal22;
	private double bal42;
	private MathildeMortalityStand stand;
	private double pred;	
	
	protected MathildeMortalityTreeImpl(double pred, 
			double dbhCm, 
			int grSpecies, 
			double bal22, 
			double bal42,
			double stepLengthYrs, 
			int upcomingCut,
			int upcomingDrought,
			int upcomingWindstorm) {
		this.pred = pred;
		this.dbhCm = dbhCm;
		this.bal22 = bal22;
		this.bal42 = bal42;
		species = MathildeTreeSpecies.getSpecies(grSpecies);
		stand = new MathildeMortalityStandImpl(stepLengthYrs, 
				upcomingCut, 
				upcomingDrought,
				upcomingWindstorm);
	}

	protected MathildeMortalityStand getStand() {
		return stand;
	}
		
	protected double getPred() {
		return pred;
	}
	
	@Override
	public double getDbhCm() {
		return dbhCm;
	}

	@Override
	public double getLnDbhCm() {
		return Math.log(getDbhCm());
	}

	@Override
	public int getSubjectId() {
		return 0;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.Tree;
	}

	@Override
	public void setMonteCarloRealizationId(int i) {
		stand.setMonteCarloRealizationId(i);
	}

	@Override
	public int getMonteCarloRealizationId() {
		return stand.getMonteCarloRealizationId();
	}

	@Override
	public MathildeTreeSpecies getMathildeTreeSpecies() {
		return species;
	}

	@Override
	public double getBasalAreaLargerThanSubjectM2Ha(MathildeTreeSpecies species) {
		if (species == MathildeTreeSpecies.QUERCUS) {
			return bal22;
		} else if (species == MathildeTreeSpecies.FAGUS) {
			return bal42;
		}
		return 0;
	}

}
