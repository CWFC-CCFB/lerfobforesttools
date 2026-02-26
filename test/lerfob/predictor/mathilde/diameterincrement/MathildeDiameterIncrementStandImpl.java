package lerfob.predictor.mathilde.diameterincrement;

public class MathildeDiameterIncrementStandImpl implements MathildeDiameterIncrementStand {

	private double st;
	private boolean isGoingToBeHarvested;
	private int deltaT;
	private int monteCarloRealization;
	private double tIntervalVeg6;

	protected MathildeDiameterIncrementStandImpl(double st, 
			double upcomingCut, 
			int deltaT, 
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
	public String getSubjectId() {
		return ((Integer) hashCode()).toString();
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
	public int getGrowthStepLengthYr() {
		return deltaT;
	}

	@Override
	public double getMeanSeasonalTemperatureCelsius() {
		return tIntervalVeg6;
	}

}
