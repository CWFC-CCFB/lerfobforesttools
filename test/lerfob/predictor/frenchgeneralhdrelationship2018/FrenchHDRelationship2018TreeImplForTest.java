package lerfob.predictor.frenchgeneralhdrelationship2018;

public class FrenchHDRelationship2018TreeImplForTest extends FrenchHDRelationship2018TreeImpl {
	
	protected static boolean BlupPrediction = false;
	
	final double reference;
	final double weight;
	
	FrenchHDRelationship2018TreeImplForTest(double heightM, double dbhCm, double gOther, String speciesName, double weight, double pred, FrenchHDRelationship2018PlotImpl plot) {
		super(heightM, dbhCm, gOther, speciesName, plot);
		this.reference = pred;
		this.weight = weight;
	}
	
	@Override
	public double getHeightM() {
		if (BlupPrediction) {
			return super.getHeightM();
		} else {
			return -1d;
		}
	}


	protected double getPred() {return reference;}
	
}
