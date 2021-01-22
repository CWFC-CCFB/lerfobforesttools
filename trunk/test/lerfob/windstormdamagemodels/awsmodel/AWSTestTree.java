package lerfob.windstormdamagemodels.awsmodel;


/**
 * Simple implementation of AWSTree for testing.
 * @author M. Fortin - August 2010
 */
public class AWSTestTree extends AWSTreeImpl {

	private double predictedProbabilityFromFile;
	private double predictedProbabilityFromModel;

	protected AWSTestTree (AWSTreeSpecies species,
			double relativeDbh,
			double relativeHDRatio,
			double regOffset,
			double predictedProbabilityFromFile) {
		super(species, relativeDbh, relativeHDRatio, regOffset);
		this.predictedProbabilityFromFile = predictedProbabilityFromFile;
	}
	
	protected double getPredictedProbabilityFromFile() {return predictedProbabilityFromFile;}

	protected double getPredictedOffsetFromFile() {return (Double) getAWSTreeVariable(AWSTreeImpl.TestTreeVariable.RegOffset);}

	@Override
	public void registerProbability(double probability) {this.predictedProbabilityFromModel = probability;}

	@Override
	public double getProbability() {return predictedProbabilityFromModel;}
	
}
