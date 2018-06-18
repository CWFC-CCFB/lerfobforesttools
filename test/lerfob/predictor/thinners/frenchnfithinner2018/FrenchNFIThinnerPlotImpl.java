package lerfob.predictor.thinners.frenchnfithinner2018;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerStandingPriceProvider.Species;
import repicea.simulation.HierarchicalLevel;

class FrenchNFIThinnerPlotImpl implements FrenchNFIThinnerPlot {

	final String plotID;
	FrenchRegion2016 region2016;
	double basalAreaM2Ha;
	double stemDensityHa;
	double slopeInclination;
	Species targetSpecies;
	boolean interventionInPrevious5Years;
	double predictedProb;
	int year0;
	int year1;
	int monteCarloRealization;
	double probabilityPrivateLand;
	
	/**
	 * Constructor for prediction purpose.
	 */
	FrenchNFIThinnerPlotImpl(String plotID, FrenchRegion2016 region2016, double basalAreaM2Ha, double stemDensityHa,	
							double slopeInclination, Species targetSpecies, boolean interventionInPrevious5Years,
							double probabilityPrivateLand,
							double predictedProb, int year0, int year1) {
		this(plotID, region2016, basalAreaM2Ha, 
				stemDensityHa, slopeInclination, targetSpecies, 
				interventionInPrevious5Years, probabilityPrivateLand);
		this.predictedProb = predictedProb;
		this.year0 = year0;
		this.year1 = year1;
	}
	
	/**
	 * Constructor for prediction.
	 */
	FrenchNFIThinnerPlotImpl(String plotID, FrenchRegion2016 region2016, double basalAreaM2Ha, double stemDensityHa,	
							double slopeInclination, Species targetSpecies, boolean interventionInPrevious5Years,
							double probabilityPrivateLand) {
		this.plotID = plotID;
		this.region2016 = region2016;
		this.basalAreaM2Ha = basalAreaM2Ha;
		this.stemDensityHa = stemDensityHa;
		this.slopeInclination = slopeInclination;
		this.targetSpecies = targetSpecies;
		this.interventionInPrevious5Years = interventionInPrevious5Years;
		this.probabilityPrivateLand = probabilityPrivateLand;
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

	@Override
	public double getProbabilityOfBeingOnPrivateLand() {return probabilityPrivateLand;}


}
