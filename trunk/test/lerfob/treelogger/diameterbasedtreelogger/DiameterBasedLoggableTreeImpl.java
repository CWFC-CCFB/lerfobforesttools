package lerfob.treelogger.diameterbasedtreelogger;

import repicea.simulation.species.REpiceaSpecies;

class DiameterBasedLoggableTreeImpl implements DiameterBasedLoggableTree {

	final double dbhCm;
	
	DiameterBasedLoggableTreeImpl(double dbhCm) {
		this.dbhCm = dbhCm;
	}
	
	@Override
	public double getCommercialVolumeM3() {return 1d;}

	@Override
	public String getSpeciesName() {return REpiceaSpecies.Species.Pinus_pinaster.toString();}

	@Override
	public double getDbhCm() {return dbhCm;}


}
