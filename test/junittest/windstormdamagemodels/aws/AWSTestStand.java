package junittest.windstormdamagemodels.aws;

import lerfob.windstormdamagemodels.awsmodel.AWSStandImpl;
import lerfob.windstormdamagemodels.awsmodel.AWSTreeImpl;
import lerfob.windstormdamagemodels.awsmodel.AWSTree.AWSTreeSpecies;

public class AWSTestStand extends AWSStandImpl {
	
	private double[] predictedProbabilitiesFromFile;
	private double[] predictedProbabilitiesFromModel;

	
	protected AWSTestStand(String id,
			AWSTreeSpecies dominantSpecies,
			double d100,
			double h100,
			int age,
			double v,
			double g,
			boolean stagnantMoisture,
			double topex,
			double wind50,
			double wind99,
			boolean carbonateInUpperSoil,
			int year,
			double cumulatedRemovals,
			double relativeRemovedVolume,
			double thinningQuotient,
			double relativeRemovedVolumeOfPreviousIntervention,
			int nbYrsSincePreviousIntervention,
			double relativeRemovedVolumeInPast10Yrs,
			double thinningQuotientOfPast10Yrs,
			Double relHDRatio,
			double[] predictedProbabilities) {
		super(id, dominantSpecies, d100, h100, age, v, g, stagnantMoisture, topex, wind50, wind99, 
				carbonateInUpperSoil, year, cumulatedRemovals, relativeRemovedVolume, thinningQuotient, 
				relativeRemovedVolumeOfPreviousIntervention, nbYrsSincePreviousIntervention, relativeRemovedVolumeInPast10Yrs, 
				thinningQuotientOfPast10Yrs, relHDRatio, predictedProbabilities);
		this.predictedProbabilitiesFromFile = predictedProbabilities;

	}
	
	
	protected void compareStandPredictions(Boolean[] isTheSame) {
		double verySmall;
		if (getDominantSpecies() == AWSTreeSpecies.Beech || getDominantSpecies() == AWSTreeSpecies.SilverFir) {
			verySmall = 0.02;
		} else  {
			verySmall = 0.001;
		}
		for (int i = 0; i < predictedProbabilitiesFromFile.length; i++) {
			if (isTheSame[i] && predictedProbabilitiesFromFile[i] > 0) {
				if (Math.abs(predictedProbabilitiesFromFile[i] - getProbabilities()[i]) > verySmall) {
					isTheSame[i] = false;
				}
			}
		}
	}
	
	@Override
	protected AWSTreeSpecies getDominantSpecies() {return super.getDominantSpecies();}

	@Override
	protected void addTree(AWSTreeImpl tree) {
		super.addTree(tree);
	}

	@Override
	public void registerProbabilities(double[] probabilities) {this.predictedProbabilitiesFromModel = probabilities;}

	@Override
	public double[] getProbabilities() {return predictedProbabilitiesFromModel;}
	
	protected double getHDRatioRel() {return (Double) getAWSStandVariable(TestStandVariable.hdRelRatio);}
//	protected double getD100() {return (Double) getAWSStandVariable(StandVariable.d100);}
	protected double getRelRemovedVol() {return (Double) getAWSTreatmentVariable(TreatmentVariable.relativeRemovedVolume);}
	protected double getCumulRemoval() {return (Double) getAWSTreatmentVariable(TreatmentVariable.cumulatedRemovals);}
	protected double getPredictedJava() {return getProbabilities()[0];}
	protected double getPredictedSAS() {return predictedProbabilitiesFromFile[0];}
	
}
