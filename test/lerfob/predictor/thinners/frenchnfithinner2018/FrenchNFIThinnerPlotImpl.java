package lerfob.predictor.thinners.frenchnfithinner2018;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerStandingPriceProvider.Species;
import repicea.simulation.HierarchicalLevel;

class FrenchNFIThinnerPlotImpl implements FrenchNFIThinnerPlot {

	final String plotID;
	final FrenchRegion2016 region2016;
	final double basalAreaM2Ha;
	final double stemDensityHa;
	final double slopeInclination;
	final Species targetSpecies;
	final boolean interventionInPrevious5Years;
	final double predictedProb;
	final int year0;
	final int year1;
	int monteCarloRealization;
	
	/**
	 * Constructor.
	 * @param department
	 * @param basalAreaM2Ha
	 * @param stemDensityHa
	 * @param slopeInclination
	 * @param targetSpecies
	 * @param interventionInPrevious5Years
	 */
	FrenchNFIThinnerPlotImpl(String plotID, FrenchRegion2016 region2016, double basalAreaM2Ha, double stemDensityHa,	
							double slopeInclination, Species targetSpecies, boolean interventionInPrevious5Years,
							double predictedProb, int year0, int year1) {
		this.plotID = plotID;
		this.region2016 = region2016;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.stemDensityHa = stemDensityHa;
		this.slopeInclination = slopeInclination;
		this.targetSpecies = targetSpecies;
		this.interventionInPrevious5Years = interventionInPrevious5Years;
		this.predictedProb = predictedProb;
		this.year0 = year0;
		this.year1 = year1;
	}
	
	
	double getPredictedProbability() {return predictedProb;}
	int getYear0() {return year0;}
	int getYear1() {return year1;}
 	
	@Override
	public FrenchRegion2016 getFrenchRegion2016() {return region2016;}

	@Override
	public double getBasalAreaM2Ha() {return basalAreaM2Ha;}

	@Override
	public double getNumberOfStemsHa() {return stemDensityHa;}

	@Override
	public double getSlopeInclinationPercent() {return slopeInclination;}

	@Override
	public String getSubjectId() {return plotID;}

	@Override
	public HierarchicalLevel getHierarchicalLevel() {return HierarchicalLevel.PLOT;}

	@Override
	public int getMonteCarloRealizationId() {return monteCarloRealization;}

	@Override
	public boolean wasThereAnySiliviculturalTreatmentInTheLast5Years() {return interventionInPrevious5Years;}

	@Override
	public Species getTargetSpecies() {return targetSpecies;}

}
