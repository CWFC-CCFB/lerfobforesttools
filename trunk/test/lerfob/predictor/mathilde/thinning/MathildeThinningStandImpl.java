package lerfob.predictor.mathilde.thinning;

import repicea.simulation.HierarchicalLevel;

class MathildeThinningStandImpl implements MathildeThinningStand {

	private final String id;
	private final double mqdCm;
	private final int dateYr;
	
	MathildeThinningStandImpl(String id, double mqdCm, int dateYr) {
		this.id = id;
		this.mqdCm = mqdCm;
		this.dateYr = dateYr;
	}
	
	@Override
	public String getSubjectId() {return id;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getTimeSinceLastCutYr() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getMeanQuadraticDiameterCm() {return mqdCm;}

	@Override
	public int getDateYr() {return dateYr;}

}
