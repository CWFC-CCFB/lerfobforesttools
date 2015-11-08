package lerfob.predictor.mathilde;

import repicea.simulation.ModelBasedSimulator.HierarchicalLevel;

public class MathildeMortalityStandImpl implements MathildeMortalityStand {

	private boolean isGoingToBeHarvested;
	private double stepLengthYrs;
	private boolean isAWindstormGoingToOccur;
	private boolean isADroughtGoingToOccur;
	private int monteCarloRealizationID;
	
	protected MathildeMortalityStandImpl(double stepLengthYrs, int upcomingCut, int upcomingDought, int upcomingWindstorm) {
		this.stepLengthYrs = stepLengthYrs;
		isGoingToBeHarvested = false;
		if (upcomingCut == 1) {
			isGoingToBeHarvested = true;
		}
		isADroughtGoingToOccur = false;
		if (upcomingDought == 1) {
			isADroughtGoingToOccur = true;
		}
		isAWindstormGoingToOccur = false;
		if (upcomingWindstorm == 1) {
			isAWindstormGoingToOccur = true;
		}
	}
	
	
	@Override
	public boolean isGoingToBeHarvested() {
		return isGoingToBeHarvested;
	}

	@Override
	public double getGrowthStepLengthYr() {
		return stepLengthYrs;
	}

	@Override
	public int getSubjectId() {
		return 0;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {
		return HierarchicalLevel.Plot;
	}

	protected void setMonteCarloRealizationId(int i) {
		monteCarloRealizationID = i;
	}

	@Override
	public int getMonteCarloRealizationId() {
		return monteCarloRealizationID;
	}

	@Override
	public int getDateYr() {
		return 2013;
	}

	@Override
	public boolean isADroughtGoingToOccur() {
		return isADroughtGoingToOccur;
	}

	@Override
	public boolean isAWindstormGoingToOccur() {
		return isAWindstormGoingToOccur;
	}

}
