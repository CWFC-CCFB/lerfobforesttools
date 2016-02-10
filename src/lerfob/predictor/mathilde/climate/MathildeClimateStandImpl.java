package lerfob.predictor.mathilde;

import java.math.BigInteger;

import repicea.simulation.HierarchicalLevel;

class MathildeClimateStandImpl implements MathildeStand {

	final String name;
	final int id;
	final double meanAnnualTempAbove6C;
	final int dateYr;
	final double x_resc;
	final double y_resc;
	final double pred;
	
	MathildeClimateStandImpl(String name, double x_resc, double y_resc, int dateYr, double meanAnnualTempAbove6C, double pred) {
		this.name = name;
		id = new BigInteger(name.getBytes()).intValue();
		this.x_resc = x_resc;
		this.y_resc = y_resc;
		this.dateYr = dateYr;
		this.meanAnnualTempAbove6C = meanAnnualTempAbove6C;
		this.pred = pred;
	}
	
	@Override
	public int getSubjectId() {
		return id;
	}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getDateYr() {
		return dateYr;
	}

	@Override
	public double getMeanAnnualTempAbove6C() {
		return meanAnnualTempAbove6C;
	}

	protected double getPrediction() {return pred;}
	
	@Override
	public double getBasalAreaM2Ha() {return 0;}

	@Override
	public double getMeanQuadraticDiameterCm() {return 0;}

	@Override
	public boolean isGoingToBeHarvested() {return false;}

	@Override
	public boolean isADroughtGoingToOccur() {return false;}

	@Override
	public boolean isAWindstormGoingToOccur() {return false;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getGrowthStepLengthYr() {return 0;}

	@Override
	public double getLatitude() {
		return x_resc * 100000;
	}

	@Override
	public double getLongitude() {
		return y_resc * 100000;
	}

	@Override
	public double getElevationM() {
		return 0;
	}

}
