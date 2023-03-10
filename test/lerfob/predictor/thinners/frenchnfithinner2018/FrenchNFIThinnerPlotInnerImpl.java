package lerfob.predictor.thinners.frenchnfithinner2018;

import java.util.Map;

import lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPredictor.FrenchNFIThinnerSpecies;

class FrenchNFIThinnerPlotInnerImpl implements FrenchNFIThinnerPlot, InnerValidationPlot {

	final String plotID;
	FrenchRegion2016 region2016;
	double basalAreaM2Ha;
	double stemDensityHa;
	double slopeInclination;
	FrenchNFIThinnerSpecies targetSpecies;
	boolean interventionInPrevious5Years;
	double predictedProb;
	int year0;
	int year1;
	int monteCarloRealization;
	double probabilityPrivateLand;
	
	/**
	 * Constructor for prediction purpose.
	 */
	FrenchNFIThinnerPlotInnerImpl(String plotID, FrenchRegion2016 region2016, double basalAreaM2Ha, double stemDensityHa,	
							double slopeInclination, FrenchNFIThinnerSpecies targetSpecies, boolean interventionInPrevious5Years,
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
	FrenchNFIThinnerPlotInnerImpl(String plotID, FrenchRegion2016 region2016, double basalAreaM2Ha, double stemDensityHa,	
							double slopeInclination, FrenchNFIThinnerSpecies targetSpecies, boolean interventionInPrevious5Years,
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
	public int getMonteCarloRealizationId() {return monteCarloRealization;}

	@Override
	public boolean wasThereAnySiliviculturalTreatmentInTheLast5Years() {return interventionInPrevious5Years;}

	@Override
	public FrenchNFIThinnerSpecies getTargetSpecies() {return targetSpecies;}

	@Override
	public double getProbabilityOfBeingOnPrivateLand() {return probabilityPrivateLand;}

	/*
	 * Useless (non-Javadoc)
	 * @see lerfob.predictor.thinners.frenchnfithinner2018.FrenchNFIThinnerPlot#getVolumeBySpecies()
	 */
	@Override
	public Map<FrenchNFIThinnerSpecies, Double> getOverbarkVolumeM3BySpecies() {
		return null;
	}


}
