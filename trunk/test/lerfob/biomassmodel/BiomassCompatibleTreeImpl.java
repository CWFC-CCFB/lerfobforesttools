package lerfob.biomassmodel;

public class BiomassCompatibleTreeImpl implements BiomassCompatibleTree {

	private double dbhCm;
	private double heightM;
	private int ageYr;
	private FgSpecies species;
	
	
	protected BiomassCompatibleTreeImpl(double dbhCm, double heightM, int ageYr, FgSpecies species) {
		this.dbhCm = dbhCm;
		this.heightM = heightM;
		this.ageYr = ageYr;
		this.species = species;
	}
	
	
	
	@Override
	public double getHeightM() {return heightM;}

	@Override
	public double getDbhCm() {return dbhCm;}

	@Override
	public int getAgeYr() {return ageYr;}

	@Override
	public FgSpecies getFgSpecies() {return species;}

}
