package lerfob.predictor.frenchcommercialvolume2014;

public class FrenchCommercialVolume2014TreeImpl implements FrenchCommercialVolume2014Tree {

	protected double pred;
	private FrenchCommercialVolume2014TreeSpecies species;
	private double heightM;
	private double dbhCm;
	private int id;
	
	FrenchCommercialVolume2014TreeImpl(int id, double dbhCm, double heightM, String speciesName, double pred) {
		this.id = id;
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.pred = pred;
		String newSpeciesName = speciesName.trim().toUpperCase().replace(" ", "_");
		this.species = FrenchCommercialVolume2014TreeSpecies.valueOf(newSpeciesName);
	}
	
	protected double getPred() {return pred;}
	
	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public double getSquaredDbhCm() {return getDbhCm() * getDbhCm();}

	@Override
	public double getHeightM() {return heightM;}

	@Override
	public String getSubjectId() {return ((Integer) id).toString();}

	@Override
	public int getMonteCarloRealizationId() {return 0;}

	@Override
	public FrenchCommercialVolume2014TreeSpecies getFrenchCommercialVolume2014TreeSpecies() {return species;}

}
