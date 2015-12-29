package lerfob.predictor.mathilde;

import repicea.simulation.HierarchicalLevel;

public class MathildeDiameterIncrementStandImpl implements MathildeDiameterIncrementStand {

	private double st;
	private boolean isGoingToBeHarvested;
	private double deltaT;
	private int monteCarloRealization;
	private double tIntervalVeg6;

	protected MathildeDiameterIncrementStandImpl(double st, 
			double upcomingCut, 
			double deltaT, 
			int realization,
			double tIntervalVeg6) {
		this.st = st;
		isGoingToBeHarvested = (upcomingCut == 1);
		this.tIntervalVeg6 = tIntervalVeg6;
		this.deltaT = deltaT;
		this.setMonteCarloRealizationId(realization);
	}

	@Override
	public double getBasalAreaM2Ha() {
		return st;
	}

	@Override
	public int getSubjectId() {
		return this.hashCode();
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.PLOT;
	}

	protected void setMonteCarloRealizationId(int i) {
		monteCarloRealization = i;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloRealization;
	}

	@Override
	public boolean isGoingToBeHarvested() {
		return isGoingToBeHarvested;
	}

	@Override
	public double getGrowthStepLengthYr() {
		return deltaT;
	}

	@Override
	public double getMeanAnnualTempAbove6C() {
		return tIntervalVeg6;
	}


}
