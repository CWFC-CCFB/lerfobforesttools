package lerfob.allometricrelationships.stemtaper.oakandbeech;


public class OakAndBeechStemTaperTreeImpl implements OakAndBeechStemTaperTree {

	private double crownBase;
	private double dbhCm;
	private double heightM;
	private Species species;
	
	private OakAndBeechStemTaperTreeImpl(double crownBase, double dbhCm, double heightM, Species species) {
		this.crownBase = crownBase;
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.species = species;
	}
	
	
	@Override
	public double getCrownBaseHeightM() {return crownBase;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public Species getSpecies() {return species;}
	
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		OakAndBeechStemTaperTreeImpl tree = new OakAndBeechStemTaperTreeImpl(5,20,10,Species.Beech);
		OakAndBeechStemProfileCalculator calculator = new OakAndBeechStemProfileCalculator();
//		List<Double> heights = calculator.getHeightsArrayWithSmallEndDiameter(tree, 0.3, 20);
//		StemTaperEstimate taperEstimate = calculator.getPredictedTaperForTheseHeights(tree, heights);
//		Estimate estimate = taperEstimate.getVolumeEstimate();
//		double volume = estimate.getMean().getSumOfElements();
		double volume20cm = calculator.getVolumeBetweenThisHeightAndThisSmallEndDiameter(tree, 0.3, 20);
		double volume7cm = calculator.getVolumeBetweenThisHeightAndThisSmallEndDiameter(tree, 0.3, 7);
		int u = 0;
	}

}
