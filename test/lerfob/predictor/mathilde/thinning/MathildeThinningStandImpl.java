package lerfob.predictor.mathilde.thinning;

class MathildeThinningStandImpl implements MathildeThinningStand {

	private final String id;
	private final double mqdCm;
	private final int dateYr;
	private final double timeSinceLastCut;
	private final double linearPlotPred;
	private final int excludedGroup;
	private final double basalAreaM2Ha;
	
	MathildeThinningStandImpl(String id, double mqdCm, double basalAreaM2Ha, int dateYr, double timeSinceLastCut, int excludedGroup, double linearPlotPred) {
		this.id = id;
		this.mqdCm = mqdCm;
		this.dateYr = dateYr;
		this.timeSinceLastCut = timeSinceLastCut;
		this.linearPlotPred = linearPlotPred;
		this.excludedGroup = excludedGroup;
		this.basalAreaM2Ha = basalAreaM2Ha;
	}
	
	@Override
	public String getSubjectId() {return id;}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public double getTimeSinceLastCutYr() {return timeSinceLastCut;}

	@Override
	public double getMeanQuadraticDiameterCm() {return mqdCm;}

	@Override
	public int getDateYr() {return dateYr;}
	
	protected double getLinearPlotPred() {return linearPlotPred;}

	protected int getExcludedGroup() {return excludedGroup;}

	@Override
	public double getBasalAreaM2Ha() {
		return basalAreaM2Ha;
	}

	@Override
	public double getGrowthStepLengthYr() {
		return 0;
	}
}
