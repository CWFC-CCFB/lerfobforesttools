package lerfob.predictor.mathilde.diameterincrement;

import lerfob.predictor.mathilde.MathildeTree;

public class MathildeDiameterIncrementTreeImpl implements MathildeTree {

	private double dbh;
	private double bal42;
	private double bal22;
	private MathildeTreeSpecies species;
	private MathildeDiameterIncrementStand stand;
	private double pred;
//	private int monteCarloRealization;
	
	protected MathildeDiameterIncrementTreeImpl(double diam0,
			double bal22,
			double bal42,
			int grEss,
			double st,
			double upcomingCut,
			double deltaT,
			double pred,
			double tIntervalVeg6) {
		dbh = diam0;
		this.bal22 = bal22;
		this.bal42 = bal42;
		species = MathildeTreeSpecies.getSpecies(grEss);
		this.pred = pred;
//		this.setMonteCarloRealizationId(1);
		stand = new MathildeDiameterIncrementStandImpl(st, 
				upcomingCut, 
				deltaT, 
				1, 
				tIntervalVeg6);
	}
	
	protected double getPred() {
		return pred;
	}
	
	protected double getBacktransformedPred(double variance) {
		return Math.exp(getPred() + variance * .5) - 1;
	}
	
	protected MathildeDiameterIncrementStand getStand() {
		return stand;
	}
	
	@Override
	public double getDbhCm() {
		return dbh;
	}

	@Override
	public double getLnDbhCm() {
		return Math.log(dbh);
	}

	@Override
	public String getSubjectId() {
		return ((Integer) hashCode()).toString();
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
